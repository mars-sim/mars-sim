package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PerformanceLevelTest {
    @Test
    void testFromValue() {
        assertEquals(PerformanceLevel.PEAK, PerformanceLevel.fromValue(1.0));
        assertEquals(PerformanceLevel.ACTIVE, PerformanceLevel.fromValue(0.9));
        assertEquals(PerformanceLevel.BORDERLINE, PerformanceLevel.fromValue(0.70));   
        assertEquals(PerformanceLevel.DRAGGING, PerformanceLevel.fromValue(0.4));
        assertEquals(PerformanceLevel.CRIPPING, PerformanceLevel.fromValue(0.1));
    }
}
