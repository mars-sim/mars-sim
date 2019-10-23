/**
 * Mars Simulation Project
 * Pilot.java
 * @version 3.1.0 2018-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.AreologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.MaintainGroundVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.MaintainGroundVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.RepairEVAMalfunction;
import org.mars_sim.msp.core.person.ai.task.RepairMalfunction;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleEVA;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Pilot class represents a pilot job.
 */
public class Pilot extends Job implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final int JOB_ID = 12;
	
	private double[] roleProspects = new double[] {5.0, 20.0, 20.0, 25.0, 5.0, 15.0, 10.0};

	/**
	 * Constructor.
	 */
	public Pilot() {
		// Use Job constructor
		super(Pilot.class);

		// Add driver-related tasks.
		jobTasks.add(MaintainGroundVehicleGarage.class);
		jobTasks.add(MaintainGroundVehicleEVA.class);
		jobTasks.add(RepairMalfunction.class);
		jobTasks.add(RepairEVAMalfunction.class);
		jobTasks.add(LoadVehicleGarage.class);
		jobTasks.add(UnloadVehicleGarage.class);
		jobTasks.add(LoadVehicleEVA.class);
		jobTasks.add(UnloadVehicleEVA.class);

		// Add side tasks
		jobTasks.add(ConsolidateContainers.class);

		// Add driver-related mission joins.
		jobMissionJoins.add(Exploration.class);
		
		jobMissionJoins.add(CollectIce.class);
		
		jobMissionJoins.add(CollectRegolith.class);
		
		jobMissionJoins.add(Trade.class);
		
		jobMissionJoins.add(Mining.class);
		
		jobMissionJoins.add(AreologyFieldStudy.class);
		
		jobMissionJoins.add(BiologyFieldStudy.class);
		
		jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);
		
		jobMissionStarts.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(RescueSalvageVehicle.class);
		
//		jobMissionJoins.add(BuildingConstructionMission.class);
		
//		jobMissionJoins.add(BuildingSalvageMission.class);
		
		jobMissionStarts.add(EmergencySupply.class);
		jobMissionJoins.add(EmergencySupply.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;

		int drivingSkill = person.getSkillManager().getSkillLevel(SkillType.PILOTING);
		result = drivingSkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result += result * ((experienceAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

//		System.out.println(person + " driver : " + Math.round(result*100.0)/100.0);

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {
		double result = 0.1;
		// Get number of associated vehicles at a settlement.	
		return result + settlement.getVehicleNum()/3;
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