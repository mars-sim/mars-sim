/*
 * Mars Simulation Project
 * PersonTaskManager.java
 * @date 2023-09-04
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private static final SimLogger logger = SimLogger.getLogger(PersonTaskManager.class.getName());

	private static CacheCreator<TaskJob> defaultInsideTasks;
	private static CacheCreator<TaskJob> defaultOutsideTasks;

	private static final String SLEEP = "Sleep";
	private static final String EAT = "Eat";

	private static final String DIAGS_MODULE = "taskperson";

	// ---------------------------------------------------------------------
	// Log TTLs (avoid magic numbers in logger calls)
	// ---------------------------------------------------------------------
	private static final long TTL_INFO_RETURN_INSIDE = 10_000L;
	private static final long TTL_WARN_NO_TASKS = 30_000L;
	private static final long TTL_WARN_META_EXCEPTION = 5_000L;

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

	// ---------------------------------------------------------------------
	// Per-task cooldown state to prevent immediate reselection thrash
	// ---------------------------------------------------------------------

	/** Number of cache rebuilds to cool down a task type after it was selected. */
	private static final int JOB_COOLDOWN_REBUILDS =
			Integer.getInteger("mars.sim.task.cooldown.rebuilds", 3);

	/**
	 * Apply a probability factor while cooled. This class uses a hard block when cooled
	 * (factor 0). Keeping the property for easy tuning later.
	 */
	private static final double JOB_COOLDOWN_FACTOR =
			Double.parseDouble(System.getProperty("mars.sim.task.cooldown.factor", "0"));

	/** Local sequence incremented each time we actually rebuild the task cache. */
	private transient long cacheRebuildSeq = 0L;

	/** Per-task-type cooldown (keyed by TaskJob class name) measured in rebuild sequence. */
	private transient Map<String, Long> jobCooldownUntil = new HashMap<>();

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
	 * @return diagnostics module id
	 */
	@Override
	protected String getDiagnosticsModule() {
		return DIAGS_MODULE;
	}

	/**
	 * Calculates and caches the probabilities. This will NOT use the external cache but
	 * internally throttles rebuilds to avoid excessive recomputation. Also applies
	 * a short, configurable cooldown to the task type that was just selected so
	 * agents don't immediately reselect the same task after an abandonment/block.
	 *
	 * @param now The current mars time
	 */
	@Override
	protected CacheCreator<TaskJob> rebuildTaskCache(MarsTime now) {

		// --- simplified throttle: early-exit via helper ---
		if (shouldReuseCache(now)) {
			return lastCacheSnapshot;
		}

		// We are about to perform a real rebuild; advance the sequence.
		cacheRebuildSeq++;

		List<FactoryMetaTask> mtList;
		String shiftDesc;
		WorkStatus workStatus = person.getShiftSlot().getStatus();
		switch (workStatus) {
			case OFF_DUTY:
			case ON_LEAVE:
				mtList = MetaTaskUtil.getNonDutyHourTasks();
				shiftDesc = "Shift: Non-Duty";
				break;
			case ON_CALL:
				mtList = MetaTaskUtil.getOnCallMetaTasks();
				shiftDesc = "Shift: On-Call";
				break;
			case ON_DUTY:
				mtList = MetaTaskUtil.getDutyHourTasks();
				shiftDesc = "Shift: On-Duty";
				break;
			default:
				throw new IllegalStateException("Do not know status " + workStatus);
		}

		// Create new taskProbCache
		CacheCreator<TaskJob> newCache = new CacheCreator<>(shiftDesc, now);

		// Determine probabilities from meta tasks (defensive + cooldown-aware).
		for (FactoryMetaTask mt : mtList) {
			try {
				List<TaskJob> jobs = mt.getTaskJobs(person);
				if (jobs != null) {
					for (TaskJob j : jobs) {
						if (!isCooled(j)) {
							newCache.put(j);
						}
					}
				}
			}
			catch (RuntimeException ex) {
				// Defensive: a single misbehaving meta must not break the scheduler.
				logger.warning(person, TTL_WARN_META_EXCEPTION,
						"Suppressed exception collecting jobs from "
								+ mt.getClass().getSimpleName() + ": " + ex.toString());
			}
		}

		// Add in any Settlement Tasks (defensive + cooldown-aware)
		if ((workStatus == WorkStatus.ON_DUTY) && person.isInSettlement()) {
			try {
				SettlementTaskManager stm = person.getAssociatedSettlement().getTaskManager();
				List<TaskJob> settlementJobs = stm.getTasks(person);
				if (settlementJobs != null) {
					for (TaskJob j : settlementJobs) {
						if (!isCooled(j)) {
							newCache.put(j);
						}
					}
				}
			}
			catch (RuntimeException ex) {
				logger.warning(person, TTL_WARN_META_EXCEPTION,
						"Suppressed exception collecting settlement tasks: " + ex.toString());
			}
		}

		// Check if the map cache is empty
		if (newCache.getCache().isEmpty()) {
			// When falling back, do NOT apply cooldown; always offer these basics.
			if (person.isOutside()) {
				newCache = getDefaultOutsideTasks();
			}
			else {
				newCache = getDefaultInsideTasks();
			}
			logger.warning(person, TTL_WARN_NO_TASKS,
					"No normal task available. Get default "
							+ (person.isOutside() ? "outside" : "inside") + " tasks.");
		}

		// Keep as the current snapshot and remember rebuild time.
		lastCacheSnapshot = newCache;
		lastCacheRebuildTime = now;

		return newCache;
	}

	/**
	 * Decide whether to reuse the last built cache snapshot to throttle rebuilds.
	 *
	 * @param now current Mars time
	 * @return true if the previous snapshot is fresh enough to reuse
	 */
	private boolean shouldReuseCache(MarsTime now) {
		if ((lastCacheSnapshot == null) || (lastCacheRebuildTime == null)) {
			return false;
		}
		// Use absolute difference to be robust to direction of getTimeDiff(..)
		double dt = Math.abs(now.getTimeDiff(lastCacheRebuildTime));
		double jitter = ThreadLocalRandom.current().nextDouble(REBUILD_JITTER_MSOLS);
		return dt < (MIN_REBUILD_INTERVAL_MSOLS + jitter);
	}

	/** Returns a stable key for cooldown, based on the TaskJob type. */
	private static String jobKey(TaskJob job) {
		return (job != null ? job.getClass().getName() : "null");
	}

	/** Whether the given job is currently under cooldown. */
	private boolean isCooled(TaskJob job) {
		if (job == null || JOB_COOLDOWN_REBUILDS <= 0) {
			return false;
		}
		Long until = jobCooldownUntil.get(jobKey(job));
		// Hard block while cooled (factor == 0).
		return (until != null) && (cacheRebuildSeq < until) && (JOB_COOLDOWN_FACTOR == 0D);
	}

	/** Mark the given job type as just selected; cool it for a few rebuilds. */
	private void markJobJustSelected(TaskJob job) {
		if (job == null || JOB_COOLDOWN_REBUILDS <= 0) {
			return;
		}
		long until = cacheRebuildSeq + Math.max(0, JOB_COOLDOWN_REBUILDS);
		jobCooldownUntil.put(jobKey(job), until);
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
					logger.info(person, TTL_INFO_RETURN_INSIDE, "Returning inside to find work.");
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
	@Override
	protected boolean isPendingPossible() {
		return (!person.isOutside() || (person.getMission() == null));
	}

	@Override
	protected Task createTask(TaskJob selectedWork) {
		// Record the selected job type so it is briefly cooled down.
		markJobJustSelected(selectedWork);
		return selectedWork.createTask(mind.getPerson());
	}

	/**
	 * Sets the list of mission ratings and the selected mission rating.
	 *
	 * @param missionProbCache cache of mission ratings
	 * @param selectedMetaMissionRating selected mission rating
	 */
	public void setMissionRatings(List<MissionRating> missionProbCache,
								  MissionRating selectedMetaMissionRating) {
		this.missionProbCache = missionProbCache;
		this.selectedMissionRating = selectedMetaMissionRating;
	}

	/**
	 * Gets the list of mission ratings.
	 *
	 * @return mission rating cache
	 */
	public List<MissionRating> getMissionProbCache() {
		return missionProbCache;
	}

	/**
	 * Gets the selected mission rating.
	 *
	 * @return selected mission rating
	 */
	public MissionRating getSelectedMission() {
		return selectedMissionRating;
	}

	/** Reinitialise after load/reset. */
	public void reinit() {
		person = mind.getPerson();
		super.reinit(person);
		// Reset throttling state so the next call will rebuild immediately.
		lastCacheSnapshot = null;
		lastCacheRebuildTime = null;

		// Reset cooldown state
		cacheRebuildSeq = 0L;
		jobCooldownUntil = new HashMap<>();
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

		// Clear transient maps
		if (jobCooldownUntil != null) {
			jobCooldownUntil.clear();
		}
		jobCooldownUntil = null;

		super.destroy();
	}
}
