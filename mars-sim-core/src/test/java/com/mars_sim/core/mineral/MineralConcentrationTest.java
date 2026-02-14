package com.mars_sim.core.mineral;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;

class MineralConcentrationTest {
    @Test
    void testAdjustMineral() throws CoordinatesException {
        var base = CoordinatesFormat.fromString("35.23 163.95");
        MineralDeposit conc = new MineralDeposit(base);

        conc.adjustMineral(1, 10);
        conc.adjustMineral(2, 20);

        assertEquals(10, conc.getConcentration(1), "Mineral A");
        assertEquals(20, conc.getConcentration(2), "Mineral B");
        assertEquals(0, conc.getConcentration(3), "Mineral C");

    }

    @Test
    void testUpdateMineral() throws CoordinatesException {
        var base = CoordinatesFormat.fromString("45.0 23.0");
        MineralDeposit conc = new MineralDeposit(base);

        conc.adjustMineral(1, 10);
        conc.adjustMineral(1, 20);

        assertEquals(15, conc.getConcentration(1), "Mineral A");
    }

    
    @Test
    void testAddMineral() throws CoordinatesException {
        var base = CoordinatesFormat.fromString("45.0 23.0");
        MineralDeposit conc = new MineralDeposit(base);

        conc.addMineral(1, 10);
        conc.addMineral(1, 20);

        assertEquals(30, conc.getConcentration(1), "Mineral A");
    }
}
