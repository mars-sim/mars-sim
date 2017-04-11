/**
 * Mars Simulation Project
 * TaskManager.java
 * @version 3.1.0 2017-02-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.HashMap;
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
import org.mars_sim.msp.core.person.ai.job.JobAssignment;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTask;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTaskUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.SystemCondition;
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
	private String taskNameCache = "", taskDescriptionCache = "", taskPhaseCache = "";
	private String oldJob = "";
	/** The current task the person/robot is doing. */
	private Task currentTask, lastTask;
	/** The mind of the person the task manager is responsible for. */
	private Mind mind;
	private BotMind botMind;

	// Cache variables.
	private transient MarsClock timeCache;
	private static MarsClock marsClock;
	private transient double totalProbCache;
	private transient Map<MetaTask, Double> taskProbCache;
	private transient List<MetaTask> mtListCache;
	private transient List<MetaTask> oldAnyHourTasks, oldNonWorkTasks, oldWorkTasks;

	private Person person = null;

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

		if (Simulation.instance().getMasterClock() != null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock(); // marsClock won't pass maven test
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
	 * Returns the task name of the current or last task (one without the word "walk" in it).
	 * @return task name
	 */
	// 2016-12-01 Added getFilteredTaskName()
	public String getFilteredTaskName() {
		String s = getTaskName();
		if (s.toLowerCase().contains("walk")) {
			// check if the last task is walking related
			if (lastTask != null) {
				s = lastTask.getName();
				if (lastTask != null && !s.toLowerCase().contains("walk")) {
					return s;
				}
				else
					return "";
			}
			else
				return "";
		}
		else
			return s;

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
			return currentTask.getDescription(subTask);
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

	public String getLastTaskName() {
		return taskNameCache;
	}

	public String getLastTaskDescription() {
		return taskDescriptionCache;
	}


	/**
	 * Sets the current task to null.
	 */
	public void clearTask() {
		if (currentTask != null) {
			currentTask.endTask();
			currentTask = null;
		}

		person.fireUnitUpdate(UnitEventType.TASK_EVENT);

	}

	/*
	 * Prepares the task for recording in the task schedule
	 */
	// 2015-10-22 Added recordTask()
	@SuppressWarnings("null")
	public void recordTask() {
		String taskDescription = getTaskDescription(true);//currentTask.getDescription(); //
		String taskName = getTaskClassName();//getTaskClassName();//currentTask.getTaskName(); //
		String taskPhase = null;

		if (!taskName.toLowerCase().contains("walk")) {//.equals("WalkRoverInterior")
				//&& !taskName.equals("WalkSettlementInterior")
				//&& !taskName.equals("WalkSteps")) { // filter off Task phase "Walking" due to its excessive occurrences
			if (!taskDescription.equals(taskDescriptionCache)
					&& !taskDescription.toLowerCase().contains("walk") //.equals("Walking inside a settlement")
					&& !taskDescription.equals("")) {

				if (getPhase() != null) {

					taskPhase = getPhase().getName();

					if (!taskPhase.equals(taskPhaseCache)) {
						taskPhaseCache = taskPhase;
					}

				}

				person.getTaskSchedule().recordTask(taskName, taskDescription, taskPhase);
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
			lastTask = currentTask;
			currentTask = newTask;
			taskNameCache = currentTask.getTaskName();
			taskDescriptionCache = currentTask.getDescription();

			TaskPhase tp = currentTask.getPhase();
			if (tp != null)
				if (tp.getName() != null)
					taskPhaseCache = tp.getName();
				else
					taskPhaseCache = "";
			else
				taskPhaseCache = "";
			// initialize lastTask at the start of sim
			//if (lastTask == null)
			//	lastTask = currentTask;
		}

		person.fireUnitUpdate(UnitEventType.TASK_EVENT, newTask);

	}

	/**
	 * Reduce the person's caloric energy over time.
	 * @param time the passing time (
	 */
    public void reduceEnergy(double time) {
    	PhysicalCondition health = person.getPhysicalCondition();
//			int ACTIVITY_FACTOR = 6;
//			double newTime = ACTIVITY_FACTOR * time ;
//			health.reduceEnergy(newTime);
			// Changing reduce energy to be just time as it otherwise
			// ends up being too much energy reduction compared to the
			// amount gained from eating.
		health.reduceEnergy(time);


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
			throw new IllegalStateException(mind.getPerson() +
						" has zero total task probability weight.");
		}

		double r = RandomUtil.getRandomDouble(totalProbability);

		MetaTask selectedMetaTask = null;
		//System.out.println("size of metaTask : " + taskProbCache.size());

/*
		taskProbCache.keySet().forEach(mt -> {
			double probWeight = taskProbCache.get(mt);
			if (r <= probWeight) {
				selectedMetaTask = mt;
			}
			else {
				r -= probWeight;
			}
		});
*/
		// Determine which task is selected.
		for (MetaTask mt : taskProbCache.keySet()) {
			double probWeight = taskProbCache.get(mt);
			if (r <= probWeight) {
				// Select this task
				selectedMetaTask = mt;
			}
			else {
				r -= probWeight;
			}
		}

		if (selectedMetaTask == null) {
			throw new IllegalStateException(mind.getPerson() +
						" could not determine a new task.");
		}
		else {
			// Call constructInstance of the selected Meta Task to commence the ai task
			result = selectedMetaTask.constructInstance(mind.getPerson());
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

/*
		    //2016-10-04 Checked if the job is changed
		    List<JobAssignment> list = person.getJobHistory().getJobAssignmentList();
		    String newJob = "";
		    boolean jobChanged = false;
		    int num = list.size();
		    if (num == 0) {
		    	System.out.println(" list is zero");
		    }
		    else {
			    newJob = person.getJobHistory().getJobAssignmentList().get(num-1).getJobType();
			    //System.out.println("newJob is " + newJob);
			    if (!oldJob.equals(newJob)) {
			    	jobChanged = true;
			    	oldJob = newJob;
			    }
		    }

		    List<MetaTask> newAnyHourTasks, newNonWorkTasks, newWorkTasks;
		    // if there's a job change, do the following
		    //if (jobChanged) {
		    //	newAllWorkTasks = MetaTaskUtil.getAllWorkHourTasks();
		    //	newNonWorkTasks = MetaTaskUtil.getNonWorkHourTasks();
		    //	oldAllWorkTasks = newAllWorkTasks;
		    //	oldNonWorkTasks = newNonWorkTasks;
		    //}
		    //else {
		    //}

		    if (isOnCall) {
		    	if (jobChanged) {
			    	newAnyHourTasks = MetaTaskUtil.getAnyHourTasks();
		    		if (newAnyHourTasks != null)
		    			oldAnyHourTasks = newAnyHourTasks;
		    	}
		    	mtList = oldAnyHourTasks;
		    	//mtList = MetaTaskUtil.getAllWorkHourTasks();
		    }
		    else if (isOff) {
		    	if (jobChanged) {
			    	newNonWorkTasks = MetaTaskUtil.getNonWorkHourTasks();
		    		if (newNonWorkTasks != null)
		    			oldNonWorkTasks = newNonWorkTasks;
		    	}
		    	mtList = oldNonWorkTasks;
		    	//mtList = MetaTaskUtil.getNonWorkHourTasks();
		    }
		    else {
		    	// is the person off the shift ?
		    	isShiftHour = person.getTaskSchedule().isShiftHour(millisols);

			    if (isShiftHour) {
			    	if (jobChanged) {
				    	newWorkTasks = MetaTaskUtil.getWorkHourTasks();
			    		if (newWorkTasks != null)
			    			oldWorkTasks = newWorkTasks;
			    	}
			    	mtList = oldWorkTasks;
			    	//mtList = MetaTaskUtil.getWorkHourTasks();
			    }
			    else {
			    	if (jobChanged) {
				    	newNonWorkTasks = MetaTaskUtil.getNonWorkHourTasks();
			    		if (newNonWorkTasks != null)
			    			oldNonWorkTasks = newNonWorkTasks;
			    	}
			    	mtList = oldNonWorkTasks;
			    	//mtList = MetaTaskUtil.getNonWorkHourTasks();
			    }
		    }
*/

		    if (isOnCall) {
		    	mtList = MetaTaskUtil.getAnyHourTasks();
		    }
		    else if (isOff) {
		    	mtList = MetaTaskUtil.getNonWorkHourTasks();
		    }
		    else {
		    	// is the person off the shift ?
		    	isShiftHour = person.getTaskSchedule().isShiftHour(millisols);

			    if (isShiftHour) {
			    	mtList = MetaTaskUtil.getWorkHourTasks();
			    }
			    else {
			    	mtList = MetaTaskUtil.getNonWorkHourTasks();
			    }
		    }

		    //if (mtList == null)
		    	//System.out.println("mtList is null");

		    if (mtListCache != mtList && mtList != null) {
		    	//System.out.println("mtListCache : " + mtListCache);
		    	//System.out.println("mtList : " + mtList);
		    	mtListCache = mtList;
		    	taskProbCache = new HashMap<MetaTask, Double>(mtListCache.size());
		    }
/*
			if (taskProbCache == null) {
		    	System.out.println("mtListCache : " + mtListCache);
		    	System.out.println("mtList : " + mtList);
		    	taskProbCache = new HashMap<MetaTask, Double>(mtList.size());
			}


			// Note: one cannot compare the difference between two lists with .equals()
		    if (mtListCache == null || !mtListCache.equals(mtList)) {
		    	System.out.println("mtListCache : " + mtListCache);
		    	System.out.println("mtList : " + mtList);
		    	taskProbCache = null;
		    	mtListCache = mtList;
		    	taskProbCache = new HashMap<MetaTask, Double>(mtListCache.size());
		    }

		    //System.out.println("mtListCache is "+ mtListCache);
*/
			// Clear total probabilities.
			totalProbCache = 0D;
			// Determine probabilities.
			for (MetaTask mt : mtListCache) {
				double probability = mt.getProbability(person);

				if ((probability >= 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
					taskProbCache.put(mt, probability);
					totalProbCache += probability;
				}
				else {
					taskProbCache.put(mt, 0D);

					logger.severe(mind.getPerson().getName() + " bad task probability: " +  mt.getName() +
								" probability: " + probability);
				}
			}
		}

		// Set the time cache to the current time.
		//if (marsClock != null)
		//	marsClock = Simulation.instance().getMasterClock().getMarsClock();
		timeCache = (MarsClock) marsClock.clone();
	}

	/**
	 * Checks if task probability cache should be used.
	 * @return true if cache should be used.
	 */
	private boolean useCache() {
		//MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		//return currentTime.equals(timeCache);
		return marsClock.equals(timeCache);
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
		timeCache = null;
		marsClock = null;
		if (taskProbCache != null) {
			taskProbCache.clear();
			taskProbCache = null;
		}
	}
}