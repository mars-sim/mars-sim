/*
 * Mars Simulation Project
 * MalfunctionConfig.java
 * @date 2021-11-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.malfunction.MalfunctionMeta.EffortSpec;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.HeatSourceType;
import org.mars_sim.msp.core.structure.building.function.PowerSourceType;
import org.mars_sim.msp.core.structure.building.function.SystemType;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Provides configuration information about malfunctions. Uses a DOM document to
 * get the information.
 */
public class MalfunctionConfig implements Serializable {


	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(MalfunctionConfig.class.getName());

	// Element names
	private static final String MALFUNCTION_EL = "malfunction";
	private static final String NAME_ATTR = "name";
	private static final String SEVERITY_EL = "severity";
	private static final String PROBABILITY_EL = "probability";
	private static final String SYSTEM_LIST_EL = "system-list";
	private static final String SYSTEM_EL = "system";
	private static final String EFFECT_LIST = "effect-list";
	private static final String EFFECT = "effect";
	private static final String TYPE_ATTR = "type";
	private static final String CHANGE_RATE = "change-rate";
	private static final String MEDICAL_COMPLAINT_LIST = "medical-complaint-list";
	private static final String MEDICAL_COMPLAINT = "medical-complaint";
	private static final String REPAIR_PARTS_LIST = "repair-parts-list";
	private static final String PART_EL = "part";
	private static final String NUMBER_ATTR = "number";
	private static final String VALUE_ATTR = "value";
	private static final String INSIDE_EL = "repair-inside";
	private static final String EVA_EL = "repair-eva";
	private static final String REPAIRERS_ATTR = "repairers";
	private static final String TIME_ATTR = "time";

	private static final int MIN_REPAIRERS = 2; // Minimum repairers
	private static final int MAX_REPAIRERS = 4; // Maximum repairers

	private List<MalfunctionMeta> malfunctionList;


	/**
	 * Constructor
	 *
	 * @param malfunctionDoc DOM document containing malfunction configuration.
	 */
	public MalfunctionConfig(Document malfunctionDoc) {
		buildMalfunctionList(malfunctionDoc);
	}

	/**
	 * Gets a list of malfunctions
	 *
	 * @return list of malfunctions
	 * @throws Exception when malfunctions can not be resolved.
	 */
	public List<MalfunctionMeta> getMalfunctionList() {
		return malfunctionList;
	}

