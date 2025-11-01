package com.mars_sim.core.mineral;

import java.util.Set;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;
import com.mars_sim.core.map.location.Direction;

public class MineralMapTest extends AbstractMarsSimUnitTest {

    public void testExactLocation() throws CoordinatesException {
        var config = getConfig().getMineralMapConfiguration();
        var minerals = config.getMineralTypes();
        var type1 = minerals.get(0);
        var type2 = minerals.get(1);
        Set<String> all = Set.of(type1.getName(), type2.getName());

        MineralMap newMap = new MineralMap(config);

        var center = CoordinatesFormat.fromString("-3.0 67.0");
        
        newMap.addMineral(center, type1, 10);
        newMap.addMineral(center, type2, 20);

        var results = newMap.getDeposits(center, 0D, all);
        assertEquals("All Concentrations", 1, results.size());

        var found = results.get(0);
        assertEquals("Type1 Concentrations", 10, found.getConcentration(type1.getName()));
        assertEquals("Type2 Concentrations", 20, found.getConcentration(type2.getName()));
    }

    public void testTwoLocation() throws CoordinatesException {
        var config = getConfig().getMineralMapConfiguration();
        var minerals = config.getMineralTypes();
        var type1 = minerals.get(0);
        var type2 = minerals.get(1);
        Set<String> all = Set.of(type1.getName(), type2.getName());

        MineralMap newMap = new MineralMap(config);

        var center = CoordinatesFormat.fromString("-3.0 67.0");
        
        newMap.addMineral(center, type1, 10);
        newMap.addMineral(center.getNewLocation(new Direction(0.1), 0.01), type2, 20);

        var results = newMap.getDeposits(center, 1D, all);
        assertEquals("All Concentrations", 2, results.size());

        all = Set.of(type1.getName());
        results = newMap.getDeposits(center, 1D, all);
        assertEquals("Filtered 1 Concentrations", 1, results.size());
        assertEquals("Filtered Type1 Concentrations", 10, results.get(0).getConcentration(type1.getName()));

        all = Set.of(type2.getName());
        results = newMap.getDeposits(center, 1D, all);
        assertEquals("Filtered 2 Concentrations", 1, results.size());
        assertEquals("Filtered Type2 Concentrations", 20, results.get(0).getConcentration(type2.getName()));
    }
}
