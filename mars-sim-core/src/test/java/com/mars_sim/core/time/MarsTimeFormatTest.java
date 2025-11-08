package com.mars_sim.core.time;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MarsTimeFormatTest {

    @Test
    public void testFromDate1() {
        testMarsTime("01-Adir-02:123.000", 1, 1, 2, 123D);
    }

    @Test
    public void testFromDate2() {
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
    public void testConvert() {
        MarsTime start = new MarsTime(1, 4, 15, 123.456, 1);
        String text = MarsTimeFormat.getDateTimeStamp(start);
        MarsTime result = MarsTimeFormat.fromDateString(text);
        assertEquals(start, result, "Converted to String and back");
    }
}
