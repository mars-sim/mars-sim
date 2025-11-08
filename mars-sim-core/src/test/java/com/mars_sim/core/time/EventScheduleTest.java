package com.mars_sim.core.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.Coordinates;

class EventScheduleTest {
    private static final int NOW_MSOL = 500;
    private static final double LONG = Math.PI/2;

    private static final Coordinates CENTER = new Coordinates(LONG, 0D);
    private static final Coordinates ONE_THIRD = new Coordinates(LONG, Math.PI * 2 / 3);
    private static final Coordinates TWO_THIRDS = new Coordinates(LONG, -Math.PI * 2 / 3);

    @Test
    void testBasic() {
        assertCalendar("Basic", 0, 300, CENTER);
    }
    
    @Test
    void testThird() {
        assertCalendar("Third", 0, 300, ONE_THIRD);
    }

    @Test
    void testRollover() {
        assertCalendar("Rollover", 0, 300, TWO_THIRDS);
    }

    @Test
    void testRolloverBig() {
        assertCalendar("Rollover big", 0, 300, new Coordinates(LONG, -0.1));
    }

    @Test
    void testBasicDelayed() {
        assertCalendar("Basic Delayed", 3, 300, CENTER);
    }

    @Test
    void testThirdDelayed() {
        assertCalendar("Delayed", 2, 300, ONE_THIRD);
    }

    @Test
    void testRolloverDelayed() {
        assertCalendar("Delayed Rollover", 2, 300, TWO_THIRDS);
    }

    @Test
    void testPastTime() {
        // Time of event start has just past
        assertCalendar("Delayed Rollover event", 0, NOW_MSOL-1, CENTER);
    }

    private void assertCalendar(String message, int firstSol, int timeOfDay, Coordinates locn) {
        var zone = MarsZone.getMarsZone(locn);
        var cal = new EventSchedule(firstSol, 0, timeOfDay);
        var now = new MarsTime(1, 1, 1, NOW_MSOL, 1);
        var event = cal.getFirstEvent(now, zone);

        // Check first event is correct time of day
        assertEquals((timeOfDay + zone.getMSolOffset()) % 1000, event.getMillisolInt(), message + " : time of day");

        // Check number of full sols and time of event
        var diff = event.getTimeDiff(now);
        assertEquals(firstSol, (int)diff/1000, message + " : sols to event");
        assertTrue(diff > 0, message + " : time now or future");
    }
}
