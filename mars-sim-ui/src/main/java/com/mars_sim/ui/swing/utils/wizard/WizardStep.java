/*
 * Mars Simulation Project
 * WizardStep.java
 * @date 2026-02-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.wizard;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.core.tool.Msg;

/**
 * A generic wizard step implementation that holds a title and references the parent wizard.
 * The subclass is responsible for creating the visual content. As well as calling the {@link #setMandatoryDone(boolean)} method
 * once enough data has been entered to advance.
 */
public abstract class WizardStep<T> extends JPanel{

    private String title;
    private String instructions;
    private boolean mandatoryDone = false;
    private WizardPane<T> wizard;
    private String id;

    protected WizardStep(String id, WizardPane<T> parent) {
        this.id = id;
        this.wizard = parent;

        // Get the title and instructions from the message bundle based on the class name and ID
        var category = parent.getClass().getSimpleName().toLowerCase();
        this.title = Msg.getStringOptional(category, id);
        this.instructions = Msg.getStringWithFallback(category +  "." + id.toLowerCase() + ".instruction", "");

        setLayout(new BorderLayout());
    }

    
	/**
	 * Define an instruction to display under the title. This will override any automatically generated instruction
	 * based on the id.
	 * @param instructions Notes for user.
	 */
	protected void setInstructions(String instructions) {
		this.instructions = instructions;
	}

    public abstract void updateState(T state);

    /**
     * The next button should be enabled/disabled
     * @param enabled New state
     */
    protected void setMandatoryDone(boolean enabled) {
        this.mandatoryDone = enabled;
        wizard.updateButtonState();
    }

    /**
     * This will advance the step emulating the user activating the next button.
     * If only finish is active; then that is done.
     * @return True if the step was advanced, false if mandatory is missing
     */
    protected boolean advanceStep() {
        if (mandatoryDone) {
            wizard.advanceStep();
        }
        return mandatoryDone;
    }   

    /**
     * Get the title of this step displayed
     */
    String getTitle() {
        return title;
    }

    /**
     * Get the instructions for this step, displayed under the title. This may be null if no instructions are needed.
     * @return The instructions, or null
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Get the parent wizard
     * @return The wizard
     */
    protected WizardPane<T> getWizard() {
        return wizard;
    }

    /**
     * Clear any previous state from this step
     * @param state Current state
     */
    public void clearState(T state) {
        mandatoryDone = false;
    }

    /**
     * Get the unique ID of this step. Matches the logic in createStep method
     */
    public String getID() {
        return id;
    }

    /**
     * Have the mandatory fields been completed on this step
     * @return True if all completed
     */
    public boolean isMandatoryDone() {
        return mandatoryDone;
    }

    /**
     * The Step is no longer needed by the wizard and it's released. This implementation does nothing but
     * subclasses may override.
     */
    void release() {
        // Default implementation does nothing, but subclasses may override to release resources
    }
}
