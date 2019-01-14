/**
 * Mars Simulation Project
 * Astronomer.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.AreologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.task.AssistScientificStudyResearcher;
import org.mars_sim.msp.core.person.ai.task.CompileScientificStudyResults;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.ObserveAstronomicalObjects;
import org.mars_sim.msp.core.person.ai.task.PeerReviewStudyPaper;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryResearch;
import org.mars_sim.msp.core.person.ai.task.ProposeScientificStudy;
import org.mars_sim.msp.core.person.ai.task.ResearchScientificStudy;
import org.mars_sim.msp.core.person.ai.task.RespondToStudyInvitation;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Research;

/**
 * The Astronomer class represents a job for an astronomer.
 */
public class Astronomer extends Job implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// private static Logger logger = Logger.getLogger(Astronomer.class.getName());

	/** Constructor. */
	public Astronomer() {
		// Use Job constructor
		super(Astronomer.class);

		// 2015-01-03 Added PrepareDessert
		// jobTasks.add(PrepareDessert.class);

		// Add astronomer-related tasks.
		jobTasks.add(ObserveAstronomicalObjects.class);

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

		// Add astronomer-related missions.
		jobMissionStarts.add(Exploration.class);
		jobMissionJoins.add(Exploration.class);
		
		jobMissionStarts.add(AreologyStudyFieldMission.class);
		jobMissionJoins.add(AreologyStudyFieldMission.class);
		
		jobMissionStarts.add(Mining.class);
		jobMissionJoins.add(Mining.class);
		
//		jobMissionStarts.add(TravelToSettlement.class);
//		jobMissionJoins.add(TravelToSettlement.class);
		
//		jobMissionStarts.add(RescueSalvageVehicle.class);
//		jobMissionJoins.add(RescueSalvageVehicle.class);
		
//		jobMissionJoins.add(BuildingConstructionMission.class);
//		
//		jobMissionJoins.add(BuildingSalvageMission.class);
		
//		jobMissionStarts.add(EmergencySupplyMission.class);
//		jobMissionJoins.add(EmergencySupplyMission.class);
	}

	@Override
	public double getCapability(Person person) {
		double result = 0D;

		int astronomySkill = person.getMind().getSkillManager().getSkillLevel(SkillType.ASTRONOMY);
		result = astronomySkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result += result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

//		System.out.println(person + " astro : " + Math.round(result*100.0)/100.0);

		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = 0D;

		BuildingManager manager = settlement.getBuildingManager();

		// Add (labspace * tech level / 2) for all labs with astronomy specialties.
		Iterator<Building> i = manager.getBuildings(FunctionType.RESEARCH).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.hasSpecialty(ScienceType.ASTRONOMY))
				result += lab.getLaboratorySize() * lab.getTechnologyLevel() / 3.5D;
		}

		// Add astronomical observatories (observer capacity * tech level * 2).
		Iterator<Building> j = manager.getBuildings(FunctionType.ASTRONOMICAL_OBSERVATIONS).iterator();
		while (j.hasNext()) {
			Building building = j.next();
			AstronomicalObservation observatory = building.getAstronomicalObservation();
			result += observatory.getObservatoryCapacity() * observatory.getTechnologyLevel() * 1.5D;
		}

		return result;
	}

}