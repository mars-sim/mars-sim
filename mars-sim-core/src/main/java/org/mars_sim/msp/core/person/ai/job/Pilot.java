/*
 * Mars Simulation Project
 * Pilot.java
 * @date 2021-09-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
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
		super(JobType.PILOT, Job.buildRoleMap(5.0, 5.0, 20.0, 20.0, 25.0, 5.0, 15.0, 10.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = person.getSkillManager().getSkillLevel(SkillType.PILOTING);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result += result * ((experienceAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

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
		result = (result + settlement.getOwnedVehicleNum() / 2.5 + population / 4.0) / 3.0;
				
		return result;
	}
}
