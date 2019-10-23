/**
 * Mars Simulation Project
 * Meteorologist.java
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
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.task.AssistScientificStudyResearcher;
import org.mars_sim.msp.core.person.ai.task.CompileScientificStudyResults;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.PeerReviewStudyPaper;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryResearch;
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
 * The Meteorologist class represents a job for a meteorologist.
 */
public class Meteorologist extends Job implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// private static Logger logger = Logger.getLogger(Meteorologist.class.getName());

	private final int JOB_ID = 10;
	
	private double[] roleProspects = new double[] {5.0, 10.0, 10.0, 10.0, 15.0, 20.0, 30.0};
	
	/** Constructor. */
	public Meteorologist() {
		// Use Job constructor
		super(Meteorologist.class);

		// Add meteorologist-related tasks.
		jobTasks.add(StudyFieldSamples.class);

		// Research related tasks
		jobTasks.add(AssistScientificStudyResearcher.class);
		jobTasks.add(CompileScientificStudyResults.class);
		jobTasks.add(InviteStudyCollaborator.class);
		jobTasks.add(PeerReviewStudyPaper.class);
		jobTasks.add(PerformLaboratoryResearch.class);
		jobTasks.add(ProposeScientificStudy.class);
		jobTasks.add(ResearchScientificStudy.class);
		jobTasks.add(RespondToStudyInvitation.class);

		// Add side tasks
		jobTasks.add(ConsolidateContainers.class);

		// Add meteorologist-related missions.
		jobMissionStarts.add(AreologyFieldStudy.class);		
		jobMissionJoins.add(AreologyFieldStudy.class);
		
		jobMissionJoins.add(BiologyFieldStudy.class);
		
		jobMissionStarts.add(Exploration.class);
		jobMissionJoins.add(Exploration.class);
		
//		jobMissionJoins.add(BuildingConstructionMission.class);
//		jobMissionJoins.add(BuildingSalvageMission.class);
	}

	@Override
	public double getCapability(Person person) {
		double result = 0D;

		int meteorologySkill = person.getSkillManager().getSkillLevel(SkillType.METEOROLOGY);
		result = meteorologySkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result += result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = 0.1;

		// Add (labspace * tech level / 2) for all labs with meteorology specialties.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
		Iterator<Building> i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.hasSpecialty(ScienceType.METEOROLOGY)) {
				result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 1.5D);
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