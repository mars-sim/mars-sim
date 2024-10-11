/*
 * Mars Simulation Project
 * SurfaceFeature.java
 * @date 2024-10-02
 * @author Barry Evans
 */
package com.mars_sim.core.map.location;

/**
 * This represents a feature in terms of it's Coordinate on the surface of Mars,
 */
public interface SurfaceFeature {

    /**
     * Location of the feature on the surface
     * @return
     */
    Coordinates getLocation();
}
