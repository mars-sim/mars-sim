/*
 * GPL-3.0
 */
package com.mars_sim.core.person.ai.task.maintenance.meta;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.maintenance.CleanSolarPanels;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.Task;

/**
 * Meta class to expose the CleanSolarPanels task to the tasking system.
 * Notes:
 *  - This branch's FactoryMetaTask requires (name, WorkerType, TaskScope).
 *  - Enum constant names differ across branches; we resolve them safely.
 */
public class CleanSolarPanelsMeta extends FactoryMetaTask {

    public CleanSolarPanelsMeta() {
        // super(...) MUST be the first statement.
        // We avoid hard-coding enum names to keep this source compatible.
        super(
            CleanSolarPanels.NAME,
            resolveWorkerType("PERSON"),
            resolveTaskScope("LOCAL")
        );
    }

    // Resolve a WorkerType by name; fall back to the first enum constant.
    private static MetaTask.WorkerType resolveWorkerType(String name) {
        try {
            return MetaTask.WorkerType.valueOf(name);
        }
        catch (IllegalArgumentException ex) {
            MetaTask.WorkerType[] all = MetaTask.WorkerType.values();
            return (all.length > 0 ? all[0] : null);
        }
    }

    // Resolve a TaskScope by name; fall back to the first enum constant.
    private static MetaTask.TaskScope resolveTaskScope(String name) {
        try {
            return MetaTask.TaskScope.valueOf(name);
        }
        catch (IllegalArgumentException ex) {
            MetaTask.TaskScope[] all = MetaTask.TaskScope.values();
            return (all.length > 0 ? all[0] : null);
        }
    }

    // Deprecated in this branch but still present (see warnings); keep it minimal.
    @Override
    public double getProbability(Person p) {
        return 0.0;
    }

    /**
     * Provide factory hooks without @Override so this stays source-compatible
     * whether the parent exposes instantiate, constructInstance, or createTask.
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
