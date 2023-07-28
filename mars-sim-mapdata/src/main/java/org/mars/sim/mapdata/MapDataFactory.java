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
 public class MapDataFactory {

	private static Logger logger = Logger.getLogger(MapDataFactory.class.getName());

	private static final double PI = Math.PI;
 	private static final double TWO_PI = Math.PI * 2D;
 	private static final double DEG_PER_RADIAN = 180/Math.PI;
 	
	// The map properties MUST contain at least this map
	private static final String SURF_MAP = "surface";
	private static final String MAP_PROPERTIES = "/mapdata.properties";

 	private static short height;
 	private static short width;
 	
	private Map<String, MapMetaData> metaDataMap = new HashMap<>();

	private MEGDRMapReader reader;
	
	private MapData mapData = null;
	
 	/**
 	 * Constructor.
 	 */
 	MapDataFactory() {
 		// Set up MEGDR reader
 		setUpMEGDR();
 		
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

				metaDataMap.put(id, new MapMetaData(id, value[0], isColour, hiRes, midRes, loRes));
			}
		} catch (IOException e) {
			throw new IllegalStateException("Cannot load " + MAP_PROPERTIES, e);
		}

		if (!metaDataMap.containsKey(SURF_MAP)) {
			throw new IllegalStateException("There is no map data for '" + SURF_MAP + "' defined.");
		}
 	}

 	private void setUpMEGDR() {
        reader = new MEGDRMapReader(MEGDRMapReader.LEVEL);
        
        height = reader.getHeight();
        width = reader.getWidth();
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
		}

		if (mapData == null 
				|| !mapData.getMetaData().getName().equals(metaData.getName())) {

			try {
				// Obtain a new MapData instance
				mapData = new IntegerMapData(metaData);
				
				// Patch the metadata to be locally available
				metaData.setLocallyAvailable(true);
				
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not find the map file.", e);
			}
		}
		
		return mapData;
 	}

	/**
	 * Gets the available map types.
	 * 
	 * @return
	 */
	public Collection<MapMetaData> getLoadedTypes() {
		return metaDataMap.values();
	}
	
	/**
	 * Gets the getReaMEGDRMapReader.
	 * 
	 * @return
	 */
	public MEGDRMapReader getReader() {
		return reader;
	}
	
    /**
     * Gets the elevation array.
     * 
     * @return
     */
    public short[] elevationArray() {
    	return reader.getElevationArray();
	}
	
   /**
	 * Gets the elevation as a short integer at a given location.
	 * 
	 * @param phi   the phi location.
	 * @param theta the theta location.
	 * @return the elevation as an integer.
	 */
	public short getElevation(double phi, double theta) {
		// Note that row 0 and column 0 are at top left 
		int row = (int)Math.round(phi * height / PI);
		
		if (row == height) 
			row--;
		
		int column = (int)Math.round(theta * width / TWO_PI);

		if (column == width)
			column--;

		int index = row * width + column;
		
		if (index > height * width - 1)
			index = height * width - 1;

		return elevationArray()[index];
	}
    
	/**
	 * Transforms the pixel i and j into lat and lon coordinate.
	 * 
	 * @param i sample coordinate
	 * @param j line coordinate
	 * @param n the number of lines or samples per line in the image
      (the images are square)
	 * @param res the map resolution in pixels per degree
	 * @return
	 */
	public double[] convertToLatLon(int i, int j, int n, int res) {
		// The transformation from line and sample coordinates to planetocentric
		// latitude and longitude is given by these equations.
		
		// Convert to Cartesian coordinate system with (0,0) at center
		double x = (i - n/2.0 - 0.5)/res;
		double y = (j - n/2.0 - 0.5)/res;

		// The radius from center of map to pixel i,j
		double r = Math.sqrt(x*x + y*y);

		// The east longitude of pixel i,j in degrees
		double lon = Math.atan2(x,y) * DEG_PER_RADIAN;
		// The latitude of pixel i,j in degrees
		double lat = 0;
		
		// For northern hemisphere
		if (y > 0)
			lat = 90 - 2 * Math.atan(r * PI/360) * DEG_PER_RADIAN;
		else if (y < 0)
			// For southern hemisphere
			lat = -90 + 2 * Math.atan(r * PI/360) * DEG_PER_RADIAN;

		return new double[] {lat, lon};
	}
	
	public void destroy() {
		metaDataMap.clear();
		metaDataMap = null;
		mapData = null;
		reader = null;
	}
	
 }
