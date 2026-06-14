/*
 * Mars Simulation Project
 * LevelBand.java
 * @date 2026-06-10
 * @author Barry Evans
 */
package com.mars_sim.core.data;

/**
 * This represent a single level in a range of Bands. The bands are defined by their maximum value,
 * and the value is mapped to the first band that has a maximum value greater than the value.
 */
public interface LevelBand {
    /**
     * The maximum value for this level band.
     * @return
     */
    double getMaxValue();

    /**
     * Find the appropriate band for a value.
     * @param bands Range of bands available
     * @param v Value to map
     * @return Result found
     */
    static LevelBand fromValue(LevelBand[] bands, double v) {
        for(var c : bands) {
            if (v <= c.getMaxValue()) {
                return c;
            }
        }

        throw new IllegalStateException("No band for value " + v + " in " + bands);
    }
}