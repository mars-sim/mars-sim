/*
 * Mars Simulation Project
 * MapDataFactory.java
 * @date 2023-07-26
 * @author Scott Davis
 */

 package org.mars.sim.mapdata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**	
  * A factory for map data.
  */
 class MapDataFactory {

	private static Logger logger = Logger.getLogger(MapDataFactory.class.getName());

	// The map properties MUST contain at least this map
	private static final String SURF_MAP = "surface";
	private static final String MAP_PROPERTIES = "/mapdata.properties";

	private Map<String, MapMetaData> metaDataMap = new HashMap<>();

	private transient MapData mapData = null;
	
 	/**
 	 * Constructor.
 	 */
 	MapDataFactory() {
 		Properties mapProps = new Properties();
		try (InputStream propsStream = MapDataFactory.class.getResourceAsStream(MAP_PROPERTIES)) {
			mapProps.load(propsStream);

			for(String id : mapProps.stringPropertyNames()) {
				// Split the details into the parts
				String[] value = mapProps.getProperty(id).split(", ");
				boolean isColour = Boolean.parseBoolean(value[1]);
				String hiRes = value[2];
				String midRes = value[3];
				String loRes = value[4];

				// Locally available is based on hires image which will be bigger
//				boolean isLocal = FileLocator.isLocallyAvailable(hiRes);
				metaDataMap.put(id, new MapMetaData(id, value[0], isColour, hiRes, midRes, loRes));
			}
		} catch (IOException e) {
			throw new IllegalStateException("Cannot load " + MAP_PROPERTIES, e);
		}

		if (!metaDataMap.containsKey(SURF_MAP)) {
			throw new IllegalStateException("There is no map data for '" + SURF_MAP + "' defined.");
		}
 	}

 	/**
 	 * Gets map data of the requested type.
 	 * 
 	 * @param mapType the map type
 	 * @param selectedResolution
 	 * @return the map data
 	 */
 	void setMapData(String mapType, int selectedResolution) {

		MapMetaData metaData = metaDataMap.get(mapType);
 		if (metaData == null) {
			logger.warning("Map type " + mapType + " unknown.");
		};
		
 		// Change the map resolution
 		metaData.setResolution(selectedResolution);
 	}
 	
 	/**
 	 * Gets map data of the requested type.
 	 * 
 	 * @param mapType the map type
 	 * @return the map data
 	 */
 	MapData getMapData(String mapType) {

		MapMetaData metaData = metaDataMap.get(mapType);
 		if (metaData == null) {
			logger.warning("Map type " + mapType + " unknown.");
			return null;
		};
		
 		MapData result = null;
 		
		if (mapData == null 
				|| !mapData.getMetaData().getName().equals(metaData.getName())) {

			try {
				// Obtain a new MapData instance
				result = new IntegerMapData(metaData);
				
				// Save the result into the mapData cache
				this.mapData = result;
				
				// Patch the metadata to be locally available
				metaData.setLocallyAvailable(true);
				
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not find the map file.", e);
			}
		}
		else
			return mapData;
		
		return result;
 	}

	/**
	 * Gets the available map types.
	 * 
	 * @return
	 */
	public Collection<MapMetaData> getLoadedTypes() {
		return metaDataMap.values();
	}
	
	public void destroy() {
		metaDataMap.clear();
		metaDataMap = null;
		mapData = null;
	}
	
 }
