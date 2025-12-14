package com.mars_sim.core.time;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;

class MarsZoneTest {
    @Test
    void testGetMarsZone() throws CoordinatesException {
        int range = 360/MarsZone.NUM_ZONES;

        // Check East
        for(int i = 0; i < 180; i++) {
            int z = i / range;
            assertZone(i, "MCT+" + z, z * 50);
        }

        // Check West
        for(int i = 1; i <= 180; i++) {
            int z = 1 + ((i-1) / range);
            assertZone(-i, "MCT-" + z, 1000 - (z * 50));
        }
    }

    private void assertZone(int longitude, String name, int offset) throws CoordinatesException {
        var coord = CoordinatesFormat.fromString("20.0", Integer.toString(longitude));
        var zone =  MarsZone.getMarsZone(coord);
        assertEquals(name, zone.getId(), longitude + " zone");
        assertEquals(offset, zone.getMSolOffset(), longitude + " offset");
    }
}
