/*
 * Mars Simulation Project
 * MedicalConfig.java
 * @date 2022-07-29
 * @author Scott Davis
 */
package com.mars_sim.core.person.health;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.data.Range;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;


/**
 * Provides configuration information about a list of medical complaints and treatment.
 * Uses a JDOM document to get the information. 
 */
public class MedicalConfig {
    // Default span for a Recovery range
	static final double RECOVERY_SPAN = 1.1D;

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
	private static final String ENVIRONMENTAL = "environmental";
	private static final String EFFORT_INFLUENCE = "effort-influence";
	
	private Map<ComplaintType,Complaint> complaintList = new EnumMap<>(ComplaintType.class);
	private Map<Integer,List<Treatment>> treatmentsByTechLevel = new HashMap<>();

	private int highestLevel;

	/**
	 * Constructor.
	 * 
	 * @param medicalDoc DOM document of medical configuration.
	 */
	public MedicalConfig(Document medicalDoc) {
		buildTreatmentList(medicalDoc);
		buildComplaintList(medicalDoc);
	}

	/**
	 * Gets a list of medical complaints.
	 * 
	 * @return list of complaints
	 * @throws Exception if list could not be found.
	 */
	public Collection<Complaint> getComplaintList() {
		return complaintList.values();
	}
	
	/**
	 * Find a complaint by it's name
	 * @param type
	 * @return
	 */
	public Complaint getComplaintByName(ComplaintType type) {
		return complaintList.get(type);
	}
	
	/**
	 * Get a list of treatments that needs a level of medical technical capability.
	 * For example, searchign for level would include treatments needing levels 1,2&3
	 * @param level
	 * @return
	 */
	public List<Treatment> getTreatmentsByLevel(int level) {
		// Make sure level is not about highestLevel recorded
		level = Math.min(highestLevel, level);
		return treatmentsByTechLevel.computeIfAbsent(level, id -> Collections.emptyList());
	}

	/**
	 * Builds the treatment list.
	 * 
	 * @param configDoc
	 */
	private synchronized void buildTreatmentList(Document configDoc) {

		Element medicalTreatmentList = configDoc.getRootElement().getChild(TREATMENT_LIST);
		List<Element> treatments = medicalTreatmentList.getChildren(TREATMENT);
		
		for (Element medicalTreatment : treatments) {	
			// Get name.
			String treatmentName = medicalTreatment.getAttributeValue(NAME);
			
			int skill = getIntValue(medicalTreatment, SKILL, false, 0);
			int medicalTechLevel = getIntValue(medicalTreatment, MEDICAL_TECH_LEVEL, false, 0);
			double treatmentTime = getDoubleValue(medicalTreatment, TREATMENT_TIME, false, -1D);
			boolean selfAdmin = getBoolValue(medicalTreatment, SELF_ADMIN, false, false);

			Treatment treatment = new Treatment(treatmentName, skill, 
			                      treatmentTime, selfAdmin, medicalTechLevel);
			highestLevel = Math.max(highestLevel, medicalTechLevel);
			treatmentsByTechLevel.computeIfAbsent(medicalTechLevel, id -> new ArrayList<>()).add(treatment);
		}
		
		// Add treatment to all the levels
		var previous = treatmentsByTechLevel.computeIfAbsent(0, id -> new ArrayList<>());
		for(int i = 1; i <= highestLevel; i++) {
			var currentLevel = treatmentsByTechLevel.computeIfAbsent(i, id -> new ArrayList<>());
			currentLevel.addAll(previous);
			previous = currentLevel;
		}
	}

