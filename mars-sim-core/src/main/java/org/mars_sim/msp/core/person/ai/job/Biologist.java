/**
 * Mars Simulation Project
 * Biologist.java
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
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.AssistScientificStudyResearcher;
import org.mars_sim.msp.core.person.ai.task.CompileScientificStudyResults;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.InviteStudyCollaborator;
import org.mars_sim.msp.core.person.ai.task.PeerReviewStudyPaper;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryResearch;
import org.mars_sim.msp.core.person.ai.task.ProduceFood;
import org.mars_sim.msp.core.person.ai.task.ProposeScientificStudy;
import org.mars_sim.msp.core.person.ai.task.ResearchScientificStudy;
import org.mars_sim.msp.core.person.ai.task.RespondToStudyInvitation;
import org.mars_sim.msp.core.person.ai.task.StudyFieldSamples;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Biologist class represents a job for a biologist.
 */
public class Biologist
extends Job
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//	private static Logger logger = Logger.getLogger(Biologist.class.getName());

	private final int JOB_ID = 3;

	private double[] roleProspects = new double[] {20.0, 5.0, 5.0, 5.0, 20.0, 15.0, 30.0};
	
	/**
	 * Constructor.
	 */
	public Biologist() {
		// Use Job constructor
		super(Biologist.class);

//		missionManager = Simulation.instance().getMissionManager();
		
		// Add biologist-related tasks.
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
		jobTasks.add(ProduceFood.class);

		// Add biologist-related missions.
		jobMissionJoins.add(AreologyFieldStudy.class);
		
		jobMissionStarts.add(BiologyFieldStudy.class);
		jobMissionJoins.add(BiologyFieldStudy.class);
		
		jobMissionJoins.add(Exploration.class);
		
		jobMissionJoins.add(BuildingConstructionMission.class);
		
		jobMissionJoins.add(BuildingSalvageMission.class);
	}

	@Override
	public double getCapability(Person person) {
		double result = 0D;

		int biologySkill = person.getSkillManager().getSkillLevel(SkillType.BIOLOGY);
		result = biologySkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result+= result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;

//		System.out.println(person + " bio : " + Math.round(result*100.0)/100.0);
		
		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = 0D;

		// Add (labspace * tech level / 2) for all labs with biology specialties.
		List<Building> laboratoryBuildings = settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH);
		Iterator<Building> i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Research lab = building.getResearch();
			if (lab.hasSpecialty(ScienceType.BIOLOGY)) {
				result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 4D);
			}
		}

		// Add (labspace * tech level / 2) for all parked rover labs with biology specialties.
		Iterator<Vehicle> j = settlement.getParkedVehicles().iterator();
		while (j.hasNext()) {
			Vehicle vehicle = j.next();
			if (vehicle instanceof Rover) {
				Rover rover = (Rover) vehicle;
				if (rover.hasLab()) {
					Lab lab = rover.getLab();
					if (lab.hasSpecialty(ScienceType.BIOLOGY)) {
						result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 8D);
					}
				}
			}
		}

		// Add (labspace * tech level / 2) for all labs with biology specialties in rovers out on missions.
		//MissionManager missionManager = Simulation.instance().getMissionManager();
		Iterator<Mission> k = missionManager.getMissionsForSettlement(settlement).iterator();
		while (k.hasNext()) {
			Mission mission = k.next();
			if (mission instanceof RoverMission) {
				Rover rover = ((RoverMission) mission).getRover();
				if ((rover != null) && !settlement.getParkedVehicles().contains(rover)) {
					if (rover.hasLab()) {
						Lab lab = rover.getLab();
						if (lab.hasSpecialty(ScienceType.BIOLOGY)) {
							result += (lab.getLaboratorySize() * lab.getTechnologyLevel() / 8D);
						}
					}
				}
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