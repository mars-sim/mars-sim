/*
 * Mars Simulation Project
 * Repairbot.java
 * @date 2022-09-01
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot.ai.job;

import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

public class Repairbot extends RobotJob  {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static final double BUILDING_PER_BOT = 10;
	private static final double VEHICLE_PER_BOT = 30;

	/**
	 * Constructor.
	 */
	public Repairbot() {
		// Use Job constructor
		super();
	}

	/**
	 * Gets the base settlement need for this job.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	@Override
	public double getOptimalCount(Settlement settlement) {

		double result = 0;

		// Add number of buildings in settlement that a robot can enter
		result += settlement.getBuildingManager().getBuildingSet(FunctionType.LIFE_SUPPORT).size() / BUILDING_PER_BOT;

		// Add number of vehicles parked at settlement.
		result += settlement.getAllAssociatedVehicles().size() / VEHICLE_PER_BOT;
		if (result < 1D) {
			// Add settlements should have at least 1 repair bot
			result = 1D;
		}
		return result;
	}

	/**
	 * Gets a robot's capability to perform this job.
	 * 
	 * @param robot the person to check.
	 * @return capability.
	 */
	@Override
	public double getCapability(Robot robot) {

		double result = 0D; // robot should be less capable than the person counterpart

		int mechanicSkill = robot.getSkillManager().getSkillLevel(SkillType.MECHANICS);
		result += mechanicSkill;

		NaturalAttributeManager attributes = robot.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result += result * ((experienceAptitude - 50D) / 100D);

		// if (robot.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;

		return result;
	}
}
