/**
 * Mars Simulation Project
 * Areologist.java
 * @version 2.76 2004-06-10
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.person.ai.mission.*;
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
		
		// Add areologist-related missions.
		jobMissionStarts.add(Exploration.class);
		jobMissionJoins.add(Exploration.class);
		jobMissionStarts.add(CollectIce.class);
		jobMissionJoins.add(CollectIce.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		double result = 0D;
		
		int areologySkill = person.getSkillManager().getSkillLevel(Skill.AREOLOGY);
		result = areologySkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute("Academic Aptitude");
		int experienceAptitude = attributes.getAttribute("Experience Aptitude");
		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
		result+= result * ((averageAptitude - 50D) / 100D);
		
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
		
		// Add number of exploration-capable rovers.
		VehicleCollection vehicles = settlement.getParkedVehicles();
		VehicleIterator j = vehicles.iterator();
		while (j.hasNext()) {
			Vehicle vehicle = j.next();
			if (vehicle instanceof Rover) {
				if (vehicle.getInventory().getResourceCapacity(Resource.ROCK_SAMPLES) > 0D) result++;
			}
		}
		
		return result;	
	}
}