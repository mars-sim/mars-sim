/*
 * Mars Simulation Project
 * PersonTaskManager.java
 * @date 2025-08-25
 * @author Barry Evans (original),
 *         Patch: sticky-task window & throttled rebuild by GPT-5 Pro
 */
package com.mars_sim.core.person.ai.task.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
    // Sticky-task commitment window (reduces Sleep/Eat thrash)
    // ---------------------------------------------------------------------

    /**
     * For a short period after choosing a task, prefer to keep the same one
     * (if still valid in the new context). Value in millisols (~1.48 minutes/msol).
     */
    private static final double TASK_STICKY_WINDOW_MSOLS = 15.0D;

    /** Last chosen task name (normalized); used to keep commitment briefly. */
    private transient String lastChosenTaskName;
    /** When the last task was chosen; sticky window measured from here. */
    private transient MarsTime lastTaskChosenTime;

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
        this.mind = mind;
        this.person = mind.getPerson();
    }

    /** Gets the diagnostics module name to used in any output. */
    @Override
    protected String getDiagnosticsModule() {
        return DIAGS_MODULE;
    }

    /**
     * Calculates and caches the probabilities.
     * Internally throttles rebuilds and applies a short sticky-task commitment window
     * to reduce thrash (e.g., Sleep/Eat swapping).
     */
    @Override
    protected CacheCreator<TaskJob> rebuildTaskCache(MarsTime now) {
        // --- 1) Reuse snapshot if called too often (with small per-agent jitter) ---
        if ((lastCacheSnapshot != null) && (lastCacheRebuildTime != null)) {
            final double dt = Math.abs(lastCacheRebuildTime.getTimeDiff(now));
            final double jitter = ThreadLocalRandom.current().nextDouble(0.0D, REBUILD_JITTER_MSOLS);
            if (dt < (MIN_REBUILD_INTERVAL_MSOLS + jitter)) {
                return applyStickyWindowIfPossible(lastCacheSnapshot, now);
            }
        }

        // --- 2) Build a fresh cache based on shift status ---
        List<FactoryMetaTask> mtList;
        String shiftDesc;
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

        CacheCreator<TaskJob> newCache = new CacheCreator<>(shiftDesc, now);

        for (FactoryMetaTask mt : mtList) {
            List<TaskJob> jobs = mt.getTaskJobs(person);
            if (jobs != null) newCache.add(jobs);
        }

        // Add in any Settlement Tasks
        if ((workStatus == WorkStatus.ON_DUTY) && person.isInSettlement()) {
            SettlementTaskManager stm = person.getAssociatedSettlement().getTaskManager();
            newCache.add(stm.getTasks(person));
        }

        // If empty, fall back to safe defaults
        if (newCache.getCache().isEmpty()) {
            newCache = person.isOutside() ? getDefaultOutsideTasks() : getDefaultInsideTasks();
            logger.warning(person, 30_000L, "No normal task available. Using default "
                    + (person.isOutside() ? "outside" : "inside") + " tasks.");
        }

        // --- 3) Save snapshot metadata ---
        lastCacheSnapshot = newCache;
        lastCacheRebuildTime = now;

        // --- 4) Apply sticky preference if still inside the window ---
        return applyStickyWindowIfPossible(newCache, now);
    }

    /**
     * If we are inside the sticky window and the previously selected task is still available,
     * return a filtered cache that only contains that task; otherwise return the original cache.
     */
    private CacheCreator<TaskJob> applyStickyWindowIfPossible(CacheCreator<TaskJob> src, MarsTime now) {
        if ((lastChosenTaskName == null) || (lastTaskChosenTime == null)) {
            return src;
        }
        double age = Math.abs(lastTaskChosenTime.getTimeDiff(now));
        if (age >= TASK_STICKY_WINDOW_MSOLS) {
            return src; // window expired
        }

        // Try to keep the same task if it's still present & legal in current context
        Set<TaskJob> matching = src.getCache().keySet().stream()
            .filter(j -> taskNameMatches(j, lastChosenTaskName))
            .collect(Collectors.toSet());

        if (!matching.isEmpty()) {
            CacheCreator<TaskJob> pinned = new CacheCreator<>("Sticky: " + lastChosenTaskName, now);
            matching.forEach(pinned::put);
            return pinned;
        }

        return src;
    }

    /** Name matcher that tolerates metas appending extra detail (e.g., "Sleep (Hab 1)"). */
    private static boolean taskNameMatches(TaskJob job, String wanted) {
        String n = job.getName();
        return (n != null) && (n.equals(wanted) || n.startsWith(wanted + " "));
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
    @Override
    protected boolean isPendingPossible() {
        return (!person.isOutside() || (person.getMission() == null));
    }

    @Override
    protected Task createTask(TaskJob selectedWork) {
        // Remember the last chosen task & timestamp to enable the sticky window.
        lastChosenTaskName = selectedWork.getName();
        // Use the last rebuild time as a close approximation to the pick time.
        lastTaskChosenTime = (lastCacheRebuildTime != null) ? lastCacheRebuildTime : null;

        return selectedWork.createTask(mind.getPerson());
    }

    /**
     * Sets the list of mission ratings and the selected mission rating.
     */
    public void setMissionRatings(List<MissionRating> missionProbCache,
                                  MissionRating selectedMetaMissionRating) {
        this.missionProbCache = missionProbCache;
        this.selectedMissionRating = selectedMetaMissionRating;
    }

    /** Gets the list of mission ratings. */
    public List<MissionRating> getMissionProbCache() {
        return missionProbCache;
    }

    /** Gets the selected mission rating. */
    public MissionRating getSelectedMission() {
        return selectedMissionRating;
    }

    public void reinit() {
        person = mind.getPerson();
        super.reinit(person);
        // Reset throttling & sticky state so the next call will rebuild immediately.
        lastCacheSnapshot = null;
        lastCacheRebuildTime = null;
        lastChosenTaskName = null;
        lastTaskChosenTime = null;
    }

    /** Prepares object for garbage collection. */
    @Override
    public void destroy() {
        mind.destroy();
        mind = null;
        person.destroy();
        person = null;
        super.destroy();
    }
}
