/*
 * Mars Simulation Project
 * BuildingPlan.java
 * @date 2026-03-25
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

/**
 * This record represents a building planned for future construction with a delayed start.
 * It defines the building type and the number of sols to delay before construction begins.
 * 
 * @param buildingType the type name of the building to construct
 * @param delayInSols the number of sols to delay before starting construction (must be >= 0)
 */
public record BuildingPlan(String buildingType, int delayInSols) {

    /**
     * Compact constructor with validation.
     */
    public BuildingPlan {
        if (buildingType == null || buildingType.trim().isEmpty()) {
            throw new IllegalArgumentException("Building type cannot be null or empty");
        }
        if (delayInSols < 0) {
            throw new IllegalArgumentException("Delay in sols cannot be negative");
        }
        
        // Normalize the building type by trimming whitespace
        buildingType = buildingType.trim();
    }
}