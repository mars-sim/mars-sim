/*
 * Mars Simulation Project
 * StressLevel.java
 * @date 2026-06-10
 * @author Barry Evans
 */
package com.mars_sim.core.person;

import com.mars_sim.core.Named;
import com.mars_sim.core.data.LevelBand;
import com.mars_sim.core.tool.Msg;

/**
 * Represents the stress level of a person, which is a factor in their overall physical condition.
 * Uses the LevelBand pattern to map a raw stress value to a named level.
 */
public enum StressLevel implements LevelBand, Named {
    
    RELAXED(10),
    NOMINAL(40),
    DISTURBED(75),
    BEATEN(95),
    BREAKDOWN(100);

    private final double maxValue;
    private final String name;

    StressLevel(double maxValue) {
        this.maxValue = maxValue;
        this.name = Msg.getStringOptional("StressLevel", name());
    }

    /**
     * Gets the internationalized name of this stress level.
     * @return Stress level name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the maximum value for this level band.
     * @return Maximum value
     */
    @Override
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Find the appropriate band for a value.
     * @param value Value to map
     * @return Applicable band
     */
    public static StressLevel fromValue(double value) {
        return (StressLevel)LevelBand.fromValue(values(), value);
    }

    /**
     * Checks if the stress level is at or above the point where the person is considered stressed out.
     * @return  True if stressed out, false otherwise
     */
    public boolean isStressedOut() {
        return this == BEATEN || this == BREAKDOWN;
    }
}
