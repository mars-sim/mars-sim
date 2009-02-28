/**
 * Mars Simulation Project
 * PersonConfig.java
 * @version 2.81 2007-08-27
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.medical;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.w3c.dom.NodeList;



/**
 * Provides configuration information about medical complaints.
 * Uses a JDOM document to get the information. 
 */
public class MedicalConfig implements Serializable {
	
	// Element names
    private static final String NAME = "name";
    private static final String VALUE = "value";
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
	private List<Complaint> complaintList;
	private List<Treatment> treatmentList;

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
	public List<Complaint> getComplaintList() throws Exception {
		
		if (complaintList == null) {
			complaintList = new ArrayList<Complaint>();
			Element root = medicalDoc.getRootElement();
			Element medicalComplaintList = root.getChild(MEDICAL_COMPLAINT_LIST);
			List<Element> medicalComplaints = medicalComplaintList.getChildren(MEDICAL_COMPLAINT);
			
			for (Element medicalComplaint : medicalComplaints) {
				String complaintName = "";
				try {
					
					// Get name.
					complaintName = medicalComplaint.getAttributeValue(NAME);
				
					// Get seriousness.
					Element seriousnessElement = medicalComplaint.getChild(SERIOUSNESS);
					int seriousness = Integer.parseInt(seriousnessElement.getAttributeValue(VALUE));
				
					// Get degrade time. (optional)
					double degradeTime = 0D;
		            Element degradeTimeElement = medicalComplaint.getChild(DEGRADE_TIME);
		            
		            if(degradeTimeElement != null)
				    degradeTime = Double.parseDouble(degradeTimeElement.getAttributeValue(VALUE));
				
					// Get recovery time.
					Element recoveryTimeElement = medicalComplaint.getChild(RECOVERY_TIME);
					double recoveryTime = Double.parseDouble(recoveryTimeElement.getAttributeValue(VALUE));
				
					// Get probability.
					Element probabilityElement = medicalComplaint.getChild(PROBABILITY);
					double probability = Double.parseDouble(probabilityElement.getAttributeValue(VALUE));
				
					// Get performance-percent.
					Element performanceElement = medicalComplaint.getChild(PERFORMANCE_PERCENT);
					double performance = Double.parseDouble(performanceElement.getAttributeValue(VALUE));
				
					// Get the treatment. (optional)
					String treatmentStr = "";
				    Element treatmentElement = medicalComplaint.getChild(TREATMENT);
				    
				    if(treatmentElement != null)
				    treatmentStr = treatmentElement.getAttributeValue(VALUE);

					
					Treatment treatment = null;
					List<Treatment> treatmentList = getTreatmentList();
					
					for (Treatment tempTreatment : treatmentList) {
						if (tempTreatment.getName().equals(treatmentStr)) {
						    treatment = tempTreatment;
						    break;
						}
					}
					
					if (!treatmentStr.equals("") && (treatment == null)) 
						throw new Exception("treatment: " + treatmentStr + " could not be found in treatment list");
				
					// Get the degrade complaint. (optional)
					String degradeComplaint = "";
				
				    Element degradeComplaintElement = medicalComplaint.getChild(DEGRADE_COMPLAINT);
				    
				    if(degradeComplaintElement != null)
				    degradeComplaint = degradeComplaintElement.getAttributeValue(VALUE);
				
				
					Complaint complaint = new Complaint(complaintName, seriousness, 
					                                    degradeTime * 1000D, recoveryTime * 1000D, 
						                                probability, treatment, 
						                                degradeComplaint, performance);
					
					complaintList.add(complaint);
				}
				catch (Exception e) {
					throw new Exception("Error parsing medical complaint: " + complaintName + ": " + e.getMessage());
				}
			}
			
			// Fill in degrade complaint objects based on complaint names.
			for (Complaint complaint : complaintList) {
				String degradeComplaintName = complaint.getNextPhaseStr();
				
				if (!degradeComplaintName.equals("")) {
					Iterator<Complaint> j = complaintList.iterator();
					for (Complaint degradeComplaint : complaintList) {
						if (degradeComplaint.getName().equals(degradeComplaintName))
							complaint.setNextComplaint(degradeComplaint);
					}
					
					if (complaint.getNextPhase() == null){ 
						throw new Exception("Degrade complaint " + degradeComplaintName + 
							" can not be found in medical complaint list.");
					}
				} 
			}
		}
		
		return complaintList;
	}

	public List<Treatment> getTreatmentList() throws Exception {
		
		if (treatmentList == null) {
			treatmentList = new ArrayList<Treatment>();
			
			Element root = medicalDoc.getRootElement();
			Element medicalTreatmentList = root.getChild(TREATMENT_LIST);
			List<Element> treatments = medicalTreatmentList.getChildren(TREATMENT);
			
			for (Element medicalTreatment : treatments) {	
				// Get name.
				String treatmentName = medicalTreatment.getAttributeValue(NAME);
				
				// Get skill. (optional)
				int skill = 0;
			    Element skillElement = medicalTreatment.getChild(SKILL);
			    
			    if(skillElement != null)
			    skill = Integer.parseInt(skillElement.getAttributeValue(VALUE));
			
				
				// Get medical tech level. (optional)
				int medicalTechLevel = 0;
				Element medicalTechLevelElement = medicalTreatment.getChild(MEDICAL_TECH_LEVEL);
				
				if(medicalTechLevelElement != null)
				medicalTechLevel = Integer.parseInt(medicalTechLevelElement.getAttributeValue(VALUE));
			
				
				// Get treatment time., optional
				double treatmentTime = -1D;
				Element treatmentTimeElement = medicalTreatment.getChild(TREATMENT_TIME);
				
				if(treatmentTimeElement != null)
				treatmentTime = Double.parseDouble(treatmentTimeElement.getAttributeValue(VALUE));
			
				
				// Get retainaid, optional
				boolean retainaid = false;
		
			    Element retainaidElement = medicalTreatment.getChild(RETAINAID);
			    
			    if(retainaidElement != null) {
				 String retainaidStr = retainaidElement.getAttributeValue(VALUE);
				 retainaid = (retainaidStr.toLowerCase().equals("true"));
			    }
				
				
				Treatment treatment = new Treatment(treatmentName, skill, 
				                      treatmentTime, false, retainaid, medicalTechLevel);
				
				treatmentList.add(treatment);
			}
		}
		
		return treatmentList;
	}
}