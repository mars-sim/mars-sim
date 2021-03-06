/**
 * Mars Simulation Project
 * Pilot.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.AreologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.Delivery;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelToSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.GroundVehicleMaintenance;

/**
 * The Pilot class represents a pilot job.
 */
public class Pilot extends Job {
	/**
	 * Constructor.
	 */
	public Pilot() {
		// Use Job constructor
		super(JobType.PILOT, Job.buildRoleMap(5.0, 20.0, 20.0, 25.0, 5.0, 15.0, 10.0));
				
		// Add driver-related mission joins.

		
		jobMissionJoins.add(Exploration.class);
		
		jobMissionJoins.add(CollectIce.class);
		
		jobMissionJoins.add(CollectRegolith.class);
		
		jobMissionJoins.add(Trade.class);
		
		jobMissionJoins.add(Mining.class);
		
		jobMissionJoins.add(AreologyFieldStudy.class);
		
		jobMissionJoins.add(BiologyFieldStudy.class);
		
		jobMissionStarts.add(Delivery.class);
		jobMissionJoins.add(Delivery.class);
		
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
		double result = .1;

		int population = settlement.getNumCitizens();

		// Add contributions from all garage.
		List<Building> garage = settlement.getBuildingManager().getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE);
		Iterator<Building> j = garage.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			GroundVehicleMaintenance g = building.getGroundVehicleMaintenance();
			result += (double) g.getVehicleCapacity() / 2.5;
		}
		
		// Get number of associated vehicles at a settlement.
		result = (result + settlement.getVehicleNum() / 2.5 + population / 4.0) / 3.0;
		
//		System.out.println(settlement + " Pilot need: " + result);
		
		return result;
	}
}
