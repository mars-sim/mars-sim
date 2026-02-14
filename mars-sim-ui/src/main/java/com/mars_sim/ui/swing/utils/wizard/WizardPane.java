/*
 * Mars Simulation Project
 * WizardPane.java
 * @date 2026-02-01
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;

/**
 * A generic wizard pane implementation that holds a central state class and follows a
 * sequence of WizardSteps.
 * The subclass must implement the factory pattern to create WizardSteps and a method to finish the wizard.
 * @param <T> The wizard state type.
 */
public abstract class WizardPane<T> {

    private T status;
    private UIContext context;
    private List<WizardStep<T>> previousSteps = new ArrayList<>();
    private List<String> steps = null;
    private WizardStep<T> currentStep;
    private JPanel content;
    private JLabel stepTitleLabel;
    private JLabel instructionLabel;
    private JButton backButton;
    private JButton nextButton;
    private JButton finishButton;
    private String title;
    private JPanel mainPane;
    private JDialog dialog;

    protected WizardPane(String title, UIContext context, T state, String initialStep) {
        this.title = title;
        this.status = state;
        this.context = context;

        buildUI();

        setCurrentStep(createStep(initialStep, state));
    }

    /**
     * Get the UI context
     * @return Context for other controls.
     */
    protected UIContext getContext() {
        return context;
    }

    /**
     * This creates a new step based on the current wizard
     * @param initialStep
     * @return
     */
    protected abstract WizardStep<T> createStep(String initialStep, T state);

    private void buildUI() {
        mainPane = new JPanel(new BorderLayout());

        stepTitleLabel = new JLabel();
        StyleManager.applySubHeading(stepTitleLabel);
        stepTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		stepTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPane.add(stepTitleLabel, BorderLayout.NORTH);

        var buttonPanel = new JPanel();
        mainPane.add(buttonPanel, BorderLayout.SOUTH);

        var cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> cancel());
        buttonPanel.add(cancelButton);
        backButton = new JButton("Back");
        backButton.addActionListener(e -> previousActioned());  
        buttonPanel.add(backButton);
        nextButton = new JButton("Next");
        nextButton.addActionListener(e -> nextActioned());
        buttonPanel.add(nextButton);
        finishButton = new JButton("Finish");
        finishButton.addActionListener(e -> finishedActioned());
        buttonPanel.add(finishButton);

        content = new JPanel(new BorderLayout());
        content.setBorder(new MarsPanelBorder());

        instructionLabel = new JLabel();
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setFont(StyleManager.getLabelFont());
        content.add(instructionLabel, BorderLayout.NORTH);
        
        mainPane.add(content, BorderLayout.CENTER);
    }

    /**
     * Next button pressed. Advance if possible saving the state.
     */
    private void nextActioned() {
        currentStep.updateState(status);

        if (steps == null) {
            throw new IllegalStateException("WizardPane subclass must implement steps list");
        }
        
        int idx = steps.indexOf(currentStep.getID());
        if (idx < 0 || idx + 1 >= steps.size()) {
            throw new IllegalStateException("No next step defined for " + currentStep.getID());
        }
        String nextStepName = steps.get(idx + 1);
        var nextStep = createStep(nextStepName, status);

        if (nextStep == null) {
            finishedActioned();
            return;
        }

        previousSteps.add(currentStep);
        setCurrentStep(nextStep);
    }

    /**
     * Previous button pressed. Go back to prior step.
     */
    private void previousActioned() {
        // Clear anything set by the current step
        currentStep.clearState(status);
        currentStep.release();

        // Implementation for going to the previous step
        var previousStep = previousSteps.remove(previousSteps.size() - 1);
        setCurrentStep(previousStep);
    }

    /**
     * Finish the wizard via user action
     */
    private void finishedActioned() {
        currentStep.updateState(status);
        finish(status);
        complete();
    }

    /**
     * Wizard is complete so tidy up.
     */
    private void complete() {
        if (dialog != null) {
            dialog.dispose();
        }

        // Release the steps
        if (currentStep != null) {
            currentStep.release();
        }
        for (var step : previousSteps) {
            step.release();
        }
    }

    /**
     * User has triggered the cancel action. Dispose of the wizard without saving any state.
     * This can be overridden.
     */
    protected void cancel() {
        complete();
    }

    /**
     * Show the wizard in a standalone frame.
     * @param parent
     * @return
     */
    protected JDialog showInDialog(JFrame parent) {
        dialog = new JDialog(parent, title);
        dialog.setContentPane(mainPane);
       	dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				cancel();
			}
		});

        return dialog;
    }

    /**
     * Update the control buttons based on the current step
     */
    void updateButtonState() {
        backButton.setEnabled(!previousSteps.isEmpty());

        // Check the last step
        boolean lastStep = false;
        if (steps != null) {
            int idx = steps.indexOf(currentStep.getID());
            lastStep = (idx == (steps.size() - 1));
        }

        var mandatoryDone = currentStep.isMandatoryDone();
        nextButton.setEnabled(mandatoryDone && !lastStep);
        finishButton.setEnabled(mandatoryDone && lastStep);
    }

    /**
     * Set the current step to be displayed  
     * @param step Next step to render
     */
    private void setCurrentStep(WizardStep<T> step) {
        this.currentStep = step;

        int idx = 1;
        String max = "?";
        if (steps != null) {
            idx = steps.indexOf(currentStep.getID()) + 1;
            if (idx <= 0) {
                throw new IllegalStateException("Current step " + currentStep.getID() + " not in steps list");
            }
            max = Integer.toString(steps.size());

        }
        var stepTitle = currentStep.getTitle() + " (" + idx + " out of " + max + ")";
        stepTitleLabel.setText(stepTitle);

        var instructions = currentStep.getInstructions();
        instructionLabel.setText(instructions);
        updateButtonState();

        // Update content panel with current step's UI
        if (content.getComponentCount() > 1) {
            content.remove(1);
        }
        content.add(currentStep, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    /**
     * Define the sequence of steps to follow
     */
    public void setStepSequence(List<String> steps) {
        this.steps = new ArrayList<>(steps);
    }

    /**
     * Finish the wizard processing
     * @param state Final end state
     */
    protected abstract void finish(T state);

    
    /**
     * This will advance the wizard emulating the user activating the next button.
     * If only finish is active; then that is done.
     */
    void advanceStep() {
        if (nextButton.isEnabled()) {
            nextActioned();
        }
        else if (finishButton.isEnabled()) {
            finishedActioned();
        }
        else {
            throw new IllegalStateException("Cannot advance step, mandatory not done");
        }
    }
}
