/*
 * Mars Simulation Project
 * Makerbot.java
 * @date 2022-09-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai.job;

import java.util.Iterator;
import java.util.List;

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
public class Makerbot
extends RobotJob {

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
	 * Gets the base settlement need for this job.
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public double getSettlementNeed(Settlement settlement) {

		double result = 0D;

		// Add (tech level * process number / 2) for all manufacture buildings.
		List<Building> manufactureBuildings = settlement.getBuildingManager().getBuildings(FunctionType.MANUFACTURE);
		Iterator<Building> i = manufactureBuildings.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Manufacture workshop = (Manufacture) building.getFunction(FunctionType.MANUFACTURE);
			result += workshop.getTechLevel() * workshop.getMaxProcesses() / 2D;
		}

		return result;
	}

}
