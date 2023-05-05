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
    private String description;

    protected ProjectStep(Stage stage, String description) {
        if ((stage == Stage.WAITING) || (stage == Stage.DONE) || (stage == Stage.ABORTED)) {
            throw new IllegalArgumentException("The step can used the internal Stage " + stage);
        }

        this.stage = stage;
        this.description = description;
    }

    void setParent(Project parent) {
        this.parent = parent;
    }

    /**
     * The descriptin of this step
     * @return
     */
    public String getDescription() {
        return description;
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
    void start() {
        // Default requires no special start
    }

    /**
     * This step has just completed and is stopping
     * @param worker Triggered the stop
     */
    void complete() {
        parent.completeCurrentStep();
    }
}
