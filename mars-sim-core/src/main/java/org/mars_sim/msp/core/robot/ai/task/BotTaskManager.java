/**
 * Mars Simulation Project
 * BotTaskManager.java
 * @version 3.1.0 2017-01-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.robot.ai.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
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
public class BotTaskManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(BotTaskManager.class.getName());
	
	private static String loggerName = logger.getName();
	
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	// Data members
    /** The cache for msol. */     
 	private double msolCache = -1D;
	/** The cache for total probability. */
	private transient double totalProbCache;
	/** The cache for task description phase. */		
	private String taskDescriptionCache = "";
	/** The cache for task phase. */	
	private String taskPhaseCache = "";
	/** The current task the robot is doing. */
	private Task currentTask; 
	/** The mind of the robot. */
	private BotMind botMind;
	
	/** The robot instance. */
	private transient Robot robot = null;
	
	/** The MasterClock static instance. */	
	private static MasterClock masterClock;
	/** The MarsClock instance. */	
	private static MarsClock marsClock;

	/** The cache map for the task probability. */		
	private transient Map<MetaTask, Double> taskProbCache;

	static {
		masterClock = Simulation.instance().getMasterClock();
		/** The MarsClock instance. */	
		marsClock = masterClock.getMarsClock(); 
	}
	
	/**
	 * Constructor.
	 * @param botMind the mind that uses this bot task manager.
	 */
	public BotTaskManager(BotMind botMind) {
		// Initialize data members
		this.botMind = botMind;

		this.robot = botMind.getRobot();

		currentTask = null;

		taskProbCache = new HashMap<MetaTask, Double>(MetaTaskUtil.getRobotMetaTasks().size());
		totalProbCache = 0D;
	
//		if (masterClock != null) // use this check to pass maven test
//			marsClock = masterClock.getMarsClock(); 
	}
	
	/**
	 * Returns true if person has an active task.
	 * @return true if person has an active task
	 */
	public boolean hasActiveTask() {
		return (currentTask != null) && !currentTask.isDone();
	}

	/**
	 * Returns true if person has a task (may be inactive).
	 * @return true if person has a task
	 */
	public boolean hasTask() {
		return currentTask != null;
	}

	/**
	 * Returns the name of the current task for UI purposes.
	 * Returns a blank string if there is no current task.
	 * @return name of the current task
	 */
	public String getTaskName() {
		if (currentTask != null) {
			return currentTask.getName();
		} else {
			return "";
		}
	}

	/**
	 * Returns the name of the current task for UI purposes.
	 * Returns a blank string if there is no current task.
	 * @return name of the current task
	 */
	public String getTaskClassName() {
		if (currentTask != null) {
			return currentTask.getTaskName();
		} else {
			return "";
		}
	}


	/**
	 * Returns a description of current task for UI purposes.
	 * Returns a blank string if there is no current task.
	 * @return a description of the current task
	 */
	public String getTaskDescription(boolean subTask) {
		if (currentTask != null) {
			String t = currentTask.getDescription(subTask);
			if (t != null)
				return t;
			else
				return "";		
		} 
		
		else
			return "";
	}
	
	public String getSubTaskDescription() {
		if (currentTask.getSubTask() != null) {
			String t = currentTask.getSubTask().getDescription();
			if (t != null)
				return t;
			else
				return "";		
		} 
		
		else
			return "";
	}
	

