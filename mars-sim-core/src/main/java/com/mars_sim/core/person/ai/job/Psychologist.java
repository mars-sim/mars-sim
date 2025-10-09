/*
 * Mars Simulation Project
 * Psychologist.java
 * @date 2021-09-27
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.job;

import java.util.Iterator;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.Job;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;

/**
 * The Psychologist class represents a job for evaluating a person's mind and behavior.
 */
public class Psychologist extends Job {
	
	/** Constructor. */
	public Psychologist() {
		// Use Job constructor
		super(JobType.PSYCHOLOGIST, Job.buildRoleMap(5.0, 0.0, 5.0, 25.0, 20.0, 10.0, 15.0, 20.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = person.getSkillManager().getSkillLevel(SkillType.PSYCHOLOGY);

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

		// Add total population / 10
		int population = settlement.getNumCitizens();

		// Add (labspace * tech level) / 2 for all labs with medical specialties.
		result += getBuildingScienceDemand(settlement, ScienceType.PSYCHOLOGY, 6D);

		// Add (tech level / 2) for all medical infirmaries.
		Iterator<Building> j = settlement.getBuildingManager().getBuildingSet(FunctionType.MEDICAL_CARE).iterator();
		while (j.hasNext()) {
			result += (double) j.next().getMedical().getTechLevel() / 7D;
		}

		result = (result + population / 12D) / 6.0;
				
		return result;
	}
}
