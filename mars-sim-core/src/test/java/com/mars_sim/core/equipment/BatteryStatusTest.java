package com.mars_sim.core.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BatteryStatusTest {
    @Test
    void testFromValue() {
        assertEquals(BatteryStatus.FULL, BatteryStatus.fromValue(100));
        assertEquals(BatteryStatus.HIGH, BatteryStatus.fromValue(95));
        assertEquals(BatteryStatus.MED_HIGH, BatteryStatus.fromValue(80));
        assertEquals(BatteryStatus.MED, BatteryStatus.fromValue(60));
        assertEquals(BatteryStatus.MED_LOW, BatteryStatus.fromValue(40));
        assertEquals(BatteryStatus.LOW, BatteryStatus.fromValue(20));  
        assertEquals(BatteryStatus.DEPLETING, BatteryStatus.fromValue(10));
        assertEquals(BatteryStatus.DEPLETED, BatteryStatus.fromValue(1));         
    }
}