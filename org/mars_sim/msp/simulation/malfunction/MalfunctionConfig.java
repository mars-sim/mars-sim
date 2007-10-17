/**
 * Mars Simulation Project
 * MalfunctionConfig.java
 * @version 2.81 2007-08-26
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.malfunction;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.w3c.dom.*;

/**
 * Provides configuration information about malfunctions.
 * Uses a DOM document to get the information. 
 */
public class MalfunctionConfig implements Serializable {

	// Element names
	private static final String MALFUNCTION = "malfunction";
	private static final String NAME = "name";
	private static final String SEVERITY = "severity";
	private static final String PROBABILITY = "probability";
	private static final String REPAIR_TIME = "repair-time";
	private static final String EMERGENCY_REPAIR_TIME = "emergency-repair-time";
	private static final String EVA_REPAIR_TIME = "eva-repair-time";
	private static final String ENTITY_LIST = "entity-list";
	private static final String ENTITY = "entity";
	private static final String EFFECT_LIST = "effect-list";
	private static final String EFFECT = "effect";
	private static final String TYPE = "type";
	private static final String CHANGE_RATE = "change-rate";
	private static final String MEDICAL_COMPLAINT_LIST = "medical-complaint-list";
	private static final String MEDICAL_COMPLAINT = "medical-complaint";
	private static final String REPAIR_PARTS_LIST = "repair-parts-list";
	private static final String PART = "part";
	private static final String NUMBER = "number";

	private Document malfunctionDoc;
	private List<Malfunction> malfunctionList;
	private Map<String, List<RepairPart>> repairParts;
	
	/**
	 * Constructor
	 * @param malfunctionDoc DOM document containing malfunction configuration.
	 */
	public MalfunctionConfig(Document malfunctionDoc) {
		this.malfunctionDoc = malfunctionDoc;
		repairParts = new HashMap<String, List<RepairPart>>();
	}
	
	/**
	 * Gets a list of malfunctions
	 * @return list of malfunctions
	 * @throws Exception when malfunctions can not be resolved.
	 */
	public List<Malfunction> getMalfunctionList() throws Exception {
		
		if (malfunctionList == null) {
			malfunctionList = new ArrayList<Malfunction>();
			
			Element root = malfunctionDoc.getDocumentElement();
			NodeList malfunctionNodes = root.getElementsByTagName(MALFUNCTION);
			for (int x=0; x < malfunctionNodes.getLength(); x++) {
				String name = "";
				
				try {
					Element malfunctionElement = (Element) malfunctionNodes.item(x);
					
					// Get name.
					name = malfunctionElement.getAttribute(NAME);
					
					// Get severity.
					Element severityElement = (Element) malfunctionElement.getElementsByTagName(SEVERITY).item(0);
					int severity = Integer.parseInt(severityElement.getAttribute("value"));
					
					// Get probability.
					Element probabilityElement = (Element) malfunctionElement.getElementsByTagName(PROBABILITY).item(0);
					double probability = Double.parseDouble(probabilityElement.getAttribute("value"));
					
					// Get repair time. (optional)
					double repairTime = 0D;
					try {
						Element repairTimeElement = (Element) malfunctionElement.getElementsByTagName(REPAIR_TIME).item(0);
						repairTime = Double.parseDouble(repairTimeElement.getAttribute("value"));
					}
					catch (NullPointerException e) {}
					
					// Get emergency repair time. (optional)
					double emergencyRepairTime = 0D;
					try {
						Element emergencyRepairTimeElement = (Element) malfunctionElement.getElementsByTagName(EMERGENCY_REPAIR_TIME).item(0);
						emergencyRepairTime = Double.parseDouble(emergencyRepairTimeElement.getAttribute("value"));
					}
					catch (NullPointerException e) {}					
					
					// Get EVA repair time. (optional)
					double evaRepairTime = 0D;
					try {
						Element evaRepairTimeElement = (Element) malfunctionElement.getElementsByTagName(EVA_REPAIR_TIME).item(0);
						evaRepairTime = Double.parseDouble(evaRepairTimeElement.getAttribute("value"));
					}
					catch (NullPointerException e) {}
					
					// Get affected entities.
					List<String> entities = new ArrayList<String>();
					Element entityListElement = (Element) malfunctionElement.getElementsByTagName(ENTITY_LIST).item(0);
					NodeList entityNodes = entityListElement.getElementsByTagName(ENTITY);
					for (int y = 0; y < entityNodes.getLength(); y++) {
						Element entityElement = (Element) entityNodes.item(y);
						entities.add(entityElement.getAttribute(NAME));	
					}
					
					// Get effects.
					Map<String, Double> lifeSupportEffects = new HashMap<String, Double>();
					Map<AmountResource, Double> resourceEffects = new HashMap<AmountResource, Double>();
					try {
						Element effectListElement = (Element) malfunctionElement.getElementsByTagName(EFFECT_LIST).item(0);
						NodeList effectNodes = effectListElement.getElementsByTagName(EFFECT);
						for (int y = 0; y < effectNodes.getLength(); y++) {
							Element effectElement = (Element) effectNodes.item(y);
							String type = effectElement.getAttribute(TYPE);
							String effectName = effectElement.getAttribute(NAME);
							Double changeRate = new Double(effectElement.getAttribute(CHANGE_RATE));
							
							if (type.equals("life support")) lifeSupportEffects.put(effectName, changeRate);
							else if (type.equals("resource")) {
								AmountResource resource = AmountResource.findAmountResource(effectName);
								resourceEffects.put(resource, changeRate);
							}
							else throw new Exception("Effect " + effectName + " type not correct in malfunction " + name);
						}
					}
					catch (NullPointerException e) {}
					
					// Get medical complaints.
					Map<String, Double> medicalComplaints = new HashMap<String, Double>();
					try {
						Element medicalComplaintListElement = (Element) malfunctionElement.getElementsByTagName(MEDICAL_COMPLAINT_LIST).item(0);
						NodeList medicalComplaintNodes = medicalComplaintListElement.getElementsByTagName(MEDICAL_COMPLAINT);
						for (int y = 0; y < medicalComplaintNodes.getLength(); y++) {
							Element medicalComplaintElement = (Element) medicalComplaintNodes.item(y);
							String complaintName = medicalComplaintElement.getAttribute(NAME);
							Double complaintProbability = new Double(medicalComplaintElement.getAttribute(PROBABILITY));
							medicalComplaints.put(complaintName, complaintProbability);
						}
					}
					catch (NullPointerException e) {}
					
					// Create malfunction.
					Malfunction malfunction = new Malfunction(name, severity, probability, emergencyRepairTime, repairTime, 
						evaRepairTime, entities, resourceEffects, lifeSupportEffects, medicalComplaints);
					
					// Add repair parts.
					Element repairPartsListElement = (Element) malfunctionElement.getElementsByTagName(REPAIR_PARTS_LIST).item(0);
					if (repairPartsListElement != null) {
						NodeList partNodes = repairPartsListElement.getElementsByTagName(PART);
						for (int y = 0; y < partNodes.getLength(); y++) {
							Element partElement = (Element) partNodes.item(y);
							String partName = partElement.getAttribute(NAME);
							int partNumber = Integer.parseInt(partElement.getAttribute(NUMBER));
							int partProbability = Integer.parseInt(partElement.getAttribute(PROBABILITY));
							addMalfunctionRepairPart(name, partName, partNumber, partProbability);
						}
					}
					
					malfunctionList.add(malfunction);
				}
				catch (Exception e) {
					throw new Exception("Error reading malfunction " + name + ": " + e.getMessage());
				}
			}
		}
		
		return malfunctionList;
	}
	
