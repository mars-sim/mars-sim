/**
 * Mars Simulation Project
 * MedicalConfig.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.health;

import org.jdom2.Document;
import org.jdom2.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Provides configuration information about medical complaints.
 * Uses a JDOM document to get the information. 
 */
public class MedicalConfig implements Serializable {
	
    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
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
	private static final String BED_REST_RECOVERY = "bed-rest-recovery";
	private static final String TREATMENT_TYPE = "treatment-type";
	private static final String DEGRADE_COMPLAINT = "degrade-complaint";
	private static final String TREATMENT_LIST = "treatment-list";
    private static final String TREATMENT = "treatment";
	private static final String SKILL = "skill";
	private static final String MEDICAL_TECH_LEVEL = "medical-tech-level";
	private static final String TREATMENT_TIME = "treatment-time";
	private static final String SELF_ADMIN = "self-admin";
	
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
    //@SuppressWarnings("unchecked")
	public List<Complaint> getComplaintList() {
		
		if (complaintList == null) {
			complaintList = new ArrayList<Complaint>();
			Element root = medicalDoc.getRootElement();
			Element medicalComplaintList = root.getChild(MEDICAL_COMPLAINT_LIST);
			List<Element> medicalComplaints = medicalComplaintList.getChildren(MEDICAL_COMPLAINT);
			
			for (Element medicalComplaint : medicalComplaints) {
				String complaintName = "";
					
				// Get name.
				complaintName = medicalComplaint.getAttributeValue(NAME);
				
				//2016-06-15 
				//TODO: Converted all complaint String names to ComplaintType
				
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

				// Get bed rest recovery.
				Element bedRestRecoveryElement = medicalComplaint.getChild(BED_REST_RECOVERY);
				boolean bedRestRecovery = Boolean.parseBoolean(bedRestRecoveryElement.getAttributeValue(VALUE));
				
				// Get the treatment. (optional)
				String treatmentStr = "";
				Element treatmentElement = medicalComplaint.getChild(TREATMENT_TYPE);

				if (treatmentElement != null) {
				    treatmentStr = treatmentElement.getAttributeValue(VALUE);
				}

				Treatment treatment = null;
				List<Treatment> treatmentList = getTreatmentList();
				for (Treatment tempTreatment : treatmentList) {
				    if (tempTreatment.getName().equals(treatmentStr)) {
				        treatment = tempTreatment;
				        break;
				    }
				}

				if (treatmentStr.length() != 0 && (treatment == null))
				    throw new IllegalStateException("treatment: " + treatmentStr + " could not be found in treatment list");

				// Get the degrade complaint. (optional)
				String degradeComplaint = "";

				Element degradeComplaintElement = medicalComplaint.getChild(DEGRADE_COMPLAINT);

				if (degradeComplaintElement != null) {
				    degradeComplaint = degradeComplaintElement.getAttributeValue(VALUE);
				}

				Complaint complaint = new Complaint(ComplaintType.fromString(complaintName), seriousness, 
				        degradeTime * 1000D, recoveryTime * 1000D, probability, 
				        treatment, ComplaintType.fromString(degradeComplaint), performance, bedRestRecovery);

				//System.out.println("ComplaintName is " + complaintName);
				//System.out.println("ComplaintType is " + ComplaintType.valueOf(complaintName).getName());
				complaintList.add(complaint);
			}
			
			// Fill in degrade complaint objects based on complaint names.
			for (Complaint complaint : complaintList) {
				ComplaintType degradeComplaintName = complaint.getNextPhaseStr();
				
				if (degradeComplaintName != null) {//.length() != 0) {
					Iterator<Complaint> j = complaintList.iterator();
                    while (j.hasNext()) {
                        Complaint degradeComplaint = j.next();
                        //if (degradeComplaint.getType().equals(degradeComplaintName))
                        if (degradeComplaint.getType() == degradeComplaintName)
                        	complaint.setNextComplaint(degradeComplaint);
                    }
					
					if (complaint.getNextPhase() == null){ 
						throw new IllegalStateException("Degrade complaint " + degradeComplaintName +
							" cannot be found in medical complaint list.");
					}
				} 
			}
		}
		
		return complaintList;
	}

    @SuppressWarnings("unchecked")
	public List<Treatment> getTreatmentList() {
		
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
			
				// Get self-admin. (optional)
				boolean selfAdmin = false;
				
				Element selfAdminElement = medicalTreatment.getChild(SELF_ADMIN);
				
				if (selfAdminElement != null) {
				    String selfAdminStr = selfAdminElement.getAttributeValue(VALUE);
				    selfAdmin = (selfAdminStr.toLowerCase().equals("true"));
				}
				
				
				Treatment treatment = new Treatment(treatmentName, skill, 
				                      treatmentTime, selfAdmin, medicalTechLevel);
				
				treatmentList.add(treatment);
			}
		}
		
		return treatmentList;
	}
    
    /**
     * Prepare the object for garbage collection.
     */
    public void destroy() {
        medicalDoc = null;
        if(complaintList !=  null){

            complaintList.clear();
            complaintList = null;
        }
        if(treatmentList != null){

            treatmentList.clear();
            treatmentList = null;
        }
    }
}