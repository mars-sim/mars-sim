package com.mars_sim.core.map;

import org.junit.Test;

import com.mars_sim.core.map.MapData.MapState;
import com.mars_sim.core.map.location.Coordinates;

import junit.framework.TestCase;

public class MapDataFactoryTest extends TestCase {
    @Test
    public void testDefaultMap() {
        var found = MapDataFactory.getMapMetaData(MapDataFactory.DEFAULT_MAP_TYPE);
        assertNotNull("Default map found", found);
        assertEquals("Default map id", MapDataFactory.DEFAULT_MAP_TYPE, found.getId());
        assertTrue("Default Map has resolution layers", found.getNumLevel() > 0);
    }
	
    @Test
    public void testMultipleMaps() {
        assertTrue("Multiple map types", !MapDataFactory.getLoadedTypes().isEmpty());
    }

    @Test
    public void testLoadResolution() {
        var found = MapDataFactory.getMapMetaData(MapDataFactory.DEFAULT_MAP_TYPE);
        var mapData = found.getData(0);
        assertNotNull("Resolution 0 of default", mapData);
        assertEquals("Is ready", mapData.getStatus(), MapState.LOADED);
        assertTrue("Has default +ve RHO", mapData.getRhoDefault() > 0);
        assertEquals("Correct meta data", found, mapData.getMetaData());
        assertEquals("Correct resolution", 0, mapData.getResolution());

        var center = new Coordinates("10 N", "10 E");
        var image = mapData.createMapImage(center, 100, 100, mapData.getRhoDefault());
        assertEquals("Image width", 100, image.getWidth(null));
        assertEquals("Image height", 100, image.getHeight(null));
    }

}
