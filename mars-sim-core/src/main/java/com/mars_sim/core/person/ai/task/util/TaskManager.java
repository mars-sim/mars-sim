/*
 * Mars Simulation Project
 * TaskManager.java
 * @date 2022-06-24
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.data.History;
import com.mars_sim.core.data.RatingLog;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.CacheCreator;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.science.task.RespondToStudyInvitation;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.vehicle.Vehicle;

/*
 * The TaskManager class keeps track of a Worker's current task and can randomly
 * assign a new task based on a list of possible tasks and the current situation.
 */
public abstract class TaskManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(TaskManager.class.getName());

	/** Number of days to record Tack Activities. */	
	private static MasterClock master;

	/**The worker **/
	private transient Worker worker;
	/** The current task the worker is doing. */
	private Task currentTask;
	private RatingScore currentScore;

	/** The last task the person was doing. */
	private Task lastTask;

	private transient CacheCreator<TaskJob> taskProbCache = null;


	/** The history of tasks. */
	private History<OneActivity> allActivities;
	/** The list of pending of tasks. */
	private List<PendingTask> pendingTasks;
	
	/**
	 * Constructor.
	 * 
	 * @param worker
	 */
	protected TaskManager(Worker worker) {
		this.worker = worker;
		allActivities = new History<>(150);   // Equivalent of 3 days
		pendingTasks = new CopyOnWriteArrayList<>();
	}

	/**
	 * Returns true if person has a task (may be inactive).
	 * 
	 * @return true if person has a task
	 */
	public boolean hasTask() {
		return currentTask != null;
	}

	/**
	 * Returns the name of the current task for UI purposes. Returns a blank string
	 * if there is no current task.
	 * 
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
	 * Gets the stack of Tasks that are active. The 1st entry in the is the top level task
	 * whilst the last entry is the one actually active.
	 * @return Top down list of Tasks
	 */
	public List<Task> getTaskStack() {
		List<Task> stack = new ArrayList<>();

		if (currentTask != null) {
			currentTask.buildStack(stack);
		}
		return stack;
	}

	/**
	 * Gets the score of the current Task if it was chosen at random. 
	 * 
	 * @return Will be null if task has been preselected.
	 */
    public RatingScore getScore() {
        return currentScore;
    }

	/**
	 * Gets the bottom-most real-time task. 
	 * 
	 * @return
	 */
	public Task getRealTask() {
		if (currentTask == null) {
			return null;
		}
		
		Task subtask1 = currentTask.getSubTask();
		if (subtask1 == null) {
			return currentTask;
		}
		
		if (subtask1.getSubTask() == null) {
			return subtask1;
		}
		
		Task subtask2 = subtask1.getSubTask();
		if (subtask2 == null) {
			return subtask1;
		}
		
		if (subtask2.getSubTask() == null) {
			return subtask2;
		}
		
		return subtask2.getSubTask();
	}

	/**
	 * Returns the name of the current task for UI purposes. Returns a blank string
	 * if there is no current task.
	 * 
	 * @return name of the current task
	 */
	public String getTaskClassName() {
		if (currentTask != null) {
			return currentTask.getTaskSimpleName();
		} else {
			return "";
		}
	}

	/**
	 * Returns a description of current task for UI purposes. Returns a blank string
	 * if there is no current task.
	 * 
	 * @return a description of the current task
	 */
	public String getTaskDescription(boolean subTask) {
		if (currentTask != null) {
			return currentTask.getDescription(subTask);
		}
		return "";
	}

	/**
	 * Returns the current task phase if there is one. Returns null if current task
	 * has no phase. Returns null if there is no current task.
	 * 
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
	 * Returns the current task. Return null if there is no current task.
	 * 
	 * @return the current task
	 */
	public Task getTask() {
		return currentTask;
	}

	public String getLastTaskName() {
		return (lastTask != null ? lastTask.getTaskSimpleName() : "");
	}

	public String getLastTaskDescription() {
		return (lastTask != null ? lastTask.getDescription() : "");
	}

	/**
	 * Returns true if person has an active task.
	 * 
	 * @return true if person has an active task
	 */
	public boolean hasActiveTask() {
		return (currentTask != null && !currentTask.isDone());
	}

	/**
	 * Sets the current task to null.
	 * 
	 * @param reason May be used in an override method
	 */
	public void clearAllTasks(String reason) {
		endCurrentTask();
	}

	/**
	 * Ends the current task.
	 */
	public void endCurrentTask() {
		if (currentTask != null) {
			currentTask.endTask();
			currentTask = null;
			worker.fireUnitUpdate(UnitEventType.TASK_EVENT, null);
		}
	}

	/**
	 * Clears a specific task.
	 * 
	 * @param taskString
	 */
	public void clearSpecificTask(String taskString) {
		
		Task subTask1 = currentTask.getSubTask();
		
		if (subTask1 != null) {
			
			Task subTask2 = subTask1.getSubTask();
			
			if (subTask2 != null) {
				String taskName2 = subTask2.getClass().getSimpleName();
				if (taskName2.equalsIgnoreCase(taskString)) {
					subTask2.endTask();
				}
			}
			
			else {				
				String taskName1 = subTask1.getClass().getSimpleName();
				if (taskName1.equalsIgnoreCase(taskString)) {
					subTask1.endTask();
				}
			}
		}
		
		else {
			String taskName0 = currentTask.getClass().getSimpleName();
			if (taskName0.equalsIgnoreCase(taskString)) {
				endCurrentTask();
			}
		}
	}


	/**
	 * Calculates and caches the probabilities.
	 * Note: This will NOT use the cache but assumes the callers know when a cache can be used or not used. 
	 * 
	 * @param now The current MarsTime
	 */
	protected abstract CacheCreator<TaskJob> rebuildTaskCache(MarsTime now);

	/**
	 * Constructs a new Task of the specified type.
	 * 
	 * @param selectedMetaTask Type of task to create.
	 * @return New Task.
	 */
	protected abstract Task createTask(TaskJob selectedMetaTask);

	/**
	 * Returns the last calculated probability map.
	 *
	 * @return
	 */
	public CacheCreator<TaskJob> getLatestTaskProbability() {
		return taskProbCache;
	}
	
	/**
	 * Records a task onto the schedule.
	 * 
	 * @param changed The active task.
	 * @param mission Associated mission.
	 */
	void recordTask(Task changed, Mission mission) {
		String newDescription = changed.getDescription();
		String newPhase = "";
		if (changed.getPhase() != null)
			newPhase = changed.getPhase().getName();
		
		// If there is no details; then skip it
		if (!newDescription.equals("") && !newPhase.equals("")) {
			String newTask = changed.getName(false);

			recordActivity(newTask, newPhase, newDescription, mission);
		}
	}

	/**
	 * Record an activity on the Task Activity log.
	 */
	public void recordActivity(String newTask, String newPhase, String newDescription, Mission mission) {
		String missionName = (mission != null ? mission.getName() : null);
		
		// This is temp.
		String location = " in";
		if (worker.isInVehicle()) {
			location += " V";
		}
		if (worker.isInSettlement()) {
			location += " S";
		}
		if (worker.isOutside()) {
			location += " O";
		}

		OneActivity newActivity = new OneActivity(
											newTask + location,
											newDescription,
											newPhase, 
											missionName);

		allActivities.add(newActivity);
	}
	
	
	/**
	 * Gets all activities of all days a person.
	 * 
	 * @return all activity schedules
	 */
	public History<OneActivity> getAllActivities() {
		return allActivities;
	}
	
	/**
	 * Checks if the person or robot is walking through a given vehicle.
	 * 
	 * @param vehicle the vehicle.
	 * @return true if walking through vehicle.
	 */
	public boolean isWalkingThroughVehicle(Vehicle vehicle) {
	
	    boolean result = false;
	
	    Task task = currentTask;
	    while ((task != null) && !result) {
	        if ((task instanceof Walk walkTask) && walkTask.isWalkingThroughVehicle(vehicle)) {
	            result = true;
	        }
	        task = task.getSubTask();
	    }
	
	    return result;
	}

	/**
	 * Checks if the person or robot is walking through a given building.
	 * 
	 * @param building the building.
	 * @return true if walking through building.
	 */
	public boolean isWalkingThroughBuilding(Building building) {
	
		boolean result = false;
	
		Task task = currentTask;
		while ((task != null) && !result) {
			if ((task instanceof Walk walkTask) && walkTask.isWalkingThroughBuilding(building)) {
				result = true;
			}
			task = task.getSubTask();
		}
	
		return result;
	}
	
	/**
	 * Checks if any pending task can be processed.
	 * 
	 * @return Whether a task was started
	 */
	private boolean processPendingTask() {
		// Check if there are any assigned tasks that are pending
		// and Worker can accept a change
		// and the current Task can be interrupted
		if (!pendingTasks.isEmpty() && isPendingPossible()
			&& ((currentTask == null) || currentTask.isInterruptable())) {
			PendingTask firstTask = pendingTasks.remove(0);
			TaskJob job = firstTask.job();
			Task newTask = createTask(job);

			if (newTask == null) {
				logger.severe(worker, 30_000, "The Pending task '" + job.getName() + "' could not be created.");				
			}
			
			else if (newTask.isDone()) {
				logger.warning(worker, 30_000, "The Pending task '" + job.getName() + "' was no longer possible.");				
			}
			// Potential here to loose started Task if new pending matches the existing
			// check should be done against the TaskJob and delay the Task creation until the name check
			else if ((currentTask == null) || !hasSameTask(newTask.getName())) {		
				// Note: this is the only eligible condition for replacing the
				// current task with the new task
				replaceTask(newTask);
				currentScore = null; // Clear score to show it was directly assigned
				return true;
			}
		}
		return false;
	}

	/**
	 * Can this worker start a pending task?
	 * 
	 * @return
	 */
	protected abstract boolean isPendingPossible();

	/**
	 * Starts a new task for the worker based on tasks available at their location.
	 * Uses the task probability cache. If a task is found; then it is assigned
	 * to the manager to start working.
	 */
	public void startNewTask() {
		if (processPendingTask()) {
			return;
		}

		Task selectedTask = null;
		TaskJob selectedJob = null;

		// If cache is not current, calculate the probabilities. If it is a static cache, i.e. no createdOn then
		// ignore the cache
		MarsTime now = master.getMarsTime();
		if ((taskProbCache == null)  || (taskProbCache.getCreatedTime() == null) || taskProbCache.getCache().isEmpty()
				|| (now.getMillisol() != taskProbCache.getCreatedTime().getMillisol())) {
			taskProbCache = rebuildTaskCache(now);
		}

		if (taskProbCache.getCache().isEmpty()) {
			// Should never happen since TaskManagers have to return a populated list
			// with doable defaults if needed
			logger.severe(worker, "No normal Tasks available in " + taskProbCache.getContext());
		}
		else {
			selectedJob = taskProbCache.getRandomSelection();

			// Call constructInstance of the selected Meta Task to commence the ai task
			selectedTask = createTask(selectedJob);
			
			if (taskProbCache.getCreatedTime() != null) {
				// If it is a cache made dynamically then log it
				RatingLog.logSelectedRating(getDiagnosticsModule(), worker.getName(), selectedJob,
									taskProbCache.getCache());
			}

			// Start this newly selected task
			replaceTask(selectedTask);
			currentScore = selectedJob.getScore();
		}
	}

	/**
	 * The diagnostics module name to used in any output
	 * @return
	 */
	protected abstract String getDiagnosticsModule();

	/**
	 * Performs the current task for a given amount of time.
	 *
	 * @param time       amount of time to perform the action
	 * @return remaining time.
	 */
	public double executeTask(double time) {
		double remainingTime = 0D;
		
		if (currentTask != null) {
			remainingTime = currentTask.performTask(time);
		}

		return remainingTime;

	}
	
	/**
	 * Checks to see if it's okay to replace a task.
	 * 
	 * @param newTask the task to be executed
	 */
	public boolean checkReplaceTask(Task newTask) {
		return checkReplaceTask(newTask, false); 
	}
	
	/**
	 * Checks to see if it's okay to replace a task.
	 * 
	 * @param newTask the task to be executed
	 * @param allowSameTask is it allowed to execute the same task as previous
	 */
	public boolean checkReplaceTask(Task newTask, boolean allowSameTask) {
		
		if (newTask == null) {
			return false;
		}
		
		if (hasActiveTask()) {
			
			String currentDes = currentTask.getDescription();

			if (!currentTask.isInterruptable())
				return false;
			
			if (!allowSameTask) {
				if (newTask.getDescription().equalsIgnoreCase(currentDes))
					return false;	
			
		
				if (newTask.getName().equals(getTaskName())) {
					return false;
				}
			}
		}
		
		// Records current task as last task and replaces it with a new task.
		replaceTask(newTask);
		
		return true;
	}
	
	/**
	 * Records the current task as last task and replaces it with a new task.
	 * 
	 * @param newTask
	 */
	public void replaceTask(Task newTask) {
		if ((newTask == null) || newTask.equals(currentTask)) {
			return;
		}
			
		// Backup the current task as last task
		if (currentTask != null)
			lastTask = currentTask;
		
		// Inform that the current task will be terminated
		if (hasActiveTask()) {
			String des = currentTask.getDescription();
			
			currentTask.endTask();
			
			logger.info(worker, 20_000, "Quit '" + des + "' and replace with the new task of '"
						+ newTask.getName() + "'.");
		}
		
		// Make the new task as the current task
		currentTask = newTask;
		currentScore = null;
		
		// Send out the task event
		worker.fireUnitUpdate(UnitEventType.TASK_EVENT, newTask);
	}
	
	/**
	 * Gets all pending tasks.
	 *
	 * @return
	 */
	public List<PendingTask> getPendingTasks() {
		return pendingTasks;
	}
	
	/**
	 * Adds a pending task if it is not in the pendingTask list yet.
	 *
	 * @param task
	 * @return
	 */
	public boolean addPendingTask(String taskName) {
		return addPendingTask(taskName, false, 0, 0);
	}
	
	/**
	 * Adds a pending task if it is not in the pendingTask list yet.
	 *
	 * @param task				the pending task 
	 * @param allowDuplicate	Can this pending task be repeated in the queue
	 * @param countDownTime 	the count down time for executing the new pending task
	 * @param duration 			the duration of the new task
	 * @return
	 */
	public boolean addPendingTask(String taskName, boolean allowDuplicate, int countDownTime, int duration) {
		
		// Shorten the remaining duration of the current task so as to execute the pending time right after the countDownTime in a timely manner
		if (currentTask != null && countDownTime > 0) {
			double oldDuration = currentTask.getDuration();
			double newDuration = countDownTime + currentTask.getTimeCompleted();
			
			if (newDuration < oldDuration) {
				currentTask.setDuration(newDuration);
				logger.info(worker, "Updating current task '" + currentTask.getName() 
					+ "'  duration: " + Math.round(oldDuration * 10.0)/10.0 + " -> " + Math.round(newDuration * 10.0)/10.0 + ".");
			}
		}
		
		// Potential ClassCast but only temp. measure
		FactoryMetaTask mt = (FactoryMetaTask) MetaTaskUtil.getMetaTask(taskName);
		if (mt == null) {
			logger.warning(worker, "Cannot find pending task '" + taskName + "'.");
			return false;
		}

		BasicTaskJob task = new BasicTaskJob(mt, RatingScore.ZERO_RATING);
		return addPendingTask(task, allowDuplicate);
	}
	
	/**
	 * Adds a pending task if it is not in the pendingTask list yet.
	 *
	 * @param task
	 * @param allowDuplicate
	 * @return
	 */
	public boolean addPendingTask(TaskJob task, boolean allowDuplicate) {
		boolean addTask = true;

		// Duplicate need a check on the actual Task
		if (!allowDuplicate) {
			// Add the task if this TaskJob does not appear in the List
			addTask = (pendingTasks.stream().filter(s -> s.job().equals(task)).count() == 0);
		}

		// Add it
		if (addTask) {
			addTask = pendingTasks.add(new PendingTask(master.getMarsTime(), task));
		}
		return addTask;
	}

	/**
	 * Removes a pending task in the queue.
	 *
	 * @return
	 */
	public boolean removePendingTask(PendingTask n) {
		boolean success = pendingTasks.remove(n);
		if (success)
			logger.info(worker, "Successfully removed the pending task '" + n.job().getName() + "'.");
		else
			logger.warning(worker, "Failed to remove the pending task '" + n.job().getName() + "'.");
		return success;
	}
	
	/**
	 * Checks if the worker is currently performing this task.
	 * 
	 * @param task
	 * @return
	 */
	public boolean hasSameTask(String task) {
		return getTaskName().equalsIgnoreCase(task);
	}
	
	/**
	 * Reloads instances after loading from a saved sim.
	 * 
	 * @param sim
	 */
	public static void initializeInstances(Simulation sim, SimulationConfig conf) {

		MetaTaskUtil.initialiseInstances(sim);
		Task.initializeInstances(sim, conf.getPersonConfig());
		RespondToStudyInvitation.initialiseInstances(conf.getScienceConfig());
		master = sim.getMasterClock();
	}
	

	/**
	 * Re-initializes instances when loading from a saved sim
	 */
	protected void reinit(Worker worker) {
		if (currentTask != null)		
			currentTask.reinit();
		if (lastTask != null)
			lastTask.reinit();
		this.worker = worker;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		worker = null;
		currentTask = null;
		lastTask = null;
		taskProbCache = null;
		allActivities = null;
		pendingTasks.clear();
		pendingTasks = null;
	}
}
