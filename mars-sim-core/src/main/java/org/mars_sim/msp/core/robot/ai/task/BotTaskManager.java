/*
 * Mars Simulation Project
 * BotTaskManager.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.robot.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The BotTaskManager class keeps track of a robot's current task and can randomly
 * assign a new task to a robot.
 */
public class BotTaskManager extends TaskManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(BotTaskManager.class.getName());

	// Data members
	/** The mind of the robot. */
	private BotMind botMind;
	/** The work power demand of the robot. Will move to xml once stable. */
	private double workPower = .2;
	
	/** The robot instance. */
	private transient Robot robot = null;
	
	private transient List<MetaTask> mtList;

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
	

	/**
	 * Reduce the robot's energy for executing a task.
	 * 
	 * @param time the passing time (
	 */
    private void reduceEnergy(double time) {
    	robot.consumeEnergy(time * MarsClock.HOURS_PER_MILLISOL * workPower);
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

			if (efficiency < 0D) {
				efficiency = 0D;
			}

			if (currentTask.isEffortDriven()) {
				// For effort driven task, reduce the effective time based on efficiency.
				time *= efficiency;
			}

			try {
				
				// Receive StackOverflowError
				remainingTime = currentTask.performTask(time);

			} catch (Exception e) {
				logger.severe(robot, "currentTask: " + currentTask.getDescription()
						+ " - Trouble calling performTask(): ", e);
				return remainingTime;
			}
			
			// Calculate the energy time
		    double energyTime = time - remainingTime;
		    
		    // Double energy expenditure if performing effort-driven task.
		    if (currentTask != null && currentTask.isEffortDriven()) {
		        energyTime *= 2D;
		    }

		    // Checks if the robot is charging
		    if (energyTime > 0D && !robot.getSystemCondition().isCharging()) {
		    	// Expend energy based on activity.
		    	reduceEnergy(energyTime);
		    }
		}

		return remainingTime;

	}

	
	/**
	 * Calculates and caches the probabilities.
	 */
	protected synchronized void rebuildTaskCache() {
		if (mtList == null) {
			List<MetaTask> list = MetaTaskUtil.getRobotMetaTasks();
			List<MetaTask> newList = new ArrayList<>();
			// Create new taskProbCache
			taskProbCache = new ConcurrentHashMap<>(list.size());
			totalProbCache = 0D;
	
			// Determine probabilities.
			for (MetaTask mt : list) {
				// Get task name
				String taskName = mt.getClass().getSimpleName().replaceAll("Meta", "");
		        // Prevent bots from performing tasks not being programmed for
		     	RobotJob job = robot.getBotMind().getRobotJob();
		     	if (job != null) {
		     		double mod = job.getStartTaskProbabilityModifier(taskName);
		     		if (mod > 0) {
		     			newList.add(mt);
		     		}
		     	}
			}
			
			mtList = newList;
		}
		
		// Determine probabilities.
		for (MetaTask mt : mtList) {
			double probability = mt.getProbability(robot);
	
			if ((probability > 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
				taskProbCache.put(mt, probability);
				totalProbCache += probability;
//				logger.info(robot, 
//						mt +
//						"   totalProbCache: " + totalProbCache
//						+ "   probability: " + probability);
			}
		}
	
		// Output shift
		if (diagnosticFile != null) {
			outputCache();
		}
	}

	public void reinit() {
		super.reinit();

		robot = botMind.getRobot();
		worker = robot;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		botMind = null;
		robot = null;
	}

}
