/*
 * Mars Simulation Project
 * PersonTaskManager.java
 * @date 2021-12-05
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.role.Role;

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

	private static TaskCache defaultTasks;

	private static final int MAX_TASK_PROBABILITY = 35_000;

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

			if (efficiency <= 0D) {
				efficiency = 0D;
			}

			if (currentTask.isEffortDriven()) {
				// For effort driven task, reduce the effective time based on efficiency.
				time *= efficiency;
			}

			try {
				remainingTime = currentTask.performTask(time);

			} catch (Exception e) {
				logger.severe(person, "Trouble calling performTask(): ", e);
				return remainingTime;
			}

			// Calculate the energy time
			double energyTime = time - remainingTime;

			// Double energy expenditure if performing effort-driven task.
			if (energyTime > 0D && currentTask.isEffortDriven()) {

				if (person.isOutside()) {
					// Take more energy to be in EVA doing work
					// TODO: should also consider skill level and strength and body weight
					reduceEnergy(energyTime * 1.5);
				} else {
					// Expend nominal energy based on activity.
					reduceEnergy(energyTime);
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

		String shiftDesc = null;
		TaskSchedule taskSchedule = person.getTaskSchedule();
		List<MetaTask> mtList = null;
		if (taskSchedule.getShiftType() == ShiftType.ON_CALL) {
			mtList = MetaTaskUtil.getPersonMetaTasks();
			shiftDesc = "OnCall";
		}
		else if (taskSchedule.isShiftHour(marsClock.getMillisolInt())) {
			mtList = MetaTaskUtil.getDutyHourTasks();
			shiftDesc = "Duty";
		}
		else {
			mtList = MetaTaskUtil.getNonDutyHourTasks();
			shiftDesc = "NonDuty";
		}


		// Create new taskProbCache
		TaskCache newCache = new TaskCache();

		// Determine probabilities.
		for (MetaTask mt : mtList) {
			double probability = mt.getProbability(person);
			if ((probability > 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
				if (probability > MAX_TASK_PROBABILITY) {
					if (!mt.getName().toLowerCase().contains("sleep")) {
						logger.log(person, Level.WARNING, 10_000,
							mt.getName() + "'s probability is at all time high ("
							+ Math.round(probability) + ").");
					}
					probability = MAX_TASK_PROBABILITY;
				}

				newCache.put(mt, probability);
			}
		}

		// Check somethign has been found
		if (newCache.getTasks().isEmpty()) {
			newCache = getDefaultTasks();
			logger.warning(person, "No normal task available");
		}

		// Output shift
		if (diagnosticFile != null) {
            Role role = person.getRole();
            String roleDetails = "Role: " + (role != null ? role.getType() : "None");
			outputCache(newCache, "Shift: " + shiftDesc, roleDetails);
		}

		return newCache;
	}

	private static synchronized TaskCache getDefaultTasks() {
		if (defaultTasks == null) {
			defaultTasks = new TaskCache();
			for(MetaTask mt : MetaTaskUtil.getNonWorkHourMetaTasks()) {
				defaultTasks.put(mt, 1D);
			}

		}
		return defaultTasks;
	}

	/**
	 * Start a new Task by first checking for pending tasks.
	 */
	@Override
	public void startNewTask() {
		// Check if there are any assigned tasks that are pending
		if (!getPendingTasks().isEmpty()) {
			MetaTask metaTask = getAPendingMetaTask();
			if (metaTask != null) {
				Task newTask = metaTask.constructInstance(person);
				startTask(newTask);
			}

			return;
		}

		super.startNewTask();
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

	@Override
	protected Task createTask(MetaTask selectedMetaTask) {
		return selectedMetaTask.constructInstance(mind.getPerson());
	}
}
