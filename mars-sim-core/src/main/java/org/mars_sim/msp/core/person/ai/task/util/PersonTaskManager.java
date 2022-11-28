/*
 * Mars Simulation Project
 * PersonTaskManager.java
 * @date 2021-12-05
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.util.List;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.task.EatDrink;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.structure.ShiftSlot.WorkStatus;

/**
 * The PersonTaskManager class keeps track of a person's current task and can randomly
 * assign a new task to a person based on a list of possible tasks and that
 * person's current situation.
 *
 * There is one instance of TaskManager per person.
 */
public class PersonTaskManager extends TaskManager {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(PersonTaskManager.class.getName());

	private static TaskCache defaultInsideTasks;
	private static TaskCache defaultOutsideTasks;

	// Data members
	/** The mind of the person the task manager is responsible for. */
	private Mind mind;

	private transient Person person;

	/**
	 * Constructor.
	 *
	 * @param mind the mind that uses this task manager.
	 */
	public PersonTaskManager(Mind mind) {
		super(mind.getPerson());

		// Initialize data members
		this.mind = mind;

		this.person = mind.getPerson();
	}

	/**
	 * Reduce the person's caloric energy over time.
	 *
	 * @param time the passing time (
	 */
	private void reduceEnergy(double time) {
		person.getPhysicalCondition().reduceEnergy(time);
	}

	/**
	 * Performs the current task for a given amount of time.
	 *
	 * @param time       amount of time to perform the action
	 * @param efficiency The performance rating of person performance task.
	 * @return remaining time.
	 * @throws Exception if error in performing task.
	 */
	public double executeTask(double time, double efficiency) {
		double remainingTime = 0D;
		
		if (currentTask != null) {
			
			boolean effort = currentTask.isEffortDriven();

			try {
				remainingTime = currentTask.performTask(time);

			} catch (Exception e) {
				logger.severe(person, "Trouble calling performTask(): ", e);
				return remainingTime;
			}

			if (effort) {
				
				if (efficiency <= 0D)
					efficiency = 0D;
				// For effort driven task, reduce the effective time based on efficiency.
				time *= efficiency;
				// Calculate the energy time
				double energyTime = time - remainingTime;
				
				// Double energy expenditure if performing effort-driven task.
				if (energyTime > 0D) {
	
					if (person.isOutside()) {
						// Take more energy to be in EVA doing work
						// Future: should also consider skill level and strength and body weight
						reduceEnergy(energyTime * 1.25);
					} else {
						// Expend nominal energy based on activity.
						reduceEnergy(energyTime);
					}
				}
			}
		}

		return remainingTime;

	}


	/**
	 * Calculates and caches the probabilities.
	 * This will NOT use the cache but assumes the callers know when a cache can be used or not used.
	 */
	@Override
	protected TaskCache rebuildTaskCache() {

		List<MetaTask> mtList = null;
		String shiftDesc = null;
		WorkStatus workStatus = person.getShiftSlot().getStatus();
		switch(workStatus) {
			case OFF_DUTY:
			case ON_LEAVE:
				mtList = MetaTaskUtil.getNonDutyHourTasks();
				shiftDesc = "Shift: NonDuty";
				break;
			case ON_CALL:
				mtList = MetaTaskUtil.getPersonMetaTasks();
				shiftDesc = "Shift: OnCall";
				break;
			case ON_DUTY:
				mtList = MetaTaskUtil.getDutyHourTasks();
				shiftDesc = "Shift: Duty";
				break;
			default:
				throw new IllegalStateException("Do not know status " + workStatus);
		}

		// Create new taskProbCache
		TaskCache newCache = new TaskCache(shiftDesc, marsClock);

		// Determine probabilities.
		for (MetaTask mt : mtList) {
			List<TaskJob> job = mt.getTaskJobs(person);
			if (job != null) {
				newCache.add(job);
			}
		}

		// Add in any Settlement Tasks
		if (workStatus == WorkStatus.ON_DUTY) {
			SettlementTaskManager stm = person.getAssociatedSettlement().getTaskManager();
			newCache.add(stm.getTasks(person));
		}

		// Check if the map cache is empty
		if (newCache.getTasks().isEmpty()) {
			if (person.isOutside()) {
				newCache = getDefaultOutsideTasks();
			}
			else {
				newCache = getDefaultInsideTasks();
			}
			logger.warning(person, 30_000L, "No normal task available. Get default "
								+ (person.isOutside() ? "outside" : "inside") + " tasks.");
		}
		return newCache;
	}

	/**
	 * Shared cache for person who are Inside. Contains the basic Task
	 * that can always be done.
	 */
	private static synchronized TaskCache getDefaultInsideTasks() {
		if (defaultInsideTasks == null) {
			defaultInsideTasks = new TaskCache("Default Inside", null);
			
			// Create a fallback Task job that can always be done
			TaskJob sleepJob = new AbstractTaskJob("Sleep", 1D) {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Task createTask(Person person) {
					return new Sleep(person);
				}	
			};
			defaultInsideTasks.put(sleepJob);

			TaskJob eatJob = new AbstractTaskJob("Eat", 1D) {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Task createTask(Person person) {
					return new EatDrink(person);
				}	
			};
			defaultInsideTasks.put(eatJob);
		}
		return defaultInsideTasks;
	}

	/**
	 * Shared cache for person who are Outside. This forces a return to the settlement.
	 */
	private static synchronized TaskCache getDefaultOutsideTasks() {
		if (defaultOutsideTasks == null) {
			defaultOutsideTasks = new TaskCache("Default Outside", null);

			// Create a MetaTask to return inside
			TaskJob walkBack = new AbstractTaskJob("Return Inside", 1D) {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Task createTask(Person person) {
					logger.info(person, "Returning inside to find work.");
					return new Walk(person);
				}	
			};
			defaultOutsideTasks.put(walkBack);
		}
		return defaultOutsideTasks;
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
				Task newTask = pending.createTask(person);
				startTask(newTask);
			}

			return;
		}

		super.startNewTask();
	}


	@Override
	protected Task createTask(TaskJob selectedWork) {
		return selectedWork.createTask(mind.getPerson());
	}
	
	@Override
	public void reinit() {
		person = mind.getPerson();
		worker = person;
		super.reinit();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		mind = null;
		person = null;
	}
}
