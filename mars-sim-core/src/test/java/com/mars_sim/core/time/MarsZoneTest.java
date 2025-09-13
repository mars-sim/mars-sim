package com.mars_sim.core.time;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.Coordinates;

class MarsZoneTest {
    @Test
    void testGetMarsZone() {
        int range = 360/MarsZone.NUM_ZONES;

        // Check East
        for(int i = 0; i < 180; i++) {
            int z = i / range;
            assertZone(i + "E", "MCT+" + z, z * 50);
        }

        // Check West
        for(int i = 1; i <= 180; i++) {
            int z = 1 + ((i-1) / range);
            assertZone(i + "W", "MCT-" + z, 1000 - (z * 50));
        }
    }

    private void assertZone(String longitude, String name, int offset) {
        var coord = new Coordinates("20N", longitude);
        var zone =  MarsZone.getMarsZone(coord);
        assertEquals(longitude, name, zone.getId());
        assertEquals(longitude + " offset", offset, zone.getMSolOffset());
    }
}
