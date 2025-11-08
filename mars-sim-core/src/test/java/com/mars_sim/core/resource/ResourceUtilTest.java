package com.mars_sim.core.resource;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;

class ResourceUtilTest {
    @BeforeEach
    void setUp() {
        SimulationConfig.loadConfig();
    }
	
    @Test
    void testIsInSitu() {
        assertFalse(ResourceUtil.isInSitu(ResourceUtil.OXYGEN_ID), "Oxygen is not raw material");
        assertTrue(ResourceUtil.isInSitu(ResourceUtil.REGOLITH_ID), "Regolith is an insitu material");
    }

    @Test
    void testIsLifeSupport() {
        assertTrue(ResourceUtil.isLifeSupport(ResourceUtil.OXYGEN_ID), "Oxygen is a life support resource");
        assertFalse(ResourceUtil.isLifeSupport(ResourceUtil.SAND_ID), "Sand is not life support resource"   );

    }

    @Test
    void testIsRawMaterial() {
        assertFalse(ResourceUtil.isRawMaterial(ResourceUtil.OXYGEN_ID), "Oxygen is not raw material");
        assertTrue(ResourceUtil.isRawMaterial(ResourceUtil.SAND_ID), "Sand is raw material");
    }
}
