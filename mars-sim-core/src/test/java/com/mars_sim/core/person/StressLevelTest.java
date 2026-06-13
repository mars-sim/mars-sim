package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StressLevelTest {
    @Test
    void testFromValue() {
        assertEquals(StressLevel.RELAXED, StressLevel.fromValue(0));
        assertEquals(StressLevel.NOMINAL, StressLevel.fromValue(20));
        assertEquals(StressLevel.DISTURBED, StressLevel.fromValue(50));
        assertEquals(StressLevel.BEATEN, StressLevel.fromValue(80));
        assertEquals(StressLevel.BREAKDOWN, StressLevel.fromValue(100));
    }

    @Test
    void testIsStressedOut() {
        assertTrue(StressLevel.BEATEN.isStressedOut());
        assertTrue(StressLevel.BREAKDOWN.isStressedOut());
        assertFalse(StressLevel.RELAXED.isStressedOut());
        assertFalse(StressLevel.NOMINAL.isStressedOut());
        assertFalse(StressLevel.DISTURBED.isStressedOut());
    }
}
