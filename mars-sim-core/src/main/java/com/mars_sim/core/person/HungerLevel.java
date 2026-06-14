/*
 * Mars Simulation Project
 * HungerLevel.java
 * @date 2026-06-14
 * @author Barry Evans
 */
package com.mars_sim.core.person;

import com.mars_sim.core.Named;
import com.mars_sim.core.data.LevelBand;
import com.mars_sim.core.tool.Msg;

/**
 * Enum representing different levels of hunger.
 */
public enum HungerLevel implements Named, LevelBand {

    TOP_OFF(50),
    SATISFIED(250),
    COMFY(500),
    ADEQUATE(750),
    RUMBLING(1000),
    RAVENOUS(1500),
    FAMISHED(Double.MAX_VALUE);
    
    // Final words for death by hunger
    public static final String DEATH_QUOTE = "So hungry";

    private final String name;
    private final double max;

    HungerLevel(double max) {
        this.name = Msg.getStringOptional("HungerLevel", name());
        this.max = max;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getMaxValue() {
        return max;
    }

    public static HungerLevel fromValue(double hunger) {
        return (HungerLevel) LevelBand.fromValue(HungerLevel.values(), hunger);
    }

    /**
     * Checks if the hunger level is considered full (TOP_OFF or SATISFIED).
     * @return
     */
    public boolean isFull() {
        return this == TOP_OFF || this == SATISFIED;
    }

    /**
     * Checks if the hunger level is considered adequate (COMFY or ADEQUATE or full).
     */
    public boolean isAdequate() {
        return this == COMFY || this == ADEQUATE || isFull();

    }
}
