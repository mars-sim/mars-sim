package com.mars_sim.core.environment;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.map.location.Coordinates;

public class LandmarkConfigTest extends AbstractMarsSimUnitTest {
    public void testGetLandmarks() {
        var mgr = getSim().getConfig().getLandmarkConfiguration().getLandmarks();

        // Check some have been loaded; center of global and full radius
        var center = new Coordinates(Math.PI/2, 0D);
        var results = mgr.getFeatures(center, Math.PI/2);

        assertFalse("No landmarks found", results.isEmpty());
    }
}
