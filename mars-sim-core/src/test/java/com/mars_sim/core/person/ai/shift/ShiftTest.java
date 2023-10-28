package com.mars_sim.core.person.ai.shift;


import junit.framework.TestCase;

public class ShiftTest extends TestCase  {
    public void testShiftCompletedSameSol() {
        ShiftSpec spec = new ShiftSpec("SameDay", 400, 900, 0);
        Shift shift = new Shift(spec, 0);
        
        // Check time outside shift pattern
        assertEquals("No shift start of day", 0D, shift.getShiftCompleted(0));
        assertEquals("No shift time 100", 0D, shift.getShiftCompleted(100));
        assertEquals("No shift time 951", 0D, shift.getShiftCompleted(951));
        assertEquals("No shift time 990", 0D, shift.getShiftCompleted(990));

        // Check time inside shift pattern
        assertEquals("Shift at early", 0.1D, shift.getShiftCompleted(450));
        assertEquals("Shift at middle", 0.5D, shift.getShiftCompleted(650));
        assertEquals("Shift at end", 0.9D, shift.getShiftCompleted(850));
    }

    public void testShiftCompletedDifferentSol() {
        ShiftSpec spec = new ShiftSpec("TwoDay", 800, 300, 0);
        Shift shift = new Shift(spec, 0);
        
        // Check time outside shift pattern
        assertEquals("No shift start of day", 0D, shift.getShiftCompleted(350));
        assertEquals("No shift time 500", 0D, shift.getShiftCompleted(500));
        assertEquals("No shift time 600", 0D, shift.getShiftCompleted(600));
        assertEquals("No shift time 750", 0D, shift.getShiftCompleted(750));

        // Check time inside shift pattern
        assertEquals("Shift at early", 0.1D, shift.getShiftCompleted(850));
        assertEquals("Shift at middle", 0.5D, shift.getShiftCompleted(50));
        assertEquals("Shift at end", 0.9D, shift.getShiftCompleted(250));
    }
}
