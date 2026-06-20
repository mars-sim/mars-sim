/*
 * Mars Simulation Project
 * FatigueLevel.java
 * @date 2026-06-13
 * @author Barry Evans
 */
package com.mars_sim.core.person;

import com.mars_sim.core.Named;
import com.mars_sim.core.data.LevelBand;
import com.mars_sim.core.tool.Msg;

/**
 * This enum represents the different levels of fatigue a person can experience.
 * It follows the LevelBand pattern, where each level has a maximum value associated with it.
 */
public enum FatigueLevel implements Named, LevelBand {

    RESTED(500),
    NOMINAL(800),
    FRAZZLED(1200),
    EXHAUSTED(1600),
    BEDBOUND(Double.MAX_VALUE);

    private double maxValue;
    private String name;

    FatigueLevel(double maxValue) {
        this.maxValue = maxValue;
        this.name = Msg.getStringOptional("FatigueLevel", name());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Is the level a sleep level
     */
    public boolean isSleepy() {
        return this == FRAZZLED || this == EXHAUSTED || this == BEDBOUND;
    }

    /**
     * Find the appropriate FatigueLevel for a value.
     */
    public static FatigueLevel fromValue(double value) {
        return (FatigueLevel)LevelBand.fromValue(values(), value);
    }

}
