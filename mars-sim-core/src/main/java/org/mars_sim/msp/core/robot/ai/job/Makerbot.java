/*
 * Mars Simulation Project
 * Makerbot.java
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
import org.mars_sim.msp.core.structure.building.function.Manufacture;

/**
 * The Makerbot class represents an engineer job focusing on manufacturing goods
 */
public class Makerbot extends RobotJob {

	private static final long serialVersionUID = 1L;
	private static final double PROCESSES_PER_BOT = 4;

	//	private static final Logger logger = Logger.getLogger(Engineer.class.getName());

	/** Constructor. */
	public Makerbot() {
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

		double result = 0D;

		int materialsScienceSkill = robot.getSkillManager().getSkillLevel(SkillType.MATERIALS_SCIENCE);
		result = materialsScienceSkill;

		NaturalAttributeManager attributes = robot.getNaturalAttributeManager();
		int experienceAptitude = attributes.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		result+= result * ((experienceAptitude - 50D) / 100D);

		return result;
	}

	/**
	 * Gets the base settlement need for this job. Based on the number of manufacturing points
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	@Override
	public double getOptimalCount(Settlement settlement) {

		double processes = 0D;
		for(Building building : settlement.getBuildingManager().getBuildings(FunctionType.MANUFACTURE)) {
			Manufacture workshop = (Manufacture) building.getFunction(FunctionType.MANUFACTURE);
			processes += workshop.getMaxProcesses();
		}

		return processes/PROCESSES_PER_BOT;
	}

}
