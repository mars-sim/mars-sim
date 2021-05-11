/**
 * Mars Simulation Project
 * BotTaskManager.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.robot.ai.task;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.SystemCondition;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The TaskManager class keeps track of a person's current task and can randomly
 * assign a new task to a person based on a list of possible tasks and that person's
 * current situation.
 *
 * There is one instance of TaskManager per person.
 */
public class BotTaskManager extends TaskManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(BotTaskManager.class.getName());
	
	private static String loggerName = logger.getName();
	
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	private static String WALKING = "Walking";
	private static String WALK = "Walk";
	
	// Data members
	/** The mind of the robot. */
	private BotMind botMind;
	
	/** The robot instance. */
	private transient Robot robot = null;

	/**
	 * Constructor.
	 * @param botMind the mind that uses this bot task manager.
	 */
	public BotTaskManager(BotMind botMind) {
		super(botMind.getRobot());
		// Initialize data members
		this.botMind = botMind;

		this.robot = botMind.getRobot();
	}

	@Override
	protected Task createTask(MetaTask selectedMetaTask) {
		return selectedMetaTask.constructInstance(robot);
	}
	
	/*
	 * Prepares the task for recording in the task schedule
	 */
	public void recordFilterTask() {
		Task task = getRealTask();
		if (task == null)
			return;
		String taskDescription = task.getDescription();
		String taskName = task.getTaskName();
		String taskPhase = "";
	
//		if (!taskName.equals("") && !taskDescription.equals("")
//				&& !taskName.contains(WALK)) {
//			
//			if (!taskDescription.equals(taskDescriptionCache)
//				|| !taskPhase.equals(taskPhaseCache)) {
//				
//				if (task.getPhase() != null)
//					taskPhase = task.getPhase().getName();
//			
//				robot.getTaskSchedule().recordTask(taskName, taskDescription, taskPhase, "");
//				//taskPhaseCache = taskPhase;
//				taskDescriptionCache = taskDescription;
//			}
//		}
	}

	/**
	 * Reduce the person's caloric energy over time.
	 * @param time the passing time (
	 */
    private void reduceEnergy(double time) {
    	SystemCondition sys = robot.getSystemCondition();
		sys.reduceEnergy(time);
    }

	/**
	 * Perform the current task for a given amount of time.
	 * @param time amount of time to perform the action
	 * @param efficiency The performance rating of person performance task.
	 * @return remaining time.
	 * @throws Exception if error in performing task.
	 */
	public double executeTask(double time, double efficiency) {
		double remainingTime = 0D;
		
		if (currentTask != null) {
			// For effort driven task, reduce the effective time based on efficiency.
			if (efficiency < .1D) {
				efficiency = .1D;
			}

			if (currentTask.isEffortDriven()) {
				time *= efficiency;
			}

			//checkForEmergency();
			
			remainingTime = currentTask.performTask(time);
			// Record the action (task/mission)
			recordFilterTask();			
			// Expend energy based on activity.
		    double energyTime = time - remainingTime;
		    // Double energy expenditure if performing effort-driven task.
		    if (currentTask.isEffortDriven()) {
		        energyTime *= 2D;
		    }

		    if (energyTime > 0D) {
		        reduceEnergy(energyTime);
		    }
		}

		return remainingTime;

	}
	
	/**
	 * Checks if the person or robot is walking through a given building.
	 * @param building the building.
	 * @return true if walking through building.
	 */
	public boolean isWalkingThroughBuilding(Building building) {

	    boolean result = false;

	    Task task = currentTask;
	    while ((task != null) && !result) {
	        if (task instanceof Walk) {
	            Walk walkTask = (Walk) task;
	            if (walkTask.isWalkingThroughBuilding(building)) {
	                result = true;
	            }
	        }
	        task = task.getSubTask();
	    }

	    return result;
	}

	/**
	 * Checks if the person or robot is walking through a given vehicle.
	 * @param vehicle the vehicle.
	 * @return true if walking through vehicle.
	 */
	public boolean isWalkingThroughVehicle(Vehicle vehicle) {

	    boolean result = false;

        Task task = currentTask;
        while ((task != null) && !result) {
            if (task instanceof Walk) {
                Walk walkTask = (Walk) task;
                if (walkTask.isWalkingThroughVehicle(vehicle)) {
                    result = true;
                }
            }
            task = task.getSubTask();
        }

        return result;
	}

	/**
	 * Calculates and caches the probabilities.
	 */
	protected synchronized void rebuildTaskCache() {
		    	
		List<MetaTask> mtList = MetaTaskUtil.getRobotMetaTasks();

		// Create new taskProbCache
		taskProbCache = new ConcurrentHashMap<MetaTask, Double>(mtList.size());
		totalProbCache = 0D;

		// Determine probabilities.
		for (MetaTask mt : mtList) {
			double probability = mt.getProbability(robot);

			if ((probability >= 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
				taskProbCache.put(mt, probability);
				totalProbCache += probability;
			}
			else {
				taskProbCache.put(mt, 0D);

				logger.severe(robot.getName() + " bad task probability: " +  mt.getName() +
							" probability: " + probability);
			}
		}
	}

	public void reinit() {
		super.reinit();

		robot = botMind.getRobot();
		worker = robot;
		taskSchedule = robot.getTaskSchedule();
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		botMind = null;
		robot = null;
	}

}
