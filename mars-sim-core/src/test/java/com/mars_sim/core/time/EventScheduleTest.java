package com.mars_sim.core.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

class EventScheduleTest {
    private static final int NOW_MSOL = 500;

    @Test
    void testBasic() {
        assertCalendar("Basic", 0, 300, 0);
    }
    
    @Test
    void testThird() {
        assertCalendar("Third", 0, 300, 300);
    }

    @Test
    void testRollover() {
        assertCalendar("Rollover", 0, 300, 700);
    }

    @Test
    void testRolloverBig() {
        assertCalendar("Rollover big", 0, 300, 900);
    }

    @Test
    void testBasicDelayed() {
        assertCalendar("Basic Delayed", 3, 300, 0);
    }

    @Test
    void testThirdDelayed() {
        assertCalendar("Delayed", 2, 300, 300);
    }

    @Test
    void testRolloverDelayed() {
        assertCalendar("Delayed Rollover", 2, 300, 700);
    }

    @Test
    void testPastTime() {
        // Time of event start has just past
        assertCalendar("Delayed Rollover event", 0, NOW_MSOL-1, 0);
    }

    private void assertCalendar(String message, int firstSol, int timeOfDay, int timeOffset) {
        var cal = new EventSchedule(firstSol, 0, timeOfDay);
        var now = new MarsTime(1, 1, 1, NOW_MSOL, 1);
        var event = cal.getFirstEvent(now, timeOffset);

        // Check first event is correct time of day
        assertEquals(message + " : time of day", (timeOfDay + timeOffset) % 1000, event.getMillisolInt());

        // Check number of full sols and time of event
        var diff = event.getTimeDiff(now);
        assertEquals(message + " : sols to event", firstSol, (int)diff/1000);
        assertTrue(message + " : time now or future", diff > 0);
    }
}
