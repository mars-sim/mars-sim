/*
 * Mars Simulation Project
 * MetricManager.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.metrics.memory.MemoryMetric;
import com.mars_sim.core.time.MarsTime;

/**
 * Central manager for all metrics in the system. Provides methods to store,
 * retrieve, and manage metrics data organized by entity, category, and measure.
 */
public class MetricManager implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Map<MetricKey, Metric> metrics;
    private transient Set<MetricManagerListener> listeners = null;
    
    /**
     * Creates a new MetricManager.
     */
    public MetricManager() {
        this.metrics = new HashMap<>();
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

    /**
     * Returns all categories used. Can be filtered by entity.
     * 
     * @param asset The entity to filter by, or null for all entities
     * @return List of categories choosen
     */
    public List<MetricCategory> getCategories(Entity asset) {
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
     * @param tempCat The category to get entities for
     * @return List of entities that use the specified category
     */
    public List<Entity> getEntities(MetricCategory tempCat) {
        return metrics.keySet().stream()
                .filter(key -> (tempCat == null) || key.category().equals(tempCat))
                .map(MetricKey::asset)
                .distinct()
                .toList();
    }
    
    /**
     * Gets a metric for the specified entity, category, and measure.
     * If the metric doesn't exist, a new one is created.
     * 
     * @param asset The entity
     * @param tempCat The category
     * @param measure The measure
     * @return The metric for the specified parameters
     */
    public Metric getMetric(Entity asset, MetricCategory tempCat, String measure) {
        MetricKey key = new MetricKey(asset, tempCat, measure);
        var m = metrics.get(key);
        if (m == null) {
            m = createMetric(key);
            metrics.put(key, m);

            if (listeners != null) {
                for (var listener : listeners) {
                    listener.newMetric(m);
                }
            }
        }
        return m;
    }
    
    /**
     * Add a listener to be notified when new metrics are created.
     * @param listener
     */
    public void addListener(MetricManagerListener listener) {
        if (listeners == null) {
            listeners = new HashSet<>();
        }
        listeners.add(listener);
    }

    /**
     * Remove a previously added listener.
     * @param listener
     */
    public void removeListener(MetricManagerListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
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
    public void addValue(Entity asset, MetricCategory category, String measure, double value) {
        var m = getMetric(asset, category, measure);
        m.recordValue(value);
    }
    
    /**
     * Gets all measures for a specific entity and category.
     * 
     * @param asset The entity
     * @param tempCat The category
     * @return List of measure names for the specified entity and category
     */
    public List<String> getMeasures(Entity asset, MetricCategory tempCat) {
        return metrics.keySet().stream()
                .filter(key -> key.asset().equals(asset) && key.category().equals(tempCat))
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