/*
 * Mars Simulation Project
 * WizardStep.java
 * @date 2026-02-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * A generic wizard step implementation that holds a title and references the parent wizard.
 * The subclass is responsible for creating the visual content. As well as calling the {@link #setMandatoryDone(boolean)} method
 * once enough data has been entered to advance.
 */
public abstract class WizardStep<T> extends JPanel{

    private String title;
    private boolean mandatoryDone = false;
    private WizardPane<T> wizard;
    private String id;

    protected WizardStep(String id, String title, WizardPane<T> parent) {
        this.id = id;
        this.title = title;
        this.wizard = parent;

        setLayout(new BorderLayout());
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
     * Get the title of this step displayed
     */
    public String getTitle() {
        return title;
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
}
