/**
 * Mars Simulation Project
 * MalfunctionConfig.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
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

	private static Document malfunctionDoc;

	private static List<MalfunctionMeta> malfunctionList;

	private static Map<String, List<RepairPart>> repairParts;

	/**
	 * Constructor
	 * 
	 * @param malfunctionDoc DOM document containing malfunction configuration.
	 */
	public MalfunctionConfig(Document malfunctionDoc) {
		MalfunctionConfig.malfunctionDoc = malfunctionDoc;
		repairParts = new ConcurrentHashMap<String, List<RepairPart>>();
	}

	/**
	 * Gets a list of malfunctions
	 * 
	 * @return list of malfunctions
	 * @throws Exception when malfunctions can not be resolved.
	 */
	public static List<MalfunctionMeta> getMalfunctionList() {

		if (malfunctionList == null) {
			buildMalfunctionList(malfunctionDoc);
		}
		
		return malfunctionList;
	}
	
	private static synchronized void buildMalfunctionList(Document configDoc) {
		if (malfunctionList != null) {
			// Another thread has created the list whilst I was blocked
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
			Map<MalfunctionRepairWork, Double> workEffort = new HashMap<>();
			addWorkEffort(workEffort, MalfunctionRepairWork.GENERAL, malfunctionElement, REPAIR_TIME);
			addWorkEffort(workEffort, MalfunctionRepairWork.EMERGENCY, malfunctionElement, EMERGENCY_REPAIR_TIME);
			addWorkEffort(workEffort, MalfunctionRepairWork.EVA, malfunctionElement, EVA_REPAIR_TIME);

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
			Map<String, Double> lifeSupportEffects = new ConcurrentHashMap<String, Double>();
			Map<AmountResource, Double> resourceEffects = new ConcurrentHashMap<AmountResource, Double>();
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
			Map<ComplaintType, Double> medicalComplaints = new ConcurrentHashMap<>();

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
			Map<Integer, Double> resourceEffectIDs = new ConcurrentHashMap<Integer, Double>();
			for (AmountResource ar : resourceEffects.keySet()) {
				resourceEffectIDs.put(ar.getID(), resourceEffects.get(ar));
			}
			
			// Create malfunction.
			MalfunctionMeta malfunction = new MalfunctionMeta(name, severity, probability, workEffort , systems,
															  resourceEffectIDs, lifeSupportEffects,
															  medicalComplaints);

			// Add repair parts.
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
						addMalfunctionRepairPart(name, partName, partNumber, partProbability);
					}
				}
			}

			newList.add(malfunction);
		}
		
		// Assign the list now built
		malfunctionList = newList;
	}
	
	private static void addWorkEffort(Map<MalfunctionRepairWork, Double> workEffort, MalfunctionRepairWork type,
			Element parent, String childName) {
		Element childElement = parent.getChild(childName);

		if (childElement != null) {
			double workTime = Double.parseDouble(childElement.getAttributeValue(VALUE));
			workEffort.put(type, workTime);
		}
	}

	/**
	 * Adds a repair part for a malfunction.
	 * 
	 * @param malfunctionName the malfunction name.
	 * @param partName        the repair part name.
	 * @param number          the maximum number of parts required (min 1).
	 * @param probability     the probability the part will be needed (0 - 100).
	 */
	private static void addMalfunctionRepairPart(String malfunctionName, String partName, int number, int probability) {
		List<RepairPart> partList = repairParts.get(malfunctionName);
		if (partList == null) {
			partList = new ArrayList<RepairPart>();
			repairParts.put(malfunctionName, partList);
		}
		partList.add(new RepairPart(partName, number, probability));
	}

	/**
	 * Gets all the repair part names for a malfunction.
	 * 
	 * @param malfunctionName the name of the malfunction.
	 * @return array of part names.
	 */
	public String[] getRepairPartNamesForMalfunction(String malfunctionName) {
		if (malfunctionName == null) {
			throw new IllegalArgumentException("malfunctionName is null");
		}
		List<RepairPart> partList = repairParts.get(malfunctionName);
		if (partList != null) {
			String[] partNames = new String[partList.size()];
			for (int x = 0; x < partList.size(); x++) {
				partNames[x] = partList.get(x).name;
			}
			return partNames;
		} else {
			return new String[0];
		}
	}

	/**
	 * Gets the maximum number of a repair part for a malfunction.
	 * 
	 * @param malfunctionName the name of the malfunction.
	 * @param partName        the name of the part.
	 * @return the maximum number of parts.
	 */
	public int getRepairPartNumber(String malfunctionName, String partName) {
		int result = 0;
		List<RepairPart> partList = repairParts.get(malfunctionName);
		if (partList != null) {
			Iterator<RepairPart> i = partList.iterator();
			while (i.hasNext()) {
				RepairPart part = i.next();
				if (part.name.equalsIgnoreCase(partName)) {
					result = part.number;
				}
			}
		}
		return result;
	}

	/**
	 * Gets the probability of a repair part for a malfunction.
	 * 
	 * @param malfunctionName the name of the malfunction.
	 * @param partName        the name of the part.
	 * @return the probability of the repair part.
	 */
	public double getRepairPartProbability(String malfunctionName, String partName) {
		double result = 0;
		List<RepairPart> partList = repairParts.get(malfunctionName);
		if (partList != null) {
			Iterator<RepairPart> i = partList.iterator();
			while (i.hasNext()) {
				RepairPart part = i.next();
				if (part.name.equalsIgnoreCase(partName)) {
					result = part.probability;
				}
			}
		}
		return result;
	}

	public static Map<String, List<RepairPart>> getRepairParts() {
		return repairParts;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		malfunctionDoc = null;

		if (malfunctionList != null) {

			malfunctionList.clear();
			malfunctionList = null;
		}

		if (repairParts != null) {

			repairParts.clear();
			repairParts = null;
		}

	}

	/**
	 * Private inner class for repair part information.
	 */
	private static class RepairPart implements Serializable {

		private static final long serialVersionUID = 1L;

		// Data members
		private String name;
		private int number;
		private double probability;

		/**
		 * Constructor
		 * 
		 * @param name        the name of the part.
		 * @param number      the maximum number of parts.
		 * @param probability the probability of the part being needed.
		 */
		private RepairPart(String name, int number, double probability) {
			this.name = name;
			this.number = number;
			this.probability = probability;
		}
	}
}
