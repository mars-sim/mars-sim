/*
 * Mars Simulation Project
 * PersonTaskManager.java
 * @date 2021-12-05
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.CircadianClock;
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
	private transient CircadianClock circadian = null;

	private List<String> pendingTasks;

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
		circadian = person.getCircadianClock();

		pendingTasks = new CopyOnWriteArrayList<>();
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
			if (currentTask != null && currentTask.isEffortDriven()) {
				// why java.lang.NullPointerException at TR = 2048 ?
				energyTime *= 2D;
			}

			if (energyTime > 0D) {
				if (person.isOutside()) {
					// Take more energy to be in EVA doing work
					reduceEnergy(energyTime * 1.1);

//					if (circadian == null)
//						circadian = person.getCircadianClock();
//					// Regulate hormones
//					circadian.exercise(time);
					
				} else {
					// Expend energy based on activity.
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
							+ Math.round(probability * 10.0) / 10.0 + ").");
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
		if (!pendingTasks.isEmpty()) {
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

	/**
	 * Gets all pending tasks
	 *
	 * @return
	 */
	public List<String> getPendingTasks() {
		return pendingTasks;
	}

	/**
	 * Adds a pending task if it is not in the pendingTask list yet.
	 *
	 * @param task
	 * @return
	 */
	public boolean addAPendingTask(String task, boolean allowDuplicate) {
		if (allowDuplicate) {
			if (!pendingTasks.contains(task)) {
				logger.info(person, 20_000L, "Given a new task order of '" + task + "'.");
			}
			else {
				logger.info(person, 20_000L, "Given a duplicated task order of '" + task + "'.");
			}
			pendingTasks.add(task);
			return true;
		}
		
		if (!pendingTasks.contains(task)) {
			pendingTasks.add(task);
			logger.info(person, 20_000L, "Given a new task order of '" + task + "'.");
			return true;
		}
		
		return false;
	}

	/**
	 * Deletes a pending task
	 *
	 * @param task
	 */
	public void deleteAPendingTask(String task) {
		pendingTasks.remove(task);
		logger.info(worker, "Removed the task order of '" + task + "'.");
	}

	/**
	 * Checks if the person is currently performing this task.
	 * 
	 * @param task
	 * @return
	 */
	public boolean hasSameTask(String task) {
		if (getTaskName().equals(task))
			return true;
		
		return false;
	}
	
	/**
	 * Gets the first pending meta task in the queue
	 *
	 * @return
	 */
	private MetaTask getAPendingMetaTask() {
		if (!pendingTasks.isEmpty()) {
			String firstTask = pendingTasks.get(0);
			pendingTasks.remove(firstTask);
			return convertTask2MetaTask(firstTask);
		}
		return null;
	}

	/**
	 * Converts a task to its corresponding meta task
	 *
	 * @param a task
	 */
	private static MetaTask convertTask2MetaTask(String task) {
		return MetaTaskUtil.getMetaTask(task.replaceAll(" ","") + "Meta");
	}

	public void reinit() {
		super.reinit();

		person = mind.getPerson();
		circadian = person.getCircadianClock();

		worker = person;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {

		mind = null;
		person = null;
		circadian = null;
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
