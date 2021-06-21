/**
 * Mars Simulation Project
 * Trader.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Delivery;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.structure.Settlement;

class Trader extends Job {
	
	private static double TRADING_RANGE = 500D;
	private static double SETTLEMENT_MULTIPLIER = 3D;

	/**
	 * Constructor.
	 */
	public Trader() {
		// Use Job constructor.
		super(JobType.TRADER, Job.buildRoleMap(5.0, 5.0, 30.0, 25.0, 25.0, 5.0, 5.0));

		// Add trader-related missions.
		jobMissionStarts.add(Trade.class);
		jobMissionJoins.add(Trade.class);
		jobMissionStarts.add(Delivery.class);
		jobMissionJoins.add(Delivery.class);
		
//		jobMissionJoins.add(BuildingConstructionMission.class);
//		jobMissionJoins.add(BuildingSalvageMission.class);

	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;

		int tradingSkill = person.getSkillManager().getSkillLevel(SkillType.TRADING);
		result = tradingSkill;

		NaturalAttributeManager attributes = person.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
//		result += result * ((experienceAptitude - 50D) / 100D);
		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeType.CONVERSATION);
//		result += result * ((conversation - 50D) / 100D);

		double averageAptitude = experienceAptitude + conversation;
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
//				if (distance <= TRADING_RANGE) {
//					result += SETTLEMENT_MULTIPLIER;
//				}
			}
		}

		result = (result + population / 12D) / 2.0;

//		System.out.println(settlement + " Trader need: " + result);
		
		return result;
	}
}
