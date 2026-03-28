/*
 * Mars Simulation Project
 * BuildingPlanTest.java
 * @date 2026-03-25
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit test for the BuildingPlan class.
 */
public class BuildingPlanTest {

    @Test
    void testBuildingPlanConstructor() {
        BuildingPlan plan = new BuildingPlan("Laboratory", 30);
        
        assertEquals("Laboratory", plan.buildingType());
        assertEquals(30, plan.delayInSols());
    }

    @Test
    void testBuildingPlanInvalidArguments() {
        // Test null building type
        assertThrows(IllegalArgumentException.class, () -> {
            new BuildingPlan(null, 30);
        });
        
        // Test empty building type
        assertThrows(IllegalArgumentException.class, () -> {
            new BuildingPlan("", 30);
        });
        
        // Test whitespace-only building type
        assertThrows(IllegalArgumentException.class, () -> {
            new BuildingPlan("   ", 30);
        });
        
        // Test negative delay
        assertThrows(IllegalArgumentException.class, () -> {
            new BuildingPlan("Laboratory", -5);
        });
    }

    @Test
    void testBuildingPlanEquality() {
        BuildingPlan plan1 = new BuildingPlan("Laboratory", 30);
        BuildingPlan plan2 = new BuildingPlan("Laboratory", 30);
        BuildingPlan plan3 = new BuildingPlan("Workshop", 30);
        BuildingPlan plan4 = new BuildingPlan("Laboratory", 60);
        
        assertEquals(plan1, plan2);
        assertNotEquals(plan1, plan3);
        assertNotEquals(plan1, plan4);
        
        assertEquals(plan1.hashCode(), plan2.hashCode());
    }
}