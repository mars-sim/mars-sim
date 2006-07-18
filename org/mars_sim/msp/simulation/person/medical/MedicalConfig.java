/**
 * Mars Simulation Project
 * PersonConfig.java
 * @version 2.75 2004-03-16
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.medical;

import java.io.Serializable;
import java.util.*;
import org.w3c.dom.*;

/**
 * Provides configuration information about medical complaints.
 * Uses a DOM document to get the information. 
 */
public class MedicalConfig implements Serializable {
	
	// Element names
	private static final String MEDICAL_COMPLAINT_LIST = "medical-complaint-list";
	private static final String MEDICAL_COMPLAINT = "medical-complaint";
	private static final String SERIOUSNESS = "seriousness";
	private static final String DEGRADE_TIME = "degrade-time";
	private static final String RECOVERY_TIME = "recovery-time";
	private static final String PROBABILITY = "probability";
	private static final String PERFORMANCE_PERCENT = "performance-percent";
	private static final String TREATMENT = "treatment";
	private static final String DEGRADE_COMPLAINT = "degrade-complaint";
	private static final String TREATMENT_LIST = "treatment-list";
	private static final String SKILL = "skill";
	private static final String MEDICAL_TECH_LEVEL = "medical-tech-level";
	private static final String TREATMENT_TIME = "treatment-time";
	private static final String RETAINAID = "retainaid";
	
	private Document medicalDoc;
	private List complaintList;
	private List treatmentList;

	/**
	 * Constructor
	 * @param medicalDoc DOM document of medical configuration.
	 */
	public MedicalConfig(Document medicalDoc) {
		this.medicalDoc = medicalDoc;
	}

	/**
	 * Gets a list of medical complaints.
	 * @return list of complaints
	 * @throws Exception if list could not be found.
	 */
	public List getComplaintList() throws Exception {
		
		if (complaintList == null) {
			complaintList = new ArrayList();
			Element root = medicalDoc.getDocumentElement();
			Element medicalComplaintList = (Element) root.getElementsByTagName(MEDICAL_COMPLAINT_LIST).item(0);
			NodeList medicalComplaints = medicalComplaintList.getElementsByTagName(MEDICAL_COMPLAINT);
			for (int x=0; x < medicalComplaints.getLength(); x++) {
				String complaintName = "";
				try {
					Element medicalComplaint = (Element) medicalComplaints.item(x);
				
					// Get name.
					complaintName = medicalComplaint.getAttribute("name");
				
					// Get seriousness.
					Element seriousnessElement = (Element) medicalComplaint.getElementsByTagName(SERIOUSNESS).item(0);
					int seriousness = Integer.parseInt(seriousnessElement.getAttribute("value"));
				
					// Get degrade time. (optional)
					double degradeTime = 0D;
					try {
						Element degradeTimeElement = (Element) medicalComplaint.getElementsByTagName(DEGRADE_TIME).item(0);
						degradeTime = Double.parseDouble(degradeTimeElement.getAttribute("value"));
					}
					catch (NullPointerException e) {}
				
					// Get recovery time.
					Element recoveryTimeElement = (Element) medicalComplaint.getElementsByTagName(RECOVERY_TIME).item(0);
					double recoveryTime = Double.parseDouble(recoveryTimeElement.getAttribute("value"));
				
					// Get probability.
					Element probabilityElement = (Element) medicalComplaint.getElementsByTagName(PROBABILITY).item(0);
					double probability = Double.parseDouble(probabilityElement.getAttribute("value"));
				
					// Get performance-percent.
					Element performanceElement = (Element) medicalComplaint.getElementsByTagName(PERFORMANCE_PERCENT).item(0);
					double performance = Double.parseDouble(performanceElement.getAttribute("value"));
				
					// Get the treatment. (optional)
					String treatmentStr = "";
					try {
						Element treatmentElement = (Element) medicalComplaint.getElementsByTagName(TREATMENT).item(0);
						treatmentStr = treatmentElement.getAttribute("value");
					}
					catch (NullPointerException e) {}
					Treatment treatment = null;
					Iterator i = getTreatmentList().iterator();
					while (i.hasNext()) {
						Treatment tempTreatment = (Treatment) i.next();
						if (tempTreatment.getName().equals(treatmentStr)) treatment = tempTreatment;
					}
					if (!treatmentStr.equals("") && (treatment == null)) 
						throw new Exception("treatment: " + treatmentStr + " could not be found in treatment list");
				
					// Get the degrade complaint. (optional)
					String degradeComplaint = "";
					try {
						Element degradeComplaintElement = (Element) medicalComplaint.getElementsByTagName(DEGRADE_COMPLAINT).item(0);
						degradeComplaint = degradeComplaintElement.getAttribute("value");
					}
					catch (NullPointerException e) {}
				
					Complaint complaint = new Complaint(complaintName, seriousness, degradeTime * 1000D, recoveryTime * 1000D, 
						probability, treatment, degradeComplaint, performance);
					
					complaintList.add(complaint);
				}
				catch (Exception e) {
					throw new Exception("Error parsing medical complaint: " + complaintName + ": " + e.getMessage());
				}
			}
			
			// Fill in degrade complaint objects based on complaint names.
			Iterator i = complaintList.iterator();
			while (i.hasNext()) {
				Complaint complaint = (Complaint) i.next();
				String degradeComplaintName = complaint.getNextPhaseStr();
				if (!degradeComplaintName.equals("")) {
					Iterator j = complaintList.iterator();
					while (j.hasNext()) {
						Complaint degradeComplaint = (Complaint) j.next();
						if (degradeComplaint.getName().equals(degradeComplaintName))
							complaint.setNextComplaint(degradeComplaint);
					}
					if (complaint.getNextPhase() == null) 
						throw new Exception("Degrade complaint " + degradeComplaintName + 
							" can not be found in medical complaint list.");
				} 
			}
		}
		
		return complaintList;
	}

