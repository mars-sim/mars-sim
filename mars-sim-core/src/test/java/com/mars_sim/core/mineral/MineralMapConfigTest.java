package com.mars_sim.core.mineral;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;

class MineralMapConfigTest {

    private MineralMapConfig config;

    @BeforeEach
    void setUp() {
        config = SimulationConfig.loadConfig().getMineralMapConfiguration();
    }

    @Test
    void testGetMineralTypes() {
        var minerals = config.getMineralTypes();

        assertTrue(!minerals.isEmpty(), "Minerals loaded");

        // Check one
        MineralType found = minerals.stream()
                .filter(m -> m.getName().equals("Chalcopyrite"))
                .findFirst().orElse(null);
        assertNotNull(found, "Found match");
        assertEquals(MineralMapConfig.UNCOMMON_FREQUENCY, found.getFrequency(), "Frequency");
        assertTrue(found.getLocales().contains("sedimentary"), "Locale contains sedimentary");
        assertTrue(found.getLocales().contains("volcanic"), "Locale contains volcanic");
    }
}
