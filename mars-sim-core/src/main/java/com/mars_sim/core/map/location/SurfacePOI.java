/*
 * Mars Simulation Project
 * SurfacePOI.java
 * @date 2024-10-02
 * @author Barry Evans
 */
package com.mars_sim.core.map.location;

/**
 * This represents a point of interest on the surface of Mars,
 */
public interface SurfacePOI {

    /**
     * Location of the feature on the surface. 
     * This is a default implementation to support older code
     * @return
     */
    default Coordinates getLocation() {
        return getCoordinates();
    }

    /**
     * Location of the feature on the surface
     * @return
     */
    Coordinates getCoordinates();
}
