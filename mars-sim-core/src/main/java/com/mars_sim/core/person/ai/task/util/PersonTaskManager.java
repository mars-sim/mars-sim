/*
 * Mars Simulation Project
 * PersonTaskManager.java
 * @date 2023-09-04
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.util.MissionRating;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.CacheCreator;
import com.mars_sim.core.person.ai.Mind;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.person.ai.task.EatDrink;
import com.mars_sim.core.person.ai.task.Sleep;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

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

	private static CacheCreator<TaskJob> defaultInsideTasks;
	private static CacheCreator<TaskJob> defaultOutsideTasks;
	
	private static final String SLEEP = "Sleep";
	private static final String EAT = "Eat";

	private static final String DIAGS_MODULE = "taskperson";
	
	// Data members
	
	private transient List<MissionRating> missionProbCache;
	
	private transient MissionRating selectedMissionRating;
	
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
	 * Gets the diagnostics module name to used in any output.
	 * 
	 * @return
	 */
	@Override
	protected String getDiagnosticsModule() {
		return DIAGS_MODULE;
	}

	/**
	 * Calculates and caches the probabilities. This will NOT use the cache but 
	 * assumes the callers know when a cache can be used or not used.
	 * 
	 * @param now The current mars time
	 */
	@Override
	protected CacheCreator<TaskJob> rebuildTaskCache(MarsTime now) {

		List<FactoryMetaTask> mtList = null;
		String shiftDesc = null;
		WorkStatus workStatus = person.getShiftSlot().getStatus();
        shiftDesc = switch (workStatus) {
            case OFF_DUTY, ON_LEAVE -> {
                mtList = MetaTaskUtil.getNonDutyHourTasks();
                yield "Shift: Non-Duty";
            }
            case ON_CALL -> {
                mtList = MetaTaskUtil.getOnCallMetaTasks();
                yield "Shift: On-Call";
            }
            case ON_DUTY -> {
                mtList = MetaTaskUtil.getDutyHourTasks();
                yield "Shift: On-Duty";
            }
            default -> throw new IllegalStateException("Do not know status " + workStatus);
        };

		// Create new taskProbCache
		CacheCreator<TaskJob> newCache = new CacheCreator<>(shiftDesc, now);

		// Determine probabilities.
		for (FactoryMetaTask mt : mtList) {
			List<TaskJob> job = mt.getTaskJobs(person);
			if (job != null) {
				newCache.add(job);
			}
		}

		// Add in any Settlement Tasks
		if ((workStatus == WorkStatus.ON_DUTY) && person.isInSettlement()) {
			SettlementTaskManager stm = person.getAssociatedSettlement().getTaskManager();
			newCache.add(stm.getTasks(person));
		}

		// Check if the map cache is empty
		if (newCache.getCache().isEmpty()) {
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
	private static synchronized CacheCreator<TaskJob> getDefaultInsideTasks() {
		if (defaultInsideTasks == null) {
			defaultInsideTasks = new CacheCreator<>("Default Inside", null);
			
			// Create a fallback Task job that can always be done
			RatingScore base = new RatingScore(1D);
			TaskJob sleepJob = new AbstractTaskJob(SLEEP, base) {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Task createTask(Person person) {
					return new Sleep(person);
				}	
			};
			defaultInsideTasks.put(sleepJob);

			TaskJob eatJob = new AbstractTaskJob(EAT, base) {
				
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
	private static synchronized CacheCreator<TaskJob> getDefaultOutsideTasks() {
		if (defaultOutsideTasks == null) {
			defaultOutsideTasks = new CacheCreator<>("Default Outside", null);

			// Create a MetaTask to return inside
			TaskJob walkBack = new AbstractTaskJob("Return Inside", new RatingScore(1D)) {
				
				private static final long serialVersionUID = 1L;

				@Override
				public Task createTask(Person person) {
					logger.info(person, 10_000L, "Returning inside to find work.");
					return new Walk(person);
				}	
			};
			defaultOutsideTasks.put(walkBack);
		}
		return defaultOutsideTasks;
	}
	
	/**
	 * A Person can do pending tasks if they are not outside and not on a Mission.
	 * @return Whether person is inside
	 */
	protected boolean isPendingPossible() {
		return (!person.isOutside() || (person.getMission() == null));
	}

	@Override
	protected Task createTask(TaskJob selectedWork) {
		return selectedWork.createTask(mind.getPerson());
	}
	
	
	/**
	 * Directly assign a Task to a person if they have any issues in starting a new task.
	 *
	 * @param newTask   the new task to be assigned
	 * @param allowSameTask is it allowed to execute the same task as previous
	 * @return true if task can be performed.
	 */
	@Override
	public boolean directlyAssignTask(Task newTask, boolean allowSameTask) {
		// If task is physical effort driven and person too ill, do not assign task.
		Task currentTask = getTask();

		String newTaskName = newTask.getName();
		
		if (currentTask != null && !currentTask.isDone()) {

			String currentTaskName = currentTask.getName();
			if (!allowSameTask && currentTaskName.equals(newTaskName)){
	      		logger.info(person, 20_000, "Already '" + currentTaskName 
	      				+ "' as of this moment.");
				// If the person has been doing this task, 
				// then there is no need of adding it.
				return false;
			}

			if (currentTaskName.equals(Sleep.NAME)) {
	      		logger.info(person, 20_000, "Currently asleep. Not available to be assigned with other tasks.");
				// If the person is asleep, 
				// do not assign this task.
	      		
	      		// Note: what if it's an emergency that one must wake up and respond ?
				return false;
			}

			Vehicle v = person.getVehicle();
			if (v != null && v instanceof Rover r && r.isInAirlock(person)) {	
	      		logger.info(person, 20_000, "Currently inside a vehicular airlock. Not available to be assigned with other tasks.");
		
	      		// Note: need to wait until the person has exited the vehicular airlock
				return false;
			}
			Settlement settlement = person.getSettlement();
			if (settlement != null && settlement.isInAirlock(person)) {	
	      		logger.info(person, 20_000, "Currently inside a vehicular airlock. Not available to be assigned with other tasks.");
		
	      		// Note: need to wait until the person has exited the vehicular airlock
				return false;
			}
		}
		
		if (!newTaskName.equals(Sleep.NAME) && person.isSuperUnfit()) {
			logger.warning(person, 20_000, "Super unfit to be assigned with '" + newTask + ".");
			return false;
		}
		
		// If Task is easy or person is fit enough, then assign the task.
		if ((!newTask.isEffortDriven() || person.getPerformanceRating() > 0D)
				 && checkReplaceTask(newTask, allowSameTask)) {
			return true;
		}

		logger.info(person, 20_000, "Unable to assign with '" + newTaskName + "'.");		
		return false;
	}

	/**
	 * Sets the list of mission ratings and the selected mission rating.
	 * 
	 * @param missionProbCache
	 * @param selectedMetaMissionRating
	 */
	public void setMissionRatings(List<MissionRating> missionProbCache,
								  MissionRating selectedMetaMissionRating) {
		this.missionProbCache = missionProbCache;
		this.selectedMissionRating = selectedMetaMissionRating;
	}
	
	/**
	 * Gets the list of mission ratings.
	 * 
	 * @return
	 */
	public List<MissionRating> getMissionProbCache() {
		return missionProbCache;
	}
	
	/**
	 * Gets the selected mission rating.
	 * 
	 * @return
	 */
	public MissionRating getSelectedMission() {
		return selectedMissionRating;
	}
	
	public void reinit() {
		person = mind.getPerson();
		super.reinit(person);
	}

	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		mind.destroy();
		mind = null;
		person.destroy();
		person = null;

		super.destroy();
	}
}
