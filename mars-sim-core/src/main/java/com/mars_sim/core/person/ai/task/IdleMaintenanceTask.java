/*
 * Mars Simulation Project
 * IdleMaintenanceTask.java
 * @date 2025-08-28
 * @author Contributors
 *
 * Notes:
 * - This task performs a short, harmless "idle maintenance" period when a person
 *   has nothing better to do. The intent is to represent light upkeep without
 *   consuming notable resources or requiring specific building context.
 * - This version fixes build errors by importing the correct Task base class and
 *   removing unused imports. It also avoids direct calls to unknown Malfunction
 *   APIs to remain build-safe across modules.
 */

package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsClock;

/**
 * A lightweight task representing simple upkeep actions when a person is idle.
 * <p>
 * This task does not require special tools or a specific building; it simply
 * occupies a small, finite duration and then finishes. If you later wish to
 * hook into settlement maintenance systems, use the {@link Settlement}
 * reference provided and perform guarded calls to malfunction/maintenance
 * managers where available.
 */
public class IdleMaintenanceTask extends Task {

    /** Serial ID. */
    private static final long serialVersionUID = 1L;

    /** Default duration (in millisols) to spend on idle maintenance. */
    private static final double DURATION_MILLISOLS = 50.0;

    /** The person performing this task (cached for convenience). */
    private final Person person;

    /** The settlement the person is associated with at task start (may be null). */
    private final Settlement settlement;

    /** Millisol timestamp when the task first began work; NaN indicates uninitialized. */
    private double startMillisol = Double.NaN;

    /** Whether this task has completed. */
    private boolean completed;

    /**
     * Creates an IdleMaintenanceTask for the given person.
     *
     * @param person the person who will perform the task
     */
    public IdleMaintenanceTask(Person person) {
        super(person);
        this.person = person;
        this.settlement = (person != null) ? person.getAssociatedSettlement() : null;

        setName("Idle Maintenance");
        setDescription("Performing small upkeep while idle.");
    }

    /**
     * Perform a small slice of work. The task tracks elapsed millisols using the
     * global master clock and completes once {@link #DURATION_MILLISOLS} has elapsed.
     * <p>
     * NOTE: We intentionally keep this implementation conservative to avoid
     * compile-time coupling to optional maintenance/malfunction subsystems. If
     * you need to expand functionality, add guarded calls against available
     * managers here (e.g., a MalfunctionManager) and keep null/availability checks.
     */
    public void perform() {
        if (completed) {
            return;
        }

        // Acquire current mission time in millisols.
        final MarsClock marsClock = Simulation.instance()
                                              .getMasterClock()
                                              .getMarsClock();
        final double now = marsClock.getMillisol();

        // Initialize start time on first perform tick.
        if (Double.isNaN(startMillisol)) {
            startMillisol = now;
        }

        // ---- Optional hook point (kept commented to remain build-safe) ----
        // if (settlement != null) {
        //     final var mm = settlement.getMalfunctionManager();
        //     if (mm != null) {
        //         // Example guarded call (replace with a real API available in your branch):
        //         // mm.applyIdleUpkeepTick(person, 0.01D);
        //     }
        // }

        // Complete the task after the configured idle duration has elapsed.
        if ((now - startMillisol) >= DURATION_MILLISOLS) {
            completed = true;
        }
    }

    /**
     * Indicates if the task has finished.
     *
     * @return true if complete; false otherwise
     */
    public boolean isFinished() {
        return completed;
    }
}
