/*
 * Mars Simulation Project
 * MetricManager.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.metrics.memory.MemoryMetric;
import com.mars_sim.core.time.MarsTime;

/**
 * Central manager for all metrics in the system. Provides methods to store,
 * retrieve, and manage metrics data organized by entity, category, and measure.
 */
public class MetricManager {
    private Map<MetricKey, Metric> metrics;
    
    /**
     * Creates a new MetricManager.
     */
    public MetricManager() {
        this.metrics = new HashMap<>();
    }
    
    /**
     * Returns all categories used. Can be filtered by entity.
     * 
     * @param asset The entity to filter by, or null for all entities
     * @return List of categories choosen
     */
    public List<String> getCategories(Entity asset) {
        return metrics.keySet().stream()
                .filter(key -> (asset == null) || key.asset().equals(asset))
                .map(MetricKey::category)
                .distinct()
                .sorted()
                .toList();
    }
    
    /**
     * Returns all entities using a specific category.
     * 
     * @param category The category to get entities for
     * @return List of entities that use the specified category
     */
    public List<Entity> getEntities(String category) {
        return metrics.keySet().stream()
                .filter(key -> (category == null) || key.category().equals(category))
                .map(MetricKey::asset)
                .distinct()
                .toList();
    }
    
    /**
     * Gets a metric for the specified entity, category, and measure.
     * If the metric doesn't exist, a new one is created.
     * 
     * @param asset The entity
     * @param category The category
     * @param measure The measure
     * @return The metric for the specified parameters
     */
    public Metric getMetric(Entity asset, String category, String measure) {
        MetricKey key = new MetricKey(asset, category, measure);
        return metrics.computeIfAbsent(key, this::createMetric);
    }
    
    /**
     * Creates a new Metric instance based on the provided key.
     * Currently, all metrics are in-memory, but this could be extended to support other types
     * @param key
     * @return
     */
    private Metric createMetric(MetricKey key) {
        // For now, all metrics are in-memory. This could be extended to support other types.
        return new MemoryMetric(key);
    }
    
    /**
     * Adds a value to a metric. If the metric doesn't exist, it will be created.
     * 
     * @param asset The entity this value is for
     * @param category The category of the metric
     * @param measure The specific measure
     * @param value The value to add
     */
    public void addValue(Entity asset, String category, String measure, double value) {
        var m = getMetric(asset, category, measure);
        m.recordValue(value);
    }
    
    /**
     * Gets all metrics managed by this manager.
     * 
     * @return A map of all metrics keyed by their MetricKey
     */
    public Map<MetricKey, Metric> getAllMetrics() {
        return Collections.unmodifiableMap(metrics);
    }
    
    /**
     * Gets all measures for a specific entity and category.
     * 
     * @param asset The entity
     * @param category The category
     * @return List of measure names for the specified entity and category
     */
    public List<String> getMeasures(Entity asset, String category) {
        return metrics.keySet().stream()
                .filter(key -> key.asset().equals(asset) && key.category().equals(category))
                .map(MetricKey::measure)
                .toList();
    }
    
    @Override
    public String toString() {
        return String.format("MetricManager{metrics=%d}", metrics.size());
    }

    /**
     * What is the Mars time now?
     * @return
     */
    static MarsTime getNow() {
        // Needs solving
        return Simulation.instance().getMasterClock().getMarsTime();
    }
}