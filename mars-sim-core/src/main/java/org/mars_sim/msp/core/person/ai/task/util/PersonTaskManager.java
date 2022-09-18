/*
 * Mars Simulation Project
 * PersonTaskManager.java
 * @date 2021-12-05
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.job.util.ShiftType;
import org.mars_sim.msp.core.person.ai.role.Role;

/**
 * The PersonTaskManager class keeps track of a person's current task and can randomly
 * assign a new task to a person based on a list of possible tasks and that
 * person's current situation.
 *
 * There is one instance of TaskManager per person.
 */
public class PersonTaskManager extends TaskManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(PersonTaskManager.class.getName());

	private static final int MAX_TASK_PROBABILITY = 35_000;
	/** A decimal number a little bigger than zero for comparing doubles. */
//	private static final double SMALL_AMOUNT = 0.001;

	// Data members
	/** The mind of the person the task manager is responsible for. */
	private Mind mind;

	private transient Person person;

	/** The CircadianClock reference */
//	private transient CircadianClock circadian = null;


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
//		circadian = person.getCircadianClock();
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
			if (energyTime > 0D && currentTask != null && currentTask.isEffortDriven()) {

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
	protected synchronized void rebuildTaskCache() {

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
		taskProbCache = new HashMap<>(mtList.size());
		totalProbCache = 0D;

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

				taskProbCache.put(mt, probability);
				totalProbCache += probability;
			}
		}

		// Output shift
		if (diagnosticFile != null) {
            Role role = person.getRole();
            String roleDetails = "Role: " + (role != null ? role.getType() : "None");
			outputCache("Shift: " + shiftDesc, roleDetails);
		}
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
//				logger.info(person, 20_000L, "Starting a task order of '" + newTask.getName() + "'.");
				startTask(newTask);
			}

			return;
		}

		super.startNewTask();
	}

	public void reinit() {
		person = mind.getPerson();
//		circadian = person.getCircadianClock();
		worker = person;
		super.reinit();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {

		mind = null;
		person = null;
//		circadian = null;
		if (taskProbCache != null) {
			taskProbCache.clear();
			taskProbCache = null;
		}
	}

	@Override
	protected Task createTask(MetaTask selectedMetaTask) {
		return selectedMetaTask.constructInstance(mind.getPerson());
	}
}
