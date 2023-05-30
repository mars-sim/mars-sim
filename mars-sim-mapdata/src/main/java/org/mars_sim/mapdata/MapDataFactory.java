/*
 * Mars Simulation Project
 * MapDataFactory.java
 * @date 2023-05-02
 * @author Scott Davis
 */

 package org.mars_sim.mapdata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.common.FileLocator;

/**	
  * A factory for map data.
  */
 class MapDataFactory {

	private static Logger logger = Logger.getLogger(MapDataFactory.class.getName());

	// The map properties MUST contain at least this map
	private static final String SURF_MAP = "surface";
	private static final String MAP_PROPERTIES = "/mapdata.properties";

	private Map<String,MapMetaData> metaData = new HashMap<>();

 	/**
 	 * Constructor.
 	 */
 	MapDataFactory() {
 		Properties mapProps = new Properties();
		try (InputStream propsStream = MapDataFactory.class.getResourceAsStream(MAP_PROPERTIES)) {
			mapProps.load(propsStream);

			for(String id : mapProps.stringPropertyNames()) {
				// Split the details into the parts
				String[] value = mapProps.getProperty(id).split(",");
				boolean isColour = Boolean.parseBoolean(value[1]);
				String hiRes = value[2];
				String loRes = value[3];

				// Locally avialable is based on hires image which will be bigger
				boolean isLocal = FileLocator.isLocallyAvailable(hiRes);
				metaData.put(id, new MapMetaData(id, value[0], isLocal, isColour, hiRes, loRes));
			}
		} catch (IOException e) {
			throw new IllegalStateException("Cannot load " + MAP_PROPERTIES, e);
		}

		if (!metaData.containsKey(SURF_MAP)) {
			throw new IllegalStateException("There is no map data for '" + SURF_MAP + "' defined");
		}
 	}

 	/**
 	 * Gets map data of the requested type.
 	 * 
 	 * @param mapType the map type.
 	 * @return the map data. Maybe null if problems
 	 */
 	MapData getMapData(String mapType) {

		MapMetaData mt = metaData.get(mapType);
 		if (mt == null) {
			logger.warning("Map type " + mapType + " unknown.");
			return null;
		};
		

 		MapData result = null;
		try {
			result = new IntegerMapData(mt);
			
			// Patch the metadata to be locally available
			mt.setLocallyAvailable(true);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not find the map file.", e);
		}
		return result;
 	}

	/**
	 * Gets the available map types.
	 * 
	 * @return
	 */
	public Collection<MapMetaData> getLoadedTypes() {
		return metaData.values();
	}
 }
