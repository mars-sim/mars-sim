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
 * power subsystem (e.g., a solar-source soiling factor this task can reset).
 */
public class CleanSolarPanels extends Task {

    public static final String NAME = "Clean Solar Panels";

    private final Building target;
    /** Simple work counter so we don't depend on other subsystems here. */
    private double workRemainingMillisols;

    public CleanSolarPanels(Person worker, Building target) {
        // Use a Task constructor available in this branch:
        // Task(String, Worker, boolean, boolean, double, double)
        // The booleans and doubles are kept conservative/minimal.
        super(NAME, worker, /*interruptible*/ false, /*requiresEVA?*/ false,
              /*stressImpact*/ 0.0, /*fatigueImpact*/ 0.0);

        this.target = target;
        // Finish after ~10 millisols of "work".
        this.workRemainingMillisols = 10.0;
    }

    /**
     * Primary Task hook used by the Task engine in this branch.
     * Must return a double; we return the amount of work (millisols) consumed.
     */
    @Override
    protected double performMappedPhase(double millisols) {
        if (millisols <= 0) {
            return 0.0;
        }

        double used = Math.min(millisols, workRemainingMillisols);
        workRemainingMillisols -= used;

        if (workRemainingMillisols <= 0) {
            endTask();
        }

        return used;
    }

    /** Human-readable description used by the UI. */
    public String getDescription() {
        String where = (target != null) ? target.getName() : "target building";
        return "Cleaning dust off solar panels at " + where + ".";
    }
}
