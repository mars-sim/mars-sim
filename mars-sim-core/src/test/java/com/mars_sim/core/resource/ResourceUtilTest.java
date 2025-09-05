package com.mars_sim.core.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;

public class ResourceUtilTest {
    @BeforeEach
    public void setUp() {
        SimulationConfig.loadConfig();
    }
	
    @Test
    void testIsInSitu() {
        assertFalse("Oxygen is not raw material", ResourceUtil.isInSitu(ResourceUtil.OXYGEN_ID));
        assertTrue("Regolith is an insitu material", ResourceUtil.isInSitu(ResourceUtil.REGOLITH_ID)); 
    }

    @Test
    void testIsLifeSupport() {
        assertTrue("Oxygen is a life support resource", ResourceUtil.isLifeSupport(ResourceUtil.OXYGEN_ID));
        assertFalse("Sand is not life support resource", ResourceUtil.isLifeSupport(ResourceUtil.SAND_ID));

    }

    @Test
    void testIsRawMaterial() {
        assertFalse("Oxygen is not raw material", ResourceUtil.isRawMaterial(ResourceUtil.OXYGEN_ID));
        assertTrue("Sand is raw material", ResourceUtil.isRawMaterial(ResourceUtil.SAND_ID));
    }
}
