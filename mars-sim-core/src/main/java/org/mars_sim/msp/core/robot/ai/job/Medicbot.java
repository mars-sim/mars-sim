/*
 * Mars Simulation Project
 * Medicbot.java
 * @date 2022-09-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai.job;

import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/** 
 * The Medicbot class represents a job for an medical treatment expert.
 */
public class Medicbot extends RobotJob {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static final double POP_PER_BOT = 15;

	/** Constructor. */
	public Medicbot() {
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

		int kkill = robot.getSkillManager().getSkillLevel(SkillType.MEDICINE);
		result = kkill;

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
	@Override
	public double getOptimalCount(Settlement settlement) {

		// Add (tech level / 2) for all medical infirmaries.
		double result = settlement.getAllAssociatedPeople().size()/POP_PER_BOT;

		// Put a lower limit
		if (result < 1D) {
			result = 0D;
		}
		return result;
	}

}
