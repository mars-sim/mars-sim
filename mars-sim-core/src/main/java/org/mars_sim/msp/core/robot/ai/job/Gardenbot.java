/*
 * Mars Simulation Project
 * Gardenbot.java
 * @date 2022-09-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai.job;

import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

/**
 * The Gardenbot class represents a job for a gardenbot.
 */
public class Gardenbot extends RobotJob {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static final double M_PER_BOT = 200;

	//private static final Logger logger = Logger.getLogger(Botanist.class.getName());

	/**
	 * Constructor.
	 */
	public Gardenbot() {
		// Use Job constructor
		super();
	}

	/**
	 * Gets a robot's capability to perform this job.
	 * @param robot the robot to check.
	 * @return capability (min 0.0).
	 */
	@Override
	public double getCapability(Robot robot) {

		double result = 10D;

		int botanySkill = robot.getSkillManager().getSkillLevel(SkillType.BOTANY);
		result += botanySkill;

		NaturalAttributeManager attributes = robot.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result+= result * ((experienceAptitude - 50D) / 100D);

		//if (robot.getPhysicalCondition().hasSeriousMedicalProblems()) result = 0D;

		return result;
	}

	/**
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	@Override
	public double getOptimalCount(Settlement settlement) {
		double growingArea = 0;

		// Add (growing area in greenhouses) / 10
		for(Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.FARMING)) {
			Farming farm = building.getFarming();
			growingArea += farm.getGrowingArea();
		}
		return growingArea/M_PER_BOT;
	}
}
