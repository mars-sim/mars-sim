/**
 * Mars Simulation Project
 * Engineer.java
 * @version 2.76 2004-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.task.*;

/** 
 * The Engineer class represents an engineer job focusing on repair and maintenance of buildings and vehicles.
 */
public class Engineer extends Job implements Serializable {

	/**
	 * Constructor
	 */
	public Engineer() {
		// Use Job constructor
		super("Engineer");
		
		// Add engineer-related tasks.
		jobTasks.add(Maintenance.class);
		jobTasks.add(MaintainGroundVehicleGarage.class);
		jobTasks.add(MaintainGroundVehicleEVA.class);
		jobTasks.add(RepairMalfunction.class);
		jobTasks.add(RepairEVAMalfunction.class);
		
		// No mission start or mission joins for doctors.
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		double result = 0D;
		
		int areologySkill = person.getSkillManager().getSkillLevel(Skill.MECHANICS);
		result = areologySkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute("Experience Aptitude");
		result+= result * ((experienceAptitude - 50D) / 100D);
		
		return result;
	}
}