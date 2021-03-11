/**
 * Mars Simulation Project
 * TaskManager.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The TaskManager class keeps track of a person's current task and can randomly
 * assign a new task to a person based on a list of possible tasks and that
 * person's current situation.
 *
 * There is one instance of TaskManager per person.
 */
public class TaskManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(TaskManager.class.getName());

	private static String loggerName = logger.getName();

	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

	private static String EVA = "eva";
	private static String DIG = "dig";
	private static String EXPLORE_SITE = "exploresite";
	private static String SALVAGE_BUILDING = "salvagebuilding";
	private static String WALK_OUTSIDE = "walkoutside";
	private static String MINE_SITE = "minesite";
	private static String COLLECT = "collect";
	private static String FIELDWORK = "fieldwork";
	private static String WALKING = "Walking";
	private static String WALK = "Walk";
	
//	private static final String WALK = "walk";

	private static final int MAX_TASK_PROBABILITY = 35_000;
	/** A decimal number a little bigger than zero for comparing doubles. */
//	private static final double SMALL_AMOUNT = 0.001;
	
	// Data members
	/** The cache for work shift. */
	private int shiftCache;
	/** The cache for msol. */
	private double msolCache = -1.0;
	/** The cache for total probability. */
	private double totalProbCache;
	/** The cache for task name. */
