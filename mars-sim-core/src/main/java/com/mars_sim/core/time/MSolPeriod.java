/*
 * Mars Simulation Project
 * MSolPeriod.java
 * @date 2025-03-23
 * @author Barry Evans
 */
package com.mars_sim.core.time;

import java.io.Serializable;

/**
 * Represents a period of time in a Sol. The period could span midnight into the next Sol.
 */
public record MSolPeriod(int start, int end) implements Serializable {
    /**
     * Is the given time within the period. Caters for a period that spans midnight.
     * @param timeOfDay Time within the day to check
     * @return
     */
    public boolean isBetween(int timeOfDay) {
        return ((start < end) && start <= timeOfDay && timeOfDay < end)
            || ((start > end) && (start <= timeOfDay || timeOfDay < end));
    }
}