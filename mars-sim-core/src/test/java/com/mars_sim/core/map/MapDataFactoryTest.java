package com.mars_sim.core.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.MapData.MapState;
import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;

class MapDataFactoryTest {
    @Test
    void testDefaultMap() {
        var found = MapDataFactory.getMapMetaData(MapDataFactory.DEFAULT_MAP_TYPE);
        assertNotNull(found, "Default map found");
        assertEquals(MapDataFactory.DEFAULT_MAP_TYPE, found.getId(), "Default map id");
        assertTrue(found.getNumLevel() > 0, "Default Map has resolution layers");
    }
	
    @Test
    void testMultipleMaps() {
        assertTrue(!MapDataFactory.getLoadedTypes().isEmpty(), "Multiple map types");
    }

    @Test
    void testLoadResolution() throws CoordinatesException {
        var found = MapDataFactory.getMapMetaData(MapDataFactory.DEFAULT_MAP_TYPE);
        var mapData = found.getData(0);
        assertNotNull(mapData, "Resolution 0 of default");
        assertEquals(MapState.LOADED, mapData.getStatus(), "Is ready");
        assertTrue(mapData.getRhoDefault() > 0, "Has default +ve RHO");
        assertEquals(found, mapData.getMetaData(), "Correct meta data");
        assertEquals(0, mapData.getResolution(), "Correct resolution");

        var center = CoordinatesFormat.fromString("10.0 10.0");
        var image = mapData.createMapImage(center, 100, 100, mapData.getRhoDefault());
        assertEquals(100, image.getWidth(null), "Image width");
        assertEquals(100, image.getHeight(null), "Image height");
    }

}
