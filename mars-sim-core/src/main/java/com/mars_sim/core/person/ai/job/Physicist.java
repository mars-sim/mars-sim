/*
 * Mars Simulation Project
 * Physicist.java
 * @date 2021-09-27
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.job;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.Job;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;

/**
 * The Physicist class represents a job for a physicist.
 */
public class Physicist extends Job {
	
	/** Constructor. */
	public Physicist() {
		// Use Job constructor
		super(JobType.PHYSICIST, Job.buildRoleMap(5.0, 25.0, 15.0, 10.0, 10.0, 15.0, 15.0, 30.0));
	}

	@Override
	public double getCapability(Person person) {
		double result = person.getSkillManager().getSkillLevel(SkillType.PHYSICS);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result+= result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) 
			result = 0D;

		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = 0.1;

		int population = settlement.getNumCitizens();
		
		// Add (labspace * tech level / 2D) for all labs with physics specialties.
		result += getBuildingScienceDemand(settlement, ScienceType.PHYSICS, 12D);

		result += getMissionScienceDemand(settlement, ScienceType.PHYSICS, 12D);

		
		result = (result + population / 24D) / 5.0;
				
		return result;
	}
}
