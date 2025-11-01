package com.mars_sim.core.resourceprocess;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.resource.ResourceUtil;

class ResourceProcessConfigTest {
    
    private ResourceProcessConfig resConfig;

    @BeforeEach
    void setUp() {
        var config = SimulationConfig.loadConfig();
        resConfig = config.getResourceProcessConfiguration();
    }

    
    @Test
    void testRGWSReactor() {
        var rConfig = resConfig;

        var name = "Sabatier RWGS Reactor";
        var spec = rConfig.getProcessSpec(name);

        assertNotNull(spec, "Found " + name);
        assertEquals(name, spec.getName(), "Name");

        // Check inputs
        var inputs = spec.getInputResources();
        assertEquals(2, inputs.size(), "Input resources");
        assertTrue(inputs.contains(ResourceUtil.HYDROGEN_ID), "Inputs contains hydrogen");
        assertTrue(inputs.contains(ResourceUtil.CO2_ID), "Inputs contains CO2");

        // Check minumum
        var mins = spec.getMinimumInputs();
        assertEquals(1, mins.size(), "Minumum resources");
        assertTrue(mins.keySet().contains(ResourceUtil.HYDROGEN_ID), "Minimums contains");
    }
}
