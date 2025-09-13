package com.mars_sim.core.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.Coordinates;

class EventScheduleTest {
    private static final int NOW_MSOL = 500;

    private static final Coordinates CENTER = new Coordinates("10N", "0E");
    private static final Coordinates ONE_THIRD = new Coordinates("10N", "120E");
    private static final Coordinates TWO_THIRDS = new Coordinates("10N", "120W");

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
        assertCalendar("Rollover big", 0, 300, new Coordinates("10N", "5W"));
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
        assertEquals(message + " : time of day", (timeOfDay + zone.getMSolOffset()) % 1000, event.getMillisolInt());

        // Check number of full sols and time of event
        var diff = event.getTimeDiff(now);
        assertEquals(message + " : sols to event", firstSol, (int)diff/1000);
        assertTrue(message + " : time now or future", diff > 0);
    }
}
