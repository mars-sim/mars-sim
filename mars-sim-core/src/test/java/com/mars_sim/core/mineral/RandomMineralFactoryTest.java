package com.mars_sim.core.mineral;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;

class RandomMineralFactoryTest {

    private SimulationConfig config;

    @BeforeEach
    void setUp() {
        config = SimulationConfig.loadConfig();
    }

    @Test
    void testCreateLocalConcentration() throws CoordinatesException {
        var newMap = new MineralMap(config.getMineralMapConfiguration());

        var center = CoordinatesFormat.fromString("30.0 -25.0");
        RandomMineralFactory.createLocalConcentration(newMap, center);

        // Search all types
        var types = newMap.getTypes().stream()
                        .map(MineralType::getName)
                        .collect(Collectors.toSet());

        var results = newMap.getDeposits(center, 1D, types);
        assertTrue(!results.isEmpty(), "Combined in random map found");
    }

    @Test
    void testCreateRandomMap() {
        var newMap = RandomMineralFactory.createRandomMap();
        assertNotNull(newMap, "New map created");
    }

    @Test
    void testGetTopoRegionSet() {
        // Test by loadng the volcanic topo map and extract hotpsots
        var locns = RandomMineralFactory.getTopoRegionSet("TopographyVolcanic.png", 300, 150);

        assertNotNull(locns, "Volcanic hotspots found");
        assertTrue(locns.size() > 50, "Numerous volcanic hotspots found");
    }
}
