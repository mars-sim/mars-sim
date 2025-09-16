/*
 * Mars Simulation Project
 * Sociologist.java
 * @date 2025-09-15
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
import com.mars_sim.core.structure.Settlement;

public class Sociologist extends Job {
	
	private static final double TRADING_RANGE = 1500D;
	private static final double SETTLEMENT_MULTIPLIER = 1D;

	/**
	 * Constructor.
	 */
	public Sociologist() {
		// Use Job constructor.
		super(JobType.SOCIOLOGIST, Job.buildRoleMap(0.0, 5.0, 0.0, 15.0, 
													15.0, 20.0, 5.0, 40.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = person.getSkillManager().getSkillLevel(SkillType.ORGANISATION);
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result += result * ((experienceAptitude - 50D) / 100D);

		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeType.CONVERSATION);
		result += result * ((conversation - 50D) / 100D);

		// Add artistry aptitude.
		int artistry = attributes.getAttribute(NaturalAttributeType.ARTISTRY);
		result += result * ((artistry - 50D) / 100D);

		// Add attractiveness.
		int attractiveness = attributes.getAttribute(NaturalAttributeType.ATTRACTIVENESS);
		result += result * ((attractiveness - 50D) / 100D);

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
		
		Iterator<Building> j = settlement.getBuildingManager()
				.getBuildingSet(FunctionType.LIVING_ACCOMMODATION).iterator();
		while (j.hasNext()) {
			result += (double) j.next().getLivingAccommodation().getBedCap();
		}
		
		double settlementFactor = 0;
		
		Iterator<Settlement> i = unitManager.getSettlements().iterator();
		while (i.hasNext()) {
			Settlement otherSettlement = i.next();
			if (otherSettlement != settlement) {
				double distance = settlement.getCoordinates().getDistance(otherSettlement.getCoordinates());
				if (distance <= TRADING_RANGE)
					settlementFactor += SETTLEMENT_MULTIPLIER / 10.0;
			}
		}

		result = (result + population) / 48D + settlementFactor / 2;
				
		return result;
	}
}
