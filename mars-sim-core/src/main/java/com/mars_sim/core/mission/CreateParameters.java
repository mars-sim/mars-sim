/**
 * Mars Simulation Project
 * CreateParameters.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package com.mars_sim.core.mission;

/**
 * Parameters controlling mission construction behavior.
 * @param needsReview indicates if the mission needs review before creation.
 * @param daylightStart indicates if the mission should start during daylight.
 */
public record CreateParameters(boolean needsReview, boolean daylightStart) {

    /**
     * Default disables all options (no review, no daylight start).
     */
    public CreateParameters() {
        this(false, false);
    }
}