package com.mars_sim.core.time;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MarsTime Tests")
class MarsTimeTest {

    @Test
    @DisplayName("Adding time should increment sol and maintain time difference")
    void testAddTime() {
        MarsTime start = new MarsTime(1, 1, 1, 100D, 1);

        MarsTime later = start.addTime(1000D);
        assertEquals(start.getSolOfMonth() + 1, later.getSolOfMonth(), "New Sol of Month");
        assertNotEquals(start, later, "Old and new time are different");
        assertEquals(1000D, later.getTimeDiff(start), "Time difference");
    }

    @Test
    @DisplayName("Adding time at month end should roll over to next month")
    void testAddTimeMonthEnd() {
        MarsTime start = new MarsTime(1, 1, MarsTime.SOLS_PER_MONTH_LONG, 100D, 1);

        MarsTime later = start.addTime(1000D);
        assertEquals(1, later.getSolOfMonth(), "New Sol of Month");
        assertEquals(start.getMonth() + 1, later.getMonth(), "New Month");
        assertNotEquals(start, later, "Old and new Sol time are different");
        assertEquals(1000D, later.getTimeDiff(start), "Sol Time difference");
    }

    @Test
    @DisplayName("Adding time at month end with millisols should handle rollover correctly")
    void testAddTimeMonthEndMSols() {
        MarsTime start = new MarsTime(1, 1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);

        MarsTime later = start.addTime(500D);
        assertEquals(1, later.getSolOfMonth(), "New Sol of Month");
        assertEquals(start.getMonth() + 1, later.getMonth(), "New Month");
        assertEquals(100D, later.getMillisol(), "New milliSols");
    }

    @Test
    @DisplayName("MarsTime objects with same values should be equal")
    void testTimeEquals() {
        MarsTime start = new MarsTime(1, 1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);
        MarsTime same = new MarsTime(1, 1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);

        assertEquals(start, same, "MarsTime equals");
    }

    @Test
    @DisplayName("Time difference calculation should work correctly")
    void testDifferenceTime() {
        MarsTime start = new MarsTime(1, 1, 1, 100D, 1);

        MarsTime later = new MarsTime(1, 1, 1, 150D, 1);
        assertEquals(50D, later.getTimeDiff(start), "Difference of same sol");

        later = new MarsTime(1, 1, 2, 150D, 1);
        assertEquals(1050D, later.getTimeDiff(start), "Difference of different sol");
    }

    @Test
    @DisplayName("Time comparison should work correctly")
    void testCompare() {
        MarsTime start = new MarsTime(1, 1, 1, 100D, 1);

        assertEquals(0, start.compareTo(start), "Compare same time");

        MarsTime later = start.addTime(1000D);
        assertEquals(-1, start.compareTo(later), "Compare old to new");
        assertEquals(1, later.compareTo(start), "Compare new to old");
    }

    @Test
    @DisplayName("Mars date should be same for times on same sol")
    void testMarsDate() {
        MarsTime start = new MarsTime(1, 1, 1, 100D, 1);
        MarsTime later = new MarsTime(1, 1, 1, 150D, 1);

        assertEquals(start.getDate(), later.getDate(), "Difference of same mars Date");

        later = new MarsTime(1, 1, 2, 150D, 1);
        assertNotEquals(start.getDate(), later.getDate(), "Difference of different MarsDates");
    }

    @Test
    @DisplayName("Creating time from millisols should work correctly")
    void testFromMillisols() {
        MarsTime time0 = new MarsTime(0, 1, 1, 0, 1);
        assertMarsTimeEquality(time0,  new MarsTime(time0.getTotalMillisols()));

        MarsTime time1 = new MarsTime(1, 2, 1, 100D, 1);
        assertMarsTimeEquality(time1,  new MarsTime(time1.getTotalMillisols()));
        
        MarsTime time2 = new MarsTime(2, 5, 18, 500D, 1);
        assertMarsTimeEquality(time2,  new MarsTime(time2.getTotalMillisols()));

        MarsTime time3 = new MarsTime(10, 10, 4, 30D, 1);
        assertMarsTimeEquality(time3,  new MarsTime(time3.getTotalMillisols()));
    }

    private void assertMarsTimeEquality(MarsTime expected, MarsTime actual) {
        var prefix = expected.getDateTimeStamp();
        assertEquals(expected.getSolOfMonth(), actual.getSolOfMonth(), prefix + " New Sol of Month");
        assertEquals(expected.getMonth(), actual.getMonth(), prefix + " New Month");
        assertEquals(expected.getOrbit(), actual.getOrbit(), prefix + " New Orbit");
        assertEquals(expected.getMillisol(), actual.getMillisol(), prefix + " New milliSols");
        assertEquals(expected, actual, prefix + " Old and new time are equal");
    }
}
