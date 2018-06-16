/**
 * Mars Simulation Project
 * MapDataUtility.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.mapdata;

/**
 * Static utility class for accessing Mars map data.
 */
public final class MapDataUtil {

    // Singleton instance.
    private static MapDataUtil instance;
    private static MapDataFactory mapDataFactory;
    
    /**
     * Private constructor for static utility class.
     */
    private MapDataUtil() {
        mapDataFactory = new MapDataFactory();
    }
    
    /**
     * Get the singleton instance of MapData.
     * @return instance.
     */
    public static MapDataUtil instance() {
        if (instance == null) {
            instance = new MapDataUtil();
        }
        return instance;
    }
    
    /**
     * Get the surface map data.
     * @return surface map data.
     */
    public MapData getSurfaceMapData() {
        return mapDataFactory.getMapData(MapDataFactory.SURFACE_MAP_DATA);
    }
    
    /**
     * Get the topographical map data.
     * @return topographical map data.
     */
    public MapData getTopoMapData() {
        return mapDataFactory.getMapData(MapDataFactory.TOPO_MAP_DATA);
    }
}