package com.mars_sim.core.mineral;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.Coordinates;

class MineralConcentrationTest {
    @Test
    void testAdjustMineral() {
        var base = new Coordinates("45 N", "23 E");
        MineralDeposit conc = new MineralDeposit(base);

        conc.adjustMineral("A", 10);
        conc.adjustMineral("B", 20);

        assertEquals("Mineral A", 10, conc.getConcentration("A"));
        assertEquals("Mineral B", 20, conc.getConcentration("B"));

        assertEquals("Mineral C", 0, conc.getConcentration("C"));

    }

    @Test
    void testUpdateMineral() {
        var base = new Coordinates("45 N", "23 E");
        MineralDeposit conc = new MineralDeposit(base);

        conc.adjustMineral("A", 10);
        conc.adjustMineral("A", 20);

        assertEquals("Mineral A", 15, conc.getConcentration("A"));
    }

    
    @Test
    void testAddMineral() {
        var base = new Coordinates("45 N", "23 E");
        MineralDeposit conc = new MineralDeposit(base);

        conc.addMineral("A", 10);
        conc.addMineral("A", 20);

        assertEquals("Mineral A", 30, conc.getConcentration("A"));
    }
}
