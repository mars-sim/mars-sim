/*
 * Mars Simulation Project
 * MetricKey.java
 * @date 2025-10-04
 * @author Barry Evans
 */
package com.mars_sim.core.metrics;

import com.mars_sim.core.Entity;

/**
 * Represents a unique key for identifying metrics, composed of an entity asset,
 * category, and measure.
 */
public record MetricKey(Entity asset, String category, String measure) {}