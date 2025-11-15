/*
 * Mars Simulation Project
 * Range.java
 * @date 2024-06-22
 * @author Barry Evans
 */
package com.mars_sim.core.data;

import java.io.Serializable;

import com.mars_sim.core.tool.RandomUtil;

/**
 * Represents a configuration attributes that specifes the valid range for a value
 */
public record Range(double min, double max) implements Serializable {

    public double getRandomValue() {
        return RandomUtil.getRandomDouble(min, max);
    }
    
    /**
     * Is the given value between the min and max (inclusive)
     * @param value
     * @return
     */
    public boolean isBetween(int value) {
        return value >= min && value <= max;
    }
}
