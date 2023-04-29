/*
 * Mars Simulation Project
 * MapDataFactory.java
 * @date 2023-04-28
 * @author Scott Davis
 */

 package org.mars_sim.mapdata;

import java.util.HashMap;
import java.util.Map;

/**
  * A factory for map data.
  */
 class MapDataFactory {

 	// Static members.
 	static final int SURFACE_MAP_DATA = 0; 		// "surface map data";
 	static final int TOPO_MAP_DATA = 1; 		// "topographical map data";
 	static final int GEOLOGY_MAP_DATA = 2; 		// "geological ages map data"
 	static final int REGION_MAP_DATA = 3; 		// "regional map data"
 	static final int VIKING_MAP_DATA = 4; 		// "viking map data"

	private static final String SURFACE_MAP_FILE = "/maps/surface8192x4096.jpg";
	private static final String TOPO_MAP_FILE = "/maps/topo8192x4096.jpg"; 
	//private static final String GEO_MAP_FILE = "/maps/geo8192x4096.jpg"; 
	private static final String GEO_MAP_FILE = "/maps/geo2880x1440.jpg"; 
	private static final String REGION_MAP_FILE = "/maps/region8192x4096.jpg"; 
	private static final String VIKING_MAP_FILE = "/maps/viking8192x4096.jpg"; 
		
private Map<Integer,MapData> mapdata = new HashMap<>();

 	/**
 	 * Constructor.
 	 */
 	MapDataFactory() {
 		// nothing
 	}

 	/**
 	 * Gets map data of the requested type.
 	 * 
 	 * @param mapType the map type.
 	 * @return the map data.
 	 */
 	MapData getMapData(int mapType) {

		MapData result = mapdata.get(mapType);
		if (result == null) {
			String filename = switch (mapType) {
				case SURFACE_MAP_DATA -> SURFACE_MAP_FILE;
				case GEOLOGY_MAP_DATA -> GEO_MAP_FILE;
				case TOPO_MAP_DATA -> TOPO_MAP_FILE;
				case REGION_MAP_DATA -> REGION_MAP_FILE; 
				case VIKING_MAP_DATA -> VIKING_MAP_FILE;
				default -> throw new IllegalArgumentException("No map data for type" + mapType);
			};
			result = new IntegerMapData(filename);
			mapdata.put(mapType, result);
		}
 		return result;
 	}
 }
