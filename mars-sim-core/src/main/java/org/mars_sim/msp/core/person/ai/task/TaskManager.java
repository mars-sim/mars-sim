/**
 * Mars Simulation Project
 * TaskManager.java
 * @version 3.08 2015-02-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTask;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTaskUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The TaskManager class keeps track of a person's current task and can randomly
 * assign a new task to a person based on a list of possible tasks and that person's
 * current situation.
 *
 * There is one instance of TaskManager per person.
 */
public class TaskManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(TaskManager.class.getName());

	// Data members
	private String taskNameCache, taskDescriptionCache, taskPhaseCache;
	/** The current task the person/robot is doing. */
	private Task currentTask;//, lastTask;
	/** The mind of the person the task manager is responsible for. */
	private Mind mind;
	private BotMind botMind;

	// Cache variables.
	private transient MarsClock timeCache;
	private transient double totalProbCache;
	private transient Map<MetaTask, Double> taskProbCache;
	private transient List<MetaTask> mtListCache;

	private Person person = null;
	private Robot robot = null;
	//private MarsClock clock;

	/**
	 * Constructor.
	 * @param mind the mind that uses this task manager.
	 */
	public TaskManager(Mind mind) {
		// Initialize data members
		this.mind = mind;

		this.person = mind.getPerson();

		currentTask = null;

		// Initialize cache values.
		timeCache = null;
		taskProbCache = new HashMap<MetaTask, Double>();
		totalProbCache = 0D;
	}

	public TaskManager(BotMind botMind) {
		// Initialize data members
		this.botMind = botMind;

		this.robot = botMind.getRobot();

		currentTask = null;

		// Initialize cache values.
		timeCache = null;
		taskProbCache = new HashMap<MetaTask, Double>(MetaTaskUtil.getRobotMetaTasks().size());
		totalProbCache = 0D;
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
	public String getTaskDescription() {
		if (currentTask != null) {
			String doAction = currentTask.getDescription();

			return doAction;
		} else {
			return "";
		}
	}

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

		if (person != null)
			person.fireUnitUpdate(UnitEventType.TASK_EVENT);
		else if (robot != null)
			robot.fireUnitUpdate(UnitEventType.TASK_EVENT);
	}

	/*
	 * Prepares the task for recording in the task schedule
	 * @param newTask
	 */
	// 2015-10-22 Added recordTask()
	@SuppressWarnings("null")
	public void recordTask() {
		String taskDescription = getTaskDescription();//currentTask.getDescription(); //
		String taskName = getTaskClassName();//currentTask.getTaskName(); //
		String taskPhase = null;

		if (!taskName.equals("WalkRoverInterior")
				&& !taskName.equals("WalkSettlementInterior")
				&& !taskName.equals("WalkSteps")
				) // filter off Task phase "Walking" due to its excessive occurrences
			if (!taskDescription.equals(taskDescriptionCache)) {

				if (getPhase() != null) {

					taskPhase = getPhase().getName();

					if (!taskPhase.equals(taskPhaseCache)) {

						if (person != null) {
							if (!taskDescription.equals(""))
								person.getTaskSchedule().recordTask(taskName, taskDescription, taskPhase);
						}
						else if (robot != null) {
							if (!taskDescription.equals(""))
								robot.getTaskSchedule().recordTask(taskName, taskDescription, taskPhase);
						}

						taskDescriptionCache = taskDescription;
						taskPhaseCache = taskPhase;
					}
				}

				else {

					if (person != null) {
						if (!taskDescription.equals(""))
							person.getTaskSchedule().recordTask(taskName, taskDescription, taskPhase);
					}
					else if (robot != null) {
						if (!taskDescription.equals(""))
							robot.getTaskSchedule().recordTask(taskName, taskDescription, taskPhase);
					}

					taskDescriptionCache = taskDescription;
				}
			}
	}

	/**
	 * Adds a task to the stack of tasks.
	 * @param newTask the task to be added
	 */
	public void addTask(Task newTask) {

		// 2015-10-22 Added recordTask()
		//recordTask();

		if (hasActiveTask()) {
			currentTask.addSubTask(newTask);

		} else {
			currentTask = newTask;
			taskNameCache = currentTask.getTaskName();
			taskDescriptionCache = currentTask.getDescription();

			if (currentTask.getPhase() != null)
				if (currentTask.getPhase().getName() != null)
					taskPhaseCache = currentTask.getPhase().getName();
				else
					taskPhaseCache = "";
			else
				taskPhaseCache = "";
			// initialize lastTask at the start of sim
			//if (lastTask == null)
			//	lastTask = currentTask;
		}

		if (person != null) {
			person.fireUnitUpdate(UnitEventType.TASK_EVENT, newTask);
		}
		else if (robot != null) {
			robot.fireUnitUpdate(UnitEventType.TASK_EVENT, newTask);
		}

	}

	/**
	 * Reduce the person's caloric energy over time.
	 * @param time the passing time (
	 */
    public void reduceEnergy(double time) {
    	PhysicalCondition health = null;
		if (person != null)
			health = person.getPhysicalCondition();
		else if (robot != null)
			health = robot.getPhysicalCondition();

//		int ACTIVITY_FACTOR = 6;
//		double newTime = ACTIVITY_FACTOR * time ;
//		health.reduceEnergy(newTime);

		// Changing reduce energy to be just time as it otherwise
		// ends up being too much energy reduction compared to the
		// amount gained from eating.
		health.reduceEnergy(time);;
        //System.out.println("TaskManager : reduce Energy by "+ Math.round( newTime * 10.0)/10.0);
    }

	/**
	 * Perform the current task for a given amount of time.
	 * @param time amount of time to perform the action
	 * @param efficiency The performance rating of person performance task.
	 * @return remaining time.
	 * @throws Exception if error in performing task.
	 */
	public double performTask(double time, double efficiency) {
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
		}

		// Expend energy based on activity.
		if (currentTask != null) {

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

		if (person != null) {

			// Check for emergency malfunction.
			if (RepairEmergencyMalfunction.hasEmergencyMalfunction(person)) {

			    // Check if person is already repairing an emergency.
			    boolean hasEmergencyRepair = doingEmergencyRepair();

				// Check if person is performing an airlock task.
				boolean hasAirlockTask = doingAirlockTask();

				// Check if person is outside.
				boolean isOutside = person.getLocationSituation() == LocationSituation.OUTSIDE;

				// Cancel current task and start emergency repair task.
				if (!hasEmergencyRepair && !hasAirlockTask && !isOutside) {

					if (RepairEmergencyMalfunctionEVA.requiresEVARepair(person)) {

			            if (RepairEmergencyMalfunctionEVA.canPerformEVA(person)) {

			                logger.fine(person + " cancelling task " + currentTask +
			                        " due to emergency EVA repairs.");
			                clearTask();
			                addTask(new RepairEmergencyMalfunctionEVA(person));
			            }
					}
					else {
					    logger.fine(person + " cancelling task " + currentTask +
		                        " due to emergency repairs.");
		                clearTask();
					    addTask(new RepairEmergencyMalfunction(person));
					}
				}
			}
		}
		else if (robot != null) {

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
		}

	}

	/**
	 * Gets a new task for the person based on tasks available.
	 * @return new task
	 */
	public Task getNewTask() {
		Task result = null;
		// If cache is not current, calculate the probabilities.
		if (!useCache()) {
			calculateProbability();
		}
		// Get a random number from 0 to the total weight
		double totalProbability = getTotalTaskProbability(true);

		if (totalProbability == 0D) {

			if (person != null) {
				throw new IllegalStateException(mind.getPerson() +
						" has zero total task probability weight.");
			}
			else if (robot != null) {
				throw new IllegalStateException(botMind.getRobot() +
						" has zero total task probability weight.");
			}
		}


		double r = RandomUtil.getRandomDouble(totalProbability);
		// Determine which task is selected.
		MetaTask selectedMetaTask = null;
		Iterator<MetaTask> i = taskProbCache.keySet().iterator();
		while (i.hasNext() && (selectedMetaTask == null)) {
			MetaTask metaTask = i.next();
			double probWeight = taskProbCache.get(metaTask);
			if (r <= probWeight) {
				selectedMetaTask = metaTask;
			}
			else {
				r -= probWeight;
			}
		}
		if (selectedMetaTask == null) {
			if (person != null)
				throw new IllegalStateException(mind.getPerson() +
						" could not determine a new task.");
			else if (robot != null)
					throw new IllegalStateException(botMind.getRobot() +
							" could not determine a new task.");

		} else {

			if (person != null) {
				// Construct the task
				result = selectedMetaTask.constructInstance(mind.getPerson());
				//person.getTaskSchedule().recordTask(getTaskName(), getTaskDescription());
			}
			else if (robot != null) {
				// Construct the task
				result = selectedMetaTask.constructInstance(botMind.getRobot());
				//robot.getTaskSchedule().recordTask(getTaskName(), getTaskDescription());
			}
		}
		// Clear time cache.
		timeCache = null;
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
		if (person != null) {

		    List<MetaTask> mtList = null;

		    if (timeCache == null)
		    	timeCache = Simulation.instance().getMasterClock().getMarsClock();
		    int millisols =  (int) timeCache.getMillisol();

		    boolean isOnCall = person.getTaskSchedule().getShiftType().equals(ShiftType.ON_CALL);
		    boolean isOff = person.getTaskSchedule().getShiftType().equals(ShiftType.OFF);
		    boolean isShiftHour = true;

		    if (isOnCall) {
		    	mtList = MetaTaskUtil.getAllWorkHourTasks();
		    }
		    else if (isOff) {
		    	mtList = MetaTaskUtil.getNonWorkHourTasks();
		    }
		    else {
		    	isShiftHour = person.getTaskSchedule().isShiftHour(millisols);

			    if (isShiftHour) {
			    	mtList = MetaTaskUtil.getWorkHourTasks();
			    }
			    else {
			    	mtList = MetaTaskUtil.getNonWorkHourTasks();
			    }
		    }

			if (taskProbCache == null) {
		    	taskProbCache = new HashMap<MetaTask, Double>(mtList.size());
			}


		    if (mtListCache == null || !mtListCache.equals(mtList)) {
		    	//System.out.println("!mtListCache.equals(mtList)");
		    	taskProbCache = null;
		    	mtListCache = mtList;
		    	taskProbCache = new HashMap<MetaTask, Double>(mtListCache.size());
		    }

		    //System.out.println("mtListCache is "+ mtListCache);

			// Clear total probabilities.
			totalProbCache = 0D;
			// Determine probabilities.
			Iterator<MetaTask> i = mtListCache.iterator();

			while (i.hasNext()) {
				MetaTask metaTask = i.next();
				double probability = 0;

				probability = metaTask.getProbability(person);

				if ((probability >= 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
					taskProbCache.put(metaTask, probability);
					totalProbCache += probability;
				}
				else {
					taskProbCache.put(metaTask, 0D);

					logger.severe(mind.getPerson().getName() + " bad task probability: " +  metaTask.getName() +
								" probability: " + probability);
				}
			}
		}

		else if (robot != null) {

			List<MetaTask> mtList = MetaTaskUtil.getRobotMetaTasks();

			if (taskProbCache == null)
				taskProbCache = new HashMap<MetaTask, Double>(mtList.size());

			// Clear total probabilities.
			totalProbCache = 0D;
			// Determine probabilities.
			Iterator<MetaTask> i = mtList.iterator();

			while (i.hasNext()) {
				MetaTask metaTask = i.next();
				double probability = 0;

				probability = metaTask.getProbability(robot);

				if ((probability >= 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
					taskProbCache.put(metaTask, probability);
					totalProbCache += probability;
				}
				else {
					taskProbCache.put(metaTask, 0D);

					logger.severe(botMind.getRobot().getName() + " bad task probability: " +  metaTask.getName() +
								" probability: " + probability);
				}
			}
		}

		// Set the time cache to the current time.
		timeCache = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
	}

	/**
	 * Checks if task probability cache should be used.
	 * @return true if cache should be used.
	 */
	private boolean useCache() {
		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		return currentTime.equals(timeCache);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (currentTask != null) {
			currentTask.destroy();
		}
		mind = null;
		botMind = null;
		person = null;
		robot = null;
		timeCache = null;
		if (taskProbCache != null) {
			taskProbCache.clear();
			taskProbCache = null;
		}
	}
}