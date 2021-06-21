/**
 * Mars Simulation Project
 * BotMind.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai;

import java.io.Serializable;

import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.robot.ai.task.BotTaskManager;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

/**
 * The BotMind class represents a robot's mind. It keeps track of missions and
 * tasks which the robot is involved.
 */
public class BotMind implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	// Data members
	/** Is the job locked so another can't be chosen? */
	private boolean jobLock;
	
	/** The robot owning this mind. */
	private Robot robot = null;
	/** The robot's task manager. */
	private BotTaskManager botTaskManager;
	/** The robot's job. */
	private RobotJob robotJob;

	/**
	 * Constructor 1.
	 * 
	 * @param robot the robot owning this mind
	 * @throws Exception if mind could not be created.
	 */
	public BotMind(Robot robot) {

		// Initialize data members
		this.robot = robot;
//		mission = null;
		robotJob = null;
		jobLock = false;

		// Define the boundary in Sense-Act-Plan (Robot control methodology
		// 1. Sense - gather information using the sensors
		// 2. Plan - create a world model using all the information, and plan the next
		// move
		// 3. Act
		// SPA is used in iterations: After the acting phase, the sensing phase, and the
		// entire cycle, is repeated.
		// https://en.wikipedia.org/wiki/Sense_Plan_Act

//		// Create CoreMind
//		coreMind = new CoreMind();
		// Construct a skill manager.
//		skillManager = new SkillManager(robot, coreMind);
//		skillManager = new SkillManager(robot);
		
		// Construct a task manager
		botTaskManager = new BotTaskManager(this);

//		missionManager = sim.getMissionManager();

	}

	/**
	 * Time passing.
	 * 
	 * @param time the time passing (millisols)
	 * @throws Exception if error.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {

		if (botTaskManager != null) {
			botTaskManager.timePassing(pulse);
			// Take action as necessary.
			takeAction(pulse.getElapsed());
		}
		return true;
	}

	/**
	 * Take appropriate action for a given amount of time.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during action.
	 */
	private void takeAction(double time) {
		boolean hasActiveTask = botTaskManager.hasActiveTask();
		// Perform a task if the robot has one, or determine a new task/mission.
		if (hasActiveTask) {
			double remainingTime = botTaskManager.executeTask(time, robot.getPerformanceRating());
			if (remainingTime > 0D) {
				takeAction(remainingTime);
			}
		} 
		
		else {
			if (!botTaskManager.hasActiveTask()) {
				botTaskManager.startNewTask();
			}
		}
	}

	public Robot getRobot() {
		return robot;
	}

	/**
	 * Returns the robot's task manager
	 * 
	 * @return botTaskManager
	 */
	public BotTaskManager getBotTaskManager() {
		return botTaskManager;
	}

	/**
	 * Returns the robot's current mission. Returns null if there is no current
	 * mission.
	 * 
	 * @return current mission
	 */
	public Mission getMission() {
		return null;//mission;
	}

	/**
	 * Gets the robot's job
	 * 
	 * @return job or null if none.
	 */
	public RobotJob getRobotJob() {
		return robotJob;
	}

	/**
	 * Checks if the robot's job is locked and can't be changed.
	 * 
	 * @return true if job lock.
	 */
	public boolean getJobLock() {
		return jobLock;
	}

	/**
	 * Sets the robot's job.
	 * 
	 * @param newJob the new job
	 * @param locked is the job locked so another can't be chosen?
	 */
	public void setRobotJob(RobotJob newJob, boolean locked) {

		jobLock = locked;
		if (!newJob.equals(robotJob)) {
			robotJob = newJob;

			robot.fireUnitUpdate(UnitEventType.JOB_EVENT, newJob);

		}
	}

	/**
	 * Set this mind as inactive. Needs move work on this; has to abort the Task can
	 * not just close it. This abort action would then allow the Mission to be also
	 * aborted.
	 */
	public void setInactive() {
		botTaskManager.clearAllTasks("Inactive");
//		if (hasActiveMission()) {
//			if (robot != null)
//				mission.removeMember(robot);
//			mission = null;
//		}
	}

	/**
	 * Sets the robot's current mission.
	 * 
	 * @param newMission the new mission
	 */
	public void setMission(Mission newMission) {
//		if (newMission != mission) {
//
//			if (robot != null) {
//				if (mission != null) {
//					mission.removeMember(robot);
//				}
//
//				mission = newMission;
//
//				if (newMission != null) {
//					newMission.addMember(robot);
//				}
//
//				robot.fireUnitUpdate(UnitEventType.MISSION_EVENT, newMission);
//			}
//		}
	}

	public void reinit() {
		botTaskManager.reinit();
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		robot = null;
		botTaskManager.destroy();
		botTaskManager = null;
		robotJob = null;
	}
}
