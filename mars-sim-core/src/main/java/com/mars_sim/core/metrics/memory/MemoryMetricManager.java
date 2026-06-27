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
    private static final int MAX_MEMORY_PERC = 2; // 2% of max memory

    private Map<MetricKey, MemoryMetric> metrics;
    private int maxSol = 1; // Maximum number of sols to retain in memory for each metric
    private int maxPoints;
    private int earliestSol = 1; // The earliest sol that is currently being tracked

    /**
     * Creates a new MemoryMetricManager with the specified maximum number of sols to retain.
     * @param maxSol Maximum number of sols to retain in memory for each metric.
     */
    public MemoryMetricManager(int maxSol) {
        super();
        this.metrics = new HashMap<>();
        this.maxSol = maxSol;
        var maxMem = (Runtime.getRuntime().maxMemory() * MAX_MEMORY_PERC) / 100.0;
        this.maxPoints = (int) (maxMem / MEMORY_PER_DATA);

        logger.info("MemoryMetricManager initialized with maxSol: " + maxSol + ", maxPoints: " + maxPoints);
    }

    /**
     * Ovveride the default maxPoints to a new value.
     * @param maxPoints The new maximum number of data points to retain in memory.
     */
    void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    /**
     * Get the maximum number of data points to retain in memory.
     */
    public int getMaxPoints() {
        return maxPoints;
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
        int newDataPointCount = metrics.values().stream().mapToInt(Metric::getSize).sum();

        // Check if the total sols has past the limit
        var newEarliest = time.getMissionSol() - maxSol + 1;
        if (newEarliest <= earliestSol) {
            // No sol limit so check memory usage and remove old sols if necessary
            if (newDataPointCount > maxPoints) {
                logger.info("Memory usage exceeded limit. Current data points: " + newDataPointCount + ", max allowed: " + maxPoints);
                newEarliest = earliestSol + 1; // Remove the new earliest one sol forward
            }
        }
        else {
            logger.info("Sol limit exceeded. Earliest allowed sol: " + newEarliest + ", max sols: " + maxSol);
        }

        // Remove old sols from all metrics if the earliest sol is greater than or equal to 1
        if (newEarliest > earliestSol) {
            earliestSol = newEarliest;

            metrics.values().forEach(mm -> mm.removeOldSols(earliestSol));

            // Update the total data points after removing old sols
            newDataPointCount = metrics.values().stream().mapToInt(Metric::getSize).sum();
        }  
        
        logger.info("Total data points: " + newDataPointCount + ", max: " + maxPoints
                + ", earliest sol held: " + earliestSol);
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
