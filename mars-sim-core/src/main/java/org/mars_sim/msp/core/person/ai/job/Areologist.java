/**
 * Mars Simulation Project
 * Areologist.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Lab;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.AreologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.BiologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupplyMission;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.AssistScientificStudyResearcher;
import org.mars_sim.msp.core.person.ai.task.CompileScientificStudyResults;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.DigLocalIce;
import org.mars_sim.msp.core.person.ai.task.DigLocalRegolith;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.PeerReviewStudyPaper;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryResearch;
import org.mars_sim.msp.core.person.ai.task.ProposeScientificStudy;
import org.mars_sim.msp.core.person.ai.task.ResearchScientificStudy;
import org.mars_sim.msp.core.person.ai.task.RespondToStudyInvitation;
import org.mars_sim.msp.core.person.ai.task.ReviewJobReassignment;
import org.mars_sim.msp.core.person.ai.task.StudyFieldSamples;
import org.mars_sim.msp.core.person.ai.task.WriteReport;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Areologist class represents a job for an areologist, one who studies the rocks and landforms of Mars.
 */
public class Areologist
extends Job
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private static Logger logger = Logger.getLogger(Areologist.class.getName());

	/**
	 * Constructor.
	 */
	public Areologist() {
		// Use Job constructor
		super(Areologist.class);

		// Add areologist-related tasks.
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
		jobTasks.add(DigLocalRegolith.class);
		jobTasks.add(DigLocalIce.class);
		jobTasks.add(WriteReport.class);
		jobTasks.add(ReviewJobReassignment.class);

		// Add areologist-related missions.
		jobMissionStarts.add(Exploration.class);
		jobMissionJoins.add(Exploration.class);
		jobMissionStarts.add(CollectIce.class);
		jobMissionJoins.add(CollectIce.class);
		jobMissionStarts.add(CollectRegolith.class);
		jobMissionJoins.add(CollectRegolith.class);
        jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);
		jobMissionStarts.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(RescueSalvageVehicle.class);
		jobMissionStarts.add(Mining.class);
		jobMissionJoins.add(Mining.class);
        jobMissionJoins.add(BuildingConstructionMission.class);
        jobMissionJoins.add(BuildingSalvageMission.class);
        jobMissionStarts.add(AreologyStudyFieldMission.class);
        jobMissionJoins.add(AreologyStudyFieldMission.class);
        jobMissionJoins.add(BiologyStudyFieldMission.class);
        jobMissionStarts.add(EmergencySupplyMission.class);
        jobMissionJoins.add(EmergencySupplyMission.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;

		int areologySkill = person.getMind().getSkillManager().getSkillLevel(SkillType.AREOLOGY);
		result = areologySkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttribute.ACADEMIC_APTITUDE);
		int experienceAptitude = attributes.getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
		result+= result * ((averageAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {
		double result = 0D;

		// Add (labspace * tech level / 2) for all labs with areology specialties.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.RESEARCH);
		Iterator<Building> i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
		    Building building = i.next();
		    Research lab = (Research) building.getFunction(BuildingFunction.RESEARCH);
		    if (lab.hasSpecialty(ScienceType.AREOLOGY)) {
		        result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 2D);
		    }
		}

		// Add (labspace * tech level / 2) for all parked rover labs with areology specialties.
		Iterator<Vehicle> j = settlement.getParkedVehicles().iterator();
		while (j.hasNext()) {
			Vehicle vehicle = j.next();
			if (vehicle instanceof Rover) {
                Rover rover = (Rover) vehicle;
                if (rover.hasLab()) {
                    Lab lab = rover.getLab();
                    if (lab.hasSpecialty(ScienceType.AREOLOGY)) {
                        result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 2D);
                    }
                }
			}
		}

		// Add (labspace * tech level / 2) for all labs with areology specialties in rovers out on missions.
		MissionManager missionManager = Simulation.instance().getMissionManager();
		Iterator<Mission> k = missionManager.getMissionsForSettlement(settlement).iterator();
		while (k.hasNext()) {
			Mission mission = k.next();
			if (mission instanceof RoverMission) {
				Rover rover = ((RoverMission) mission).getRover();
                if ((rover != null) && !settlement.getParkedVehicles().contains(rover)) {
                    if (rover.hasLab()) {
                        Lab lab = rover.getLab();
                        if (lab.hasSpecialty(ScienceType.AREOLOGY)) {
                            result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 2D);
                        }
                    }
                }
			}
		}

		return result;
	}

}