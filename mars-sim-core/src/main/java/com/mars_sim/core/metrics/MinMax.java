/*
 * Mars Simulation Project
 * MinMax.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

/**
 * Calculator implementation that tracks the minimum and maximum values
 * of all data points it receives.
 */
public class MinMax implements Calculator {
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private boolean hasData = false;
    
    /**
     * Accepts a data point and updates the min/max values accordingly.
     * 
     * @param value The data point to process
     */
    @Override
    public void accept(DataPoint value) {
        if (value != null) {
            double val = value.getValue();
            if (!hasData) {
                min = val;
                max = val;
                hasData = true;
            } else {
                if (val < min) {
                    min = val;
                }
                if (val > max) {
                    max = val;
                }
            }
        }
    }
    
    /**
     * Gets the minimum value of all data points processed so far.
     * 
     * @return The minimum value, or Double.NaN if no data points have been processed
     */
    public double getMin() {
        return hasData ? min : Double.NaN;
    }
    
    /**
     * Gets the maximum value of all data points processed so far.
     * 
     * @return The maximum value, or Double.NaN if no data points have been processed
     */
    public double getMax() {
        return hasData ? max : Double.NaN;
    }
    
    /**
     * Gets the range (difference between max and min) of all data points.
     * 
     * @return The range, or Double.NaN if no data points have been processed
     */
    public double getRange() {
        return hasData ? max - min : Double.NaN;
    }
    
    /**
     * Checks if this calculator has processed any data points.
     * 
     * @return true if data has been processed, false otherwise
     */
    public boolean hasData() {
        return hasData;
    }
    
    @Override
    public String toString() {
        if (!hasData) {
            return "MinMax{no data}";
        }
        return String.format("MinMax{min=%.2f, max=%.2f, range=%.2f}", min, max, getRange());
    }
}