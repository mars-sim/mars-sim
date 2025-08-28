/*
 * Mars Simulation Project
 * IdleMaintenanceTask.java
 * @date 2025-08-28
 * @author Contributors
 *
 * A lightweight, low-priority task settlers perform when idle.
 * It occupies a small, finite duration and then completes. This version is
 * intentionally conservative (no hard coupling to malfunction/maintenance
 * subsystems) so it compiles cleanly across branches.
 */

package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsClock;

/**
 * Represents simple upkeep/inspection during idle periods.
 * <p>
 * The task completes after a short, fixed amount of simulation time.
 * Future extensions can add guarded hooks into maintenance systems.
 */
public class IdleMaintenanceTask extends Task {

    /** Serial ID. */
    private static final long serialVersionUID = 1L;

    /** Duration to spend on maintenance (in millisols). */
    private static final double DURATION_MILLISOLS = 50.0;

    /** The worker doing the task. */
    private final Person person;

    /** Settlement snapshot at start (may be null). */
    private final Settlement settlement;

    /** Millisol when work began; NaN until first perform() tick. */
    private double startMillisol = Double.NaN;

    /** Completion flag. */
    private boolean completed;

    /**
     * Creates an IdleMaintenanceTask for the given person.
     * @param person the worker (may be null if constructed defensively)
     */
    public IdleMaintenanceTask(Person person) {
        super(person);
        this.person = person;
        this.settlement = (person != null) ? person.getAssociatedSettlement() : null;

        setName("Idle Maintenance");
        setDescription("Performing small upkeep while idle.");
    }

    /**
     * Advances the task. Uses the master clock's Mars time to measure elapsed work.
     * Keeps implementation conservative to remain build-safe across modules.
     */
    @Override
    public void perform() {
        if (completed) return;

        final MarsClock clock = Simulation.instance()
                                          .getMasterClock()
                                          .getMarsClock();
        final double now = clock.getMillisol();

        // Initialize start time on first tick.
        if (Double.isNaN(startMillisol)) {
            startMillisol = now;
        }

        // OPTIONAL HOOK (kept commented to avoid compile-time coupling):
        // if (settlement != null) {
        //     var mm = settlement.getMalfunctionManager();
        //     if (mm != null) {
        //         // Example guarded call for future use:
        //         // mm.applyIdleUpkeepTick(person, 0.01D);
        //     }
        // }

        // Complete once the maintenance interval has elapsed.
        if ((now - startMillisol) >= DURATION_MILLISOLS) {
            completed = true;
        }
    }

    /** @return true when the task has finished. */
    @Override
    public boolean isFinished() {
        return completed;
    }
}
