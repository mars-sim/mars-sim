/*
 * Mars Simulation Project
 * Politician.java
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
import com.mars_sim.core.person.ai.job.util.JobSpec;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.structure.Settlement;

public class Politician extends JobSpec {
	
	private static final double TRADING_RANGE = 1500D;
	private static final double SETTLEMENT_MULTIPLIER = .3D;

	/**
	 * Constructor.
	 */
	public Politician() {
		// Use Job constructor.
		super(JobType.POLITICIAN, JobSpec.buildRoleMap(15.0, 0.0, 5.0, 25.0, 25.0, 20.0, 5.0, 5.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result =  person.getSkillManager().getSkillLevel(SkillType.MANAGEMENT);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result += result * ((experienceAptitude - 50D) / 100D);

		// Add leadership aptitude.
		int leadershipAptitude = attributes.getAttribute(NaturalAttributeType.LEADERSHIP);
		result += result * ((leadershipAptitude - 50D) / 100D);

		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeType.CONVERSATION);
		result += result * ((conversation - 50D) / 100D);

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
		
		Iterator<Settlement> i = unitManager.getSettlements().iterator();
		while (i.hasNext()) {
			Settlement otherSettlement = i.next();
			if (otherSettlement != settlement) {
				double distance = settlement.getCoordinates().getDistance(otherSettlement.getCoordinates());
				if (distance <= TRADING_RANGE)
					result += SETTLEMENT_MULTIPLIER / 12.0;
			}
		}

		Iterator<Building> j = settlement.getBuildingManager().getBuildingSet(FunctionType.ADMINISTRATION).iterator();
		while (j.hasNext()) {
			result += j.next().getAdministration().getStaffCapacity()/24D;
		}
		
		result = (result + population / 64D) / 2.0;
				
		return result;
	}
}
