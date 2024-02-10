package com.mars_sim.core.environment;

import com.mars_sim.mapdata.location.Coordinates;

import junit.framework.TestCase;

public class MarsSurfaceTest extends TestCase {
    public void testGetTimeOffset() {
        Coordinates point = new Coordinates("0.0 N", "0.0 E");
        assertEquals("Offset on merdian", 0, MarsSurface.getTimeOffset(point));

        point = new Coordinates("0.0 N", "90.0 E");
        assertEquals("Offset on quarter round", 250, MarsSurface.getTimeOffset(point));

        point = new Coordinates("0.0 N", "180.0 E");
        assertEquals("Offset on half round", 500, MarsSurface.getTimeOffset(point));

        point = new Coordinates("0.0 N", "270.0 E");
        assertEquals("Offset on three quarters round", 750, MarsSurface.getTimeOffset(point));

        point = new Coordinates("0.0 N", "360.0 E");
        assertEquals("Offset on full round", 0, MarsSurface.getTimeOffset(point));
    }
}
