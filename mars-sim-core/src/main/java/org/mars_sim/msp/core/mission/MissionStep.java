/**
 * Mars Simulation Project
 * MissionStep.java
 * @date 2023-05-06
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission;

import java.util.Collections;
import java.util.Map;

import org.mars_sim.msp.core.project.ProjectStep;
import org.mars_sim.msp.core.project.Stage;

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
}
