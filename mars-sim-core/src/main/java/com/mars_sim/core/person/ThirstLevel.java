/*
 * Mars Simulation Project
 * ThirstLevel.java
 * @date 2026-06-13
 * @author Barry Evans
 */
package com.mars_sim.core.person;

import com.mars_sim.core.Named;
import com.mars_sim.core.data.LevelBand;
import com.mars_sim.core.tool.Msg;

/**
 * This enum represents the different levels of thirst a person can experience.
 * It follows the LevelBand pattern, where each level has a maximum value associated with it. The levels are:
 */
public enum ThirstLevel implements Named, LevelBand {

    ISOTONIC(150),
    WANT_A_SIP(500),
    DRY(1000),
    BONE_DRY(1600),
    DESICCATED(Double.MAX_VALUE);

    private double maxValue;
    private String name;

    ThirstLevel(double maxValue) {
        this.maxValue = maxValue;
        this.name = Msg.getStringOptional("ThirstLevel", name());
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
	 * Checks if it passes the thirst x2 threshold
	 *
	 * @return
	 */
	public boolean isDoubleThirsty() {
		return this != ISOTONIC && this != WANT_A_SIP;
	}
	
	/**
	 * Checks if it passes the thirst threshold
	 *
	 * @return
	 */
	public boolean isThirsty() {
		return this != ISOTONIC;
	}

    /**
     * Find the appropriate ThirstLevel for a value.
     */
    public static ThirstLevel fromValue(double value) {
        return (ThirstLevel)LevelBand.fromValue(values(), value);
    }
}
