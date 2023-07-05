/*
 * Mars Simulation Project
 * TaskManager.java
 * @date 2022-06-24
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.data.SolListDataLogger;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.vehicle.Vehicle;

/*
 * The TaskManager class keeps track of a Worker's current task and can randomly
 * assign a new task based on a list of possible tasks and the current situation.
 */
public abstract class TaskManager implements Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(TaskManager.class.getName());
	
	private static final String SLEEPING = "Sleeping";
	private static final String EVA = "EVA";
	private static final String AIRLOCK = "Airlock";
	
	/*
	 * This class represents a record of a given activity (task or mission)
	 * undertaken by a person or a robot
	 */
	public final class OneActivity implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private String taskName;
		private String missionName;
		private String description;
		private String phase;
		private double startTime;


		public OneActivity(double startTime, String taskName, String description, String phase, String missionName) {
			this.taskName = taskName;
			this.description = description;
			this.startTime = startTime;
			this.phase = phase;
		}

		/**
		 * Gets the start time of the task.
		 * 
		 * @return start time
		 */
		public double getStartTime() {
			return startTime;
		}

		/**
		 * Gets the task name.
		 * 
		 * @return task name
		 */
		public String getTaskName() {
			return taskName;
		}

		/**
		 * Gets the description what the actor is doing.
		 * 
		 * @return description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Gets the task phase.
		 * 
		 * @return task phase
		 */
		public String getPhase() {
			return phase;
		}

		public String getMission() {
			return missionName;
		}
	}

	/** Number of days to record Tack Activities. */
	public static final int NUM_SOLS = 7;
	
	protected static MarsClock marsClock;

	private static PrintWriter diagnosticFile = null;

	/**
	 * Enable the detailed diagnostics
	 * @throws FileNotFoundException 
	 */
	public static void setDiagnostics(boolean diagnostics) throws FileNotFoundException {
		if (diagnostics) {
			if (diagnosticFile == null) {
				String filename = SimulationFiles.getLogDir() + "/task-cache.txt";
				diagnosticFile  = new PrintWriter(filename);
				logger.config("Diagnostics enabled to " + filename);
			}
		}
		else if (diagnosticFile != null){
			diagnosticFile.close();
			diagnosticFile = null;
		}
	}

	/**The worker **/
	protected transient Unit worker;
	/** The current task the worker is doing. */
	protected Task currentTask;
	/** The last task the person was doing. */
	private Task lastTask;

	private transient TaskCache taskProbCache = null;

	// Data members
	/** The timestamp (with 2 decimal place) of the task to be recorded. */
	private double now = -1;


	/** The history of tasks. */
	private SolListDataLogger<OneActivity> allActivities;
	/** The last activity. */
	private OneActivity lastActivity = null;
	/** The list of pending of tasks. */
	private List<TaskJob> pendingTasks;

	// THese are for metric capture
	private static int totalRebuild = 0;
	private static int reuseRebuild = 0;
	private static Map<Settlement,MarsClock> metrics;
	

	protected TaskManager(Unit worker) {
		this.worker = worker;
		allActivities = new SolListDataLogger<>(NUM_SOLS);
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

	public String getSubTaskName() {
		if (currentTask != null && currentTask.getSubTask() != null) {
			return currentTask.getSubTask().getName();
		} else {
			return "";
		}
	}

	public String getSubTask2Name() {
		Task task = getRealTask();
		if (task != null) {
			return task.getName();
		} else {
			return "";
		}
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

	public String getSubTaskDescription() {
		if (currentTask != null && currentTask.getSubTask() != null) {
			String t = currentTask.getSubTask().getDescription();
			if (t != null && !t.equals(""))
				return t;
			else
				return "";
		} else
			return "";
	}

	public String getSubTask2Description() {
		if (currentTask != null && currentTask.getSubTask() != null
				&& currentTask.getSubTask().getSubTask() != null) {
			String t = currentTask.getSubTask().getSubTask().getDescription();
			if (t != null) // || !t.equals(""))
				return t;
			else
				return "";
		} else
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

	public TaskPhase getSubTaskPhase() {
		if (currentTask != null && currentTask.getSubTask() != null) {
			return currentTask.getSubTask().getPhase();
		} else {
			return null;
		}
	}

	public TaskPhase getSubTask2Phase() {
		if (currentTask != null && currentTask.getSubTask() != null
				&& currentTask.getSubTask().getSubTask() != null) {
			return currentTask.getSubTask().getSubTask().getPhase();
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
	 * Ends all sub tasks.
	 */
	public void endSubTask() {
		if (currentTask != null && currentTask.getSubTask() != null) {
			currentTask.getSubTask().endTask();
		}
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
			worker.fireUnitUpdate(UnitEventType.TASK_EVENT);
		}
	}

	/**
	 * Clears a specific task.
	 * 
	 * @param taskString
	 */
	public void clearSpecificTask(String taskString) {
		
		Task subTask1 = currentTask.getSubTask();
		
		if (currentTask != null && subTask1 != null) {
			
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
	 * 
	 * This will NOT use the cache but assumes the callers know when a cahce can be used or not used. 
	 */
	protected abstract TaskCache rebuildTaskCache();

	
	/**
	 * Simple method to capture some stats/metrics on cache rebuilds.
	 */
	private void captureStats() {
		Settlement scope = worker.getAssociatedSettlement();
		if (metrics == null) {
			metrics = new HashMap<>();
		}
		synchronized(metrics) {
			MarsClock lastRebuild = metrics.get(scope);
			totalRebuild++;

			// If time has not changed since last rebuild; count as a reuse
			if ((lastRebuild != null) && lastRebuild.equals(marsClock)) {
				reuseRebuild++;
			}
			else {
				metrics.put(scope, new MarsClock(marsClock));
			}

			// LImit output
			if ((totalRebuild % 1000) == 0) {
				String message = String.format("---- Cache Reuse stats %d/%d (%d%%)",
									reuseRebuild, totalRebuild, (100*reuseRebuild/totalRebuild));
				logger.info(message);
			}
		}
	}

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
	public TaskCache getLatestTaskProbability() {
		return taskProbCache;
	}
	
	/**
	 * Outputs the cache to a file for diagnostics.
	 * 
	 * @param extras Extra details about Task
	 */
	private void outputCache(TaskCache current) {	
		synchronized (diagnosticFile) {	
			diagnosticFile.println(MarsClockFormat.getDateTimeStamp(marsClock));
			diagnosticFile.println("Worker:" + worker.getName());
			diagnosticFile.println(current.getContext());				
			diagnosticFile.println("Total:" + current.getTotal());
			for (TaskJob task : taskProbCache.getTasks()) {
				diagnosticFile.println(task.getDescription() + ":" + task.getScore());
			}
			
			diagnosticFile.println();
			diagnosticFile.flush();
		}
	}

	/**
	 * Time has advanced on. This has to carry over the last Activity of yesterday into today.
	 */
	public boolean timePassing(ClockPulse pulse) {
		// Create a timestamp with 2 decimal place
		now = Math.round(pulse.getMarsTime().getMillisol() * 100.0)/100.0;
		
		// New day so the Activity at the end of yesterday has to be carried over to the 1st of today
		if (pulse.isNewSol() && lastActivity != null) {
			// Save the first activity at the start of the day
			// Note: it could be the previous activity from previous day
			OneActivity firstActivity = new OneActivity(0,
											lastActivity.getTaskName(),
											lastActivity.getDescription(),
											lastActivity.getPhase(),
											lastActivity.getMission());
			allActivities.addData(firstActivity);
		}
		return true;
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
	 * Record an activity on the Task Activity log
	 */
	public void recordActivity(String newTask, String newPhase, String newDescription, Mission mission) {
		// Also compare to the last activity
		if (lastActivity == null 
				|| !newDescription.equals(lastActivity.description)
				|| !newPhase.equals(lastActivity.phase)) {
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

			OneActivity newActivity = new OneActivity(now, 
												newTask + location,
												newDescription,
												newPhase, 
												missionName);

			allActivities.addData(newActivity);
			lastActivity = newActivity;
		}
	}
	
	/**
	 * Gets the today's activities.
	 * 
	 * @return a list of today's activities
	 */
	public List<OneActivity> getTodayActivities() {
		return allActivities.getTodayData();
	}
	
	/**
	 * Gets all activities of all days a person.
	 * 
	 * @return all activity schedules
	 */
	public Map<Integer, List<OneActivity>> getAllActivities() {
		return allActivities.getHistory();
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
	 * Checks if the person or robot is walking through a given building.
	 * 
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
	 * Starts a new task for the worker based on tasks available at their location.
	 * Uses the task probability cache. If a task is found; then it is assigned
	 * to the manager to start working.
	 */
	public void startNewTask() {
		Task selectedTask = null;
		TaskJob selectedJob = null;

		// If cache is not current, calculate the probabilities. If it is a static cache, i.e. no createdOn then
		// ignore the cache
		if ((taskProbCache == null)  || (taskProbCache.getCreatedOn() == null) || taskProbCache.getTasks().isEmpty()
				|| (marsClock.getMillisol() != taskProbCache.getCreatedOn().getMillisol())) {
			taskProbCache = rebuildTaskCache();
			
			// Comment out to stop capturing stats
			//captureStats();
			
			// Output shift
			if (diagnosticFile != null) {
				outputCache(taskProbCache);
			}
		}

		if (taskProbCache.getTasks().isEmpty()) { 
			// Should never happen since TaskManagers have to return a populated list
			// with doable defaults if needed
			logger.severe(worker, "No normal Tasks available in " + taskProbCache.getContext());
		}
		else {
			selectedJob = taskProbCache.getRandomSelection();

			// Call constructInstance of the selected Meta Task to commence the ai task
			selectedTask = createTask(selectedJob);

			// Start this new task
			replaceTask(selectedTask);
		}
	}

	/**
	 * Checks to see if it's okay to replace a task.
	 * 
	 * @param newTask the task to be added
	 */
	public boolean checkAndReplaceTask(Task newTask) {
		
		if (newTask == null) {
			return false;
		}
		
		if (hasActiveTask()) {
			String currentDes = currentTask.getDescription();
			String taskName = currentTask.getName(); //
			
			// Note: make sure robot's 'Sleep Mode' won't return false
			if (currentDes.contains(SLEEPING)
				|| currentDes.contains(EVA)
				|| taskName.contains(AIRLOCK))
				return false;
			
			if (newTask.getDescription().equalsIgnoreCase(currentDes))
				return false;	
		}
		
		replaceTask(newTask);
		
		return true;
	}
	
	/**
	 * Replaces old task with a new task.
	 * 
	 * @param newTask
	 */
	public void replaceTask(Task newTask) {
		if (newTask != null) {
			// Backup the current task as last task
			lastTask = currentTask;
			
			// Inform that the current task will be termined
			if ((currentTask != null) && !currentTask.isDone()) {
				String des = currentTask.getDescription();
	
				logger.info(worker, 20_000, "Quitting '" + des + "' to start new Task '"
							+ newTask.getDescription() + "'.");
				
				currentTask.endTask();
			}
			
			// Make the new task as the current task
			currentTask = newTask;
			
			// Send out the task event
			worker.fireUnitUpdate(UnitEventType.TASK_EVENT, newTask);
		}
	}
	
	/**
	 * Gets all pending tasks.
	 *
	 * @return
	 */
	public List<TaskJob> getPendingTasks() {
		return pendingTasks;
	}
	
	/**
	 * Adds a pending task if it is not in the pendingTask list yet.
	 *
	 * @param task
	 * @param countDownTime
	 * @param duration
	 * @return
	 */
	public boolean addAPendingTask(String taskName, int countDownTime, int duration) {
		return addAPendingTask(taskName, false, countDownTime, duration);
	}
	
	/**
	 * Adds a pending task if it is not in the pendingTask list yet.
	 *
	 * @param task				the pending task 
	 * @param allowDuplicate	Can this pending task be repeated in the queue
	 * @param countDownTime 	the count down time for executing the new task
	 * @param duration 			the duration of the new task
	 * @return
	 */
	public boolean addAPendingTask(String taskName, boolean allowDuplicate, int countDownTime, int duration) {
		
		if (countDownTime >= 0) {
			double oldDuration = currentTask.getDuration();
			double newDuration = countDownTime + currentTask.getTimeCompleted();
			currentTask.setDuration(newDuration);
			
			logger.info(worker, "Updating current task '" + currentTask.getName() 
				+ "''s duration: " + oldDuration + " -> " + Math.round(newDuration * 10.0)/10.0 + "'.");
		}
		
		// Potential ClassCast but only temp. measure
		FactoryMetaTask mt = (FactoryMetaTask) MetaTaskUtil.getMetaTask(taskName);
		if (mt == null) {
			logger.warning(worker, "Cannot find pending task '" + taskName + "'.");
			return false;
		}

		BasicTaskJob task = new BasicTaskJob(mt, 0, duration);
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
		if (allowDuplicate || !pendingTasks.contains(task)) {
			pendingTasks.add(task);
			logger.info(worker, 20_000L, "Given a pending/appointed task '" + task.getDescription() + "'.");
			return true;
		}
		return false;
	}

	/**
	 * Deletes a pending task.
	 *
	 * @param task
	 */
	public void deleteAPendingTask(TaskJob task) {
		pendingTasks.remove(task);
		logger.info(worker, "Removed the pending/appointed task '" + task + "'.");
	}

	/**
	 * Gets the first pending meta task in the queue.
	 *
	 * @return
	 */
	protected TaskJob getPendingTask() {
		if (!pendingTasks.isEmpty()) {
			TaskJob firstTask = pendingTasks.get(0);
			pendingTasks.remove(firstTask);
			return firstTask;
		}
		return null;
	}

	/**
	 * Checks if the worker is currently performing this task.
	 * 
	 * @param task
	 * @return
	 */
	public boolean hasSameTask(String task) {
		if (getTaskName().equalsIgnoreCase(task))
			return true;
		
		return false;
	}
	
	/**
	 * Reloads instances after loading from a saved sim.
	 * 
	 * @param sim
	 */
	public static void initializeInstances(Simulation sim, SimulationConfig conf) {

		MetaTaskUtil.initialiseInstances(sim);
		Task.initializeInstances(sim, conf.getPersonConfig());
	}
	

	/**
	 * Re-initializes instances when loading from a saved sim
	 */
	public void reinit() {
		if (currentTask != null)		
			currentTask.reinit();
		if (lastTask != null)
			lastTask.reinit();
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
		lastActivity = null;
		pendingTasks.clear();
		pendingTasks = null;
		metrics.clear();
		metrics = null;
	}
}
