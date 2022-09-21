/*
 * Mars Simulation Project
 * Technician.java
 * @date 2021-09-27
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.job;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.Job;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.structure.Settlement;

public class Technician extends Job {
	/**
	 * Constructor.
	 */
	public Technician() {
		// Use Job constructor
		super(JobType.TECHNICIAN, Job.buildRoleMap(5.0, 10.0, 20.0, 15.0, 15.0, 15.0, 15.0, 15.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		int materialsScienceSkill = person.getSkillManager().getSkillLevel(SkillType.MATERIALS_SCIENCE);
		int mechanicSkill = person.getSkillManager().getSkillLevel(SkillType.MECHANICS);
		double result = mechanicSkill *.75 + materialsScienceSkill * .25;
		
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

		double result = 1;
		
		int population = settlement.getNumCitizens();
		
		// Add number of buildings in settlement.
		result += settlement.getBuildingManager().getNumBuildings() / 12D;

		// Add number of vehicles parked at settlement.
		result += settlement.getParkedVehicleNum() / 12D;

		result = (result + population / 8D) / 2.0;
				
		return result;
	}
}
