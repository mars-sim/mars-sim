package com.mars_sim.core.mineral;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;

class MineralConcentrationTest {
    @Test
    void testAdjustMineral() throws CoordinatesException {
        var base = CoordinatesFormat.fromString("35.23 163.95");// 45.0 23.0");
        MineralDeposit conc = new MineralDeposit(base);

        conc.adjustMineral("A", 10);
        conc.adjustMineral("B", 20);

        assertEquals(10, conc.getConcentration("A"), "Mineral A");
        assertEquals(20, conc.getConcentration("B"), "Mineral B");

        assertEquals(0, conc.getConcentration("C"), "Mineral C");

    }

    @Test
    void testUpdateMineral() throws CoordinatesException {
        var base = CoordinatesFormat.fromString("45.0 23.0");
        MineralDeposit conc = new MineralDeposit(base);

        conc.adjustMineral("A", 10);
        conc.adjustMineral("A", 20);

        assertEquals(15, conc.getConcentration("A"), "Mineral A");
    }

    
    @Test
    void testAddMineral() throws CoordinatesException {
        var base = CoordinatesFormat.fromString("45.0 23.0");
        MineralDeposit conc = new MineralDeposit(base);

        conc.addMineral("A", 10);
        conc.addMineral("A", 20);

        assertEquals(30, conc.getConcentration("A"), "Mineral A");
    }
}
