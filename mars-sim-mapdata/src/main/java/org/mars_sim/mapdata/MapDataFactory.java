/**
 * Mars Simulation Project
 * MapDataFactory.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.mapdata;

/**
 * A factory for map data.
 */
class MapDataFactory {

    // Static members.
    static final String SURFACE_MAP_DATA = "surface map data";
    static final String TOPO_MAP_DATA = "topographical map data";
    
    // Data members.
    private MapData surfaceMapData;
    private MapData topoMapData;
    
    /**
     * Constructor.
     */
    MapDataFactory() {
        
    }
    
    /**
     * Gets map data of the requested type.
     * @param mapType the map type.
     * @return the map data.
     */
    MapData getMapData(String mapType) {
        MapData result = null;
        
        if (mapType.equals(SURFACE_MAP_DATA)) {
            result = getSurfaceMapData();
        }
        else if (mapType.equals(TOPO_MAP_DATA)) {
            result = getTopoMapData();
        }
        else {
            throw new IllegalArgumentException("mapType: " + mapType + " not a valid type.");
        }
        
        return result;
    }
    
    /**
     * Gets the surface map data.
     * @return surface map data.
     */
    private MapData getSurfaceMapData() {
        // Create surface map data if it doesn't exist.
        if (surfaceMapData == null) {
            surfaceMapData = new SurfaceMapData();
        }
        return surfaceMapData;
    }
    
    /**
     * Gets the topographical map data.
     * @return topographical map data.
     */
    private MapData getTopoMapData() {
        // Create topo map data if it doesn't exist.
        if (topoMapData == null) {
            topoMapData = new TopoMapData();
        }
        return topoMapData;
    }
}