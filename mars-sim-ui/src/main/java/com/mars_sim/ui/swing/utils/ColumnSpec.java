/*
 * Mars Simulation Project
 * ColumnSpec.java
 * @date 2024-03-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.io.Serializable;

/**
 * Convience class to define the specification of a column.
 */
public record ColumnSpec (String name, Class<?> type) implements Serializable {}

