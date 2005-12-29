/**
 * Mars Simulation Project
 * Areologist.java
 * @version 2.78 2005-08-22
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.person.ai.mission.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.Research;
import org.mars_sim.msp.simulation.vehicle.*;

/** 
 * The Areologist class represents a job for an areologist, one who studies the rocks and landforms of Mars.
 */
public class Areologist extends Job implements Serializable {

	/**
	 * Constructor
	 */
	public Areologist() {
		// Use Job constructor
		super("Areologist");
		
		// Add areologist-related tasks.
		jobTasks.add(StudyRockSamples.class);
		jobTasks.add(CollectResources.class);
		jobTasks.add(EnterAirlock.class);
		jobTasks.add(ExitAirlock.class);
		
		// Add areologist-related missions.
		jobMissionStarts.add(Exploration.class);
		jobMissionJoins.add(Exploration.class);
		jobMissionStarts.add(CollectIce.class);
		jobMissionJoins.add(CollectIce.class);
		jobMissionStarts.add(TravelToSettlement.class);
		jobMissionJoins.add(TravelToSettlement.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		double result = 0D;
		
		int areologySkill = person.getMind().getSkillManager().getSkillLevel(Skill.AREOLOGY);
		result = areologySkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
		int experienceAptitude = attributes.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
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
		
		// Add (labspace * tech level) for all labs with areology specialities.
		List laboratoryBuildings = settlement.getBuildingManager().getBuildings(Research.NAME);
		Iterator i = laboratoryBuildings.iterator();
		while (i.hasNext()) {
			Building building = (Building) i.next();
			try {
				Research lab = (Research) building.getFunction(Research.NAME);
				if (lab.hasSpeciality(Skill.AREOLOGY)) 
					result += (lab.getLaboratorySize() * lab.getTechnologyLevel());
			}
			catch (BuildingException e) {
				System.err.println("Areologist.getSettlementNeed(): " + e.getMessage());
			}
		}
		
		// Add number of exploration-capable rovers parked at the settlement.
		VehicleCollection vehicles = settlement.getParkedVehicles();
		VehicleIterator j = vehicles.iterator();
		while (j.hasNext()) {
			Vehicle vehicle = j.next();
			if (vehicle instanceof Rover) {
				if (vehicle.getInventory().getAmountResourceCapacity(AmountResource.ROCK_SAMPLES) > 0D) result++;
			}
		}
		
		// Add number of exploration-capable rovers out on missions for the settlement.
		MissionManager missionManager = Simulation.instance().getMissionManager();
		Iterator k = missionManager.getMissionsForSettlement(settlement).iterator();
		while (k.hasNext()) {
			Mission mission = (Mission) k.next();
			if (mission instanceof RoverMission) {
				Rover rover = ((RoverMission) mission).getRover();
				if ((rover != null) && rover.getInventory().getAmountResourceCapacity(AmountResource.ROCK_SAMPLES) > 0D) result++;
			}
		}
		
		return result;	
	}
}