	/**
	 * Adds a repair part for a malfunction.
	 * @param malfunctionName the malfunction name.
	 * @param partName the repair part name.
	 * @param number the maximum number of parts required (min 1).
	 * @param probability the probability the part will be needed (0 - 100).
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
	 * @param malfunctionName the name of the malfunction.
	 * @return array of part names.
	 */
	public String[] getRepairPartNamesForMalfunction(String malfunctionName) {
		if (malfunctionName == null) throw new IllegalArgumentException("malfunctionName is null");
		List<RepairPart> partList = repairParts.get(malfunctionName);
		if (partList != null) {
			String[] partNames = new String[partList.size()];
			for (int x = 0; x < partList.size(); x++) partNames[x] = partList.get(x).name;
			return partNames;
		}
		else return new String[0];
	}
	
	/**
	 * Gets the maximum number of a repair part for a malfunction.
	 * @param malfunctionName the name of the malfunction.
	 * @param partName the name of the part.
	 * @return the maximum number of parts.
	 */
	public int getRepairPartNumber(String malfunctionName, String partName) {
		int result = 0;
		List<RepairPart> partList = repairParts.get(malfunctionName);
		if (partList != null) {
			Iterator<RepairPart> i = partList.iterator();
			while (i.hasNext()) {
				RepairPart part = i.next();
				if (part.name.equalsIgnoreCase(partName)) result = part.number;
			}
		}
		return result;
	}
	
	/**
	 * Gets the probability of a repair part for a malfunction.
	 * @param malfunctionName the name of the malfunction.
	 * @param partName the name of the part.
	 * @return the probability of the repair part.
	 */
	public int getRepairPartProbability(String malfunctionName, String partName) {
		int result = 0;
		List<RepairPart> partList = repairParts.get(malfunctionName);
		if (partList != null) {
			Iterator<RepairPart> i = partList.iterator();
			while (i.hasNext()) {
				RepairPart part = i.next();
				if (part.name.equalsIgnoreCase(partName)) result = part.probability;
			}
		}
		return result;
	}
	
	/**
	 * Private inner class for repair part information.
	 */
	private class RepairPart implements Serializable {
    	
    	// Data members
    	private String name;
    	private int number;
    	private int probability;
    	
    	/**
    	 * Constructor
    	 * @param name the name of the part.
    	 * @param number the maximum number of parts.
    	 * @param probability the probability of the part being needed.
    	 */
    	private RepairPart(String name, int number, int probability) {
    		this.name = name;
    		this.number = number;
    		this.probability = probability;
    	}
    }
}