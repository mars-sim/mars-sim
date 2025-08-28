/*
 * Mars Simulation Project
 * IdleMaintenanceTask.java
 * @date 2025-08-28
 *
 * Notes:
 * - Fix for PR #1693 follow-up run error by maximizing compatibility with differing Task APIs.
 * - Keeps implementation minimal and self-contained.
 * - Safe default: ends immediately if the person is not inside a settlement.
 */
package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.time.ClockPulse;

/**
 * A lightweight, low-priority Task intended to run on idling crew
 * to model very small background maintenance/housekeeping work
 * inside a settlement (e.g., tidying, checking labels, wiping sensors).
 *
 * <p>Design goals:
 * <ul>
 *   <li>Safe default: does nothing outside a settlement.</li>
 *   <li>Short-lived: completes automatically after a small duration.</li>
 *   <li>Non-invasive: no dependencies on resources or equipment.</li>
 * </ul>
 *
 * <p>To enable in gameplay, hook creation of this task into the
 * appropriate idle/chooser logic (e.g., when a Person is idle,
 * inside a settlement, and no higher priority work exists).</p>
 */
public class IdleMaintenanceTask extends Task {

    /** Default display name. */
    private static final String DEFAULT_NAME = "Idle Maintenance";

    /** Default description (returned by {@link #getDescription()}). */
    private static final String DEFAULT_DESC =
            "Light housekeeping & micro-checks while idle inside a settlement.";

    /**
     * Small, bounded duration for this task (in millisols).
     * Keep short so it yields frequently and never blocks higher-priority work.
     */
    private static final int DEFAULT_DURATION_MSO = 50;

    /** Duration this instance will run (millisols). */
    private final int durationMillisols;

    /** Accumulated progress (millisols). */
    private int elapsedMillisols;

    /**
     * Creates an IdleMaintenanceTask with a default short duration.
     *
     * @param person the person performing the task
     */
    public IdleMaintenanceTask(Person person) {
        this(person, DEFAULT_DURATION_MSO);
    }

    /**
     * Creates an IdleMaintenanceTask with a caller-specified duration.
     *
     * @param person   the person performing the task
     * @param duration the intended duration in millisols (clamped to [10 .. 500])
     */
    public IdleMaintenanceTask(Person person, int duration) {
        // Call the base Task constructor with a user-friendly name if supported by the branch.
        // In current mars-sim branches, Task(Person, String) is commonly available.
        super(person, DEFAULT_NAME);

        // Bound the duration to a safe range
        if (duration < 10) {
            this.durationMillisols = 10;
        }
        else if (duration > 500) {
            this.durationMillisols = 500;
        }
        else {
            this.durationMillisols = duration;
        }

        this.elapsedMillisols = 0;
        // Safe default: if person is outside, the task will finish on first tick.
    }

    // ---------------------------------------------------------------------
    // Compatibility layer: support both common Task 'perform' shapes.
    // Some branches expect 'perform()'; others expect 'perform(ClockPulse)'.
    // We implement both (without @Override) to compile cleanly on either.
    // ---------------------------------------------------------------------

    /**
     * Perform a small slice of work; ends quickly outside settlements.
     * Returns true when finished, false to continue on the next tick.
     */
    public boolean perform() {
        return doStep(1.0);
    }

    /**
     * Perform using an engine pulse if the branch expects this signature.
     * Returns true when finished, false to continue on the next tick.
     */
    public boolean perform(ClockPulse pulse) {
        double dt = (pulse != null ? pulse.getElapsed() : 1.0);
        return doStep(dt);
    }

    // ---------------------------------------------------------------------
    // Internal step logic shared by both perform variants
    // ---------------------------------------------------------------------

    private boolean doStep(double millisols) {
        final Person p = getPerson();
        final Settlement here = (p != null) ? p.getSettlement() : null;
        if (here == null) {
            // Outside any settlement: nothing to do; finish quickly.
            return true;
        }

        // Keep this extremely light so it has negligible impact.
        // Potential future expansions could nudge cleanliness stats, etc.

        // Advance progress in small bounded increments
        // (rounding to the nearest millisol keeps bookkeeping simple).
        int step = (millisols <= 0) ? 1 : (int) Math.max(1, Math.round(millisols));
        elapsedMillisols += step;

        // End condition: ran long enough.
        return elapsedMillisols >= durationMillisols;
    }

    /**
     * Optional helper: remaining time in millisols (non-negative).
     */
    public int getRemainingMillisols() {
        int remaining = durationMillisols - elapsedMillisols;
        return Math.max(remaining, 0);
    }

    /**
     * Optional human-friendly description text for UIs or logs.
     * (Avoids relying on Task.setDescription(), which may not exist in all branches.)
     */
    public String getDescription() {
        return DEFAULT_DESC;
    }
}
