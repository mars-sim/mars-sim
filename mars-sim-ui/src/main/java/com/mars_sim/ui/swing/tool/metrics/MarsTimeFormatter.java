package com.mars_sim.ui.swing.tool.metrics;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MarsTimeFormat;

/**
 * Formats a number as a MarsTime string.
 * This is not good design subclassing a standard class but there is no other way
 * to plug in a custom formatter into JFreeChart.
 */
class MarsTimeFormatter extends NumberFormat {

    /**
     * Returns a string representing the value which is total millisol.
     *
     * @param millisols  the millisols.
     *
     * @return A string.
     */
    private String getMarsTime(double milisol) {
        MarsTime mt = new MarsTime(milisol);
        return MarsTimeFormat.getTruncatedDateTimeStamp(mt);
    }

    /**
     * Formats a number into the specified string buffer.
     *
     * @param number  the number to format.
     * @param toAppendTo  the string buffer.
     * @param pos  the field position (ignored here).
     *
     * @return The string buffer.
     */
    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo,
            FieldPosition pos) {
        return toAppendTo.append(getMarsTime(number));
    }

    /**
     * Formats a number into the specified string buffer.
     *
     * @param number  the number to format.
     * @param toAppendTo  the string buffer.
     * @param pos  the field position (ignored here).
     *
     * @return The string buffer.
     */
    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo,
            FieldPosition pos) {
        return toAppendTo.append(getMarsTime(number));
    }

    /**
     * This method returns {@code null} for all inputs.  This class cannot
     * be used for parsing.
     *
     * @param source  the source string.
     * @param parsePosition  the parse position.
     *
     * @return {@code null}.
     */
    @Override
    public Number parse(String source, ParsePosition parsePosition) {
        return null;
    }
}
