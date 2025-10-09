/*
 * Mars Simulation Project
 * Architect.java
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
import com.mars_sim.core.structure.Settlement;

/**
 * The Architect class represents an architect job focusing on construction of buildings, settlement
 * and other structures.
 */
public class Architect
extends Job {

	/** Constructor. */
	public Architect() {
		// Use Job constructor.
		super(JobType.ARCHITECT, Job.buildRoleMap(5.0, 15.0, 25.0, 10.0, 10.0, 5.0, 20.0, 10.0));
	}

	@Override
	public double getCapability(Person person) {

		double result = person.getSkillManager().getSkillLevel(SkillType.CONSTRUCTION);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		int arts = attributes.getAttribute(NaturalAttributeType.ARTISTRY);
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double averageAptitude = (academicAptitude + experienceAptitude + arts) / 3D;
		result+= result * ((averageAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0;

		return result;
	}

	@Override
	public double getSettlementNeed(Settlement settlement) {
		double result = .1;
		
		int population = settlement.getNumCitizens();
		
		// Add number of buildings currently at settlement.
		result += settlement.getBuildingManager().getNumBuildings() / 36D;
		
		result = (result + population / 30D) / 6.0;
		
		return result;
	}
}
