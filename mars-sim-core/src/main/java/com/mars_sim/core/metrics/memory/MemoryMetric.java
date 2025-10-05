/*
 * Mars Simulation Project
 * MemoryMetric.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.metrics.Calculator;
import com.mars_sim.core.metrics.DataPoint;
import com.mars_sim.core.metrics.Metric;
import com.mars_sim.core.metrics.MetricKey;

/**
 * Represents a metric where the data pointds are held in memory.
 * These are organized by Sol (Martian days); each one represents a series of data points for that Sol.
 */
public class MemoryMetric extends Metric {
    private final Map<Integer,Set<DataPoint>> solSeries;
    
    /**
     * Creates a new Metric with the specified key.
     * 
     * @param key The unique key identifying this metric
     */
    public MemoryMetric(MetricKey key) {
        super(key);
        this.solSeries = new HashMap<>();
    }

    /**
     * Gets the number of Sol series in this metric.
     * 
     * @return The number of Sol series
     */
    public Set<Integer> getSolRange() {
        return solSeries.keySet();
    }
    
    @Override
    public String toString() {
        return String.format("Metric InMemory{key=%s", getKey());
    }

    @Override
    protected void addDataPoint(int sol, DataPoint dataPoint) {
        var series = solSeries.computeIfAbsent(sol, k ->new HashSet<>());
        series.add(dataPoint);
    }

    @Override
    protected void applyCalculator(Integer sol, Calculator evaluator) {
        var series = solSeries.get(sol);
        if (series != null) {
            series.forEach(evaluator::accept);
        }
    }
}