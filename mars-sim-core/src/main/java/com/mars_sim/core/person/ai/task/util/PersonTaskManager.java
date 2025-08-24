/*
 * Mars Simulation Project
 * PersonTaskManager.java
 * @date 2023-09-04
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
import com.mars_sim.core.time.MarsTime;

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

	// ---------------------------------------------------------------------
	// Cache rebuild throttling to reduce task churn and CPU spikes per agent
	// ---------------------------------------------------------------------

	/** Do not rebuild the task probability cache more often than this (millisols). */
	private static final double MIN_REBUILD_INTERVAL_MSOLS = 1.0D;
	/** Add a tiny random jitter to avoid all agents rebuilding in lockstep. */
	private static final double REBUILD_JITTER_MSOLS = 0.25D;
	/** Last time (sim time) we rebuilt the task probability cache. */
	private transient MarsTime lastCacheRebuildTime;
	/** Last computed cache snapshot; reused when within cooldown. */
	private transient CacheCreator<TaskJob> lastCacheSnapshot;

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
	 * Calculates and caches the probabilities. This will NOT use the external cache but
	 * internally throttles rebuilds to avoid excessive recomputation.
	 * 
	 * @param now The current mars time
	 */
	@Override
	protected CacheCreator<TaskJob> rebuildTaskCache(MarsTime now) {

		// Throttle cache rebuilds to reduce churn and CPU spikes at high sim speed.
		if (lastCacheSnapshot != null && lastCacheRebuildTime != null) {
			// Use absolute difference to be robust to direction of getTimeDiff(..)
			double dt = Math.abs(lastCacheRebuildTime.getTimeDiff(now));
			double jitter = ThreadLocalRandom.current().nextDouble(0.0D, REBUILD_JITTER_MSOLS);
			if (dt < (MIN_REBUILD_INTERVAL_MSOLS + jitter)) {
				return lastCacheSnapshot; // reuse recent snapshot
			}
		}

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

		// Keep as the current snapshot and remember rebuild time.
		lastCacheSnapshot = newCache;
		lastCacheRebuildTime = now;

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
		// Reset throttling state so the next call will rebuild immediately.
		lastCacheSnapshot = null;
		lastCacheRebuildTime = null;
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
