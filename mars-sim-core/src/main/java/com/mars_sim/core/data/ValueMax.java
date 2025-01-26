/*
 * Mars Simulation Project
 * ValueMax.java
 * @date 2025-01-26
 * @author Barry Evans
 */
package com.mars_sim.core.data;

/**
 * Represents a value that is associated with a maximum
 */
public record ValueMax(double value, double max) {

    public double remaining() {
        return max - value;
    }
    
    @Override
    public String toString() {
        return value + "/" + max;
    }
}