	/**
	 * Builds the complaint list.
	 * 
	 * @param configDoc
	 */
	private synchronized void buildComplaintList(Document configDoc) {

		// Highest level will be all treatments is all tretments
		var treatmentList = treatmentsByTechLevel.get(highestLevel);

		Element root = configDoc.getRootElement();
		Element medicalComplaintList = root.getChild(MEDICAL_COMPLAINT_LIST);
		List<Element> medicalComplaints = medicalComplaintList.getChildren(MEDICAL_COMPLAINT);
		
		for (Element medicalComplaint : medicalComplaints) {				
			// Get name.
			String complaintName = medicalComplaint.getAttributeValue(NAME).toUpperCase().replace(' ', '_');
						
			int seriousness = getIntValue(medicalComplaint, SERIOUSNESS, true, 0);
			double degradeTime = getDoubleValue(medicalComplaint, DEGRADE_TIME, false, 0D);
			double probability = getDoubleValue(medicalComplaint, PROBABILITY, false, 0D);
			double performance = getDoubleValue(medicalComplaint, PERFORMANCE_PERCENT, true, 0D);
			boolean bedRestRecovery = getBoolValue(medicalComplaint, BED_REST_RECOVERY, true, false);
			boolean environmental = getBoolValue(medicalComplaint, ENVIRONMENTAL, false, false);

			// Get recovery time. If no max; then max is x0.1 larger than min
			Element recoveryTimeElement = medicalComplaint.getChild(RECOVERY_TIME);
			Range recoveryTime = ConfigHelper.parseRange(recoveryTimeElement, RECOVERY_SPAN);

			// Get the treatment. (optional)
			String treatmentStr = "";
			Element treatmentElement = medicalComplaint.getChild(TREATMENT_TYPE);
			if (treatmentElement != null) {
			    treatmentStr = treatmentElement.getAttributeValue(VALUE);
			}

			Treatment treatment = null;
			for (Treatment tempTreatment : treatmentList) {
			    if (tempTreatment.getName().equals(treatmentStr)) {
			        treatment = tempTreatment;
			        break;
			    }
			}

			if (treatmentStr.length() != 0 && (treatment == null))
			    throw new IllegalStateException("treatment: " + treatmentStr + " could not be found in treatment list");

			// Get the degrade complaint. (optional)
			Complaint degradeComplaint = null;
			Element degradeComplaintElement = medicalComplaint.getChild(DEGRADE_COMPLAINT);
			if (degradeComplaintElement != null) {
			    String degradeComplaintName = degradeComplaintElement.getAttributeValue(VALUE);
				var degradeType = ConfigHelper.getEnum(ComplaintType.class, degradeComplaintName);
				degradeComplaint = complaintList.get(degradeType);
				if (degradeComplaint == null) {
					throw new IllegalStateException("Degrade Complaint: " + degradeType + " could not be found");
				}
			}

			String effortName = getStringValue(medicalComplaint, EFFORT_INFLUENCE, false);
			PhysicalEffort effort = PhysicalEffort.NONE;
			if (effortName != null) {
				effort = ConfigHelper.getEnum(PhysicalEffort.class, effortName);
			}

			ComplaintType type = ConfigHelper.getEnum(ComplaintType.class, complaintName);
			Complaint complaint = new Complaint(type, seriousness, degradeTime * 1000D,
											recoveryTime, probability, treatment, degradeComplaint,
											performance, bedRestRecovery, environmental,
											effort);

			complaintList.put(type, complaint);
		}
	}

	private static String getStringValue(Element elem, String name, boolean mandatory) {
		String value = null;			
		Element valueElement = elem.getChild(name);
		if(valueElement != null)
			value = valueElement.getAttributeValue(VALUE);
		if ((value == null) && mandatory) {
			throw new IllegalArgumentException("Element " + name + " must be defined with a " + VALUE);
		}
		return value;
	}

	private static boolean getBoolValue(Element elem, String name, boolean mandatory, boolean defaultValue) {
		String text = getStringValue(elem, name, mandatory);
		if(text != null)
			return Boolean.parseBoolean(text);
		return defaultValue;
	}

	private static int getIntValue(Element elem, String name, boolean mandatory, int defaultValue) {
		String text = getStringValue(elem, name, mandatory);
		if(text != null)
			return Integer.parseInt(text);
		return defaultValue;
	}

	private static double getDoubleValue(Element elem, String name, boolean mandatory, double defaultValue) {
		String text = getStringValue(elem, name, mandatory);
		if(text != null)
			return Double.parseDouble(text);
		return defaultValue;
	}
}
