package com.mars_sim.core.mineral;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;
import com.mars_sim.core.map.location.Direction;

class MineralMapTest {

    private MineralMapConfig config;

    @BeforeEach
    void setUp() {
        config = SimulationConfig.loadConfig().getMineralMapConfiguration();
    }

    @Test
    void testExactLocation() throws CoordinatesException {
        var minerals = config.getMineralTypes();
        var type1 = minerals.get(0);
        var type2 = minerals.get(1);
        Set<String> all = Set.of(type1.getName(), type2.getName());

        MineralMap newMap = new MineralMap(config);

        var center = CoordinatesFormat.fromString("-3.0 67.0");
        
        newMap.addMineral(center, type1, 10);
        newMap.addMineral(center, type2, 20);

        var results = newMap.getDeposits(center, 0D, all);
        assertEquals(1, results.size(), "All Concentrations");

        var found = results.get(0);
        assertEquals(10, found.getConcentration(type1.getName()), "Type1 Concentrations");
        assertEquals(20, found.getConcentration(type2.getName()), "Type2 Concentrations");
    }

    @Test
    void testTwoLocation() throws CoordinatesException {
        var minerals = config.getMineralTypes();
        var type1 = minerals.get(0);
        var type2 = minerals.get(1);
        Set<String> all = Set.of(type1.getName(), type2.getName());

        MineralMap newMap = new MineralMap(config);

        var center = CoordinatesFormat.fromString("-3.0 67.0");
        
        newMap.addMineral(center, type1, 10);
        newMap.addMineral(center.getNewLocation(new Direction(0.1), 0.01), type2, 20);

        var results = newMap.getDeposits(center, 1D, all);
        assertEquals(2, results.size(), "All Concentrations");

        all = Set.of(type1.getName());
        results = newMap.getDeposits(center, 1D, all);
        assertEquals(1, results.size(), "Filtered 1 Concentrations");
        assertEquals(10, results.get(0).getConcentration(type1.getName()), "Filtered Type1 Concentrations");

        all = Set.of(type2.getName());
        results = newMap.getDeposits(center, 1D, all);
        assertEquals(1, results.size(), "Filtered 2 Concentrations");
        assertEquals(20, results.get(0).getConcentration(type2.getName()), "Filtered Type2 Concentrations");
    }
}
