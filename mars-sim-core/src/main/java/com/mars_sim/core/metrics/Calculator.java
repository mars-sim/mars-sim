/*
 * Mars Simulation Project
 * Calculator.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

/**
 * Interface for calculating metrics from data points using the Visitor pattern.
 * Different implementations can provide different types of calculations (average, min/max, etc.).
 */
public interface Calculator {
    
    /**
     * Accepts a data point and processes it according to the specific calculation implementation.
     * This method implements the Visitor pattern, allowing different calculators to process
     * data points in their own way.
     * 
     * @param value The data point to process
     */
    void accept(DataPoint value);
}