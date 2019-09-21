/**
 * Mars Simulation Project
 * Deliverybot.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai.job;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.UnloadVehicleGarage;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.robot.RoboticAttributeManager;
import org.mars_sim.msp.core.structure.Settlement;

public class Deliverybot
extends RobotJob
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static double TRADING_RANGE = 1500D;
	private static double SETTLEMENT_MULTIPLIER = 3D;

	/**
	 * Constructor.
	 */
	public Deliverybot() {
		// Use Job constructor.
		super(Deliverybot.class);

		//jobTasks.add(LoadVehicleEVA.class); //determine to what extend the walking bug is affecting the outdoor portion of this task
        jobTasks.add(LoadVehicleGarage.class);
        //jobTasks.add(UnloadVehicleEVA.class); //determine to what extend the walking bug is affecting the outdoor portion of this task
        jobTasks.add(UnloadVehicleGarage.class);
        jobTasks.add(ConsolidateContainers.class); //determine to what extend the walking bug is affecting the outdoor portion of this task

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

		RoboticAttributeManager attributes = robot.getRoboticAttributeManager();

		// Add experience aptitude.
		int experienceAptitude = attributes.getAttribute(RoboticAttributeType.EXPERIENCE_APTITUDE);
		result+= result * ((experienceAptitude - 50D) / 100D);

		// Add conversation.
		int conversation = attributes.getAttribute(RoboticAttributeType.CONVERSATION);
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