/**
 * Mars Simulation Project
 * BotTaskManager.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.robot.ai.task;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.SystemCondition;
import org.mars_sim.msp.core.robot.ai.BotMind;

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
	 * Calculates and caches the probabilities.
	 */
	protected synchronized void rebuildTaskCache() {
		    	
		List<MetaTask> mtList = MetaTaskUtil.getRobotMetaTasks();

		// Create new taskProbCache
		taskProbCache = new ConcurrentHashMap<>(mtList.size());
		totalProbCache = 0D;

		// Determine probabilities.
		for (MetaTask mt : mtList) {
			double probability = mt.getProbability(robot);

			if ((probability > 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
				taskProbCache.put(mt, probability);
				totalProbCache += probability;
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
