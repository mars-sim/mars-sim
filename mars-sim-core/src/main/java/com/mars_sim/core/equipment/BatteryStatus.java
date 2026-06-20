/*
 * Mars Simulation Project
 * BatteryStatus.java
 * @date 2026-06-15
 * @author Barry Evans
 */
package com.mars_sim.core.equipment;

import com.mars_sim.core.Named;
import com.mars_sim.core.data.LevelBand;
import com.mars_sim.core.tool.Msg;

/**
 * Enum representing the status of a battery based on its charge level. 
 */
public enum BatteryStatus implements Named, LevelBand {

    DEPLETED(1),
    DEPLETING(10),
    LOW(20),
    MED_LOW(40),
    MED(60),
    MED_HIGH(80),
    HIGH(95),
    FULL(Double.MAX_VALUE);

    private final String name;
    private final double max;

    BatteryStatus(double max) {
        this.name = Msg.getStringOptional("BatteryStatus", name());
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

    public static BatteryStatus fromValue(double percent) {
        return (BatteryStatus) LevelBand.fromValue(BatteryStatus.values(), percent);
    }
}
