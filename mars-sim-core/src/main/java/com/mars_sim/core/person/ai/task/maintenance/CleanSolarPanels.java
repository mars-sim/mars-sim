/*
 * Mars Simulation Project – Clean Solar Panels Task
 * GPL-3.0
 */
package com.mars_sim.core.person.ai.task.maintenance;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskCategory;
import com.mars_sim.core.person.ai.task.util.TaskPhase;

/**
 * Minimal, compile-safe task that represents cleaning dusty solar panels.
 * (Side effects on solar efficiency should be implemented in the power subsystem.)
 */
public class CleanSolarPanels extends Task {

    public static final String NAME = "Clean Solar Panels";

    private final Building target;
    /** Simple work counter to avoid depending on other subsystems right now. */
    private double workRemainingMillisols;

    public CleanSolarPanels(Person worker, Building target) {
        // Category MAINTENANCE is consistent with other upkeep tasks.
        super(NAME, worker, TaskCategory.MAINTENANCE);
        this.target = target;
        // Keep the first implementation simple: one work phase and a small duration.
        this.workRemainingMillisols = 10.0; // finish after ~10 millisols of work
        addPhase(TaskPhase.WORK);
    }

    /**
     * Primary Task hook used by mars-sim's Task engine.
     * Intentionally no @Override to keep source-compatibility across branches.
     */
    protected void performMappedPhase(TaskPhase phase, double millisols) {
        // Ensure we stay in the WORK phase for this minimal task.
        if (phase != TaskPhase.WORK) {
            setPhase(TaskPhase.WORK);
            return;
        }

        if (millisols > 0) {
            workRemainingMillisols = Math.max(0, workRemainingMillisols - millisols);
        }

        if (workRemainingMillisols <= 0) {
            endTask();
        }
    }

    /** Human-readable description used by the UI. */
    public String getDescription() {
        String where = (target != null) ? target.getName() : "target building";
        return "Cleaning dust off solar panels at " + where + ".";
    }
}
