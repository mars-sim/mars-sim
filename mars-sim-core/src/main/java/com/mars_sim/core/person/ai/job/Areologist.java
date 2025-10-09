/*
 * Mars Simulation Project
 * Areologist.java
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
 * The Areologist class represents a job for an areologist, one who studies the
 * rocks and landforms of Mars.
 */
public class Areologist extends Job {
	
	/**
	 * Constructor.
	 */
	public Areologist() {
		// Use Job constructor
		super(JobType.AREOLOGIST,  Job.buildRoleMap(5.0, 10.0, 5.0, 5.0, 20.0, 25.0, 10.0, 30.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = person.getSkillManager().getSkillLevel(SkillType.AREOLOGY);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
		result += result * ((averageAptitude - 100D) / 100D);

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

		int population = settlement.getNumCitizens();
		
		// Add (labspace * tech level / 2) for all labs with areology specialties.
		result += getBuildingScienceDemand(settlement, ScienceType.AREOLOGY, 10D);

		// Add (labspace * tech level / 2) for all parked rover labs with areology
		// specialties.
		result += getParkedVehicleScienceDemand(settlement, ScienceType.AREOLOGY, 12D);

		// Add (labspace * tech level / 2) for all labs with areology specialties in
		// rovers out on missions.
		result += getMissionScienceDemand(settlement, ScienceType.AREOLOGY, 8D);

		result = (result + population / 20D) / 4.0;
				
		return result;
	}
}
