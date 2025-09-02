/*
 * GPL-3.0
 */
package com.mars_sim.core.person.ai.task.maintenance.meta;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.maintenance.CleanSolarPanels;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;

/**
 * Meta class to expose the CleanSolarPanels task to the tasking system.
 * Uses the (deprecated but still available) probability hook for compatibility.
 */
public class CleanSolarPanelsMeta extends FactoryMetaTask {

    public CleanSolarPanelsMeta() {
        super(CleanSolarPanels.NAME);
    }

    /** Deprecated in newer branches, still present in the build (see warnings). */
    public double getProbability(Person p) {
        // Start conservative until a real solar-dust signal is available.
        return 0.0;
    }

    /**
     * Provide common factory hooks used by different branches.
     * We intentionally avoid @Override to be source-compatible whether the parent
     * declares 'instantiate', 'constructInstance', or 'createTask'.
     */
    public Task instantiate(Person p) {
        return new CleanSolarPanels(p, null);
    }

    public Task constructInstance(Person p) {
        return instantiate(p);
    }

    public Task createTask(Person p) {
        return instantiate(p);
    }
}
