/**
 * Mars Simulation Project
 * Psychologist.java
 * @version 3.1.2 2020-08-06
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.AssistScientificStudyResearcher;
import org.mars_sim.msp.core.person.ai.task.CompileScientificStudyResults;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.ExamineBody;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.PeerReviewStudyPaper;
import org.mars_sim.msp.core.person.ai.task.PrescribeMedication;
import org.mars_sim.msp.core.person.ai.task.ProposeScientificStudy;
import org.mars_sim.msp.core.person.ai.task.ResearchScientificStudy;
import org.mars_sim.msp.core.person.ai.task.RespondToStudyInvitation;
import org.mars_sim.msp.core.person.ai.task.TreatMedicalPatient;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.structure.building.function.Research;

/**
 * The Psychologist class represents a job for evaluating a person's mind and behavior.
 */
public class Psychologist extends Job implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// private static Logger logger = Logger.getLogger(Psychologist.class.getName());

	private final int JOB_ID = 14;
	
	private double[] roleProspects = new double[] {5.0, 5.0, 25.0, 20.0, 10.0, 15.0, 20.0};
	
	/** Constructor. */
	public Psychologist() {
		// Use Job constructor
		super(Psychologist.class);

		// Add doctor-related tasks.
		jobTasks.add(PrescribeMedication.class);
		jobTasks.add(TreatMedicalPatient.class);
		jobTasks.add(ExamineBody.class);

		// Research related tasks
		jobTasks.add(AssistScientificStudyResearcher.class);
		jobTasks.add(CompileScientificStudyResults.class);
		jobTasks.add(InviteStudyCollaborator.class);
		jobTasks.add(PeerReviewStudyPaper.class);
		jobTasks.add(ProposeScientificStudy.class);
		jobTasks.add(ResearchScientificStudy.class);
		jobTasks.add(RespondToStudyInvitation.class);

		// Add side tasks
		jobTasks.add(ConsolidateContainers.class);
//		jobTasks.add(ReviewMissionP lan.class);

		// Add doctor-related missions.
//		jobMissionJoins.add(BuildingConstructionMission.class);
//		
//		jobMissionJoins.add(BuildingSalvageMission.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;

		int skill = person.getSkillManager().getSkillLevel(SkillType.PSYCHOLOGY);
		result = skill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
		result += result * ((averageAptitude - 100D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

//		System.out.println(person + " doctor : " + Math.round(result*100.0)/100.0);

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {

		double result = .1;

		// Add total population / 10
		int population = settlement.getNumCitizens();
		result += population / 32D;

		// Add (labspace * tech level) / 2 for all labs with medical specialties.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
		Iterator<Building> i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.hasSpecialty(ScienceType.MEDICINE)) {
				result += ((double) (lab.getResearcherNum() * lab.getTechnologyLevel()) / 8D);
			}
		}

		// Add (tech level / 2) for all medical infirmaries.
		List<Building> medicalBuildings = settlement.getBuildingManager().getBuildings(FunctionType.MEDICAL_CARE);
		Iterator<Building> j = medicalBuildings.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			MedicalCare infirmary = building.getMedical();
			result += (double) infirmary.getTechLevel() / 8D;
		}

		return result;
	}

	public double[] getRoleProspects() {
		return roleProspects;
	}
	
	public void setRoleProspects(int index, int weight) {
		roleProspects[index] = weight;
	}
	
	public int getJobID() {
		return JOB_ID;
	}
}
