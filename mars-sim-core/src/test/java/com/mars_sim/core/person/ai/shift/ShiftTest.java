package com.mars_sim.core.person.ai.shift;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ShiftTest {
    @Test
    public void testShiftCompletedSameSol() {
        ShiftSpec spec = new ShiftSpec("SameDay", 400, 900, 0);
        Shift shift = new Shift(spec, 0);
        
        // Check time outside shift pattern
        assertEquals(0D, shift.getShiftCompleted(0), "No shift start of day");
        assertEquals(0D, shift.getShiftCompleted(100), "No shift time 100");
        assertEquals(0D, shift.getShiftCompleted(951), "No shift time 951");
        assertEquals(0D, shift.getShiftCompleted(990), "No shift time 990");

        // Check time inside shift pattern
        assertEquals(0.1D, shift.getShiftCompleted(450), "Shift at early");
        assertEquals(0.5D, shift.getShiftCompleted(650), "Shift at middle");
        assertEquals(0.9D, shift.getShiftCompleted(850), "Shift at end");
    }

    @Test
    public void testShiftCompletedDifferentSol() {
        ShiftSpec spec = new ShiftSpec("TwoDay", 800, 300, 0);
        Shift shift = new Shift(spec, 0);
        
        // Check time outside shift pattern
        assertEquals(0D, shift.getShiftCompleted(350), "No shift start of day");
        assertEquals(0D, shift.getShiftCompleted(500), "No shift time 500");
        assertEquals(0D, shift.getShiftCompleted(600), "No shift time 600");
        assertEquals(0D, shift.getShiftCompleted(750), "No shift time 750");

        // Check time inside shift pattern
        assertEquals(0.1D, shift.getShiftCompleted(850), "Shift at early");
        assertEquals(0.5D, shift.getShiftCompleted(50), "Shift at middle");
        assertEquals(0.9D, shift.getShiftCompleted(250), "Shift at end");
    }
}
