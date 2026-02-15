package com.mars_sim.core.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.resource.ResourceUtil;

class MineralSiteTest {

    private static final int MAGNETITE = ResourceUtil.MAGNETITE_ID;
    private static final int HEMATITE = ResourceUtil.HEMATITE_ID;


    @BeforeEach
    void setUp() {
        SimulationConfig.loadConfig();
    }

    private MineralSite createMineralSite() {
        Map<Integer, Double> minerals = new HashMap<>();
        minerals.put(HEMATITE, 10D);
        minerals.put(MAGNETITE, 5D);
        return new MineralSite("Site-1", new Coordinates(0.5, 1.0), 2, minerals);
    }

    @Test
    void testConstructionAndFlags() {
        MineralSite site = createMineralSite();

        assertEquals("Site-1", site.getName());
        assertFalse(site.isExplored());
        assertFalse(site.isReserved());
        assertFalse(site.isClaimed());
        assertFalse(site.isEmpty());
        assertTrue(site.isMinable());
        assertNotNull(site.getCoordinates());
    }

    @Test
    void testUpdateMineralEstimate() {
        MineralSite site = createMineralSite();

        MineralSite.MineralDetails original = site.getMinerals().get(HEMATITE);
        assertNotNull(original);

        site.updateMineralEstimate(HEMATITE, 20D);

        MineralSite.MineralDetails updated = site.getMinerals().get(HEMATITE);
        assertNotNull(updated);
        assertEquals(20D, updated.concentration(), 0.0001);
        assertEquals(original.certainty(), updated.certainty(), 0.0001);
    }

    @Test
    void testEstimatedMineralAmounts() {
        MineralSite site = createMineralSite();

        double remainingMass = site.getRemainingMass();
        double expected = remainingMass * site.getMinerals().get(HEMATITE).concentration() / 100D;

        Map<Integer, Double> amounts = site.getEstimatedMineralAmounts();
        assertEquals(expected, amounts.get(HEMATITE), 0.0001);
    }

    @Test
    void testExcavateMass() {
        MineralSite site = createMineralSite();

        double starting = site.getRemainingMass();
        double shortage = site.excavateMass(starting / 2D);
        assertEquals(0D, shortage, 0.0001);
        assertEquals(starting / 2D, site.getRemainingMass(), 0.0001);

        shortage = site.excavateMass(starting);
        assertTrue(shortage > 0D);
        assertEquals(0D, site.getRemainingMass(), 0.0001);
        assertTrue(site.isEmpty());
    }

    @Test
    void testIncrementNumImprovement() {
        MineralSite site = createMineralSite();
        int initial = site.getNumEstimationImprovement();
        site.incrementNumImprovement(3);
        assertEquals(initial + 3, site.getNumEstimationImprovement());
    }

    
    @Test
    void testIncreaseCertainty() {
        MineralSite site = createMineralSite();

        double originalCertainty = site.getAverageCertainty();
        var origMinerals = new HashMap<>(site.getMinerals());

        site.improveCertainty(10D);

        double newCertainty = site.getAverageCertainty();
        assertTrue(newCertainty > originalCertainty);

        // Check only one has been improved
        int improvedCount = 0;
        for (var entry : site.getMinerals().entrySet()) {
            var orig = origMinerals.get(entry.getKey());
            var updated = entry.getValue();
            if (updated.certainty() > orig.certainty()) {
                improvedCount++;
                assertEquals(orig.concentration(), updated.concentration(), 0.0001);
            }
        }
        assertEquals(1, improvedCount);
    }
}