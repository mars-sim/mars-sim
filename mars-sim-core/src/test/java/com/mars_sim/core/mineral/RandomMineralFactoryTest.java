package com.mars_sim.core.mineral;

import java.util.stream.Collectors;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.MapPoint;
import com.mars_sim.core.map.location.Coordinates;

public class RandomMineralFactoryTest extends AbstractMarsSimUnitTest {
    public void testCreateLocalConcentration() {
        var newMap = new MineralMap();

        var center = new Coordinates("30 N", "25 W");
        RandomMineralFactory.createLocalConcentration(newMap, center);

        // Search all types
        var types = newMap.getTypes().stream()
                        .map(MineralType::getName)
                        .collect(Collectors.toSet());

        var results = newMap.getRadiusConcentration(types,
                        new MapPoint(center.getPhi(), center.getTheta()), 1D);
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
