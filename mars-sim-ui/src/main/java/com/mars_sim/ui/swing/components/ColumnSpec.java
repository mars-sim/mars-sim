/*
 * Mars Simulation Project
 * ColumnSpec.java
 * @date 2024-03-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.components;


/**
 * A record class to define the specification of a column.
 */
public record ColumnSpec (int id, String name, Class<?> type, int style) {
    public static final int STYLE_DEFAULT = -1;
    public static final int STYLE_CURRENCY = 0;
    public static final int STYLE_INTEGER = 1;
    public static final int STYLE_DIGIT1 = 2;
    public static final int STYLE_DIGIT2 = 3;
    public static final int STYLE_DIGIT3 = 4;
    public static final int STYLE_PERCENTAGE = 5;

    public ColumnSpec(String name, Class<?> type) {
        this(-1, name, type, STYLE_DEFAULT);
    }

    public ColumnSpec(String name, Class<?> type, int style) {
        this(-1, name, type, style);
    }
}

