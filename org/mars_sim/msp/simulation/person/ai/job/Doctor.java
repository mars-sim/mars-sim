/**
 * Mars Simulation Project
 * Doctor.java
 * @version 2.76 2004-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.job;

import java.io.Serializable;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.task.*;

/** 
 * The Doctor class represents a job for an medical treatment expert.
 */
public class Doctor extends Job implements Serializable {

	/**
	 * Constructor
	 */
	public Doctor() {
		// Use Job constructor
		super("Doctor");
		
		// Add doctor-related tasks.
		jobTasks.add(MedicalAssistance.class);
		jobTasks.add(ResearchMedicine.class);
		
		// No mission start or mission joins for doctors.
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		double result = 0D;
		
		int areologySkill = person.getSkillManager().getSkillLevel(Skill.MEDICAL);
		result = areologySkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute("Academic Aptitude");
		result+= result * ((academicAptitude - 50D) / 100D);
		
		return result;
	}
}