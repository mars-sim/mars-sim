/**
 * Mars Simulation Project
 * Driver.java
 * @version 2.85 2009-01-24
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.person.ai.mission.*;
import org.mars_sim.msp.simulation.structure.Settlement;

/** 
 * The Driver class represents a rover driver job.
 */
public class Driver extends Job implements Serializable {

	/**
	 * Constructor
	 */
	public Driver() {
		// Use Job constructor
		super("Driver");
		
		// Add driver-related tasks.
		jobTasks.add(MaintainGroundVehicleGarage.class);
		jobTasks.add(MaintainGroundVehicleEVA.class);
		jobTasks.add(RepairMalfunction.class);
		jobTasks.add(RepairEVAMalfunction.class);
		jobTasks.add(LoadVehicle.class);
		jobTasks.add(UnloadVehicle.class);
		jobTasks.add(EnterAirlock.class);
		jobTasks.add(ExitAirlock.class);
		
		// Add driver-related mission joins.
		jobMissionJoins.add(Exploration.class);
		jobMissionJoins.add(CollectIce.class);
		jobMissionJoins.add(CollectRegolith.class);
		jobMissionJoins.add(Trade.class);
		jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);
		jobMissionStarts.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(RescueSalvageVehicle.class);
		jobMissionJoins.add(Mining.class);
        jobMissionJoins.add(BuildingConstructionMission.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		double result = 0D;
		
		int areologySkill = person.getMind().getSkillManager().getSkillLevel(Skill.DRIVING);
		result = areologySkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		result+= result * ((experienceAptitude - 50D) / 100D);
		
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
        
		// Get number of vehicles parked at a settlement.
		double settlementVehicleNum = settlement.getParkedVehicleNum();
		
		// Add number of vehicles out on missions for the settlement.
		MissionManager missionManager = Simulation.instance().getMissionManager();
		Iterator i = missionManager.getMissionsForSettlement(settlement).iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) settlementVehicleNum++;
		}
		
		result = settlementVehicleNum;
        
        // Add overpopulation modifier.
        int overpop = settlement.getAllAssociatedPeople().size() - settlement.getPopulationCapacity();
        if (overpop > 0) result += (overpop * 10D);
        
        return result;
	}
}