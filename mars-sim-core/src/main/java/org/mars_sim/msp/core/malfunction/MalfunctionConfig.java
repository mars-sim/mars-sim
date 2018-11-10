/**
 * Mars Simulation Project
 * MalfunctionConfig.java
 * @version 3.1.0 2017-09-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.person.health.ComplaintType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.HeatSourceType;
import org.mars_sim.msp.core.structure.building.function.PowerSourceType;
import org.mars_sim.msp.core.structure.building.function.SystemType;
import org.mars_sim.msp.core.tool.Conversion;

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

	private Document malfunctionDoc;

	private List<Malfunction> malfunctionList;

	private Map<String, List<RepairPart>> repairParts;

	/**
	 * Constructor
	 * 
	 * @param malfunctionDoc DOM document containing malfunction configuration.
	 */
	public MalfunctionConfig(Document malfunctionDoc) {
		this.malfunctionDoc = malfunctionDoc;
		repairParts = new HashMap<String, List<RepairPart>>();

	}

	/**
	 * Gets a list of malfunctions
	 * 
	 * @return list of malfunctions
	 * @throws Exception when malfunctions can not be resolved.
	 */
	// @SuppressWarnings("unchecked")
	public List<Malfunction> getMalfunctionList() {

		if (malfunctionList == null) {
			malfunctionList = new ArrayList<Malfunction>();

			Element root = malfunctionDoc.getRootElement();
			List<Element> malfunctionNodes = root.getChildren(MALFUNCTION);
			for (Element malfunctionElement : malfunctionNodes) {
				String name = "";

				try {

					// Get name.
					name = malfunctionElement.getAttributeValue(NAME);

					// Get severity.
					Element severityElement = malfunctionElement.getChild(SEVERITY);
					int severity = Integer.parseInt(severityElement.getAttributeValue(VALUE));

					// Get probability.
					Element probabilityElement = malfunctionElement.getChild(PROBABILITY);
					double probability = Double.parseDouble(probabilityElement.getAttributeValue(VALUE));

					// Get repair time. (optional)
					double repairTime = 0D;

					Element repairTimeElement = malfunctionElement.getChild(REPAIR_TIME);

					if (repairTimeElement != null) {
						repairTime = Double.parseDouble(repairTimeElement.getAttributeValue(VALUE));
					}

					// Get emergency repair time. (optional)
					double emergencyRepairTime = 0D;
					Element emergencyRepairTimeElement = malfunctionElement.getChild(EMERGENCY_REPAIR_TIME);

					if (emergencyRepairTimeElement != null) {
						emergencyRepairTime = Double.parseDouble(emergencyRepairTimeElement.getAttributeValue(VALUE));
					}

					// Get EVA repair time. (optional)
					double evaRepairTime = 0D;
					Element evaRepairTimeElement = malfunctionElement.getChild(EVA_REPAIR_TIME);

					if (evaRepairTimeElement != null) {
						evaRepairTime = Double.parseDouble(evaRepairTimeElement.getAttributeValue(VALUE));
					}

					// Get affected entities.
					List<String> systems = new ArrayList<String>();
					Element entityListElement = malfunctionElement.getChild(SYSTEM_LIST);
					List<Element> systemNodes = entityListElement.getChildren(SYSTEM);

					for (Element systemElement : systemNodes) {
						boolean exist = false;
						String sys_name = Conversion.capitalize(systemElement.getAttributeValue(NAME));
						for (FunctionType f : FunctionType.values()) {
							if (sys_name.equals(f.getName())) {
								systems.add(sys_name);
								exist = true;
							}
						}
						if (!exist) {
							for (SystemType s : SystemType.values()) {
								if (sys_name.equals(s.getName())) {
									systems.add(sys_name);
									exist = true;
								}
							}
						}
						if (!exist) {
							for (HeatSourceType h : HeatSourceType.values()) {
								if (sys_name.equals(h.getName())) {
									systems.add(sys_name);
									exist = true;
								}
							}
						}
						if (!exist) {
							for (PowerSourceType p : PowerSourceType.values()) {
								if (sys_name.equals(p.getName())) {
									systems.add(sys_name);
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
							Double changeRate = new Double(effectElement.getAttributeValue(CHANGE_RATE));

							if (type.equals("life-support")) {

//                            	if (resourceName.equals("Air Pressure"))
//                            		; // TODO: change the air pressure
//                            	else if (resourceName.equals("Temperature"))
//                            		; // TODO: change the temperature
//                            	else {
//                            	}

								lifeSupportEffects.put(resourceName, changeRate);
							} else if (type.equals(ItemType.AMOUNT_RESOURCE.getName())) {
								AmountResource resource = AmountResource.findAmountResource(resourceName);
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
							Double complaintProbability = new Double(
									medicalComplaintElement.getAttributeValue(PROBABILITY));
							medicalComplaints.put(ComplaintType.fromString(complaintName), complaintProbability);

							// logger.info("complaintName is " + complaintName);

						}
					}

					// Convert resourceEffects
					Map<Integer, Double> resourceEffectIDs = new HashMap<Integer, Double>();
					for (AmountResource ar : resourceEffects.keySet()) {
						resourceEffectIDs.put(ar.getID(), resourceEffects.get(ar));
					}

					// Create malfunction.
					Malfunction malfunction = new Malfunction(name, 0, severity, probability, emergencyRepairTime,
							repairTime, evaRepairTime, systems, resourceEffectIDs, lifeSupportEffects,
							medicalComplaints);

					// Add repair parts.
					Element repairPartsListElement = malfunctionElement.getChild(REPAIR_PARTS_LIST);
					if (repairPartsListElement != null) {
						List<Element> partNodes = repairPartsListElement.getChildren(PART);

						for (Element partElement : partNodes) {
							String partName = partElement.getAttributeValue(NAME);
							Part part = (Part) Part.findItemResource(partName);
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

					malfunctionList.add(malfunction);

				} catch (Exception e) {
					throw new IllegalStateException(
							"Error reading malfunction " + name + " in malfunctions.xml : " + e.getMessage(), e);
				}
			}
		}

		// Notes : as of 8 Sep 2017 there are 36 malfunctions

		return malfunctionList;
	}

	/**
	 * Adds a repair part for a malfunction.
	 * 
	 * @param malfunctionName the malfunction name.
	 * @param partName        the repair part name.
	 * @param number          the maximum number of parts required (min 1).
	 * @param probability     the probability the part will be needed (0 - 100).
	 */
	private void addMalfunctionRepairPart(String malfunctionName, String partName, int number, int probability) {
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

	public void setRepairPartProbability(String malfunctionName, String partName, double probability) {
		List<RepairPart> partList = repairParts.get(malfunctionName);
		if (partList != null) {
			Iterator<RepairPart> i = partList.iterator();
			while (i.hasNext()) {
				RepairPart part = i.next();
				if (part.name.equalsIgnoreCase(partName)) {
					part.setProbability(probability);
				}
			}
		}
	}

	public Map<String, List<RepairPart>> getRepairParts() {
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

		public void setProbability(double probability) {
			this.probability = probability;
		}
	}
}
