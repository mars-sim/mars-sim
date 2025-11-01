package com.mars_sim.core.mineral;

import java.util.stream.Collectors;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;

public class RandomMineralFactoryTest extends AbstractMarsSimUnitTest {
    public void testCreateLocalConcentration() throws CoordinatesException {
        var newMap = new MineralMap(getConfig().getMineralMapConfiguration());

        var center = CoordinatesFormat.fromString("30.0 -25.0");
        RandomMineralFactory.createLocalConcentration(newMap, center);

        // Search all types
        var types = newMap.getTypes().stream()
                        .map(MineralType::getName)
                        .collect(Collectors.toSet());

        var results = newMap.getDeposits(center, 1D, types);
        assertTrue("Combined in random map found", !results.isEmpty());
    }

    public void testCreateRandomMap() {
        var newMap = RandomMineralFactory.createRandomMap();
        assertNotNull("New map created", newMap);
    }

    public void testGetTopoRegionSet() {
        // Test by loadng the volcanic topo map and extract hotpsots
        var locns = RandomMineralFactory.getTopoRegionSet("TopographyVolcanic.png", 300, 150);

        assertNotNull("Volcanic hotspots found", locns);
        assertTrue("Numerous volcanic hotspots found", locns.size() > 50);
    }
}
