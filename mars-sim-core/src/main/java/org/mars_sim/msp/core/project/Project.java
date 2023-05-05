/**
 * Mars Simulation Project
 * Project.java
 * @date 2023-05-05
 * @author Barry Evans
 */
package org.mars_sim.msp.core.project;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.person.ai.task.util.Worker;

/**
 * Represents a project that has a number of steps
 */
public class Project {
    private String name;
    private List<ProjectStep> steps = new ArrayList<>();
    private ProjectStep currentStep = null;
    private boolean isDone = false;
    private boolean isAborted = false;

    public Project(String name) {
        this.name = name;
    }

    /**
     * Get the name of the Project
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get the current stage
     * @return
     */
    public Stage getStage() {
        Stage stage = Stage.WAITING;
        if (isDone) {
            stage = Stage.DONE;
        }
        else if (isAborted) {
            stage = Stage.ABORTED;
        }
        else if (currentStep != null) {
            stage = currentStep.getStage();
        }
        return stage;
    }

    /**
     * A worker executes the project
     * @param worker
     */
    public void execute(Worker worker) {
        if (currentStep == null) {
            currentStep = steps.get(0);
            currentStep.start();
        }
        currentStep.execute(worker);
    }

    /**
     * The current step is completed
     * @param worker
     */
    void completeCurrentStep() {   
        if (!isAborted) {  
            int idx = steps.indexOf(currentStep);
            idx++;

            if (idx >= steps.size()) {
                isDone = true;
            }
            else {
                currentStep = steps.get(idx);
                currentStep.start();
            }
        }
    }

    /**
     * Abort the project
     */
    public void abort(String reason) {
        isAborted = true;

        if (currentStep != null) {
            currentStep.complete();
        }
    }

    /** 
     * Add a step to this project
     * @param step
     * @see ProjectStep#setParent(Project)
     */
    public void addStep(ProjectStep step) {
        // Check the Stage is now regressing
        if (!steps.isEmpty()) {
            int lastStage = steps.get(steps.size()-1).getStage().ordinal();
            if (lastStage > step.getStage().ordinal()) {
                throw new IllegalArgumentException("The step can not move the Stage backwards" + step.getStage());
            }
        }

        steps.add(step);
        step.setParent(this);
    }

    /**
     * Remove a registered step. Can not be a step already executed
     * @param oldStep
     * @return Returns false is the step has already been executed
     */
    public boolean removeStep(ProjectStep oldStep) {
        int oldIdx = steps.indexOf(oldStep);
        if (oldIdx >= 0 && (currentStep != null)) {
            int currentIdx = steps.indexOf(currentStep);
            if (oldIdx <= currentIdx) {
                return false;
            }
        }

        return steps.remove(oldStep);
    }

    /**
     * Get the description of the current project step
     * @return
     */
    public String getStepName() {
        return (currentStep != null ? currentStep.getDescription() : null);
    }

    public boolean isFinished() {
        return (isDone || isAborted);
    }
}
