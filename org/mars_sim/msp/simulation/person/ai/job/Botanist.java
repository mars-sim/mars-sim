/**
 * Mars Simulation Project
 * Botanist.java
 * @version 2.76 2004-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.task.*;

/** 
 * The Botanist class represents a job for a botanist.
 */
public class Botanist extends Job implements Serializable {

	/**
	 * Constructor
	 */
	public Botanist() {
		// Use Job constructor
		super("Botanist");
		
		// Add botany-related tasks.
		jobTasks.add(ResearchBotany.class);
		jobTasks.add(TendGreenhouse.class);
		
		// No mission start or mission joins for botany.
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		double result = 0D;
		
		int botanySkill = person.getSkillManager().getSkillLevel(Skill.BOTANY);
		result = botanySkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute("Academic Aptitude");
		int experienceAptitude = attributes.getAttribute("Experience Aptitude");
		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
		result+= result * ((averageAptitude - 50D) / 100D);
		
		return result;
	}
}