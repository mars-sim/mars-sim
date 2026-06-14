package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HungerLevelTest {
    @Test
    void testFromValue() {
        assertEquals(HungerLevel.TOP_OFF, HungerLevel.fromValue(0));
        assertEquals(HungerLevel.SATISFIED, HungerLevel.fromValue(100));
        assertEquals(HungerLevel.COMFY, HungerLevel.fromValue(400));
        assertEquals(HungerLevel.ADEQUATE, HungerLevel.fromValue(700));         
        assertEquals(HungerLevel.RUMBLING, HungerLevel.fromValue(1000));
        assertEquals(HungerLevel.RAVENOUS, HungerLevel.fromValue(1400));
        assertEquals(HungerLevel.FAMISHED, HungerLevel.fromValue(3000));
    }

    @Test
    void testIsAdequate() {
        assertTrue(HungerLevel.TOP_OFF.isAdequate(), "TOP_OFF should be considered adequate");
        assertTrue(HungerLevel.SATISFIED.isAdequate(), "SATISFIED should be considered adequate");
        assertTrue(HungerLevel.COMFY.isAdequate(), "COMFY should be considered adequate");
        assertTrue(HungerLevel.ADEQUATE.isAdequate(), "ADEQUATE should be considered adequate");
        assertFalse(HungerLevel.RUMBLING.isAdequate(), "RUMBLING should not be considered adequate");
        assertFalse(HungerLevel.RAVENOUS.isAdequate(), "RAVENOUS should not be considered adequate");
        assertFalse(HungerLevel.FAMISHED.isAdequate(), "FAMISHED should not be considered adequate");
    }

    @Test
    void testIsFull() {
        assertTrue(HungerLevel.TOP_OFF.isFull(), "TOP_OFF should be considered full");
        assertTrue(HungerLevel.SATISFIED.isFull(), "SATISFIED should be considered full");
        assertFalse(HungerLevel.COMFY.isFull(), "COMFY should not be considered full");
        assertFalse(HungerLevel.ADEQUATE.isFull(), "ADEQUATE should not be considered full");
        assertFalse(HungerLevel.RUMBLING.isFull(), "RUMBLING should not be considered full");
        assertFalse(HungerLevel.RAVENOUS.isFull(), "RAVENOUS should not be considered full");
        assertFalse(HungerLevel.FAMISHED.isFull(), "FAMISHED should not be considered full");
    }
}
