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
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.metrics.Metric;
import com.mars_sim.core.metrics.MetricCategory;
import com.mars_sim.core.metrics.MetricKey;
import com.mars_sim.core.metrics.MetricManager;
import com.mars_sim.core.time.MarsTime;

/**
 * In-memory metric manager that creates {@link MemoryMetric} instances.
 */
public class MemoryMetricManager extends MetricManager {

    private static final SimLogger logger = SimLogger.getLogger(MemoryMetricManager.class.getName());
    private static final long serialVersionUID = 1L;

    // 1 double & 1 memory reference at 8 bytes each plus 1 reference in Metric
    private static final int MEMORY_PER_DATA = 24;

    private Map<MetricKey, MemoryMetric> metrics;
    private int maxSol = 1;
    private int totalDataPoints = 0;

    /**
     * Creates a new MemoryMetricManager with the specified maximum number of sols to retain.
     * @param maxSol Maximum number of sols to retain in memory for each metric.
     */
    public MemoryMetricManager(int maxSol) {
        super();
        metrics = new HashMap<>();
        this.maxSol = maxSol;
    }

    /**
     * Reinitializes the MetricManager after deserialization.
     */
    @Override
    public void reinit() {
        //There is a problem with dserializing the HashMap.The saved hashcode for the the same key
        // is different after deserialization, so we need to rebuild the map.
        var newMetrics = new HashMap<MetricKey, MemoryMetric>(); 
        for (var entry : metrics.entrySet()) {
            newMetrics.put(entry.getKey(), entry.getValue());
        }
        this.metrics = newMetrics;
    }

    /**
     * A new sol has started so check all metrics to see if they need to be updated.
     * @param time Current mars time.
     */
    public void newSol(MarsTime time) {
        var earliestSol = time.getMissionSol() - maxSol + 1;
        if (earliestSol>= 1) {
            metrics.values().forEach(mm -> mm.removeOldSols(earliestSol));
        }  
        
        int newDataPointCount = metrics.values().stream().mapToInt(Metric::getSize).sum();
        logger.info("Total data points in memory: " + newDataPointCount + ", previous: " + totalDataPoints
                + " (estimated memory usage: " + (newDataPointCount * MEMORY_PER_DATA)/1024D + " KB)");  
    
        totalDataPoints = newDataPointCount;
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

            // Must update metric before notifying listeners
            notifyListeners(m);
        }
        return m;
    }
}
