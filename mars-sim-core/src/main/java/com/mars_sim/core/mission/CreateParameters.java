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
 */
public record CreateParameters(boolean needsReview) {}