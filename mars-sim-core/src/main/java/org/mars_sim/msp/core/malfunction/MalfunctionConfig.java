/**
 * Mars Simulation Project
 * MalfunctionConfig.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

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
	private static final String MALFUNCTION = "malfunction";
	private static final String NAME = "name";
	private static final String SEVERITY = "severity";
	private static final String PROBABILITY = "probability";
	private static final String REPAIR_TIME = "repair-time";
	private static final String EMERGENCY_REPAIR_TIME = "emergency-repair-time";
	private static final String EVA_REPAIR_TIME = "eva-repair-time";
	private static final String SYSTEM_LIST = "system-list";
	private static final String SYSTEM = "system";
	private static final String EFFECT_LIST = "effect-list";
	private static final String EFFECT = "effect";
	private static final String TYPE = "type";
	private static final String CHANGE_RATE = "change-rate";
	private static final String MEDICAL_COMPLAINT_LIST = "medical-complaint-list";
	private static final String MEDICAL_COMPLAINT = "medical-complaint";
	private static final String REPAIR_PARTS_LIST = "repair-parts-list";
	private static final String PART = "part";
	private static final String NUMBER = "number";
	private static final String VALUE = "value";
	private static final String REPAIRERS = "repairers";

	private static final int MIN_REPAIRERS = 2;

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
			
		// Build the global list in a temp to avoid access before it is built
		List<MalfunctionMeta> newList = new ArrayList<>();

		Element root = configDoc.getRootElement();
		List<Element> malfunctionNodes = root.getChildren(MALFUNCTION);
		for (Element malfunctionElement : malfunctionNodes) {
			String name = malfunctionElement.getAttributeValue(NAME);

			// Get severity.
			Element severityElement = malfunctionElement.getChild(SEVERITY);
			int severity = Integer.parseInt(severityElement.getAttributeValue(VALUE));

			// Get probability.
			Element probabilityElement = malfunctionElement.getChild(PROBABILITY);
			double probability = Double.parseDouble(probabilityElement.getAttributeValue(VALUE));

			// Get the various work efforts
			EnumMap<MalfunctionRepairWork, EffortSpec> workEffort = new EnumMap<>(MalfunctionRepairWork.class);
			addWorkEffort(severity, workEffort, MalfunctionRepairWork.GENERAL,
						  malfunctionElement, REPAIR_TIME);
			addWorkEffort(severity, workEffort, MalfunctionRepairWork.EMERGENCY,
						  malfunctionElement, EMERGENCY_REPAIR_TIME);
			addWorkEffort(severity, workEffort, MalfunctionRepairWork.EVA,
						  malfunctionElement, EVA_REPAIR_TIME);

			// Get affected entities.
			Set<String> systems = new TreeSet<>();
			Element entityListElement = malfunctionElement.getChild(SYSTEM_LIST);
			List<Element> systemNodes = entityListElement.getChildren(SYSTEM);

			for (Element systemElement : systemNodes) {
				boolean exist = false;
				String sys_name = Conversion.capitalize(systemElement.getAttributeValue(NAME));
				for (FunctionType f : FunctionType.values()) {
					if (sys_name.equalsIgnoreCase(f.getName())) {
						systems.add(sys_name.toLowerCase());
						exist = true;
					}
				}
				if (!exist) {
					for (SystemType s : SystemType.values()) {
						if (sys_name.equalsIgnoreCase(s.getName())) {
							systems.add(sys_name.toLowerCase());
							exist = true;
						}
					}
				}
				if (!exist) {
					for (HeatSourceType h : HeatSourceType.values()) {
						if (sys_name.equalsIgnoreCase(h.getName())) {
							systems.add(sys_name.toLowerCase());
							exist = true;
						}
					}
				}
				if (!exist) {
					for (PowerSourceType p : PowerSourceType.values()) {
						if (sys_name.equalsIgnoreCase(p.getName())) {
							systems.add(sys_name.toLowerCase());
							exist = true;
						}
					}
				}
				if (!exist) {
					for (VehicleType t : VehicleType.values()) {
						if (sys_name.equalsIgnoreCase(t.getName())) {
							systems.add(sys_name.toLowerCase());
							exist = true;
						}
					}
				}
				if (!exist) {
					throw new IllegalStateException(
							"The system name '" + sys_name + "' in malfunctions.xml is NOT recognized.");
				}

			}

			// Get effects.
			Map<String, Double> lifeSupportEffects = new HashMap<String, Double>();
			Map<AmountResource, Double> resourceEffects = new HashMap<AmountResource, Double>();
			Element effectListElement = malfunctionElement.getChild(EFFECT_LIST);

			if (effectListElement != null) {
				List<Element> effectNodes = effectListElement.getChildren(EFFECT);

				for (Element effectElement : effectNodes) {
					String type = effectElement.getAttributeValue(TYPE);
					String resourceName = effectElement.getAttributeValue(NAME);
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
					String complaintName = medicalComplaintElement.getAttributeValue(NAME);
					double complaintProbability = Double.parseDouble(
							medicalComplaintElement.getAttributeValue(PROBABILITY));
					medicalComplaints.put(ComplaintType.fromString(complaintName), complaintProbability);
				}
			}

			// Convert resourceEffects
			Map<Integer, Double> resourceEffectIDs = new HashMap<Integer, Double>();
			for (AmountResource ar : resourceEffects.keySet()) {
				resourceEffectIDs.put(ar.getID(), resourceEffects.get(ar));
			}
			

			// Add repair parts.
			List<RepairPart> parts = new ArrayList<>();
			Element repairPartsListElement = malfunctionElement.getChild(REPAIR_PARTS_LIST);
			if (repairPartsListElement != null) {
				List<Element> partNodes = repairPartsListElement.getChildren(PART);

				for (Element partElement : partNodes) {
					String partName = partElement.getAttributeValue(NAME);
					Part part = (Part) (ItemResourceUtil.findItemResource(partName));
					if (part == null)
						logger.severe(
								partName + " shows up in malfunctions.xml but doesn't exist in parts.xml.");
					else {
						int partNumber = Integer.parseInt(partElement.getAttributeValue(NUMBER));
						int partProbability = Integer.parseInt(partElement.getAttributeValue(PROBABILITY));
						parts.add(new RepairPart(partName, partNumber, partProbability));
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
			double workTime = Double.parseDouble(childElement.getAttributeValue(VALUE));
			String repairersTxt = childElement.getAttributeValue(REPAIRERS);
			int repairers = 0;
			if (repairersTxt != null) {
				repairers = Integer.parseInt(repairersTxt);
			}
			else {
				int extra = 2; // The extra based on severity
				if (type == MalfunctionRepairWork.EMERGENCY) {
					extra = 3; // Allow extra people in an emergency
				}
				
				// Estimate based on severity and work type
				repairers = MIN_REPAIRERS + ((extra * severity)/100);
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
