/*
 * Mars Simulation Project
 * MetricManagerListener.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

/**
 * Listener interface for MetricManager events.
 */
public interface MetricManagerListener {

    /**
     * A new metric has been created.
     * @param m The new metric
     */
    void newMetric(Metric m);

}
