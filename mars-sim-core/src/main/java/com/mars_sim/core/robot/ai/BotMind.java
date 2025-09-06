/*
 * Mars Simulation Project
 * BotMind.java
 * @date 2022-07-19
 * @author Manny Kung
 */
package com.mars_sim.core.robot.ai;

import java.io.Serializable;
import java.util.logging.Level;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.ai.job.RobotJob;
import com.mars_sim.core.robot.ai.task.BotTaskManager;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

/**
 * The BotMind class represents a robot's mind. It keeps track of missions and
 * tasks which the robot is involved.
 */
public class BotMind implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(BotMind.class.getName());
	
	private static final double SMALL_AMOUNT_OF_TIME = 0.001;
	
	// Data members
	/** Is the job locked so another can't be chosen? */
	private boolean jobLock;
	
	/** The robot owning this mind. */
	private Robot robot = null;
	/** The robot's task manager. */
	private BotTaskManager botTaskManager;
	/** The robot's job. */
	private RobotJob robotJob;
	/** The robot's current mission (if any). */
	private Mission mission;
	
	/**
	 * Constructor 1.
	 * 
	 * @param robot the robot owning this mind
	 * @throws Exception if mind could not be created.
	 */
	public BotMind(Robot robot) {

		// Initialize data members
		this.robot = robot;
		mission = null;
		robotJob = null;
		jobLock = false;

		// Define the boundary in Sense-Act-Plan, a Robot control methodology as follows :
		//
		// 1. Sense - 	Gather information using the sensors.
		// 2.  Plan - 	Create a world model using all the information, 
		//				and plan the next move.
		// 3.   Act -		
		//
		// Note: SPA is used in iterations. After the acting phase, the sensing phase, 
		// 		 and the entire cycle, is repeated.
		//
		// Reference : https://en.wikipedia.org/wiki/Sense_Plan_Act
		
		// Construct a task manager
		botTaskManager = new BotTaskManager(this);
	}

	/**
	 * Time passing.
	 * 
	 * @param time the time passing (millisols)
	 * @throws Exception if error.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if ((botTaskManager != null) && (pulse.getElapsed() > 0)) {
			moderateTime(pulse.getElapsed());
		}

		return true;
	}

	/**
	 * Moderates the time for decisions.
	 * 
	 * @param time in millisols
	 * @throws Exception if error during action.
	 */
	private void moderateTime(double time) {
		double remaining = time;
		double pTime = Task.getStandardPulseTime();
		if (pTime == 0.0) {
			pTime = remaining;
		}
		while (remaining > 0) {
			if (remaining > pTime) {
				// Call takeAction to perform a task and consume the pulse time.
				takeAction(pTime);
				// Reduce the total time by the pulse time
				remaining -= pTime;
			}
			else {
				// Call takeAction to perform a task and consume the pulse time.
				takeAction(remaining);
				// Reduce the total time by the pulse time
				remaining = 0;
			}
		}
	}
	
	/**
	 * Takes appropriate action for a given amount of time.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during action.
	 */
	private void takeAction(double time) {
		double pulseTime = time;
		// Perform a task if the robot has one, or determine a new task/mission.
		if (robot.getSystemCondition().getBatteryLevel() <= 5D) {
			String task = botTaskManager.getTaskName();
			if (task.equalsIgnoreCase(""))
				task = "None";
			logger.log(robot, Level.WARNING, 30_000L, "Battery almost depleted and must be recharged."
					+ " Current task: " + task + ".");
		}
			
		if (botTaskManager.hasActiveTask()) {			
			// Call executeTask
			double remainingTime = botTaskManager.executeTask(pulseTime);
			
			if (remainingTime == pulseTime) {
				// Do not call takeAction
				return;
			}
			
			if (remainingTime > SMALL_AMOUNT_OF_TIME) {
				takeAction(remainingTime);
			}
		}
		
		// If the robot is inoperable, it won't look for new tasks
		else if (robot.isOperable()) {
			lookForATask();
		}
	}

	/**
	 * Looks for a new task.
	 */
	public void lookForATask() {

		boolean hasActiveMission = false;
		boolean hasTask = false;
		
		if (mission != null) {
			if (mission.isDone()) {
				// Set the mission to null since it is done
				mission = null;
			}
			else {
				hasActiveMission = true;
			}
		}
		
		if (hasActiveMission) {
			hasTask = resumeMission();

		}
		
		if (!hasTask) { 
			// don't have an active mission
			botTaskManager.startNewTask();
		}
	}
	
	/**
	 * Resumes a mission.
	 * 
	 * @param modifier
	 */
	private boolean resumeMission() {
		if (robot.isFit() && !robot.getSystemCondition().isLowPower()) {
			return mission.performMission(robot);
		}

		return false;
	}

	public Robot getRobot() {
		return robot;
	}

	/**
	 * Returns the robot's task manager.
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
		return mission;
	}

	/**
	 * Gets the robot's job.
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
	 * Returns true if person has an active mission.
	 * 
	 * @return true for active mission
	 */
	public boolean hasActiveMission() {
        return (mission != null) && !mission.isDone();
	}

	/**
	 * Returns true if person has a mission.
	 * 
	 * @return true for active mission
	 */
	public boolean hasAMission() {
        return mission != null;
    }
	
	/**
	 * Sets this bot mind as inactive. 
	 * Note: Needs to work on this. Has to abort the Task. Can 
	 * not just close it. This abort action would then allow the Mission to be also
	 * aborted.
	 */
	public void setInactive() {
//		botTaskManager.clearAllTasks("Inactive");
		if (hasActiveMission()) {
			mission.removeMember(robot);
			mission = null;
		}
	}

	/**
	 * Sets the person's current mission.
	 * 
	 * @param newMission the new mission
	 */
	public void setMission(Mission newMission) {
		if (newMission != mission) {
			if (mission != null) {
				mission.removeMember(robot);
			}
			mission = newMission;

			if (newMission != null) {
				newMission.addMember(robot);
			}

			robot.fireUnitUpdate(UnitEventType.MISSION_EVENT, newMission);
		}
	}

	/**
	 * Stops the person's current mission.
	 * 
	 */
	public void stopMission() {
		mission = null;
	}

	public void reinit() {
		botTaskManager.reinit();
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		robot = null;
		botTaskManager.destroy();
		botTaskManager = null;
		robotJob = null;
	}
}
