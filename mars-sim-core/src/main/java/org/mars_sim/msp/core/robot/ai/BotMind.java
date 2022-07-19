/**
 * Mars Simulation Project
 * BotMind.java
 * @date 2021-12-22
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
	
	/** default logger. */
//	private static SimLogger logger = SimLogger.getLogger(BotMind.class.getName());
	
	// Data members
	/** Is the job locked so another can't be chosen? */
	private boolean jobLock;
	
	/** The robot owning this mind. */
	private Robot robot = null;
	/** The robot's task manager. */
	private BotTaskManager taskManager;
	/** The robot's job. */
	private RobotJob robotJob;
	/** The robot's current mission (if any). */
	private Mission mission;
	
//	private static MissionManager missionManager;


//	static {
//		Simulation sim = Simulation.instance();
//		// Load the mission manager
//		missionManager = sim.getMissionManager();
//	}
	
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
		taskManager = new BotTaskManager(this);
	}

	/**
	 * Time passing.
	 * 
	 * @param time the time passing (millisols)
	 * @throws Exception if error.
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {

		if (taskManager != null) {
			taskManager.timePassing(pulse);
			// Take action as necessary.
			takeAction(pulse.getElapsed());
		}
		return true;
	}

	/**
	 * Takes appropriate action for a given amount of time.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during action.
	 */
	private void takeAction(double time) {
		boolean hasActiveTask = taskManager.hasActiveTask();
		// Perform a task if the robot has one, or determine a new task/mission.
		if (hasActiveTask) {
			double remainingTime = taskManager.executeTask(time, robot.getPerformanceRating());
			if (remainingTime > 0D) {
				takeAction(remainingTime);
			}
		} 
		
		else {
			lookForATask();
			if (!taskManager.hasActiveTask()
				&& !robot.getSystemCondition().isCharging()) {
				taskManager.startNewTask();
			}
		}
	}

	/**
	 * Looks for a new task.
	 */
	public void lookForATask() {

		boolean hasActiveMission = false;
		if (mission != null) {
//			System.out.println(robot + " mission: " + mission.getName());
			if (mission.isDone()) {
				// Set the mission to null since it is done
				mission = null;
			}
			else {
				hasActiveMission = true;
			}
		}
		
		if (hasActiveMission) {

			// In case of a delivery mission, the bot doesn't need to be onboard
//			if (mission instanceof Delivery) {
//				resumeMission();
//			}
//				
//			else {
				// If the mission vehicle has embarked but the robot is not on board, 
				// then release the robot from the mission
//				if (!(mission.getCurrentMissionLocation().equals(robot.getCoordinates()))) {
//					mission.removeMember(robot);
//					logger.info(robot, "Not boarded and taken out of " + mission + " mission.");
//					mission = null;
//				}
				
				if (mission.getPhase() != null) {
					resumeMission();
				}
//			}
		}
		
		if (!taskManager.hasActiveTask()
				&& !robot.getSystemCondition().isCharging()) { 
			// don't have an active mission
			taskManager.startNewTask();
		}
	}
	
	/**
	 * Resumes a mission.
	 * 
	 * @param modifier
	 */
	private void resumeMission() {
		if (mission.canParticipate(robot) && robot.isFit()) {
			mission.performMission(robot);
		}
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
		return taskManager;
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
        //			// has a mission but need to determine if this mission is active or not
        //			if (mission.isApproved()
        //				|| (mission.getPlan() != null
        //					&& mission.getPlan().getStatus() != PlanType.NOT_APPROVED))
        return mission != null;
    }
	
	/**
	 * Sets this bot mind as inactive. 
	 * Note: Needs to work on this. Has to abort the Task. Can 
	 * not just close it. This abort action would then allow the Mission to be also
	 * aborted.
	 */
	public void setInactive() {
		taskManager.clearAllTasks("Inactive");
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
		taskManager.reinit();
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		robot = null;
		taskManager.destroy();
		taskManager = null;
		robotJob = null;
	}
}
