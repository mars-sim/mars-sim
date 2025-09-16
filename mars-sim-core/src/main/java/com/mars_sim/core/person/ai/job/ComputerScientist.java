/*
 * Mars Simulation Project
 * ComputerScientist.java
 * @date 2021-09-27
 * @author Manny Kung
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
 * The ComputerScientist class represents a computer related job 
 * that specializes in operating and programming computer software 
 * and computing devices.
 */
public class ComputerScientist extends Job {
	
	/** Constructor. */
	public ComputerScientist() {
		// Use Job constructor
		super(JobType.COMPUTER_SCIENTIST, Job.buildRoleMap(0.0, 30.0, 10.0, 10.0, 15.0, 10.0, 5.0, 20.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {
		
		int mathematicsSkill = person.getSkillManager().getSkillLevel(SkillType.MATHEMATICS);
		int computingSkill = person.getSkillManager().getSkillLevel(SkillType.COMPUTING);		

		double result = mathematicsSkill *.25 + computingSkill * .75;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		result+= result * ((academicAptitude - 50D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {
		double result = 0.1;

		int population = settlement.getNumCitizens();
		
		// Add (labspace * tech level / 2) for all labs with mathematics specialties.
		result += getBuildingScienceDemand(settlement, ScienceType.COMPUTING, 16D);

		result = (result + population / 16D) / 2.0;

		return result;
	}
}
