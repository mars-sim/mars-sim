/*
 * Mars Simulation Project
 * MetricManager.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.time.MarsTime;

/**
 * Central manager for all metrics in the system. Provides methods to store,
 * retrieve, and manage metrics data organized by entity, category, and measure.
 */
public abstract class MetricManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient Set<MetricManagerListener> listeners = null;

    /**
     * Returns all categories used. Can be filtered by entity.
     * 
     * @param asset The entity to filter by, or null for all entities
     * @return List of categories choosen
     */
    public abstract List<MetricCategory> getCategories(Entity asset);
    
    /**
     * Returns all entities using a specific category.
     * 
     * @param tempCat The category to get entities for
     * @return List of entities that use the specified category
     */
    public abstract List<Entity> getEntities(MetricCategory tempCat);
        
    /**
     * Gets all measures for a specific entity and category.
     * 
     * @param asset The entity
     * @param tempCat The category
     * @return List of measure names for the specified entity and category
     */
    public abstract List<String> getMeasures(Entity asset, MetricCategory tempCat);

    
    /**
     * Get all known metrics in the system.
     * @return Keys of the metrics.
     */
    public abstract Set<MetricKey> getMetrics();

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
        return getMetric(key);
    }

    /**
     * Gets a metric for the specified key.
     * If the metric doesn't exist, a new one is created.
     * @param key The metric key
     * @return  The metric for the specified key
     */
    public abstract Metric getMetric(MetricKey key);

    protected void notifyListeners(Metric metric) {
        if (listeners != null) {
            for (var listener : listeners) {
                listener.newMetric(metric);
            }
        }
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
     * What is the Mars time now?
     * @return
     */
    static MarsTime getNow() {
        // Needs solving
        return Simulation.instance().getMasterClock().getMarsTime();
    }

    /**
     * Reinitializes the metric manager, clearing all existing metrics and resetting internal state.
     */
    public abstract void reinit();
}