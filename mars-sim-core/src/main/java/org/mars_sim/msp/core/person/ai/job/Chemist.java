/*
 * Mars Simulation Project
 * Chemist.java
 * @date 2021-09-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Chemist class represents a job for a chemist.
 */
public class Chemist extends Job {
	
	/**
	 * Constructor.
	 */
	public Chemist() {
		// Use Job constructor
		super(JobType.CHEMIST, Job.buildRoleMap(20.0, 10.0, 5.0, 5.0, 5.0, 20.0, 15.0, 30.0));
	}

	@Override
	public double getCapability(Person person) {
		double result = person.getSkillManager().getSkillLevel(SkillType.CHEMISTRY);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result += result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems())
			result = 0D;

		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = .1;
		
		int population = settlement.getNumCitizens();
		
		// Add (labspace * tech level / 2) for all labs with chemistry specialties.
		result += getBuildingScienceDemand(settlement, ScienceType.CHEMISTRY, 12D);

		result = (result + population / 12D) / 2.0;
				
		return result;
	}
}