//	public FunctionType getFunction(boolean subTask) {
//		if (currentTask != null) {
//			return currentTask.getFunction(subTask);
//		} 
//		else {
//			return FunctionType.UNKNOWN;
//		}
//	}

	/**
	 * Returns the current task phase if there is one.
	 * Returns null if current task has no phase.
	 * Returns null if there is no current task.
	 * @return the current task phase
	 */
	public TaskPhase getPhase() {
		if (currentTask != null) {
			return currentTask.getPhase();
		} else {
			return null;
		}
	}

	
	public TaskPhase getSubTaskPhase() {
		if (currentTask.getSubTask() != null) {
			return currentTask.getSubTask().getPhase();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the current task.
	 * Return null if there is no current task.
	 * @return the current task
	 */
	public Task getTask() {
		return currentTask;
	}

	/**
	 * Sets the current task to null.
	 */
	public void clearTask() {
		currentTask.endTask();
		currentTask = null;

		robot.fireUnitUpdate(UnitEventType.TASK_EVENT);
	}


	/*
	 * Prepares the task for recording in the task schedule
	 */
	public void recordFilterTask() {
		String taskDescription = getTaskDescription(false);
		String taskName = getTaskClassName();
		String taskPhase = "";
	
		if (!taskName.equals("")) {
			if (!taskDescription.equals(taskDescriptionCache)
				|| !taskPhase.equals(taskPhaseCache)) {
	
				if (getPhase() != null)
					taskPhase = getPhase().getName();
			
				robot.getTaskSchedule().recordTask(taskName, taskDescription, taskPhase, "");
				taskPhaseCache = taskPhase;
				taskDescriptionCache = taskDescription;
			}
		}
	}

	/**
	 * Adds a task to the stack of tasks.
	 * @param newTask the task to be added
	 */
	public void addTask(Task newTask) {

		if (hasActiveTask()) {
			currentTask.addSubTask(newTask);

		} else {
			//lastTask = currentTask;
			currentTask = newTask;
			//taskNameCache = currentTask.getTaskName();
			taskDescriptionCache = currentTask.getDescription();

			TaskPhase tp = currentTask.getPhase();
			if (tp != null)
				if (tp.getName() != null)
					taskPhaseCache = currentTask.getPhase().getName();
				else
					taskPhaseCache = "";
			else
				taskPhaseCache = "";
			
		}


		robot.fireUnitUpdate(UnitEventType.TASK_EVENT, newTask);

	}

	/**
	 * Reduce the person's caloric energy over time.
	 * @param time the passing time (
	 */
    public void reduceEnergy(double time) {
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

			checkForEmergency();
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
	
/*
	private boolean doingEmergencyRepair() {

	    // Check if person is already repairing an emergency.
	    boolean hasEmergencyRepair = ((currentTask != null) && (currentTask
				instanceof RepairEmergencyMalfunction));
		if (((currentTask != null) && (currentTask instanceof RepairEmergencyMalfunctionEVA))) {
		    hasEmergencyRepair = true;
		}
		return hasEmergencyRepair;
	}

	private boolean doingAirlockTask() {
		// Check if robot is performing an airlock task.
		boolean hasAirlockTask = false;
		Task task = currentTask;
		while (task != null) {
			if ((task instanceof EnterAirlock) || (task instanceof ExitAirlock)) {
				hasAirlockTask = true;
			}
			task = task.getSubTask();
		}

		return hasAirlockTask;
	}
*/
	
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
	 * Checks if any emergencies are happening in the person's local.
	 * Adds an emergency task if necessary.
	 * @throws Exception if error checking for emergency.
	 */
	private void checkForEmergency() {

		//if (robot != null) {
/*
			// Check for emergency malfunction.
			if (RepairEmergencyMalfunction.hasEmergencyMalfunction(robot)) {

			    // Check if robot is already repairing an emergency.
			    boolean hasEmergencyRepair = doingEmergencyRepair();

				// Check if robot is performing an airlock task.
				boolean hasAirlockTask = doingAirlockTask();

				// Check if robot is outside.
				boolean isOutside = robot.getLocationSituation() == LocationSituation.OUTSIDE;

				// Cancel current task and start emergency repair task.
				if (!hasEmergencyRepair && !hasAirlockTask && !isOutside) {

					if (RepairEmergencyMalfunctionEVA.requiresEVARepair(robot)) {

			            if (RepairEmergencyMalfunctionEVA.canPerformEVA(robot)) {

			                logger.fine(robot + " cancelling task " + currentTask +
			                        " due to emergency EVA repairs.");
			                clearTask();
			                addTask(new RepairEmergencyMalfunctionEVA(robot));
			            }
			            
					}
					else {
					    logger.fine(robot + " cancelling task " + currentTask +
		                        " due to emergency repairs.");
		                clearTask();
					    addTask(new RepairEmergencyMalfunction(robot));
					}
				}
			}
*/
		//}
	}

	/**
	 * Gets a new task for the person based on tasks available.
	 * @return new task
	 */
	public Task getNewTask() {
		Task result = null;
		MetaTask selectedMetaTask = null;

		// If cache is not current, calculate the probabilities.
		if (!useCache()) {
			calculateProbability();
		}
		// Get a random number from 0 to the total weight
		double totalProbability = getTotalTaskProbability(true);

		if (totalProbability == 0D) {
//			LogConsolidated.log(Level.SEVERE, 5_000, sourceName,
//			person.getName() + " has zero total task probability weight.");

			// Switch to loading non-work hour meta tasks since
			// leisure tasks are NOT based on needs
			List<MetaTask> list = MetaTaskUtil.getNonWorkHourMetaTasks();
			selectedMetaTask = list.get(RandomUtil.getRandomInt(list.size() - 1));
			
		} else {

			double r = RandomUtil.getRandomDouble(totalProbability);

			// Determine which task is selected.
			for (MetaTask mt : taskProbCache.keySet()) {
				double probWeight = taskProbCache.get(mt);
				if (r <= probWeight) {
					// Select this task
					selectedMetaTask = mt;
				} else {
					r -= probWeight;
				}
			}
		}
		
		if (selectedMetaTask == null) {
			LogConsolidated.log(Level.SEVERE, 5_000, sourceName, robot.getName() + " could not determine a new task.");
		} else {
			// Call constructInstance of the selected Meta Task to commence the ai task
			result = selectedMetaTask.constructInstance(botMind.getRobot());
//			LogConsolidated.log(Level.FINE, 5_000, sourceName, robot + " is going to " + selectedMetaTask.getName());
		}

		// Clear time cache.
		msolCache = -1;

		return result;
	}

	/**
	 * Determines the total probability weight for available tasks.
	 * @return total probability weight
	 */
	public double getTotalTaskProbability(boolean useCache) {
		// If cache is not current, calculate the probabilities.
		if (!useCache) {
			calculateProbability();
		}
		return totalProbCache;
	}

	/**
	 * Calculates and caches the probabilities.
	 */
	private void calculateProbability() {

		if (!useCache()) {
		    	
			List<MetaTask> mtList = MetaTaskUtil.getRobotMetaTasks();
	
			if (taskProbCache == null)
				taskProbCache = new HashMap<MetaTask, Double>(mtList.size());
	
			// Clear total probabilities.
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
	
					logger.severe(botMind.getRobot().getName() + " bad task probability: " +  mt.getName() +
								" probability: " + probability);
				}
			}
	    }
	}

	/**
	 * Checks if task probability cache should be used.
	 * @return true if cache should be used.
	 */
	private boolean useCache() {
		double msol = marsClock.getMillisolOneDecimal();
		int diff = Double.compare(msolCache, msol);
		if (diff > 0 || diff < 0) {
			msolCache = msol;
			return false;
		}
		return true;
	}

	public void reinit() {
		robot = botMind.getRobot();
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (currentTask != null) {
			currentTask.destroy();
		}
		botMind = null;
		robot = null;
		marsClock = null;
		if (taskProbCache != null) {
			taskProbCache.clear();
			taskProbCache = null;
		}
	}
}