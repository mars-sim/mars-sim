/**
 * Mars Simulation Project
 * MalfunctionConfig.java
 * @version 2.75 2004-03-17
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.malfunction;

import java.util.*;
import org.w3c.dom.*;

/**
 * Provides configuration information about malfunctions.
 * Uses a DOM document to get the information. 
 */
public class MalfunctionConfig {

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

	private Document malfunctionDoc;
	private List malfunctionList;
	
	/**
	 * Constructor
	 * @param malfunctionDoc DOM document containing malfunction configuration.
	 */
	public MalfunctionConfig(Document malfunctionDoc) {
		this.malfunctionDoc = malfunctionDoc;
	}
	
	/**
	 * Gets a list of malfunctions
	 * @return list of malfunctions
	 * @throws Exception when malfunctions can not be resolved.
	 */
	public List getMalfunctionList() throws Exception {
		
		if (malfunctionList == null) {
			malfunctionList = new ArrayList();
			
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
					List entities = new ArrayList();
					Element entityListElement = (Element) malfunctionElement.getElementsByTagName(ENTITY_LIST).item(0);
					NodeList entityNodes = entityListElement.getElementsByTagName(ENTITY);
					for (int y = 0; y < entityNodes.getLength(); y++) {
						Element entityElement = (Element) entityNodes.item(y);
						entities.add(entityElement.getAttribute(NAME));	
					}
					
					// Get effects.
					Map lifeSupportEffects = new HashMap();
					Map resourceEffects = new HashMap();
					try {
						Element effectListElement = (Element) malfunctionElement.getElementsByTagName(EFFECT_LIST).item(0);
						NodeList effectNodes = effectListElement.getElementsByTagName(EFFECT);
						for (int y = 0; y < effectNodes.getLength(); y++) {
							Element effectElement = (Element) effectNodes.item(y);
							String type = effectElement.getAttribute(TYPE);
							String effectName = effectElement.getAttribute(NAME);
							Double changeRate = new Double(effectElement.getAttribute(CHANGE_RATE));
							
							if (type.equals("life support")) lifeSupportEffects.put(effectName, changeRate);
							else if (type.equals("resource")) resourceEffects.put(effectName, changeRate);
							else throw new Exception("Effect " + effectName + " type not correct in malfunction " + name);
						}
					}
					catch (NullPointerException e) {}
					
					// Get medical complaints.
					Map medicalComplaints = new HashMap();
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
					
					malfunctionList.add(malfunction);
				}
				catch (Exception e) {
					throw new Exception("Error reading malfunction " + name + ": " + e.getMessage());
				}
			}
		}
		
		return malfunctionList;
	}
}