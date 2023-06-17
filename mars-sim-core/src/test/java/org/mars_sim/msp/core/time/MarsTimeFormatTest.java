package org.mars_sim.msp.core.time;

import junit.framework.TestCase;

public class MarsTimeFormatTest extends TestCase {

    public void testFromDate1() {
        testMarsTime("01-Adir-02:123.000", 1, 1, 2, 123D);
    }

    public void testFromDate2() {
        testMarsTime("01-Flo-12:789.123", 1, 6, 12, 789.123D);
    }

    private void testMarsTime(String source, int orbit, int month, int solInMonth, double milliSols) {
        MarsTime created = MarsTimeFormat.fromDateString(source);

        assertEquals("'" + source + "' orbit", orbit, created.getOrbit());
        assertEquals("'" + source + "' sol in month", month, created.getMonth());
        assertEquals("'" + source + "' orbit", solInMonth, created.getSolOfMonth());
        assertEquals("'" + source + "' milliSol", milliSols, created.getMillisol());
    }

    public void testConvert() {
        MarsTime start = new MarsTime(1, 4, 15, 123.456, 1);
        String text = MarsTimeFormat.getDateTimeStamp(start);
        MarsTime result = MarsTimeFormat.fromDateString(text);
        assertEquals("Converted to String and back", start, result);
    }
}
