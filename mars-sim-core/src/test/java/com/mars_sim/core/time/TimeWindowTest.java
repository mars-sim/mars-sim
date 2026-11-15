package com.mars_sim.core.time;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TimeWindowTest {
    @Test
    void testGetDuration() {
        TimeWindow window = new TimeWindow(100, 200);
        assertEquals(100, window.getDuration(), "Duration should be 100");

        window = new TimeWindow(200, 100);
        assertEquals(900, window.getDuration(), "Duration should wrap around to 900");
    }

    @Test
    void testIsWithinRangeSameSol() {
        TimeWindow window = new TimeWindow(100, 200);
        assertEquals(true, window.isWithinRange(150), "150 should be within range");
        assertEquals(false, window.isWithinRange(50), "50 should be outside range");
        assertEquals(false, window.isWithinRange(450), "450 should be outside range");
    }

    @Test
    void testIsWithinRangeWrapAround() {
        TimeWindow window = new TimeWindow(200, 100);
        assertEquals(true, window.isWithinRange(250), "250 should be within range");
        assertEquals(true, window.isWithinRange(50), "50 should be within range");
        assertEquals(false, window.isWithinRange(150), "150 should be outside range");
    }

    @Test
    void testStartMSolZone() {
        var zone = MarsZone.getMarsZone(2);
        TimeWindow window = new TimeWindow(100, 200, zone);
        assertEquals(100 + zone.getMSolOffset(), window.startMSol(), "Start msol should be zone adjusted");
        assertEquals(200 + zone.getMSolOffset(), window.endMSol(), "End msol should be zone adjusted");
    }
}
