/**
 * Mars Simulation Project
 * BotMind.java
 * @version 3.1.0 2017-12-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot.ai;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.robot.ai.task.BotTaskManager;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The BotMind class represents a robot's mind. It keeps track of missions and
 * tasks which the robot is involved.
 */
public class BotMind implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(BotMind.class.getName());

	// Data members
	/** Is the job locked so another can't be chosen? */
	private boolean jobLock;
	/** The cache for msol. */
	private int msolCache = -1;
	
	/** The robot owning this mind. */
	private Robot robot = null;
	/** The robot's task manager. */
	private BotTaskManager botTaskManager;
	/** The robot's current mission (if any). */
//	private Mission mission;
	/** The robot's job. */
	private RobotJob robotJob;
	/** The robot's skill manager. */
	private SkillManager skillManager;
//	/** The robot's core mind. */
//	private CoreMind coreMind;
	

//	private static MissionManager missionManager;
	private static Simulation sim;
	private static MarsClock marsClock;

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

		sim = Simulation.instance();
		marsClock = sim.getMasterClock().getMarsClock();

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
	public void timePassing(double time) {

		if (botTaskManager != null) {
			// Take action as necessary.
			takeAction(time);
		}
		
//	    if (missionManager != null)
//	    	missionManager.recordMission(robot);

//		int msolInt = marsClock.getMillisolInt();

//		if (msolCache != msolInt) {
//			msolCache = msolInt;
			// I don't think robots should be changing jobs on their own. - Scott
			// Check if this robot needs to get a new job or change jobs.
//		        if (!jobLock) {
//		        	setRobotJob(JobManager.getNewRobotJob(robot), false);
//		        }
//		}

	}

	/**
	 * Take appropriate action for a given amount of time.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during action.
	 */
	public void takeAction(double time) {
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
				try {
					getNewAction(true);
				} catch (Exception e) {
					logger.log(Level.WARNING, robot + " could not get new action", e);
					e.printStackTrace(System.err);
				}
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

//	/**
//	 * Returns true if robot has an active mission.
//	 * 
//	 * @return true for active mission
//	 */
//	public boolean hasActiveMission() {
//		return (mission != null) && !mission.isDone();
//	}

	/**
	 * Set this mind as inactive. Needs move work on this; has to abort the Task can
	 * not just close it. This abort action would then allow the Mission to be also
	 * aborted.
	 */
	public void setInactive() {
		botTaskManager.clearTask();
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

	/**
	 * Determines a new action for the robot based on available tasks, missions and
	 * active missions.
	 * 
	 * @param tasks    can actions be tasks?
	 * @param missions can actions be new missions?
	 */
	public void getNewAction(boolean tasks) {
		// Get probability weights from tasks, missions and active missions.
		double taskWeights = 0D;
		double missionWeights = 0D;

		// Determine sum of weights based on given parameters
		double weightSum = 0D;

		if (tasks) {
			taskWeights = botTaskManager.getTotalTaskProbability(false);
			weightSum += taskWeights;
		}

		if ((weightSum <= 0D) || (Double.isNaN(weightSum)) || (Double.isInfinite(weightSum))) {
			try {
				TimeUnit.MILLISECONDS.sleep(1000L);
			} catch (InterruptedException e) {
				logger.severe("BotMind.getNewAction() " + robot.getName() + " has weight sum of " + weightSum);
				e.printStackTrace();
			}

		}

		// Select randomly across the total weight sum.
		double rand = RandomUtil.getRandomDouble(weightSum);

		// Determine which type of action was selected and set new action accordingly.
		if (tasks) {
			if (rand < taskWeights) {
				Task newTask = botTaskManager.getNewTask();

				if (newTask != null)
					botTaskManager.addTask(newTask);
				else
					logger.severe(robot + " : newTask is null ");

				return;
			} else {
				rand -= taskWeights;
			}
		}

		// If reached this point, no task or mission has been found.
		logger.severe(robot.getName() + " couldn't determine new action - taskWeights: " + taskWeights
				+ ", missionWeights: " + missionWeights);
	}

	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public static void initializeInstances(MarsClock clock) {
		marsClock = clock;
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
//		if (mission != null)
//			mission.destroy();
//		mission = null;
		robotJob = null;
		// skillManager.destroy(); // not working for maven test
		 skillManager = null;
	}
}