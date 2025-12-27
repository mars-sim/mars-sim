/*
 * Mars Simulation Project
 * Metric.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

import com.mars_sim.core.time.MarsTime;

/**
 * Represents a metric that contains time series data organized by Sol (Martian days).
 * Implements the Visitor pattern to allow different calculators to process the data.
 */
public abstract class Metric {
    private final MetricKey key;
    
    /**
     * Creates a new Metric with the specified key.
     * 
     * @param key The unique key identifying this metric
     */
    protected Metric(MetricKey key) {
        this.key = key;
    }
    
    /**
     * Gets the metric key.
     * 
     * @return The metric key
     */
    public MetricKey getKey() {
        return key;
    }
    
    /**
     * Add a data point to this metric
     * @param value
     */
    public void recordValue(double value) {
        MarsTime now = getNow();
        int sol = now.getMissionSol();

        addDataPoint(sol, new DataPoint(now, value));
    }
    
    /**
     * Adds a data point to the series for the specified Sol.
     * @param sol The Sol (Martian day) number
     * @param dataPoint The data point to add
     */
    protected abstract void addDataPoint(int sol, DataPoint dataPoint);

    /**
     * Applies a calculator to every data point in this metric using the Visitor pattern.
     * The calculator will receive each data point sequentially and can accumulate results.
     * 
     * @param evaluator The calculator to apply to each data point
     * @return The same calculator instance after processing all data points
     */
    public <T extends Calculator> T apply(T evaluator) {
        
        for (var sol : getSolRange()) {
            applyCalculator(sol, evaluator);
        }
        return evaluator;
    }
    
    /**
     * Applies a calculator to data points for a specific Sol using the Visitor pattern.
     * The calculator will receive each data point for that Sol sequentially and can accumulate results.
     * @param sol Sol to scan
     * @param evaluator The calculator to apply to each data point
     */
    protected abstract void applyCalculator(Integer sol, Calculator evaluator);

    /**
     * Applies calculators to data points grouped by Sol using the Visitor pattern.
     * For each Sol in the specified range, a new calculator is created using the factory
     * and all data points for that Sol are processed by that calculator.
     * 
     * @param solRange Controls which sols to visit:
     *                 - Negative values: process the last N sols
     *                 - Positive values: process data for the specific Sol number
     *                 - Zero: process all sols
     * @param factory Function that creates a new Calculator for each Sol
     * @return A map where keys are Sol numbers and values are the calculators
     *         that processed data for that Sol
     */
    public <T extends Calculator> Map<Integer, T> applyBySol(int solRange, IntFunction<T> factory) {
        Map<Integer, T> results = new HashMap<>();

        for (Integer sol : getSeriesToProcess(solRange)) {
            var evaluator = factory.apply(sol);
            applyCalculator(sol, evaluator);

            results.put(sol, evaluator);
        }
        
        return results;
    }
    
    /**
     * Determines which Sol series to process based on the range parameter   
     *                 - Negative values: process the last N sols
     *                 - Positive values: process data for the specific Sol number
     *                 - Zero: process all sols
     * 
     * @param solRange The range specification
     * @return List of Sol s to process
     */
    private Set<Integer> getSeriesToProcess(int solRange) {
        if (solRange == 0) {
            // All
            return getSolRange();
        }
        
        if (solRange > 0) {
            return Set.of(solRange);
        }

        Set<Integer> sols = new HashSet<>();

        // Must be -vem Process last N sols
        int nowSol = getNow().getMissionSol();
        sols.add(nowSol);
        solRange++;  // Current sol already added

        // Keep adding previous sols until we reach the specified range
        for(; solRange < 0; solRange++) {
            sols.add(nowSol + solRange);
        }
        return sols;
    }

    private MarsTime getNow() {
        return MetricManager.getNow();
    }

    /**
     * Gets the Sol that are covered by this.
     * 
     * @return The range of Sols
     */
    public abstract Set<Integer> getSolRange();

    /**
     * Gets the number of data points in this metric.
     * 
     * @return The number of data points
     */
    public abstract int getSize();

    public abstract DataPoint getDataPoint(int item);
}