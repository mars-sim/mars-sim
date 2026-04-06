/*
 * Mars Simulation Project
 * JIntegerLabel.java
 * @date 2026-01-18
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import javax.swing.JLabel;

/**
 * A JLabel that formats an integer value.
 * The current numerical value is cached to avoid unnecessary updates to the label text.
 * The label updates its text only when the value changes, to minimize unnecessary updates.
 */
public class JIntegerLabel extends JLabel {

    private int currentValue = Integer.MIN_VALUE;

    /**
     * Construct a JIntegerLabel with the default value
     */
    public JIntegerLabel() {
        super();
    }

    /**
     * Construct a JIntegerLabel with the specified initial value.
     * @param initialValue Initial value to set.
     */
    public JIntegerLabel(int initialValue) {
        setValue(initialValue);
    }

    /**
     * Sets the value of the label, updating the text only if the value has changed.
     * The format specified in the constructor is used to format the value.
     * @param value New value to set.
     */
    public void setValue(int value) {
        // Use a fuzzy equals as in many cases the value may be a computed value
        // that introduces some floating point precision issues.
        if (value == currentValue) {
            return;
        }

        setText(Integer.toString(value));
        this.currentValue = value;
    }
}
