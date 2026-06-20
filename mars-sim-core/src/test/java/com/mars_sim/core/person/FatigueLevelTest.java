package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FatigueLevelTest {
    @Test
    void testFromValue() {
        assertEquals(FatigueLevel.RESTED, FatigueLevel.fromValue(0));
        assertEquals(FatigueLevel.RESTED, FatigueLevel.fromValue(400));
        assertEquals(FatigueLevel.NOMINAL, FatigueLevel.fromValue(501));
        assertEquals(FatigueLevel.NOMINAL, FatigueLevel.fromValue(799));
        assertEquals(FatigueLevel.FRAZZLED, FatigueLevel.fromValue(801));
        assertEquals(FatigueLevel.FRAZZLED, FatigueLevel.fromValue(1100));
        assertEquals(FatigueLevel.EXHAUSTED, FatigueLevel.fromValue(1201));
        assertEquals(FatigueLevel.EXHAUSTED, FatigueLevel.fromValue(1599));
        assertEquals(FatigueLevel.BEDBOUND, FatigueLevel.fromValue(1601));
        assertEquals(FatigueLevel.BEDBOUND, FatigueLevel.fromValue(2000));
    }

    @Test
    void testIsSleepy() {
        assertEquals(false, FatigueLevel.RESTED.isSleepy());
        assertEquals(false, FatigueLevel.NOMINAL.isSleepy());
        assertEquals(true, FatigueLevel.FRAZZLED.isSleepy());
        assertEquals(true, FatigueLevel.EXHAUSTED.isSleepy());
        assertEquals(true, FatigueLevel.BEDBOUND.isSleepy());
    }
}