//	private String taskNameCache = "";
	/** The cache for task description. */
	private String taskDescriptionCache = "";
	/** The cache for task phase. */
	private String taskPhaseNameCache = "";
	/** The cache for mission name. */
	private String missionNameCache = "";
	
	/** The current task the person is doing. */
	private transient Task currentTask;
	/** The last task the person was doing. */
	private transient Task lastTask;
	
	/** The mind of the person the task manager is responsible for. */
	private Mind mind;
	
	/** The person instance. */
	private transient Person person = null;
	/** The PhysicalCondition reference */ 
	private transient PhysicalCondition health = null;
	/** The CircadianClock reference */ 
	private transient CircadianClock circadian = null;
	/** The TaskSchedule reference */ 
	private transient TaskSchedule taskSchedule = null;

	private transient Map<MetaTask, Double> taskProbCache;
	private transient List<MetaTask> mtListCache;

	private List<String> pendingTasks;

	private static PrintWriter diagnosticFile = null;
	
	private static MarsClock marsClock;
	private static MissionManager missionManager;

	static {
		Simulation sim = Simulation.instance();
		missionManager = sim.getMissionManager();
		marsClock = sim.getMasterClock().getMarsClock();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param mind the mind that uses this task manager.
	 */
	public TaskManager(Mind mind) {
		// Initialize data members
		this.mind = mind;

		person = mind.getPerson();
		circadian = person.getCircadianClock();
		health = person.getPhysicalCondition();

		currentTask = null;

		// Initialize cache values.
		taskProbCache = new ConcurrentHashMap<MetaTask, Double>();
		totalProbCache = 0D;
		
		pendingTasks = new CopyOnWriteArrayList<>();
	}

	/**
	 * Initializes tash schedule instance
	 */
	public void initialize() {
		taskSchedule = person.getTaskSchedule();
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
		
//		if (currentTask != null && currentTask.getSubTask() != null
//				&& currentTask.getSubTask().getSubTask() != null) {
//			return currentTask.getSubTask().getSubTask().getName();
//		} else {
//			return "";
//		}
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
	 * Returns the task name of the current or last task (one without the word
	 * "walk" in it).
	 * 
	 * @return task name
	 */
	public String getFilteredTaskName() {
		return getTaskName();
//		String s = getTaskName();
//		if (s.toLowerCase().contains(WALK)) {
//			// check if the last task is walking related
//			if (lastTask != null) {
//				s = lastTask.getName();
//				if (lastTask != null && !s.toLowerCase().contains(WALK)) {
//					return s;
//				} else
//					return "";
//			} else
//				return "";
//		} else
//			return s;
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
				return "<no desc>";
		} else
			return "<no Task>";
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

	/**
	 * Sets the current task to null.
	 */
	public void clearAllTasks() {
//		endSubTask();
		String lastTask = (currentTask != null ? currentTask.getDescription() : "unknown");
		endCurrentTask();
		LogConsolidated.log(logger, Level.WARNING, 4_000, sourceName, 
				"[" + person.getLocale() + "] "
				+ person.getName() 
				+ " just cleared all tasks whilst doing " + lastTask + " at ("
				+ Math.round(person.getXLocation()*10.0)/10.0 + ", " 
				+ Math.round(person.getYLocation()*10.0)/10.0 + ").");
	}

	/**
	 * Ends the current task
	 */
	public void endCurrentTask() {
		if (currentTask != null) {
			currentTask.endTask();
			currentTask.destroy();
			currentTask = null;
			person.fireUnitUpdate(UnitEventType.TASK_EVENT);
		}
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
	
	
	public boolean isEVATask(String taskName) {
		String n = taskName.toLowerCase();
		return (n.contains(EVA) || n.contains(DIG)
				|| n.contains(EXPLORE_SITE) || n.contains(SALVAGE_BUILDING)
				|| n.contains(WALK_OUTSIDE) || n.contains(MINE_SITE)
				|| n.contains(COLLECT) || n.contains(FIELDWORK));
	}

	/**
	 * Filters task for recording 
	 * 
	 * @param time
	 */
	public void recordFilterTask(double time) {
		Task task = getRealTask();
		if (task == null)
			return;

		String taskDescription = task.getDescription();
		String taskName = task.getTaskName();
		String taskPhaseName = "";
		String missionName = "";
		
		if (missionManager.getMission(person) != null)
			missionName = missionManager.getMission(person).toString();

		if (!taskName.equals("") && !taskDescription.equals("")
				&& !taskName.contains(WALK)) {

			// TODO: is there a better place to track EVA time ?
			if (isEVATask(taskName)) {
				person.addEVATime(taskName, time);
			}
			
			if (!taskDescription.equals(taskDescriptionCache)
					|| !taskPhaseName.equals(taskPhaseNameCache)
					|| !missionName.equals(missionNameCache)) {

				if (task.getPhase() != null) {
					taskPhaseName = task.getPhase().getName();
				}	

				taskSchedule.recordTask(taskName, taskDescription, taskPhaseName, missionName);
				taskPhaseNameCache = taskPhaseName;
				taskDescriptionCache = taskDescription;
				missionNameCache = missionName;
			}
		}
	}

	/**
	 * Adds a task to the stack of tasks.
	 * 
	 * @param newTask the task to be added
	 * @param isSubTask adds this newTask as a subtask if possible
	 */
	public void addTask(Task newTask, boolean isSubTask) {

		if (hasActiveTask() && isSubTask) {
			currentTask.addSubTask(newTask);
//			if (!currentTask.getTaskName().equals(newTask.getTaskName())) {
//				if (currentTask.getSubTask() != null 
//						&& !currentTask.getSubTask().getTaskName().equals(newTask.getTaskName())) {
//					currentTask.addSubTask(newTask);
//				}
//			}		
			
		} else {
			lastTask = currentTask;
			currentTask = newTask;
//			taskNameCache = currentTask.getTaskName();
			taskDescriptionCache = currentTask.getDescription();

			TaskPhase tp = currentTask.getPhase();
			if (tp != null)
				if (tp.getName() != null)
					taskPhaseNameCache = tp.getName();
				else
					taskPhaseNameCache = "";
			else
				taskPhaseNameCache = "";

		}

		person.fireUnitUpdate(UnitEventType.TASK_EVENT, newTask);

	}

	/**
	 * Reduce the person's caloric energy over time.
	 * 
	 * @param time the passing time (
	 */
	public void reduceEnergy(double time) {
		if (health == null)
			health = person.getPhysicalCondition();
		health.reduceEnergy(time);

	}

	/**
	 * Perform the current task for a given amount of time.
	 * 
	 * @param time       amount of time to perform the action
	 * @param efficiency The performance rating of person performance task.
	 * @return remaining time.
	 * @throws Exception if error in performing task.
	 */
	public double executeTask(double time, double efficiency) {
		double remainingTime = 0D;

		if (currentTask != null) {
			// For effort driven task, reduce the effective time based on efficiency.
			if (efficiency <= 0D) {
				efficiency = 0D;
			}

			if (currentTask.isEffortDriven()) {
				time *= efficiency;
			}

//			 if (person.isInside()) {
//			 checkForEmergency();
//			 }
			
			try {
				remainingTime = currentTask.performTask(time);
//				logger.info(person 
//						+ " currentTask: " + currentTask.getName()
//						+ "   performTask(time: " + Math.round(time*1000.0)/1000.0 + ")"
//						+ "   remainingTime: " + Math.round(remainingTime*1000.0)/1000.0 + "");
				// Record the action (task/mission)
				recordFilterTask(time);
			} catch (Exception e) {
//				LogConsolidated.log(Level.SEVERE, 0, sourceName,
//						person.getName() + " had trouble calling performTask().", e);
				e.printStackTrace(System.err);
//				logger.info(person + " had " + currentTask.getName() + "   remainingTime : " + remainingTime + "   time : " + time); // 1x = 0.001126440159375963 -> 8192 = 8.950963852039651
				return remainingTime;
			}
			
			// Expend energy based on activity.
			double energyTime = time - remainingTime;

			// Double energy expenditure if performing effort-driven task.
			if (currentTask != null && currentTask.isEffortDriven()) {
				// why java.lang.NullPointerException at TR = 2048 ?
				energyTime *= 2D;
			}

			if (energyTime > 0D) {
				if (person.isOutside()) {

					if (circadian == null)
						circadian = person.getCircadianClock();

					// it takes more energy to be in EVA doing work
					reduceEnergy(energyTime*1.1);
					circadian.exercise(time);
				} else
					reduceEnergy(energyTime);
			}
		}

		return remainingTime;

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
		}		

		if (totalProbCache == 0D) {
//			LogConsolidated.log(Level.SEVERE, 5_000, sourceName,
//			person.getName() + " has zero total task probability weight.");

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
			LogConsolidated.log(logger, Level.SEVERE, 5_000, sourceName, 
				person.getName() + " could not determine a new task.");
		} else {
			// Call constructInstance of the selected Meta Task to commence the ai task
			result = selectedMetaTask.constructInstance(mind.getPerson());
//			LogConsolidated.log(Level.FINE, 5_000, sourceName, person + " is going to " + selectedMetaTask.getName());
		}

		// Clear time cache.
		msolCache = -1;

//		LogConsolidated.log(Level.INFO, 0, sourceName,
//				person.getName() + " will return the task of '" + result + "' from getNewTask()"); 
		
		return result;
	}

	/**
	 * Determines the total probability weight for available tasks.
	 * 
	 * @return total probability weight
	 */
	public double getTotalTaskProbability(boolean useCache) {
		// If cache is not current, calculate the probabilities.
		if (!useCache) {
			rebuildTaskCache();
		}
		return totalProbCache;
	}

	public static boolean isInMissionWindow(double time) {
		boolean result = false;

		return result;
	}

	/**
	 * Calculates and caches the probabilities.
	 * This will NOT use the cache but assumes the callers know when a cahce can be used or not used. 
	 */
	private synchronized void rebuildTaskCache() {

		int shift = 0;

		if (taskSchedule.getShiftType() == ShiftType.ON_CALL) {
			shift = 0;
		}

		else if (taskSchedule.isShiftHour(marsClock.getMillisolInt())) {
			shift = 1;
		}

		else {
			shift = 2;
		}

		// Note : mtListCache is null when loading from a saved sim
		if (shiftCache != shift || mtListCache == null) {
			shiftCache = shift;

			List<MetaTask> mtList = null;

			// NOTE: any need to use getAnyHourTasks()
			if (shift == 0) {
				mtList = MetaTaskUtil.getAllMetaTasks();
			}

			else if (shift == 1) {
				mtList = MetaTaskUtil.getDutyHourTasks();
			}

			else if (shift == 2) {
				mtList = MetaTaskUtil.getNonDutyHourTasks();
			}

			// Use new mtList
			mtListCache = mtList;
		}

		// Create new taskProbCache
		taskProbCache = new ConcurrentHashMap<MetaTask, Double>(mtListCache.size());
		totalProbCache = 0D;
		
		// Determine probabilities.
		for (MetaTask mt : mtListCache) {
			double probability = mt.getProbability(person);
			if ((probability > 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
				if (probability > MAX_TASK_PROBABILITY) {
					if (!mt.getName().equalsIgnoreCase("sleeping")) { 
						LogConsolidated.log(logger, Level.INFO, 10_000, sourceName, mind.getPerson().getName() + " - "
							+ mt.getName() + "'s probability is at all time high ("
							+ Math.round(probability * 10.0) / 10.0 + ").");
					}
					probability = MAX_TASK_PROBABILITY;
				}

				taskProbCache.put(mt, probability);
				totalProbCache += probability;
			}
		}
		
		// Safety check, there should always be something to do
		if (taskProbCache.isEmpty() || (totalProbCache == 0)) {
			LogConsolidated.log(logger, Level.SEVERE, 5_000, sourceName,
					mind.getPerson().getName() + " has invalid taskCache size=" + taskProbCache.size()
							+ " : TotalProb=" + totalProbCache);				
		}
		
		// Diagnostics on new cache
		if (diagnosticFile != null) {
			outputCache();
		}
	}

	/**
	 * Enable the detailed diagnostics
	 */
	public static void enableDiagnostics() {
		String filename = SimulationFiles.getLogDir() + "/task-cache.txt";
		try {
			diagnosticFile = new PrintWriter(filename);
		} catch (FileNotFoundException e) {
			logger.severe("Problem opening task file " + filename);
			return;
		}
	}
	
	/**
	 * This method output the cache to a file for diagnostics
	 */
	private void outputCache() {	
		synchronized (diagnosticFile) {	
			diagnosticFile.println(MarsClockFormat.getDateTimeStamp(marsClock));
			diagnosticFile.println("Person:" + person.getName());
			diagnosticFile.println("Total:" + totalProbCache);
			for (Entry<MetaTask, Double> task : taskProbCache.entrySet()) {
				diagnosticFile.println(task.getKey().getName() + ":" + task.getValue());
			}
			
			diagnosticFile.println();
			diagnosticFile.flush();
		}
	}

	/**
	 * Checks if task probability cache should be used.
	 * 
	 * @return true if cache should be used.
	 */
	private boolean useCache() {
		double msol = marsClock.getMillisol();
		double diff = msol - msolCache;
		if (diff > 0.1D) {
			msolCache = msol;
			return false;
		}
		return true;
	}

	public TaskSchedule getTaskSchedule() {
		return taskSchedule;
	}

	/**
	 * Gets all pending tasks 
	 * 
	 * @return
	 */
	public List<String> getPendingTasks() {
		return pendingTasks;
	}
	
	public boolean hasPendingTask() {
		return !pendingTasks.isEmpty();
	}
	
	/**
	 * Adds a pending task
	 * 
	 * @param task
	 */
	public void addAPendingTask(String task) {
		pendingTasks.add(task);
		LogConsolidated.log(logger, Level.INFO, 20_000, sourceName,
				person.getName() + " was given the new task order of '" + task + "'.");
	}
	
	/**
	 * Deletes a pending task
	 * 
	 * @param task
	 */
	public void deleteAPendingTask(String task) {
		pendingTasks.remove(task);
		LogConsolidated.log(logger, Level.INFO, 20_000, sourceName,
				"The task order of '" + task + "' was removed from " + person.getName() + ".");
	}
	
	/**
	 * Gets the first pending meta task in the queue
	 * 
	 * @return
	 */
	public MetaTask getAPendingMetaTask() {
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
	public static MetaTask convertTask2MetaTask(String task) {
		return MetaTaskUtil.getMetaTask(task.replaceAll(" ","") + "Meta");
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 * @param mgr
	 */
	public static void initializeInstances(MarsClock clock, MissionManager mgr) {
		marsClock = clock;
		missionManager = mgr;
	}

	public void reinit() {
		person = mind.getPerson();
		health = person.getPhysicalCondition();
		circadian = person.getCircadianClock();
		taskSchedule = person.getTaskSchedule();
		
		if (currentTask != null)		
			currentTask.reinit();
		if (lastTask != null)
			lastTask.reinit();
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		if (currentTask != null)
			currentTask.destroy();
		mind = null;
		person = null;
//		timeCache = null;
		lastTask = null;
		health = null;
		circadian = null;
		taskSchedule = null;
		marsClock = null;
		if (taskProbCache != null) {
			taskProbCache.clear();
			taskProbCache = null;
		}
	}
}
