/**
 * Mars Simulation Project
 * TaskManager.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.CircadianClock;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The TaskManager class keeps track of a person's current task and can randomly
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
	
	private static String WALK = "Walk";

	private static final int MAX_TASK_PROBABILITY = 35_000;
	/** A decimal number a little bigger than zero for comparing doubles. */
//	private static final double SMALL_AMOUNT = 0.001;
	
	// Data members
	/** The cache for work shift. */
	private int shiftCache;
	/** The cache for mission name. */
	private String missionNameCache = "";
	
	/** The mind of the person the task manager is responsible for. */
	private Mind mind;
	
	private transient Person person;

	/** The CircadianClock reference */ 
	private transient CircadianClock circadian = null;
	private transient List<MetaTask> mtListCache;

	private List<String> pendingTasks;


	private static MissionManager missionManager;

	static {
		Simulation sim = Simulation.instance();
		missionManager = sim.getMissionManager();
	}
	
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
	 * Initializes tash schedule instance
	 */
	public void initialize() {
		taskSchedule = mind.getPerson().getTaskSchedule();
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
	 * Reduce the person's caloric energy over time.
	 * 
	 * @param time the passing time (
	 */
	private void reduceEnergy(double time) {
		person.getPhysicalCondition().reduceEnergy(time);
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
	 * Calculates and caches the probabilities.
	 * This will NOT use the cache but assumes the callers know when a cahce can be used or not used. 
	 */
	@Override
	protected synchronized void rebuildTaskCache() {

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
		taskProbCache = new HashMap<MetaTask, Double>(mtListCache.size());
		totalProbCache = 0D;
		
		// Determine probabilities.
		for (MetaTask mt : mtListCache) {
			double probability = mt.getProbability(person);
			if ((probability > 0D) && (!Double.isNaN(probability)) && (!Double.isInfinite(probability))) {
				if (probability > MAX_TASK_PROBABILITY) {
					if (!mt.getName().equalsIgnoreCase("sleeping")) { 
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
		logger.info(person, "Was given the new task order of '" + task + "'.");
	}
	
	/**
	 * Deletes a pending task
	 * 
	 * @param task
	 */
	public void deleteAPendingTask(String task) {
		pendingTasks.remove(task);
		logger.info(worker, "The task order of '" + task + "' was removed.");
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
		super.reinit();
		
		person = mind.getPerson();
		circadian = person.getCircadianClock();
		taskSchedule = person.getTaskSchedule();
		
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
