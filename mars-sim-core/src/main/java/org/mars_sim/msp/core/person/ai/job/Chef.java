/*
 * Mars Simulation Project
 * Chef.java
 * @date 2021-09-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.Job;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * The Chef class represents a job for a chef.
 */
public class Chef extends Job {
		
	/** constructor. */
	public Chef() {
		// Use Job constructor
		super(JobType.CHEF, Job.buildRoleMap(35.0, 5.0, 5.0, 5.0, 5.0, 20.0, 15.0, 15.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result =  person.getSkillManager().getSkillLevel(SkillType.COOKING);

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

		// Add all kitchen work space in settlement.
		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.COOKING).iterator();
		while (i.hasNext()) {
			result += (double) i.next().getCooking().getCookCapacity()/12D;
		}

		// Add total population / 10.
		int population = settlement.getIndoorPeopleCount();

		result = (result + population / 12D) / 2.0;
		
		return result;
	}
}
