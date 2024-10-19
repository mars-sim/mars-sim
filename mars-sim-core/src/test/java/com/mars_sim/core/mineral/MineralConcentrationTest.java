package com.mars_sim.core.mineral;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.Coordinates;

class MineralConcentrationTest {
    @Test
    void testAddMineral() {
        var base = new Coordinates("45 N", "23 E");
        MineralConcentration conc = new MineralConcentration(base);

        conc.addMineral("A", 10);
        conc.addMineral("B", 20);

        assertEquals("Mineral A", 10, conc.getConcentration("A"));
        assertEquals("Mineral B", 20, conc.getConcentration("B"));
    }

    @Test
    void testUpdateMineral() {
        var base = new Coordinates("45 N", "23 E");
        MineralConcentration conc = new MineralConcentration(base);

        conc.addMineral("A", 10);
        conc.addMineral("A", 20);

        assertEquals("Mineral A", 15, conc.getConcentration("A"));
    }
}
