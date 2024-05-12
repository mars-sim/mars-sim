package com.mars_sim.core.person.ai.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.task.EVAOperation.LightLevel;
import com.mars_sim.mapdata.location.Coordinates;

public class EVAOperationTest extends AbstractMarsSimUnitTest{

    /**
     * This test does not attempt to check the solar irradiance logic just the light levels.
     */
    public void testIsSunlightAroundGlobal() {
        var locn = new Coordinates("0.0 N", "0.0 E");
        
        assertLightLevel("zero time, center locn", locn, false, false);

        // First 90 degress is on the darkside
        
        locn = new Coordinates("0.0 N", "120.0 E");
        assertLightLevel("zero time, quarter locn", locn, true, false);

        locn = new Coordinates("0.0 N", "180.0 E");
        assertLightLevel("zero time, half locn", locn, true, true);

        locn = new Coordinates("0.0 N", "120.0 W");
        assertLightLevel("zero time, three quarters locn", locn, true, false);
    }

    private void assertLightLevel(String message, Coordinates locn, boolean low, boolean high) {
        // Always returns true
        assertEquals(message + " none level", true, EVAOperation.isSunlightAboveLevel(locn, LightLevel.NONE));
        assertEquals(message + " low level", low, EVAOperation.isSunlightAboveLevel(locn, LightLevel.LOW));
        assertEquals(message + " high level", high, EVAOperation.isSunlightAboveLevel(locn, LightLevel.HIGH));
    }
}
