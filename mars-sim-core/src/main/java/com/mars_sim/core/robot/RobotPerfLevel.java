/*
 * Mars Simulation Project
 * RobotPerfLevel.java
 * @date 2026-06-16
 * @author Manny Kung
 */
package com.mars_sim.core.robot;

import com.mars_sim.core.Named;
import com.mars_sim.core.data.LevelBand;
import com.mars_sim.core.tool.Msg;

/**
 * This enum represents the performance level of a robot.
 */
public enum RobotPerfLevel implements Named, LevelBand {

    DISABLED(0),
    VERY_LOW(0.15),
    LOW(0.40),
    MEDIUM_LOW(0.65),
    MEDIUM(0.80),
    HIGH(0.90),
    PEAK(Double.MAX_VALUE);

    private final double maxValue;
    private final String name;

    private RobotPerfLevel(double maxValue) {
        this.name = Msg.getStringOptional("RobotPerfLevel", name());
        this.maxValue = maxValue;
    }

    @Override
    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public String getName() {
        return name;
    }

    public static RobotPerfLevel fromValue(double value) {
        return (RobotPerfLevel) LevelBand.fromValue(RobotPerfLevel.values(), value);
    }

}
