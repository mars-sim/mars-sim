/**
 * Mars Simulation Project
 * Driver.java
 * @version 2.76 2004-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.mission.*;

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
		
		// No tasks related to driver job.
		
		// Add driver-related mission joins.
		jobMissionJoins.add(Exploration.class);
		jobMissionJoins.add(CollectIce.class);
		jobMissionJoins.add(TravelToSettlement.class);
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		double result = 0D;
		
		int areologySkill = person.getSkillManager().getSkillLevel(Skill.DRIVING);
		result = areologySkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute("Experience Aptitude");
		result+= result * ((experienceAptitude - 50D) / 100D);
		
		return result;
	}
}