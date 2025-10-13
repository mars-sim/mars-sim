/*
 * Mars Simulation Project
 * Botanist.java
 * @date 2021-09-27
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.job;

import java.util.Iterator;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.farming.Farming;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobSpec;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;

/**
 * The Botanist class represents a job for a botanist.
 */
public class Botanist
extends JobSpec {
	
	/**
	 * Constructor.
	 */
	public Botanist() {
		// Use Job constructor
		super(JobType.BOTANIST, JobSpec.buildRoleMap(25.0, 5.0, 5.0, 5.0, 5.0, 20.0, 5.0, 35.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = person.getSkillManager().getSkillLevel(SkillType.BOTANY);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();
		int academicAptitude = attributes.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE);
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double averageAptitude = (academicAptitude + experienceAptitude) / 2D;
		result += result * ((averageAptitude - 100D) / 100D);

		if (person.getPhysicalCondition().hasSeriousMedicalProblems()) 
			result = result/2D;
		
		return result;
	}


	/**
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {
		double result = .1;

		int population = settlement.getNumCitizens();
		
		// Add (labspace * tech level) / 2 for all labs with botany specialties.
		result += getBuildingScienceDemand(settlement, ScienceType.BOTANY, 12D);
		
		// Add (growing area in greenhouses) / 25
		Iterator<Building> j = settlement.getBuildingManager().getBuildingSet(FunctionType.FARMING).iterator();
		while (j.hasNext()) {
			Building building = j.next();
			Farming farm = building.getFarming();
			result += (farm.getGrowingArea() / 100D);
		}
		
		result = (result + population / 6D) / 2.0;
		
		return result;
	}
}
