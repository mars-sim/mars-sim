/*
 * Mars Simulation Project
 * IdleMaintenanceTask.java
 * @date 2025-08-28
 * @author Contributors
 *
 * A lightweight, low-priority task settlers perform when idle.
 * This build-safe version fixes PR #1693’s CI break by importing the correct
 * Task base class and avoiding calls to non-guaranteed maintenance APIs.
 * It also provides a small morale/fitness benefit while the task runs.
 */

package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.time.MarsClock;

/**
 * Represents simple upkeep/inspection during idle periods.
 * The task runs for a short fixed duration (in millisols) and then finishes.
 * It intentionally avoids compile-time coupling to optional maintenance systems
 * so it can build cleanly across branches.
 */
public class IdleMaintenanceTask extends Task {

    /** Serial ID. */
    private static final long serialVersionUID = 1L;

    /** Duration to spend on maintenance (in millisols). */
    private static final double DURATION_MILLISOLS = 50.0;

    /** Worker performing this task. */
    private final Person person;

    /** Millisol when work began; NaN until first perform() tick. */
    private double startMillisol = Double.NaN;

    /** Completion flag. */
    private boolean completed;

    /**
     * Creates an IdleMaintenanceTask for the given person.
     * @param person the worker who will perform the task
     */
    public IdleMaintenanceTask(Person person) {
        super(person);
        this.person = person;
        setName("Idle Maintenance");
        setDescription("Performing small upkeep while idle.");
    }

    /**
     * Advances the task. Uses the master clock's Mars time to measure elapsed work.
     * Also provides a tiny wellness benefit to reflect low-effort upkeep activity.
     */
    @Override
    public void perform() {
        if (completed) return;

        // Current mission time (millisols within the current sol).
        final MarsClock clock = Simulation.instance()
                                          .getMasterClock()
                                          .getMarsClock();
        final double now = clock.getMillisol();

        // Initialize start time on first tick.
        if (Double.isNaN(startMillisol)) {
            startMillisol = now;
        }

        // Small wellness benefit while doing light upkeep.
        // Guard against nulls; these methods exist on PhysicalCondition.
        if (person != null && person.getPhysicalCondition() != null) {
            // Ease stress/fatigue a touch to model “tidy-up” type chores.
            person.getPhysicalCondition().reduceStress(0.5);
            person.getPhysicalCondition().reduceFatigue(0.5);
        }

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
