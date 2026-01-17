/*
 * Mars Simulation Project
 * JFormatLabel.java
 * @date 2026-01-11
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;

import java.text.DecimalFormat;

import javax.swing.JLabel;

import com.google.common.math.DoubleMath;

/**
 * A JLabel that formats a double value according to a specified DecimalFormat.
 * The current numerical value is cached to avoid unnecessary updates to the label text.
 * The label updates its text only when the value changes, to minimize unnecessary updates.
 * The ere a @ref TOLERANCE constant to define the precision for value comparison.
 */
public class JDoubleLabel extends JLabel {

    private double tolerance = 0.0001;
    private double currentValue = Integer.MIN_VALUE;
    private DecimalFormat formatter;

    /**
     * Construct a JFormatLabel with the specified format.
     * @param format Format to use when displaying values.
     */
    public JDoubleLabel(DecimalFormat format) {
        super();
        this.formatter = format;
    }

    /**
     * Construct a JFormatLabel with the specified format and initial value.
     * @param format Format to use when displaying values.
     * @param value Initial value to set.
     */
    public JDoubleLabel(DecimalFormat format, double value) {
        this(format);
        setValue(value);
    }

    /**
     * Construct a JFormatLabel with the specified format, initial integer value, and tolerance.
     * @param format Format for value.
     * @param value Initial value
     * @param tolerance Tolerance for fuzzy equality between current and new values.
     */
    public JDoubleLabel(DecimalFormat format, double value, double tolerance) {
        this(format, value);
        setTolerance(tolerance);
    }
    /**
     * Set the tolerance for the fuzzy equality.
     * @param tolerance
     */
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * Sets the value of the label, updating the text only if the value has changed.
     * The format specified in the constructor is used to format the value.
     * @param value New value to set.
     */
    public void setValue(double value) {
        // Use a fuzzy equals as in many cases the value may be a computed value
        // that introduces some floating point precision issues.
        if (DoubleMath.fuzzyEquals(currentValue, value, tolerance)) {
            return;
        }

        setText(formatter.format(value));
        this.currentValue = value;
    }
}
