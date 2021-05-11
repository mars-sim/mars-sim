package org.mars_sim.msp.core.person.ai.task.utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.core.tool.RandomUtil;

public abstract class TaskManager {

	protected static MarsClock marsClock;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TaskManager.class.getName());

	private static PrintWriter diagnosticFile = null;
	
	static {
		marsClock = Simulation.instance().getMasterClock().getMarsClock();
	}
	
	/**
	 * Enable the detailed diagnostics
	 */
	public static void enableDiagnostics() {
		String filename = SimulationFiles.getLogDir() + "/task-cache.txt";
		try {
			diagnosticFile = new PrintWriter(filename);
		} catch (FileNotFoundException e) {
			//logger.severe("Problem opening task file " + filename);
		}
	}

	/**The worker **/
	protected transient Worker worker;
	/** The current task the worker is doing. */
	protected transient Task currentTask;
	/** The last task the person was doing. */
	private transient Task lastTask;
	/** The TaskSchedule reference */
	protected transient TaskSchedule taskSchedule = null;
	
	/** The cache for task description. */
	protected String taskDescriptionCache = "";
	/** The cache for task phase. */
	protected String taskPhaseNameCache = "";
	
	/** The cache for msol. */
	private double msolCache = -1.0;
	/** The cache for total probability. */
	protected double totalProbCache;
	protected transient Map<MetaTask, Double> taskProbCache;

	protected TaskManager(Worker worker) {
		this.worker = worker;
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
	 * Gets the real-time task 
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
			return currentTask.getTaskName();
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
			String t = currentTask.getDescription(subTask);
			if (t != null) // || !t.equals(""))
				return t;
			else
				return "";
		} else
			return "";
	}

	public String getSubTaskDescription() {
		if (currentTask != null && currentTask.getSubTask() != null) {
			String t = currentTask.getSubTask().getDescription();
			if (t != null) // || !t.equals(""))
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
		return lastTask.getDescription();
	}

	public String getLastTaskDescription() {
		return taskDescriptionCache;
	}

	public TaskSchedule getTaskSchedule() {
		return taskSchedule;
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
	 * Ends all sub tasks
	 */
	public void endSubTask() {
		if (currentTask != null && currentTask.getSubTask() != null) {
			currentTask.getSubTask().endTask();
		}
	}

	/**
	 * Adds a task to the stack of tasks.
	 * 
	 * @param newTask the task to be added
	 */
	public void addTask(Task newTask) {

		if (hasActiveTask()) {
			// Hmm. Subtask should be controlled by Task
			throw new IllegalStateException("Already has a main task assigning");
		}
		
		lastTask = currentTask;
		currentTask = newTask;
		taskDescriptionCache = currentTask.getDescription();

		TaskPhase tp = currentTask.getPhase();
		if (tp != null)
			if (tp.getName() != null)
				taskPhaseNameCache = tp.getName();
			else
				taskPhaseNameCache = "";
		else
			taskPhaseNameCache = "";

		//person.fireUnitUpdate(UnitEventType.TASK_EVENT, newTask);
	}

	/**
	 * Sets the current task to null.
	 */
	public void clearAllTasks() {
			String lastTask = (currentTask != null ? currentTask.getDescription() : "unknown");
			endCurrentTask();
			logger.warning(worker, "Just cleared all tasks including " + lastTask
					+ " at ("
					+ Math.round(worker.getXLocation()*10.0)/10.0 + ", " 
					+ Math.round(worker.getYLocation()*10.0)/10.0 + ").");
		}

	/**
	 * Ends the current task
	 */
	public void endCurrentTask() {
		if (currentTask != null) {
			currentTask.endTask();
			currentTask.destroy();
			currentTask = null;
			//person.fireUnitUpdate(UnitEventType.TASK_EVENT);
		}
	}

	/**
	 * Clears a specific task
	 * 
	 * @param taskString
	 */
	public void clearSpecificTask(String taskString) {
	
		if (currentTask != null) {
			String taskName0 = currentTask.getClass().getSimpleName();
			if (taskName0.equalsIgnoreCase(taskString)) {
				endCurrentTask();
			}
		}
		
		else if (currentTask.getSubTask() != null) {
			String taskName1 = currentTask.getSubTask().getClass().getSimpleName();
			if (taskName1.equalsIgnoreCase(taskString)) {
				currentTask.getSubTask().endTask();
			}
		}
	
		else if (currentTask.getSubTask().getSubTask() != null) {
			String taskName2 = currentTask.getSubTask().getSubTask().getClass().getSimpleName();
			if (taskName2.equalsIgnoreCase(taskString)) {
				currentTask.getSubTask().getSubTask().endTask();
			}
		}
	}

	public void reinit() {
		if (currentTask != null)		
			currentTask.reinit();
		if (lastTask != null)
			lastTask.reinit();
	}

	/**
	 * Checks if task probability cache should be used.
	 * 
	 * @return true if cache should be used.
	 */
	protected boolean useCache() {
		double msol = marsClock.getMillisol();
		double diff = msol - msolCache;
		if (diff > 0.1D) {
			msolCache = msol;
			return false;
		}
		return true;
	}

	/**
	 * Calculates and caches the probabilities.
	 * This will NOT use the cache but assumes the callers know when a cahce can be used or not used. 
	 */
	protected abstract void rebuildTaskCache();

	/**
	 * Gets a new task for the person based on tasks available.
	 * 
	 * @return new task
	 */
	public Task getNewTask() {
		Task result = null;
		MetaTask selectedMetaTask = null;

		// If cache is not current, calculate the probabilities.
		if (!useCache()) {
			rebuildTaskCache();
			if (diagnosticFile != null) {
				outputCache();
			}
		}		

		if (totalProbCache == 0D) {
			logger.warning(worker, "No normal Tasks available");

			// Switch to loading non-work hour meta tasks since
			// leisure tasks are NOT based on needs
			List<MetaTask> list = MetaTaskUtil.getNonWorkHourMetaTasks();
			selectedMetaTask = list.get(RandomUtil.getRandomInt(list.size() - 1));
		} else if (taskProbCache != null && !taskProbCache.isEmpty()) {

			double r = RandomUtil.getRandomDouble(totalProbCache);

			// Determine which task is selected.
			Iterator<MetaTask> it = taskProbCache.keySet().iterator();
			while ((selectedMetaTask == null) && it.hasNext()) {
				MetaTask mt = it.next();
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
			logger.severe(worker, "Could not determine a new task.");
		} else {
			// Call constructInstance of the selected Meta Task to commence the ai task
			result = createTask(selectedMetaTask);
		}

		// Clear time cache.
		msolCache = -1;
		
		return result;
	}

	protected abstract Task createTask(MetaTask selectedMetaTask);

	/**
	 * This return the last calculated probability map.
	 * @return
	 */
	public Map<MetaTask, Double> getLatestTaskProbability() {
		return taskProbCache;
	}

	/**
	 * This method output the cache to a file for diagnostics
	 */
	private void outputCache() {	
		synchronized (diagnosticFile) {	
			diagnosticFile.println(MarsClockFormat.getDateTimeStamp(marsClock));
			diagnosticFile.println("Worker:" + worker.getName());
			diagnosticFile.println("Total:" + totalProbCache);
			for (Entry<MetaTask, Double> task : taskProbCache.entrySet()) {
				diagnosticFile.println(task.getKey().getName() + ":" + task.getValue());
			}
			
			diagnosticFile.println();
			diagnosticFile.flush();
		}
	}
}
