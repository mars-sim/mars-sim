/*
 * Mars Simulation Project
 * BotTaskManager.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.robot.ai.task;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.util.AbstractTaskJob;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTaskManager;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskCache;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The BotTaskManager class keeps track of a robot's current task and can randomly
 * assign a new task to a robot.
 */
public class BotTaskManager extends TaskManager {

	/** default serial id. */
	private static final long serialVersionUID = 1L;


	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(BotTaskManager.class.getName());

	// This is a shared cache with the fixed Charge taskjob
	private static TaskCache chargeMap;

	// Mapping of RobotType to the applicable MetaTasks
	private static Map<RobotType,List<FactoryMetaTask>> robotTasks;


	// Data members
	/** The mind of the robot. */
	private BotMind botMind;
	/** The work power demand of the robot. Will move to xml once stable. */
	private double workPower = .2;
	
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
	protected Task createTask(TaskJob selectedWork) {
		return selectedWork.createTask(robot);
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
		    if (currentTask.isEffortDriven()) {
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
	 * Build the assignments of RobotType to MetaTasks. This takes any Meta type and
	 * uses the preferred Robot to filter.
	 */
	private static synchronized void buildRobotTasks() {
		if (robotTasks == null) {
			Map<RobotType, List<FactoryMetaTask>> newTaskMap = new EnumMap<>(RobotType.class);

			List<FactoryMetaTask> anyRobot = new ArrayList<>();
			for(FactoryMetaTask mt : MetaTaskUtil.getRobotMetaTasks()) {
				Set<RobotType> possibleRobots = mt.getPreferredRobot();
				if (possibleRobots.isEmpty()) {
					anyRobot.add(mt);
				}
				else {
					for(RobotType rt : possibleRobots) {
						newTaskMap.computeIfAbsent(rt, k -> new ArrayList<>()).add(mt);
					}
				}
			}

			// If there are anyRobot tasks then add them to every possible Robot type
			if (!anyRobot.isEmpty()) {
				for(RobotType rt : RobotType.values()) {
					newTaskMap.computeIfAbsent(rt, k -> new ArrayList<>()).addAll(anyRobot);
				}
			}

			// Do not make it visible until fully created
			robotTasks = newTaskMap;
		}
	}

	/**
	 * Calculates and caches the probabilities.
	 */
	@Override
	protected TaskCache rebuildTaskCache() {

		// If robot is low power then can only charge
		if (robot.getSystemCondition().isLowPower()) {

			logger.info(robot, "Forcing to be recharged due to low power.");
			return getChargeTaskMap();
		}
		
		// Create a task list based on probability
		if (robotTasks == null) {
			buildRobotTasks();
		}
		
		// Reset taskProbCache and totalProbCache
		TaskCache newCache = new TaskCache("Robot", getMarsTime());
		
		// Determine probabilities.
		List<FactoryMetaTask> potentials = robotTasks.get(robot.getRobotType());
		for (FactoryMetaTask mt : potentials) {
			List<TaskJob> job = mt.getTaskJobs(robot);
	
			if (job != null) {
				newCache.add(job);
			}
		}
		
		// Add in any Settlement Tasks
		SettlementTaskManager stm = robot.getAssociatedSettlement().getTaskManager();
		newCache.add(stm.getTasks(robot));

		if (newCache.getTasks().isEmpty()) {
			newCache = getChargeTaskMap();
		}
		return newCache;
	}

	private static synchronized TaskCache getChargeTaskMap() {
		if (chargeMap == null) {
			chargeMap = new TaskCache("Robot Charge", null);
			TaskJob chargeJob = new AbstractTaskJob("Charge", 1D) {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Task createTask(Robot robot) {
					return new Charge(robot);
				}	
			};
			chargeMap.put(chargeJob);
		}
		return chargeMap;
	}

	/**
	 * Start a new Task by first checking for pending tasks.
	 */
	@Override
	public void startNewTask() {
		// Check if there are any assigned tasks that are pending
		if (!getPendingTasks().isEmpty()) {
			TaskJob pending = getPendingTask();
			if (pending != null) {
				Task newTask = pending.createTask(robot);
				replaceTask(newTask);
			}

			return;
		}

		super.startNewTask();
	}
	
	@Override
	public void reinit() {
		super.reinit();

		robot = botMind.getRobot();
		worker = robot;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		botMind = null;
		robot = null;
		chargeMap = null;
		robotTasks.clear();
		robotTasks = null;
	}

}
