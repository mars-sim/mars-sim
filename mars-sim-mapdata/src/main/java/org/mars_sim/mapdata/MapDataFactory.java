/**
 * Mars Simulation Project
 * MapDataFactory.java
 * @version 3.1.0 2018-10-04
 * @author Scott Davis
 */

package org.mars_sim.mapdata;

/**
 * A factory for map data.
 */
class MapDataFactory {

	// Static members.
	static final int SURFACE_MAP_DATA = 0; 		// "surface map data";
	static final int TOPO_MAP_DATA = 1; 		// "topographical map data";
	static final int GEOLOGY_MAP_DATA = 2; 		// "geological ages map data"

//	private boolean decompressed = false;

	// Data members.
	private MapData surfaceMapData;
	private MapData topoMapData;
	private MapData geologyMapData;

	/**
	 * Constructor.
	 */
	MapDataFactory() {

	}

	/**
	 * Gets map data of the requested type.
	 * 
	 * @param mapType the map type.
	 * @return the map data.
	 */
	MapData getMapData(int mapType) {
		MapData result = null;

		// Decompress the dat maps
//		if (!decompressed) {
//			new DecompressXZ();
//			// Only need to do it once
//			decompressed = true;
//		}

		if (mapType == SURFACE_MAP_DATA) {
			result = getSurfaceMapData();
		} else if (mapType == TOPO_MAP_DATA) {
			result = getTopoMapData();
		} else if (mapType == GEOLOGY_MAP_DATA) {
			result = getGeologyMapData();
		} else {
			throw new IllegalArgumentException("mapType: " + mapType + " not a valid type.");
		}

		return result;
	}

	/**
	 * Gets the surface map data.
	 * 
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
	 * 
	 * @return topographical map data.
	 */
	private MapData getTopoMapData() {
		// Create topo map data if it doesn't exist.
		if (topoMapData == null) {
			topoMapData = new TopoMapData();
		}
		return topoMapData;
	}

	/**
	 * Gets the geological map data.
	 * 
	 * @return geological map data.
	 */
	private MapData getGeologyMapData() {
		// Create geology map data if it doesn't exist.
		if (geologyMapData == null) {
			geologyMapData = new GeologyMapData();
		}
		return geologyMapData;
	}

}