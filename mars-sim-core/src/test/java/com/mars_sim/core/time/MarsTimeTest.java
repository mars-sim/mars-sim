package com.mars_sim.core.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class MarsTimeTest {

    @Test
    void testAddTime() {
        MarsTime start = new MarsTime(1,1, 1, 100D, 1);

        MarsTime later = start.addTime(1000D);
        assertEquals(start.getSolOfMonth() + 1, later.getSolOfMonth(), "New Sol of Month");
        assertNotEquals(start, later, "Old and new time are different");
        assertEquals(1000D, later.getTimeDiff(start), "Time difference");
    }

    @Test
    void testAddTimeMonthEnd() {
        MarsTime start = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 100D, 1);

        MarsTime later = start.addTime(1000D);
        assertEquals(1, later.getSolOfMonth(), "New Sol of Month");
        assertEquals(start.getMonth() +1, later.getMonth(), "New Month");
        assertNotEquals(start, later, "Old and new Sol time are different");
        assertEquals(1000D, later.getTimeDiff(start), "SOl Time difference");
    }

    @Test
    void testAddTimeMonthEndMSols() {
        MarsTime start = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);

        MarsTime later = start.addTime(500D);
        assertEquals(1, later.getSolOfMonth(), "New Sol of Month");
        assertEquals(start.getMonth() +1, later.getMonth(), "New Month");
        assertEquals(100D, later.getMillisol(), "New milliSols");
    }

    @Test
    void testTimeEquals() {
        MarsTime start = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);
        MarsTime same = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);

        assertEquals(start, same, "MarsTime equals");
    }

    @Test
    void testDifferenceTime() {
        MarsTime start = new MarsTime(1,1, 1, 100D, 1);

        MarsTime later = new MarsTime(1,1, 1, 150D, 1);
        assertEquals(50D, later.getTimeDiff(start), "Difference of same sol");

        later = new MarsTime(1,1, 2, 150D, 1);
        assertEquals(1050D, later.getTimeDiff(start), "Difference of different sol");
    }

    @Test
    void testCompare() {
        MarsTime start = new MarsTime(1,1, 1, 100D, 1);

        assertEquals(0, start.compareTo(start), "Compare same time");

        MarsTime later = start.addTime(1000D);
        assertEquals(-1, start.compareTo(later), "Compare old to new");
        assertEquals(1, later.compareTo(start), "Compare new to old");
    }

    @Test
    void testMarsDate() {
        MarsTime start = new MarsTime(1,1, 1, 100D, 1);
        MarsTime later = new MarsTime(1,1, 1, 150D, 1);

        assertEquals(start.getDate(), later.getDate(), "Difference of same mars Date");

        later = new MarsTime(1,1, 2, 150D, 1);
        assertNotEquals(start.getDate(), later.getDate(), "Difference of different MarsDates");
    }

    @Test
    void testAdvanceToNextMSolSameSol() {
        MarsTime start = new MarsTime(1, 1, 1, 100D, 1);

        MarsTime later = start.advanceToNextMSol(500);

        assertEquals(start.getOrbit(), later.getOrbit(), "Orbit should stay the same");
        assertEquals(start.getMonth(), later.getMonth(), "Month should stay the same");
        assertEquals(start.getSolOfMonth(), later.getSolOfMonth(), "Sol should stay the same");
        assertEquals(start.getMissionSol(), later.getMissionSol(), "Mission sol should stay the same");
        assertEquals(500D, later.getMillisol(), "Millisol should advance to target");
    }

    @Test
    void testAdvanceToNextMSolSameTimeGoesToNextSol() {
        MarsTime start = new MarsTime(1, 1, 1, 600D, 1);

        MarsTime later = start.advanceToNextMSol(500);

        assertEquals(start.getOrbit(), later.getOrbit(), "Orbit should stay the same");
        assertEquals(start.getMonth(), later.getMonth(), "Month should stay the same");
        assertEquals(start.getSolOfMonth() + 1, later.getSolOfMonth(), "Sol should advance to next sol");
        assertEquals(start.getMissionSol() + 1, later.getMissionSol(), "Mission sol should advance to next sol");
        assertEquals(500D, later.getMillisol(), "Millisol should match target on next sol");
    }

    @Test
    void testAdvanceToNextMSolAcrossMonthBoundary() {
        MarsTime start = new MarsTime(1, 1, MarsTime.SOLS_PER_MONTH_LONG, 900D, 1);

        MarsTime later = start.advanceToNextMSol(100);

        assertEquals(start.getOrbit(), later.getOrbit(), "Orbit should stay the same");
        assertEquals(start.getMonth() + 1, later.getMonth(), "Month should advance at month boundary");
        assertEquals(1, later.getSolOfMonth(), "Sol should rollover to first sol of new month");
        assertEquals(start.getMissionSol() + 1, later.getMissionSol(), "Mission sol should advance by one");
        assertEquals(100D, later.getMillisol(), "Millisol should match target on next sol");
    } 
}
