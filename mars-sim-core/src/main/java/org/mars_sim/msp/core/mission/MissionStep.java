/**
 * Mars Simulation Project
 * MissionStep.java
 * @date 2023-05-06
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import java.util.Collections;
import java.util.Map;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.project.ProjectStep;
import org.mars_sim.msp.core.project.Stage;
import org.mars_sim.msp.core.robot.Robot;

/**
 * Represent a step that a Mission has to undertake. 
 * Must implement the execute method call when a Worker is active.
 * May also override the start & complete methods to implement any startup or cleardown logic.
 */
public abstract class MissionStep extends ProjectStep {

    private MissionProject project;

    protected MissionStep(MissionProject project, Stage stage, String description) {
        super(stage, description);
        this.project = project;
    }
    
    protected MissionProject getProject() {
        return project;
    }

    /**
     * Calcaulte what resources are needed for this step.
     * The return value may change once the step is active.
     * @return Map of resource id to quantity
     */
    Map<Integer,Number> getRequiredResources() {
        return Collections.emptyMap();
    }

    /**
     * Assign a Task to a Worker as part of this mission step
     * @param worker Worker looking to work
     * @param task Task allocated
     */
    protected boolean assignTask(Worker worker, Task task) {
        // Bit messy
        if (worker instanceof Robot r) {
            if (r.getMalfunctionManager().hasMalfunction() 
                    || !r.getSystemCondition().isBatteryAbove(5)) {
                return false;
            }
        }
        else if (worker instanceof Person p) {
            if (task.isEffortDriven() && (p.getPerformanceRating() == 0D)) {
                return false;
            }
        }

        worker.getTaskManager().addTask(task);
        return true;
    }
}
