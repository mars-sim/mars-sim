package com.mars_sim.ui.swing.utils;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.mars_sim.ui.swing.UIContext;

public class WizardDemo extends WizardPane<Properties> {

    private static final String PREFIX = "STEP_";

    private static class TestWizardStep extends WizardStep<Properties> {

        private int stepNumber;
        private SpinnerNumberModel spinnerModel;

        protected TestWizardStep(WizardPane<Properties> wizard, int stepNumber, Properties status) {
            super(PREFIX + stepNumber, "Step " + stepNumber, wizard);
            this.stepNumber = stepNumber;

            var panel = new AttributePanel();
            add(panel, BorderLayout.CENTER);

            for(var v : status.stringPropertyNames()) {
                panel.addTextField(v, status.getProperty(v), null);
            }

            spinnerModel = new SpinnerNumberModel(stepNumber+2, 2, 10, 1);
            var spinner = new JSpinner(spinnerModel);
            spinner.addChangeListener(e -> setMandatoryDone(true));
            panel.addLabelledItem("Next", spinner);
        }

        @Override
        public void clearState(Properties state) {
            state.remove("step:" + stepNumber);
            super.clearState(state);
        }

        @Override
        public void updateState(Properties state) {
            int selected = (Integer) spinnerModel.getValue();

            state.setProperty("step:" + stepNumber, Integer.toString(selected));

            // Emulate the first step defining the full sequence
            if (stepNumber == 0) {
                List<String> steps = new ArrayList<>();
                for(int i = 0; i < selected; i++) {
                    steps.add(PREFIX + i);
                }
                getWizard().setStepSequence(steps);
            }
        }

        @Override
        public String toString() {
            return "TestWizardStep " + stepNumber;
        }
    }
    
    private WizardDemo(UIContext context, Properties status) {
        super("Wizard Harness", context, status, PREFIX + 0);
    }

    @Override
    protected void finish(Properties state) {
        System.out.print("Final state = " + state);
    }

    @Override
    protected WizardStep<Properties> createStep(String initialStep, Properties state) {
        var stepNumStr = initialStep.substring(PREFIX.length());
        var stepNum = Integer.parseInt(stepNumStr);
        return new TestWizardStep(this, stepNum, state);
    }

    public static void main(String[] args) {
        UIContext context = null;
        Properties status = new Properties();
        var wizard = new WizardDemo(context, status);
        wizard.setSize(600, 400);
        wizard.setVisible(true);
    }

}
