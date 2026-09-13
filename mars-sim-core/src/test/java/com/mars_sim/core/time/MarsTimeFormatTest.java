package com.mars_sim.core.time;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;

class MarsTimeFormatTest {

    @Test
    void testFromDate1() {
        testMarsTime("01-Adir-02:123.000", 1, 1, 2, 123D);
    }

    @Test
    void testFromDate2() {
        testMarsTime("01-Flo-12:789.123", 1, 6, 12, 789.123D);
    }

    private void testMarsTime(String source, int orbit, int month, int solInMonth, double milliSols) {
        MarsTime created = MarsTimeFormat.fromDateString(source);

        assertEquals(orbit, created.getOrbit(), "'" + source + "' orbit");
        assertEquals(month, created.getMonth(), "'" + source + "' sol in month");
        assertEquals(solInMonth, created.getSolOfMonth(), "'" + source + "' orbit");
        assertEquals(milliSols, created.getMillisol(), "'" + source + "' milliSol");
    }

    @Test
    void testConvert() {
        MarsTime start = new MarsTime(1, 4, 0, 15, 123.456, 1);
        String text = MarsTimeFormat.getDateTimeStamp(start);
        MarsTime result = MarsTimeFormat.fromDateString(text);
        assertEquals(start, result, "Converted to String and back");
    }

    @Test
    void testGetZonedDateTimeStamp() throws CoordinatesException {
        int mSol = 123;
        MarsTime time = new MarsTime(1, 1, 0, 4, mSol, 1);

        var zone = MarsZone.getMarsZone(CoordinatesFormat.fromString("0N 90E"));
        String text = MarsTimeFormat.getZonedDateTimeStamp(time, zone);
        assertEquals("01-Adir-04:" + Integer.toString(mSol + zone.getMSolOffset()) + " " + zone.getId(), text);
    }
}
