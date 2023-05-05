/**
 * Mars Simulation Project
 * ProjectStep.java
 * @date 2023-05-05
 * @author Barry Evans
 */
package org.mars_sim.msp.core.project;

import org.mars_sim.msp.core.person.ai.task.util.Worker;

/**
 * This represents a step in a project. The execute method is overriden by the implementing class.
 */
public abstract class ProjectStep {

    private Project parent;
    private Stage stage;

    protected ProjectStep(Stage stage) {
        if ((stage == Stage.WAITING) || (stage == Stage.DONE)) {
            throw new IllegalArgumentException("The step can used the internal Stage " + stage);
        }

        this.stage = stage;
    }

    void setParent(Project parent) {
        this.parent = parent;
    }

    /**
     * The stage that this step represents
     * @return
     */
    public Stage getStage() {
        return stage;
    }

    protected Project getParent() {
        return parent;
    }
    
    /**
     * A worker performs the current step
     * @param worker
     */
    abstract void execute(Worker worker);


    /**
     * This step has just become the active step and is starting
     * @param worker Triggered the start
     */
    void start(Worker worker) {
        // Default requires no special start
    }

    /**
     * This step has just completed and is stopping
     * @param worker Triggered the stop
     */
    void complete(Worker worker) {
        parent.completeCurrentStep(worker);
    }
}
