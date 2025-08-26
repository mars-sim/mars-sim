/*
 * Mars Simulation Project
 * PersonTaskManager.java
 * @date 2023-09-04
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	// === NEW: Meta-task failure cooldown guard rails ===
	/** Number of pulses to cool down a meta-task after it fails. */
	private static final int FAILURE_COOLDOWN_PULSES = 200;

	/** Tracks when a meta-task (by class name) is cooled down until (exclusive). */
	private transient Map<String, Long> failedUntilPulse = new ConcurrentHashMap<>();

	/** Local pulse counter advanced each rebuild of the cache. */
	private transient long pulseCounter = 0L;

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

	/** Returns true if the given meta-task key is still cooling down. */
	private boolean isCoolingDown(String key) {
		Long until = failedUntilPulse.get(key);
		return (until != null) && (until > pulseCounter);
	}

	/**
	 * Calculates and caches the probabilities. This will NOT use the cache but
	 * assumes the callers know when a cache can be used or not used.
	 *
	 * @param now The current mars time
	 */
	@Override
	protected CacheCreator<TaskJob> rebuildTaskCache(MarsTime now) {

		// Advance a lightweight "pulse" counter each rebuild; used for cooldown timing.
		pulseCounter++;

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

		// Determine probabilities with guard rails and cooldowns.
		for (FactoryMetaTask mt : mtList) {
			final String key = mt.getClass().getName();

			// If this meta-task recently failed, skip it for now to prevent thrashing.
			if (isCoolingDown(key)) {
				logger.info(person, 5_000L, "Cooling down meta-task " + key + "; skipping this tick.");
				continue;
			}

			try {
				List<TaskJob> job = mt.getTaskJobs(person);
				if (job != null) {
					newCache.add(job);
				}
				// Cooldown expired / success path: clean up stale entry if present.
				failedUntilPulse.remove(key);
			}
			catch (IllegalArgumentException | IllegalStateException ex) {
				// Common precondition issue (e.g., not a collaborator yet) — cool down this meta.
				failedUntilPulse.put(key, pulseCounter + FAILURE_COOLDOWN_PULSES);
				logger.info(person, 10_000L,
						"Cooling down meta-task " + key + " after failure: "
								+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
			}
			catch (RuntimeException ex) {
				// Any unexpected runtime failure — don't let it break the frame.
				failedUntilPulse.put(key, pulseCounter + FAILURE_COOLDOWN_PULSES);
				logger.warning(person, 10_000L,
						"Cooling down meta-task " + key + " after runtime failure: "
								+ ex.getClass().getSimpleName() + ": " + ex.getMessage());
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
		// Ensure cooldown map exists after reinit/deserialization.
		if (failedUntilPulse == null) {
			failedUntilPulse = new ConcurrentHashMap<>();
		}
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
