/*
 * Mars Simulation Project
 * MetricKey.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

import java.io.Serializable;

import com.mars_sim.core.Entity;

/**
 * Represents a unique key for identifying metrics, composed of an entity asset,
 * category, and measure.
 */
public record MetricKey(Entity asset, MetricCategory category, String measure)
                implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * Provides a string representation of the MetricKey.
     * @return
     */
    public String getDisplay() {
        return String.format("[%s:%s] %s", asset.getName(), category.getName(), measure);
    }
}