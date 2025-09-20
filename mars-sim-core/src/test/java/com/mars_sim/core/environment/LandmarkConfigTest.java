package com.mars_sim.core.environment;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;

public class LandmarkConfigTest extends AbstractMarsSimUnitTest {
    public void testGetLandmarks() {
        var mgr = getSim().getConfig().getLandmarkConfiguration().getLandmarks();

        // Check some have been loaded; center of global and full radius
        var center = new Coordinates(Math.PI/2, 0D);
        var results = mgr.getFeatures(center, Math.PI/2);

        assertFalse("No landmarks found", results.isEmpty());
    }

    public void testBeagle() throws CoordinatesException {
        var mgr = getSim().getConfig().getLandmarkConfiguration().getLandmarks();

        // Check teh lanmark for beagle
        var center = CoordinatesFormat.fromString("10.6 90.0");
        var results = mgr.getFeatures(center, 0.01);

        assertEquals("Beagle found", 1, results.size());

        var beagle = results.get(0);
        assertEquals("Beagle name", "Beagle 2 Lander", beagle.getName());
        assertEquals("Beagle origin", "ESA", beagle.getOrigin());
        assertEquals("Beagel type", LandmarkType.AO, beagle.getType());
        assertEquals("Beagle location", center, beagle.getCoordinates());
    }
}
