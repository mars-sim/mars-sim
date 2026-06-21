/*
 * Mars Simulation Project
 * MetricGroup.java
 * @date 2026-05-20
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;

/** 
 * This class represents a group of metrics for a specific category.
 * It allows recording values for different measures and provides a breakdown of totals by sol.
 */
public class MetricGroup implements Serializable {
    private MetricCategory category;
    private transient Map<String, Metric> metrics = new HashMap<>();

    public MetricGroup(MetricCategory category) {
        this.category = category;
    }

    /**
     * Records a value for a specific measure and owner entity. If the metric for the measure does not exist, it is created.
     * @param measure The measure name for the metric.
     * @param value The value to record for the metric.
     * @param owner The entity that owns the metric if a new one is required.
     */
    public void recordValue(String measure, double value, Entity owner) {
        // Get cached or find a real metric for this measure
        var found = metrics.computeIfAbsent(measure, k ->
                    Simulation.instance().getMetricManager().getMetric(owner, category, measure));
        found.recordValue(value);
    }

    /**
     * Get the totals for each sol, broken down by measure. This is useful for reporting and analysis.
     * It uses the Total calculator to get the totals for each sol.
     * @return Map keys on mission sol
     */
    public Map<Integer, Map<String, Double>> getSolBreakdown() {
        Map<Integer, Map<String, Double>> solBreakdown = new HashMap<>();
        for(Metric metric : metrics.values()) {
            var results = metric.applyBySol(0, sol -> new Total());
            for(var entry: results.entrySet()) {
                int sol = entry.getKey();
                double value = entry.getValue().getSum();
                solBreakdown.computeIfAbsent(sol, k -> new HashMap<>()).put(metric.getKey().measure(), value);
            }
        }
        return solBreakdown;
    }

    /**
     * Custom deserialization logic to reinitialize the transient metrics map after deserialization.
     * @param ois
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.metrics = new HashMap<>();
    }
}
