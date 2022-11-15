/*
 * Mars Simulation Project
 * Deliverybot.java
 * @date 2022-09-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai.job;

import java.util.Iterator;

import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Delivery;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

public class Deliverybot
extends RobotJob {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final double TRADING_RANGE = 2500D;
	private static final double SETTLEMENT_MULTIPLIER = 3D;

	/**
	 * Constructor.
	 */
	public Deliverybot() {
		// Use Job constructor.
		super();
		
        jobMissionStarts.add(Delivery.class);
        
		//jobMissionStarts.add(Trade.class);
		//jobMissionJoins.add(Trade.class);
        //jobMissionStarts.add(TravelToSettlement.class);
		//jobMissionJoins.add(TravelToSettlement.class);

	}

	/**
	 * Gets a robot's capability to perform this job.
	 * @param robot the robot to check.
	 * @return capability (min 0.0).
	 */
	public double getCapability(Robot robot) {

		double result = 0D;

		int tradingSkill = robot.getSkillManager().getSkillLevel(SkillType.TRADING);
		result = tradingSkill;

		NaturalAttributeManager attributes = robot.getNaturalAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result+= result * ((experienceAptitude - 50D) / 100D);

		// Add conversation.
		int conversation = attributes.getAttribute(NaturalAttributeType.CONVERSATION);
		result+= result * ((conversation - 50D) / 100D);

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {

        double result = 0D;

        Iterator<Settlement> i = unitManager.getSettlements().iterator();
        while (i.hasNext()) {
            Settlement otherSettlement = i.next();
            if (otherSettlement != settlement) {
                double distance = settlement.getCoordinates().getDistance(otherSettlement.getCoordinates());
                if (distance <= TRADING_RANGE) result += SETTLEMENT_MULTIPLIER;
            }
        }

		return result;
	}
	
}
