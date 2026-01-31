/*
 * Mars Simulation Project
 * MapDataFactory.java
 * @date 2024-09-30
 * @author Scott Davis
 */
 package com.mars_sim.core.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mars_sim.core.map.megdr.MEGDRFactory;

/**	
  * A factory for map data. Static helper class
  */
 public class MapDataFactory {

	static final String MAPS_FOLDER = "/maps/";
	
	private static final String SEPARATOR = ",";
	
	private static Logger logger = Logger.getLogger(MapDataFactory.class.getName());
 	
	// The map properties MUST contain at least this map
	public static final String DEFAULT_MAP_TYPE = "vikingMDIM";
	
	private static final String MAP_PROPERTIES = "/mapdata.properties";

	private static final String ELEVATION_PROP = "elevation";

	private static Map<String, MapMetaData> metaDataMap = new HashMap<>();

	static {
		loadConfig();
	}

	private MapDataFactory() {
		// Prevent static helper class being created
	}

 	/**
 	 * Constructor.
 	 */
 	private static void loadConfig() {
 		String megdrSpec = null;
		
 		Properties mapProps = new Properties();
		try (InputStream propsStream = MapDataFactory.class.getResourceAsStream(MAP_PROPERTIES)) {
			mapProps.load(propsStream);

			for(String mapString : mapProps.stringPropertyNames()) {
				if (ELEVATION_PROP.equals(mapString)) {
					megdrSpec = mapProps.getProperty(ELEVATION_PROP);
				}
				else {		
					// Split the details into the parts
					String[] array = mapProps.getProperty(mapString).split(SEPARATOR);
					String description = array[0];
					boolean isColour = Boolean.parseBoolean(array[1].trim());
								
					List<String> mapList = new ArrayList<>();
					for (int i = 2; i < array.length; i++) {
						mapList.add(array[i].trim());
					}					
					metaDataMap.put(mapString, new MapMetaData(mapString, description, isColour, mapList));
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Cannot load " + MAP_PROPERTIES, e);
		}

		if (!metaDataMap.containsKey(DEFAULT_MAP_TYPE)) {
			throw new IllegalStateException("There is no map data for '" + DEFAULT_MAP_TYPE + "' defined.");
		}

		if (megdrSpec != null) {
			MEGDRFactory.setSpec(megdrSpec);
		}
 	}

 	/**
 	 * Returns the new map meta data instance.
 	 * 
 	 * @param newMapType
 	 * @return
 	 */
 	public static MapMetaData getMapMetaData(String newMapType) { 
 		return metaDataMap.get(newMapType);
 	}
 	 	
 	/**
 	 * Loads a single image files to represent a resolution within a map stack
 	 * 
 	 * @param mapMetaData the map type defining the map family
 	 * @param res The resolution level
 	 * @param imagefile Name of the image file for this resolution
 	 * @param callback Callback when the map data is loaded async
 	 * @return the map data
 	 */
 	static MapData loadMapData(MapMetaData mapMetaData, int res, String imagefile, Consumer<MapData> callback) {

		try {
			// Obtain a new MapData instance
			var mapData = new IntegerMapData(mapMetaData, res, imagefile, callback);		
			
			logger.log(Level.CONFIG, "Loading map type '" + mapMetaData.getId() 
					+ "'. Res level: " + res 
					+ ". Map name: '" + mapMetaData.getDescription()
					+ "'. Color: " + mapMetaData.isColourful());
			return mapData;

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to instantiate IntegerMapData: ", e);
			return null;
		}		
 	}

	/**
	 * Gets the available map types.
	 * 
	 * @return
	 */
	public static Collection<MapMetaData> getLoadedTypes() {
		return metaDataMap.values();
	}
 }
