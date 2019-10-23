/**
 * Mars Simulation Project
 * Chemist.java
 * @version 3.1.0 2018-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.AreologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.AssistScientificStudyResearcher;
import org.mars_sim.msp.core.person.ai.task.CompileScientificStudyResults;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.ManufactureGood;
import org.mars_sim.msp.core.person.ai.task.PeerReviewStudyPaper;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryExperiment;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryResearch;
import org.mars_sim.msp.core.person.ai.task.ProduceFood;
import org.mars_sim.msp.core.person.ai.task.ProposeScientificStudy;
import org.mars_sim.msp.core.person.ai.task.ResearchScientificStudy;
import org.mars_sim.msp.core.person.ai.task.RespondToStudyInvitation;
import org.mars_sim.msp.core.person.ai.task.StudyFieldSamples;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Research;

/**
 * The Chemist class represents a job for a chemist.
 */
public class Chemist extends Job implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// private static Logger logger = Logger.getLogger(Chemist.class.getName());

	private final int JOB_ID = 6;
	
	private double[] roleProspects = new double[] {20.0, 5.0, 5.0, 5.0, 20.0, 15.0, 30.0};
	
	/**
	 * Constructor.
	 */
	public Chemist() {
		// Use Job constructor
		super(Chemist.class);

		// Add chemist-related tasks.
		jobTasks.add(ManufactureGood.class);
		jobTasks.add(ProduceFood.class);
		jobTasks.add(StudyFieldSamples.class);

		// Research related tasks
		jobTasks.add(AssistScientificStudyResearcher.class);
		jobTasks.add(CompileScientificStudyResults.class);
		jobTasks.add(InviteStudyCollaborator.class);
		jobTasks.add(PeerReviewStudyPaper.class);
		jobTasks.add(PerformLaboratoryExperiment.class);
		jobTasks.add(PerformLaboratoryResearch.class);
		jobTasks.add(ProposeScientificStudy.class);
		jobTasks.add(ResearchScientificStudy.class);
		jobTasks.add(RespondToStudyInvitation.class);

		// Add side tasks
		jobTasks.add(ConsolidateContainers.class);

		// Add chemist-related missions.
		jobMissionStarts.add(AreologyFieldStudy.class);
		jobMissionJoins.add(AreologyFieldStudy.class);
		
		jobMissionJoins.add(BiologyFieldStudy.class);
		
		jobMissionJoins.add(Exploration.class);
		
		jobMissionStarts.add(CollectIce.class);
		jobMissionJoins.add(CollectIce.class);
		
		jobMissionStarts.add(CollectRegolith.class);
		jobMissionJoins.add(CollectRegolith.class);
		jobMissionJoins.add(BuildingConstructionMission.class);
		
		jobMissionJoins.add(BuildingSalvageMission.class);
	}

	@Override
	public double getCapability(Person person) {
		double result = 0D;

		int chemistrySkill = person.getSkillManager().getSkillLevel(SkillType.CHEMISTRY);
		result = chemistrySkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result += result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

//		System.out.println(person + " chemist : " + Math.round(result*100.0)/100.0);

		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = .1;

		// Add (labspace * tech level / 2) for all labs with chemistry specialties.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
		Iterator<Building> i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.hasSpecialty(ScienceType.CHEMISTRY)) {
				result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 4D);
			}
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