package com.mars_sim.core.environment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;

class LandmarkConfigTest {
    private LandmarkConfig lConfig;

    @BeforeEach
    void setUp() {
        lConfig = SimulationConfig.loadConfig().getLandmarkConfiguration();
    }

    @Test
    void testGetLandmarks() {
        var mgr = lConfig.getLandmarks();

        // Check some have been loaded; center of global and full radius
        var center = new Coordinates(Math.PI/2, 0D);
        var results = mgr.getFeatures(center, Math.PI/2);

        assertFalse(results.isEmpty(), "No landmarks found");
    }

    @Test
    void testBeagle() throws CoordinatesException {
        var mgr = lConfig.getLandmarks();

        // Check teh lanmark for beagle
        var center = CoordinatesFormat.fromString("10.6 90.0");
        var results = mgr.getFeatures(center, 0.01);

        assertEquals(1, results.size(), "Beagle found");

        var beagle = results.get(0);
        assertEquals("Beagle 2 Lander", beagle.getName(), "Beagle name");
        assertEquals("ESA", beagle.getOrigin(), "Beagle origin");
        assertEquals(LandmarkType.AO, beagle.getType(), "Beagel type");
        assertEquals(center, beagle.getCoordinates(), "Beagle location");
    }
}
