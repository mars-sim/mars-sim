/*
 * Mars Simulation Project
 * PerformanceLevel.java
 * @date 2026-06-14
 * @author Barry Evans
 */
package com.mars_sim.core.person;

import com.mars_sim.core.Named;
import com.mars_sim.core.data.LevelBand;
import com.mars_sim.core.tool.Msg;

/**
 * This enum represents the performance levels of a person.
 */
public enum PerformanceLevel implements Named, LevelBand{
        CRIPPING(0.25),
        DRAGGING(0.50),
        BORDERLINE(0.75),
        ACTIVE(0.95),
        PEAK(1.0);

    private double max;
    private String name;

    private PerformanceLevel(double max) {
        this.max = max;
        this.name = Msg.getStringOptional("PerformanceLevel", name());
    }
    
    @Override
    public double getMaxValue() {
        return max;
    }

    @Override
    public String getName() {
        return name;
    }

    public static PerformanceLevel fromValue(double value) {
        return (PerformanceLevel) LevelBand.fromValue(values(), value);
    }
}