	/**
	 * Build the malfunction list
	 *
	 * @param configDoc
	 */
	private synchronized void buildMalfunctionList(Document configDoc) {
		if (malfunctionList != null) {
			// just in case if another thread is being created
			return;
		}

		// Build list of potential known systems
		Set<String> knownSystems = Arrays.stream(FunctionType.values())
									.map(i -> i.getName().toLowerCase())
									.collect(Collectors.toSet());
		knownSystems.addAll(Arrays.stream(SystemType.values())
							.map(i -> i.getName().toLowerCase())
							.collect(Collectors.toSet()));
		knownSystems.addAll(Arrays.stream(HeatSourceType.values())
							.map(i -> i.getName().toLowerCase())
							.collect(Collectors.toSet()));
		knownSystems.addAll(Arrays.stream(PowerSourceType.values())
							.map(i -> i.getName().toLowerCase())
							.collect(Collectors.toSet()));
		knownSystems.addAll(Arrays.stream(VehicleType.values())
							.map(i -> i.getName().toLowerCase())
							.collect(Collectors.toSet()));

		// Build the global list in a temp to avoid access before it is built
		List<MalfunctionMeta> newList = new ArrayList<>();

		Element root = configDoc.getRootElement();
		List<Element> malfunctionNodes = root.getChildren(MALFUNCTION_EL);
		for (Element malfunctionElement : malfunctionNodes) {
			String name = malfunctionElement.getAttributeValue(NAME_ATTR);

			// Get severity.
			Element severityElement = malfunctionElement.getChild(SEVERITY_EL);
			int severity = Integer.parseInt(severityElement.getAttributeValue(VALUE_ATTR));

			// Get probability.
			Element probabilityElement = malfunctionElement.getChild(PROBABILITY_EL);
			double probability = Double.parseDouble(probabilityElement.getAttributeValue(VALUE_ATTR));

			// Get the various work efforts
			EnumMap<MalfunctionRepairWork, EffortSpec> workEffort = new EnumMap<>(MalfunctionRepairWork.class);
			addWorkEffort(severity, workEffort, MalfunctionRepairWork.INSIDE,
					malfunctionElement, INSIDE_EL);
			addWorkEffort(severity, workEffort, MalfunctionRepairWork.EVA,
					malfunctionElement, EVA_EL);

			// Get affected entities.
			Set<String> systems = new TreeSet<>();
			Element entityListElement = malfunctionElement.getChild(SYSTEM_LIST_EL);
			List<Element> systemNodes = entityListElement.getChildren(SYSTEM_EL);

			for (Element systemElement : systemNodes) {
				String sysName = Conversion.capitalize(systemElement.getAttributeValue(NAME_ATTR)).toLowerCase();
				if (knownSystems.contains(sysName)) {
					systems.add(sysName);
				}
				else {
					throw new IllegalStateException(
							"The system name '" + sysName + "' in malfunctions.xml is NOT recognized.");
				}
			}

			// Get effects.
			Map<String, Double> lifeSupportEffects = new HashMap<>();
			Map<AmountResource, Double> resourceEffects = new HashMap<>();
			Element effectListElement = malfunctionElement.getChild(EFFECT_LIST);

			if (effectListElement != null) {
				List<Element> effectNodes = effectListElement.getChildren(EFFECT);

				for (Element effectElement : effectNodes) {
					String type = effectElement.getAttributeValue(TYPE_ATTR);
					String resourceName = effectElement.getAttributeValue(NAME_ATTR);
					double changeRate = Double.parseDouble(effectElement.getAttributeValue(CHANGE_RATE));

					if (type.equals("life-support")) {

//                            	if (resourceName.equals("Air Pressure"))
//                            		; // TODO: change the air pressure
//                            	else if (resourceName.equals("Temperature"))
//                            		; // TODO: change the temperature
//                            	else {
//                            	}

						lifeSupportEffects.put(resourceName, changeRate);
					} else if (type.equals(ItemType.AMOUNT_RESOURCE.getName())) {
						AmountResource resource = ResourceUtil.findAmountResource(resourceName);
						if (resource == null)
							logger.warning(resourceName
									+ " shows up in malfunctions.xml but doesn't exist in resources.xml.");
						else
							resourceEffects.put(resource, changeRate);
					} else {
						throw new IllegalStateException(
								"Effect " + resourceName + " type not correct in malfunction " + name);
					}
				}
			}

			// Get medical complaints.
			Map<ComplaintType, Double> medicalComplaints = new HashMap<>();

			Element medicalComplaintListElement = malfunctionElement.getChild(MEDICAL_COMPLAINT_LIST);

			if (medicalComplaintListElement != null) {
				List<Element> medicalComplaintNodes = medicalComplaintListElement
						.getChildren(MEDICAL_COMPLAINT);

				for (Element medicalComplaintElement : medicalComplaintNodes) {
					String complaintName = medicalComplaintElement.getAttributeValue(NAME_ATTR).toUpperCase().replace(' ', '_');
					double complaintProbability = Double.parseDouble(
							medicalComplaintElement.getAttributeValue(PROBABILITY_EL));
					medicalComplaints.put(ComplaintType.valueOf(complaintName), complaintProbability);
				}
			}

			// Convert resourceEffects
			Map<Integer, Double> resourceEffectIDs = new HashMap<>();
			for (AmountResource ar : resourceEffects.keySet()) {
				resourceEffectIDs.put(ar.getID(), resourceEffects.get(ar));
			}


			// Add repair parts.
			List<RepairPart> parts = new ArrayList<>();
			Element repairPartsListElement = malfunctionElement.getChild(REPAIR_PARTS_LIST);
			if (repairPartsListElement != null) {
				List<Element> partNodes = repairPartsListElement.getChildren(PART_EL);

				for (Element partElement : partNodes) {
					String partName = partElement.getAttributeValue(NAME_ATTR);
					Part part = (Part) (ItemResourceUtil.findItemResource(partName));
					if (part == null)
						logger.severe(
								partName + " shows up in malfunctions.xml but doesn't exist in parts.xml.");
					else {
						int partNumber = Integer.parseInt(partElement.getAttributeValue(NUMBER_ATTR));
						int partProbability = Integer.parseInt(partElement.getAttributeValue(PROBABILITY_EL));
						int partID = ItemResourceUtil.findIDbyItemResourceName(partName);
						parts.add(new RepairPart(partName, partID, partNumber, partProbability));
					}
				}
			}

			// Create malfunction.
			MalfunctionMeta malfunction = new MalfunctionMeta(name, severity, probability, workEffort, systems,
															  resourceEffectIDs, lifeSupportEffects,
															  medicalComplaints, parts);
			// Add malfunction meta to newList.
			newList.add(malfunction);
		}

		// Assign the newList now built
		malfunctionList = Collections.unmodifiableList(newList);
	}

	private static void addWorkEffort(int severity, Map<MalfunctionRepairWork, EffortSpec> workEffort,
									  MalfunctionRepairWork type,
									  Element parent, String childName) {
		Element childElement = parent.getChild(childName);
		if (childElement != null) {

			double workTime = Double.parseDouble(childElement.getAttributeValue(TIME_ATTR));
			String repairersTxt = childElement.getAttributeValue(REPAIRERS_ATTR);
			int repairers = 0;
			if (repairersTxt != null) {
				repairers = Integer.parseInt(repairersTxt);
			}
			else {
				// Estimate based on severity and work type
				repairers = MIN_REPAIRERS + (((MAX_REPAIRERS - MIN_REPAIRERS) * severity)/100);
			}

			workEffort.put(type, new EffortSpec(workTime, repairers));
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (malfunctionList != null) {
			malfunctionList = null;
		}
	}
}
