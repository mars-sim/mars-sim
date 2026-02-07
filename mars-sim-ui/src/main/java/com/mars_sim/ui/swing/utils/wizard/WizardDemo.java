package com.mars_sim.ui.swing.utils.wizard;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.utils.AttributePanel;

public class WizardDemo extends WizardPane<Properties> {

    private static final Logger logger = Logger.getLogger(WizardDemo.class.getName());
    private static final String PREFIX = "STEP_";
    private static final String TABLE = "TABLE";
    private static final List<String> ANIMALS = List.of("Cat", "Dog", "Mouse", "Horse", "Cow");

    private static class TestItemStep extends WizardItemStep<Properties, String> {

        private static final int TARGET_SIZE = 3;
        private static final List<ColumnSpec> COLUMNS = List.of(
                new ColumnSpec("Item", String.class),
                new ColumnSpec("Length", Double.class, ColumnSpec.STYLE_PERCENTAGE)
        );
        protected TestItemStep(WizardPane<Properties> wizard) {
            super(TABLE, wizard,
                    new WizardItemModel<String>(COLUMNS) {
                        {
                            setItems(ANIMALS);
                        }

                        @Override
                        protected boolean isFailureCell(String item, int column) {
                            return ((column == 1) && item.length() > TARGET_SIZE);
                        }

                        @Override
                        protected Object getItemValue(String item, int column) {
                            if (column == 0) {
                                return item;
                            }
                            else {
                                return (item.length() * 100D)/TARGET_SIZE;
                            }
                        }
                    },
                    1, 1);
        }

        @Override
        protected void updateState(Properties state, List<String> selectedItems) {
            state.setProperty(TABLE, selectedItems.toString());
        }
    }

    /**
     * Steps that takes a numberic value
     */
    private static class TestWizardStep extends WizardStep<Properties> {

        private int stepNumber;
        private SpinnerNumberModel spinnerModel;

        protected TestWizardStep(WizardPane<Properties> wizard, int stepNumber, Properties status) {
            super(PREFIX + stepNumber, wizard);
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
                steps.add(TABLE);
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
        logger.info("Final state = " + state);
    }

    @Override
    protected WizardStep<Properties> createStep(String initialStep, Properties state) {
        if (TABLE.equals(initialStep)) {
            return new TestItemStep(this);
        }
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
