/*
 * Mars Simulation Project – Clean Solar Panels Task
 * GPL-3.0
 */
package com.mars_sim.core.person.ai.task.maintenance;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Task;

/**
 * Minimal, compile-safe task that represents cleaning dusty solar panels.
 * Any actual power/efficiency side effects should be implemented inside the
 * power subsystem (e.g., a solar source dust/soiling factor this task can reset).
 */
public class CleanSolarPanels extends Task {

    public static final String NAME = "Clean Solar Panels";

    private final Building target;
    /** Simple work counter to avoid depending on other subsystems right now. */
    private double workRemainingMillisols;

    public CleanSolarPanels(Person worker, Building target) {
        // This branch's Task constructor does not take TaskCategory.
        super(NAME, worker);
        this.target = target;
        // Keep the first implementation simple: complete after ~10 millisols of work.
        this.workRemainingMillisols = 10.0;
    }

    /**
     * Primary Task hook used by the Task engine in this branch.
     * Called every tick with elapsed millisols.
     */
    @Override
    protected void performMappedPhase(double millisols) {
        if (millisols <= 0) return;

        workRemainingMillisols -= millisols;
        if (workRemainingMillisols <= 0) {
            // When the simple work budget is exhausted, finish the task.
            endTask();
        }
    }

    /** Human-readable description used by the UI. */
    public String getDescription() {
        String where = (target != null) ? target.getName() : "target building";
        return "Cleaning dust off solar panels at " + where + ".";
    }
}
