/*
 * Mars Simulation Project
 * MemoryMetric.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final long serialVersionUID = 1L;

    private Map<Integer,List<DataPoint>> solSeries;
    private int size = 0;
    private int firstSol = Integer.MAX_VALUE;
    private int lastSol = Integer.MIN_VALUE;
    
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
    protected void addDataPoint(int sol, DataPoint dataPoint) {
        var series = solSeries.computeIfAbsent(sol, k -> new ArrayList<>());

        if (sol < firstSol) {
            firstSol = sol;
        }
        if (sol > lastSol) {
            lastSol = sol;
        }

        if (!series.isEmpty()) {
            var lastPoint = series.get(series.size() - 1);
            if (dataPoint.getWhen().equals(lastPoint.getWhen())) {
                // Last and new data point have same time so combine
                DataPoint replacement;
                if (getKey().category().replaceExist()) {
                    // Replace last value
                    replacement = dataPoint;
                }
                else {
                    // Create new point
                    replacement = new DataPoint(lastPoint.getWhen(),
                                        lastPoint.getValue() + dataPoint.getValue());
                }
                series.set(series.size() - 1, replacement);
                return;
            }
        }
        series.add(dataPoint);
        size++;
    }

    @Override
    protected void applyCalculator(Integer sol, Calculator evaluator) {
        var series = solSeries.get(sol);
        if (series != null) {
            series.forEach(evaluator::accept);
        }
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public DataPoint getDataPoint(int item) {
        // Find correct sol series
        for(int s = firstSol; s <= lastSol; s++) {
            var series = solSeries.get(s);
            if (series != null) {
                if (item < series.size()) {
                    return series.get(item);
                } else {
                    item -= series.size();
                }
            }
        }
        return null;
    }
}