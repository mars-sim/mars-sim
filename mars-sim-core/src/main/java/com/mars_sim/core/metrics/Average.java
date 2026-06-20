/*
 * Mars Simulation Project
 * Average.java
 * @date 2025-10-04
 * @author Barry Evans
 */package com.mars_sim.core.metrics;

/**
 * Calculator implementation that computes the average of all data points it receives.
 */
public class Average implements Calculator {
    private double sum = 0.0;
    private int count = 0;
    
    /**
     * Accepts a data point and adds its value to the running sum.
     * 
     * @param value The data point to process
     */
    @Override
    public void accept(DataPoint value) {
        if (value != null) {
            sum += value.getValue();
            count++;
        }
    }
    
    /**
     * Gets the average of all data points processed so far.
     * 
     * @return The average value, or 0.0 if no data points have been processed
     */
    public double getAverage() {
        return count > 0 ? sum / count : 0.0;
    }
    
    /**
     * Gets the number of data points processed.
     * 
     * @return The count of data points
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Gets the sum of all data points processed.
     * 
     * @return The sum of values
     */
    public double getSum() {
        return sum;
    }
    
    @Override
    public String toString() {
        return String.format("Average{average=%.2f, count=%d}", getAverage(), count);
    }
}