/**
 * Mars Simulation Project
 * Areologist.java
 * @version 2.76 2004-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.person.ai.mission.*;

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
}