	public List getTreatmentList() throws Exception {
		
		if (treatmentList == null) {
			treatmentList = new ArrayList();
			
			Element root = medicalDoc.getDocumentElement();
			Element medicalTreatmentList = (Element) root.getElementsByTagName(TREATMENT_LIST).item(0);
			NodeList treatments = medicalTreatmentList.getElementsByTagName(TREATMENT);
			for (int x=0; x < treatments.getLength(); x++) {
				Element medicalTreatment = (Element) treatments.item(x);
				
				// Get name.
				String treatmentName = medicalTreatment.getAttribute("name");
				
				// Get skill. (optional)
				int skill = 0;
				try {
					Element skillElement = (Element) medicalTreatment.getElementsByTagName(SKILL).item(0);
					skill = Integer.parseInt(skillElement.getAttribute("value"));
				}
				catch (NullPointerException e) {}
				
				// Get medical tech level. (optional)
				int medicalTechLevel = 0;
				try {
					Element medicalTechLevelElement = (Element) medicalTreatment.getElementsByTagName(MEDICAL_TECH_LEVEL).item(0);
					medicalTechLevel = Integer.parseInt(medicalTechLevelElement.getAttribute("value"));
				}
				catch (NullPointerException e) {}
				
				// Get treatment time.
				double treatmentTime = -1D;
				try {
					Element treatmentTimeElement = (Element) medicalTreatment.getElementsByTagName(TREATMENT_TIME).item(0);
					treatmentTime = Double.parseDouble(treatmentTimeElement.getAttribute("value"));
				}
				catch (NullPointerException e) {}
				
				// Get retainaid.
				boolean retainaid = false;
				try {
					Element retainaidElement = (Element) medicalTreatment.getElementsByTagName(RETAINAID).item(0);
					String retainaidStr = retainaidElement.getAttribute("value");
					retainaid = (retainaidStr.toLowerCase().equals("true"));
				}
				catch (NullPointerException e) {}
				
				Treatment treatment = new Treatment(treatmentName, skill, treatmentTime, false, retainaid, medicalTechLevel);
				
				treatmentList.add(treatment);
			}
		}
		
		return treatmentList;
	}
}