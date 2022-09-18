/*
 * Mars Simulation Project
 * Mathematician.java
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
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Mathematician class represents a job for a mathematician.
 */
public class Mathematician extends Job {
		
	/**
	 * Constructor.
	 */
	public Mathematician() {
		// Use Job constructor
		super(JobType.MATHEMATICIAN, Job.buildRoleMap(5.0, 25.0, 15.0, 15.0, 15.0, 15.0, 5.0, 30.0));
	}

	@Override
	public double getCapability(Person person) {
		double result = person.getSkillManager().getSkillLevel(SkillType.MATHEMATICS);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result+= result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;

		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = 0.1;

		int population = settlement.getNumCitizens();
		
		// Add (labspace * tech level / 2) for all labs with mathematics specialties.
		result += getBuildingScienceDemand(settlement, ScienceType.MATHEMATICS, 16D);
		result = (result + population / 20D) / 2.0;

		return result;
	}
}
