/*
 * Mars Simulation Project
 * Reporter.java
 * @date 2021-09-27
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Iterator;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.Settlement;

class Reporter extends Job {
	
	private static double TRADING_RANGE = 1500D;
	private static double SETTLEMENT_MULTIPLIER = 1D;

	/**
	 * Constructor.
	 */
	public Reporter() {
		// Use Job constructor.
		super(JobType.REPORTER, Job.buildRoleMap(5.0, 0.0, 5.0, 30.0, 30.0, 20.0, 5.0, 5.0));
	}

	/**
	 * Gets a person's capability to perform this job.
	 * 
	 * @param person the person to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Person person) {

		double result = 0D;
		
		int reportSkill = person.getSkillManager().getSkillLevel(SkillType.REPORTING);
		result = reportSkill;
		
		NaturalAttributeManager attributes = person.getNaturalAttributeManager();

//		if (attributes == null)
//			attributes = person.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result += result * ((experienceAptitude - 50D) / 100D);

		// Add leadership aptitude.
//		int leadershipAptitude = attributes.getAttribute(NaturalAttributeType.LEADERSHIP);
//		result += result * ((leadershipAptitude - 50D) / 100D);

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
		
		Iterator<Settlement> i = unitManager.getSettlements().iterator();
		while (i.hasNext()) {
			Settlement otherSettlement = i.next();
			if (otherSettlement != settlement) {
				double distance = settlement.getCoordinates().getDistance(otherSettlement.getCoordinates());
				if (distance <= TRADING_RANGE)
					result += SETTLEMENT_MULTIPLIER / 6.0;
			}
		}

		result = (result + population / 24D) / 2.0;
		
//		System.out.println(settlement + " Reporter need: " + result);
		
		return result;
	}
}
