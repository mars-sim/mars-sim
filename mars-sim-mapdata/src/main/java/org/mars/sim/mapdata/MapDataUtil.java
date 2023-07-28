/*
 * Mars Simulation Project
 * MapDataUtil.java
 * @date 2023-06-03
 * @author Scott Davis
 */

 package org.mars.sim.mapdata;

import java.util.Collection;

/**
  * A singleton static utility class for accessing Mars map data.
  */
 public final class MapDataUtil {

	// Singleton instance.
	private static MapDataUtil instance;
	
	private static MapDataFactory mapDataFactory;
 	
    /**
     * Gets the singleton instance of MapData.
     * 
     * @return instance.
     */
    public static MapDataUtil instance() {
        if (instance == null) {
            instance = new MapDataUtil();
        }
        return instance;
    }
    
     /**
      * Private constructor for static utility class.
      */
     private MapDataUtil() {
         mapDataFactory = new MapDataFactory();
     }
     
     /**
      * Gets the MapDataFactory instance.
      * 
      * @return
      */
	public MapDataFactory getMapDataFactory() {
     	return mapDataFactory;
 	}
 	
 	/**
 	 * Gets the map data.
 	 * 
 	 * @param mapType
 	 * @param selectedResolution
 	 * @return
 	 */
 	public MapData getMapData(String mapType) {
 		return mapDataFactory.getMapData(mapType);
 	}

 	/**
 	 * Sets the map data.
 	 * 
 	 * @param mapType
 	 * @param resolution
 	 */
 	public void setMapData(String mapType, int resolution) {
 		mapDataFactory.setMapData(mapType, resolution);
 	}
 	
     /**
      * Gets the map types available.
      * 
      * @return
      */
    public Collection<MapMetaData> getMapTypes() {
        return mapDataFactory.getLoadedTypes();
    }
    
	/**
	 * Prepares objects for deletion.
	 */
	public void destroy() {
	 	instance = null;
		mapDataFactory = null;
	}
 	
 }
