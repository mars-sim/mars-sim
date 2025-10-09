/*
 * Mars Simulation Project
 * Astrobiologist.java
 * @date 2025-09-15
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
 * The astrobiologist class represents a job for investigating the characteristics of organic lives.
 */
public class Astrobiologist
extends Job {
	/**
	 * Constructor.
	 */
	public Astrobiologist() {
		// Use Job constructor
		super(JobType.ASTROBIOLOGIST, Job.buildRoleMap(20.0, 0.0, 5.0, 5.0, 5.0, 20.0, 15.0, 30.0));
	}

	@Override
	public double getCapability(Person person) {
		double result = person.getSkillManager().getSkillLevel(SkillType.ASTROBIOLOGY);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result+= result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;
		
		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		
		int population = settlement.getNumCitizens();
		
		// Add (labspace * tech level / 2) for all labs with biology specialties.
		double result = getBuildingScienceDemand(settlement, ScienceType.ASTROBIOLOGY, 16D);

		// Add (labspace * tech level / 2) for all parked rover labs with biology specialties.
		result += getParkedVehicleScienceDemand(settlement, ScienceType.ASTROBIOLOGY, 16D);

		// Add (labspace * tech level / 2) for all labs with biology specialties in rovers out on missions.
		result += getMissionScienceDemand(settlement, ScienceType.ASTROBIOLOGY, 16D);

		result = (result + population / 12D) / 4.0;
				
		return result;
	}
}
