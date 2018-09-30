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

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.ai.PersonalityType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.robot.ai.task.BotTaskManager;
import org.mars_sim.msp.core.time.MarsClock;

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
	private double msolCache = -1D;
	/** The robot owning this mind. */
	private Robot robot = null;
	/** The robot's task manager. */
	private BotTaskManager botTaskManager;
	/** The robot's current mission (if any). */
	private Mission mission;
	/** The robot's job. */
	private RobotJob robotJob;
	/** The robot's personality. */
	private PersonalityType personality;
	/** The robot's skill manager. */
	private static SkillManager skillManager;

	private static MissionManager missionManager;

	private static Simulation sim;

	private MarsClock marsClock;

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

		sim = Simulation.instance();
		marsClock = Simulation.instance().getMasterClock().getMarsClock();

		// Define the boundary in Sense-Act-Plan (Robot control methodology
		// 1. Sense - gather information using the sensors
		// 2. Plan - create a world model using all the information, and plan the next
		// move
		// 3. Act
		// SPA is used in iterations: After the acting phase, the sensing phase, and the
		// entire cycle, is repeated.
		// https://en.wikipedia.org/wiki/Sense_Plan_Act

		// Construct a skill manager.
		skillManager = new SkillManager(robot);

		// Construct a task manager
		botTaskManager = new BotTaskManager(this);

		missionManager = sim.getMissionManager();

	}

	/**
	 * Time passing.
	 * 
	 * @param time the time passing (millisols)
	 * @throws Exception if error.
	 */
	public void timePassing(double time) {

		if (botTaskManager != null)
			botTaskManager.recordTask();

//	    if (missionManager != null)
//	    	missionManager.recordMission(robot);

		double msol1 = marsClock.getMsol1();

		if (msolCache != msol1) {
			msolCache = msol1;

			// I don't think robots should be changing jobs on their own. - Scott
			// Check if this robot needs to get a new job or change jobs.
//		        if (!jobLock) {
//		        	setRobotJob(JobManager.getNewRobotJob(robot), false);
//		        }

			if (botTaskManager != null)
				// Take action as necessary.
				takeAction(time);
		}

	}

	/**
	 * Take appropriate action for a given amount of time.
	 * 
	 * @param time time in millisols
	 * @throws Exception if error during action.
	 */
	public void takeAction(double time) {

		if ((mission != null) && mission.isDone()) {
			mission = null;
		}

		boolean activeMission = (mission != null);

		// Check if mission creation at settlement (if any) is overridden.
		boolean overrideMission = false;

		if (robot.isInSettlement()) {
			overrideMission = robot.getSettlement().getMissionCreationOverride();
		}

		boolean hasActiveTask = botTaskManager.hasActiveTask();
		// Perform a task if the robot has one, or determine a new task/mission.
		if (hasActiveTask) {
			double remainingTime = botTaskManager.executeTask(time, robot.getPerformanceRating());
			if (remainingTime > 0D) {
				takeAction(remainingTime);
			}
		} else {

			if (activeMission) {
				mission.performMission(robot);
			}

			if (!botTaskManager.hasActiveTask()) {
				try {
					getNewAction(true, (!activeMission && !overrideMission));
				} catch (Exception e) {
					logger.log(Level.WARNING, robot + " could not get new action", e);
					e.printStackTrace(System.err);
				}
			}

			// if (botTaskManager.hasActiveTask() || hasActiveMission()) {
			// takeAction(time);
			// }
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
		return mission;
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
	 * Returns true if robot has an active mission.
	 * 
	 * @return true for active mission
	 */
	public boolean hasActiveMission() {
		return (mission != null) && !mission.isDone();
	}

	/**
	 * Set this mind as inactive. Needs move work on this; has to abort the Task can
	 * not just close it. This abort action would then allow the Mission to be also
	 * aborted.
	 */
	public void setInactive() {
		botTaskManager.clearTask();
		if (hasActiveMission()) {

			if (robot != null)
				mission.removeMember(robot);

			mission = null;
		}
	}

	/**
	 * Sets the robot's current mission.
	 * 
	 * @param newMission the new mission
	 */
	public void setMission(Mission newMission) {
		if (newMission != mission) {

			if (robot != null) {
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
	}

	/**
	 * Determines a new action for the robot based on available tasks, missions and
	 * active missions.
	 * 
	 * @param tasks    can actions be tasks?
	 * @param missions can actions be new missions?
	 */
	public void getNewAction(boolean tasks, boolean missions) {

//    	if (robot.getPerformanceRating() < 0.5D) {
//        	missions = false;
//        }

		// Get probability weights from tasks, missions and active missions.
		double taskWeights = 0D;
		double missionWeights = 0D;

		// Determine sum of weights based on given parameters
		double weightSum = 0D;

		if (tasks) {
			taskWeights = botTaskManager.getTotalTaskProbability(false);
			weightSum += taskWeights;
		}

//        if (missions) {
//        	if (missionManager == null)
//        		missionManager = sim.getMissionManager();
//        	missionWeights = missionManager.getTotalMissionProbability(robot);
//        	weightSum += missionWeights;
//	   }

		if ((weightSum <= 0D) || (Double.isNaN(weightSum)) || (Double.isInfinite(weightSum))) {
			try {
				TimeUnit.MILLISECONDS.sleep(1000L);
			} catch (InterruptedException e) {
				logger.severe("BotMind.getNewAction() " + robot.getName() + " has weight sum of " + weightSum);
				e.printStackTrace();
			}
			// throw new IllegalStateException("BotMind.getNewAction() " + robot.getName() +
			// " has weight sum of " + weightSum);
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

//        if (missions) {
//            if (rand < missionWeights) {
//            	Mission newMission = null;
//
//            	logger.fine(robot.getName() + " is starting a new mission.");
//            	newMission = missionManager.getNewMission(robot);
//
//
//                if (newMission != null) {
//                    missionManager.addMission(newMission);
//                    setMission(newMission);
//                }
//
//                return;
//            }
//            else {
//                rand -= missionWeights;
//            }
//        }

		// If reached this point, no task or mission has been found.
		logger.severe(robot.getName() + " couldn't determine new action - taskWeights: " + taskWeights
				+ ", missionWeights: " + missionWeights);

	}

	/**
	 * Gets the robot's personality type.
	 * 
	 * @return personality type.
	 */
	public PersonalityType getPersonalityType() {
		return personality;
	}

	/**
	 * Returns a reference to the robot's skill manager
	 * 
	 * @return the robot's skill manager
	 */
	public SkillManager getSkillManager() {
		return skillManager;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		robot = null;
		botTaskManager.destroy();
		botTaskManager = null;
		if (mission != null)
			mission.destroy();
		mission = null;
		robotJob = null;
		if (personality != null)
			personality.destroy();
		personality = null;
		// skillManager.destroy(); // not working for maven test
		// skillManager = null;
		// missionManager.destroy(); // not working for maven test
		// missionManager = null;
	}
}