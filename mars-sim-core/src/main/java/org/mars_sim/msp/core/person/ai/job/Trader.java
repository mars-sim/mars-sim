/*
 * Mars Simulation Project
 * Trader.java
 * @Date 2021-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.util.Job;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.structure.Settlement;

public class Trader extends Job {
	
	private static final double TRADING_RANGE = 500D;
	private static final double SETTLEMENT_MULTIPLIER = 3D;

	/**
	 * Constructor.
	 */
	public Trader() {
		// Use Job constructor.
		super(JobType.TRADER, Job.buildRoleMap(5.0, 5.0, 5.0, 30.0, 25.0, 25.0, 5.0, 5.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = person.getSkillManager().getSkillLevel(SkillType.TRADING);

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		
		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeType.CONVERSATION);

		double averageAptitude = 1.0 * experienceAptitude + conversation;
		result += result * ((averageAptitude - 100D) / 100D);
		
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
				result += TRADING_RANGE / distance * SETTLEMENT_MULTIPLIER / 4.0;
			}
		}

		result = (result + population / 12D) / 2.0;
		
		return result;
	}
}
