/*
 * Mars Simulation Project
 * RobotJob.java
 * @date 2022-09-01
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Job class represents a person's job.
 */
public abstract class RobotJob implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Probability penalty for starting a non-job-related mission. */
	private static final double NON_JOB_MISSION_START_PENALTY = 0D;
	/** Probability penalty for joining a non-job-related mission. */
	private static final double NON_JOB_MISSION_JOIN_PENALTY = 0D;

	// Domain members
	/** List of missions to be started by a person with this job. */
	protected List<Class<?>> jobMissionStarts;
	/** List of missions to be joined by a person with this job. */
	protected List<Class<?>> jobMissionJoins;
	
	/**
	 * Constructor.
	 * 
	 * @param name the name of the job.
	 */
	protected RobotJob() {
		jobMissionStarts = new ArrayList<Class<?>>();
		jobMissionJoins = new ArrayList<Class<?>>();
	}

	
	/**
	 * Gets a robot's capability to perform this job.
	 * 
	 * @param robot the robot to check.
	 * @return capability (min 0.0).
	 */
	public abstract double getCapability(Robot robot);

	/**
	 * Gets the probability modifier for starting a non-job-related mission.
	 * 
	 * @param missionClass the mission class
	 * @return modifier >= 0.0
	 */
	public double getStartMissionProbabilityModifier(Class<?> missionClass) {
		double result = 1D;
		if (!jobMissionStarts.contains(missionClass))
			result = NON_JOB_MISSION_START_PENALTY;
		return result;
	}

	/**
	 * Gets the probability modifier for joining a non-job-related mission.
	 * 
	 * @param missionClass the mission class
	 * @return modifier >= 0.0
	 */
	public double getJoinMissionProbabilityModifier(Class<?> missionClass) {
		double result = 1D;
		if (!jobMissionJoins.contains(missionClass))
			result = NON_JOB_MISSION_JOIN_PENALTY;
		return result;
	}

	/**
	 * Gets the optimal number of this type of Robot a Settlement needs.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public abstract double getOptimalCount(Settlement settlement);
}
