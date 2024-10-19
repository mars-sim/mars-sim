package com.mars_sim.core.mineral;

import java.util.Set;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.MapPoint;
import com.mars_sim.core.map.location.Coordinates;

public class MineralMapTest extends AbstractMarsSimUnitTest {

    public void testExactLocation() {
        var config = simConfig.getMineralMapConfiguration();
        var minerals = config.getMineralTypes();
        var type1 = minerals.get(0);
        var type2 = minerals.get(1);
        Set<String> all = Set.of(type1.getName(), type2.getName());

        MineralMap newMap = new MineralMap();

        var center = new Coordinates("3 S", "67 E");
        
        newMap.addMineral(center, type1, 10);
        newMap.addMineral(center, type2, 20);

        var results = newMap.getRadiusConcentration(all, new MapPoint(center.getPhi(),
                                                                center.getTheta()), 0D);
        assertEquals("All Concentrations", 2, results.getConcentrations().size());
        assertEquals("Type1 Concentrations", 10, results.getConcentration(type1.getName()));
        assertEquals("Type2 Concentrations", 20, results.getConcentration(type2.getName()));

        all = Set.of(type1.getName());
        results = newMap.getRadiusConcentration(all, new MapPoint(center.getPhi(),
                                            center.getTheta()), 1D);
        assertEquals("Filtered 1 Concentrations", 1, results.getConcentrations().size());
        assertEquals("Filtered Type1 Concentrations", 10, results.getConcentration(type1.getName()));

        all = Set.of(type2.getName());
        results = newMap.getRadiusConcentration(all, new MapPoint(center.getPhi(),
                                            center.getTheta()), 1D);
        assertEquals("Filtered 2 Concentrations", 1, results.getConcentrations().size());
        assertEquals("Filtered Type2 Concentrations", 20, results.getConcentration(type2.getName()));
    }

    public void testDistanceLocation() {
        var config = simConfig.getMineralMapConfiguration();
        var minerals = config.getMineralTypes();
        var type1 = minerals.get(0);
        Set<String> all = Set.of(type1.getName());

        MineralMap newMap = new MineralMap();

        double angle = 0.1D;
        var center = new Coordinates("3 S", "67 E");
        var halfWay = new Coordinates(center.getPhi(), center.getTheta() + (angle/2));

        newMap.addMineral(center, type1, 20);
        newMap.addMineral(halfWay, type1, 20);

        var results = newMap.getRadiusConcentration(all, new MapPoint(center.getPhi(),
                                                                center.getTheta()), angle * 1.1);
        assertEquals("Type1 Concentrations", 30, results.getConcentration(type1.getName()));
    }
}
