/*
 * Mars Simulation Project
 * RobotJob.java
 * @date 2022-09-01
 * @author Manny Kung
 */
package com.mars_sim.core.robot.ai.job;

import java.io.Serializable;

import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;

/**
 * The Job class represents a robot's job.
 */
public abstract class RobotJob implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor.
	 * 
	 * @param name the name of the job.
	 */
	protected RobotJob() {
	}
	
	/**
	 * Gets a robot's capability to perform this job.
	 * 
	 * @param robot the robot to check.
	 * @return capability (min 0.0).
	 */
	public abstract double getCapability(Robot robot);

	/**
	 * Gets the optimal number of this type of Robot a Settlement needs.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public abstract double getOptimalCount(Settlement settlement);
}
