package org.mars_sim.msp.core.time;

import junit.framework.TestCase;

public class MarsTimeTest extends TestCase {

    public void testAddTime() {
        MarsTime start = new MarsTime(1,1, 1, 100D, 1);

        MarsTime later = start.addTime(1000D);
        assertEquals("New Sol of Month", start.getSolOfMonth() + 1, later.getSolOfMonth());
    }

    public void testAddTimeMonthEnd() {
        MarsTime start = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 100D, 1);

        MarsTime later = start.addTime(1000D);
        assertEquals("New Sol of Month", 1, later.getSolOfMonth());
        assertEquals("New Month", start.getMonth() +1, later.getMonth());
    }

    public void testAddTimeMonthEndMSols() {
        MarsTime start = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);

        MarsTime later = start.addTime(500D);
        assertEquals("New Sol of Month", 1, later.getSolOfMonth());
        assertEquals("New Month", start.getMonth() +1, later.getMonth());
        assertEquals("New milliSols", 100D, later.getMillisol());
    }

    public void testTimeEquals() {
        MarsTime start = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);
        MarsTime same = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);

        assertEquals("MarsTime equals", start, same);
    }
}
