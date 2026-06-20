/*
 * Mars Simulation Project
 * MemoryMetricManager.java
 * @date 2026-06-19
 * @author Barry Evans
 */
package com.mars_sim.core.metrics.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.Entity;
import com.mars_sim.core.metrics.Metric;
import com.mars_sim.core.metrics.MetricCategory;
import com.mars_sim.core.metrics.MetricKey;
import com.mars_sim.core.metrics.MetricManager;

/**
 * In-memory metric manager that creates {@link MemoryMetric} instances.
 */
public class MemoryMetricManager extends MetricManager {

    private static final long serialVersionUID = 1L;
    private Map<MetricKey, Metric> metrics;

    public MemoryMetricManager() {
        super();
        metrics = new HashMap<>();
    }

    /**
     * Reinitializes the MetricManager after deserialization.
     */
    public void reinit() {
        //There is a problem with dserializing the HashMap.The saved hashcode for the the same key
        // is different after deserialization, so we need to rebuild the map.
        var newMetrics = new HashMap<MetricKey, Metric>(); 
        for (var entry : metrics.entrySet()) {
            newMetrics.put(entry.getKey(), entry.getValue());
        }
        this.metrics = newMetrics;
    }

    @Override
    public List<MetricCategory> getCategories(Entity asset) {
        return metrics.keySet().stream()
                .filter(key -> (asset == null) || key.asset().equals(asset))
                .map(MetricKey::category)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public List<Entity> getEntities(MetricCategory tempCat) {
        return metrics.keySet().stream()
                .filter(key -> (tempCat == null) || key.category().equals(tempCat))
                .map(MetricKey::asset)
                .distinct()
                .toList();
    }

    /**
     * Gets all measures for a specific entity and category.
     * 
     * @param asset The entity
     * @param tempCat The category
     * @return List of measure names for the specified entity and category
     */
    @Override
    public List<String> getMeasures(Entity asset, MetricCategory tempCat) {
        return metrics.keySet().stream()
                .filter(key -> key.asset().equals(asset) && key.category().equals(tempCat))
                .map(MetricKey::measure)
                .toList();
    }

    /**
     * Get the known metrics
     */
    @Override
    public Set<MetricKey> getMetrics() {
        return metrics.keySet();
    }

    /**
     * Gets a metric for the specified key.
     * If the metric doesn't exist, a new one is created.
     * 
     * @param key The metric key
     * @return The metric for the specified key
     */
    @Override
    public Metric getMetric(MetricKey key) {
        var m = metrics.get(key);
        if (m == null) {
            m = new MemoryMetric(key);
            metrics.put(key, m);
            notifyListeners(m);
        }
        return m;
    }
}
