/*
 * Mars Simulation Project
 * MapDataFactory.java
 * @date 2023-07-26
 * @author Scott Davis
 */
 package org.mars.sim.mapdata;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars.sim.mapdata.megdr.MEGDRMapArray;
import org.mars.sim.mapdata.megdr.MEGDRMapDirect;
import org.mars.sim.mapdata.megdr.MEGDRMapMemory;
import org.mars.sim.mapdata.megdr.MEGDRMapReader;
import org.mars.sim.tools.util.RandomUtil;

/**	
  * A factory for map data.
  */
 public class MapDataFactory {

	/**
	 * These are package friendly for Unit Test
	 */
	static final String ARRAY_READER = "array";
	static final String DIRECT_READER = "direct";
	static final String MEMORY_READER = "memory";

	private static Logger logger = Logger.getLogger(MapDataFactory.class.getName());

	private static final double PI = Math.PI;
 	private static final double DEG_PER_RADIAN = 180/Math.PI;
 	
	// The map properties MUST contain at least this map
	private static final String SURF_MAP = "surface";
	private static final String MAP_PROPERTIES = "/mapdata.properties";

	private static final String ELEVATION_PROP = "elevation";

	private Map<String, MapMetaData> metaDataMap = new HashMap<>();

	private MEGDRMapReader reader;

 	/**
 	 * Constructor.
 	 */
 	MapDataFactory() {
 		
		String megdrSpec = MEMORY_READER + ", " + MEGDRMapReader.DEFAULT_MEGDR_FILE;
		
 		Properties mapProps = new Properties();
		try (InputStream propsStream = MapDataFactory.class.getResourceAsStream(MAP_PROPERTIES)) {
			mapProps.load(propsStream);

			for(String mapString : mapProps.stringPropertyNames()) {
				if (ELEVATION_PROP.equals(mapString)) {
					megdrSpec = mapProps.getProperty(ELEVATION_PROP);
				}
				else {		
					// Split the details into the parts
					String[] value = mapProps.getProperty(mapString).split(", ");
					boolean isColour = Boolean.parseBoolean(value[1]);
					String hiRes = value[2];
					String midRes = value[3];
					String loRes = value[4];

					metaDataMap.put(mapString, new MapMetaData(mapString, value[0], isColour,
										hiRes, midRes, loRes));
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Cannot load " + MAP_PROPERTIES, e);
		}

		if (!metaDataMap.containsKey(SURF_MAP)) {
			throw new IllegalStateException("There is no map data for '" + SURF_MAP + "' defined.");
		}

		reader = createReader(megdrSpec);
 	}

	/**
	 * Creates a MEGDRReader based on a spec that contains the "reader type, filename".
	 * 
	 * @param spec
	 * @return
	 */
	static MEGDRMapReader createReader(String spec) {
		String [] parts = spec.split(", ");
		
		String imageName = parts[1].trim();

		logger.config("imageName: " + imageName);
		
		try {
			return switch(parts[0]) {
				case ARRAY_READER -> new MEGDRMapArray(imageName);
				case DIRECT_READER -> new MEGDRMapDirect(imageName);
				case MEMORY_READER -> new MEGDRMapMemory(imageName);
				default -> throw new IllegalArgumentException("Unknown MEGDR reader called " + parts[0]);
			};
		}
		catch(IOException ioe) {
			logger.severe("Problem creating MEGDRReader " + ioe.getMessage());
			throw new IllegalArgumentException("Problem loading MEGDRReader:" + ioe.getMessage());
		}
	}

 	/**
 	 * Gets map data of the requested type.
 	 * 
 	 * @param mapType the map type
 	 * @param res
 	 * @return the map data
 	 */
 	void setMapData(String mapType, int res) {

		MapMetaData metaData = metaDataMap.get(mapType);
 		if (metaData == null) {
			logger.warning("Map type " + mapType + " unknown.");
			
			new MapDataFactory();
		}
 		else
 			// Change the map resolution
 			metaData.setResolution(res);
 	}
 	
 	/**
 	 * Loads the map data of the requested map type.
 	 * 
 	 * @param mapType the map type
 	 * @return the map data
 	 */
 	MapData loadMapData(String mapType) {
 		
 		MapData mapData = null;
 		
		MapMetaData metaData = metaDataMap.get(mapType);
		
 		if (metaData == null) {
			logger.warning("Map type " + mapType + " unknown.");
			return null;
		}

		try {
			// Obtain a new MapData instance
			mapData = new IntegerMapData(metaData);
			
			// Patch the metadata to be locally available
			metaData.setLocallyAvailable(true);
			
			System.out.println("Map type '" + mapType + "' (res: " + metaData.getResolution() 
					+ ") has been selected.  Map name: '" + metaData.getName() + "'"
					+ ".  Filename: " + metaData.getFile()
					+ ".  Locally AV: " + metaData.isLocallyAvailable() + ".");
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not find the map file.", e);
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
		if (reader instanceof Closeable cl) {
			try {
				cl.close();
			} catch (IOException e) {
			}
		}
		reader = null;
	}

   /**
	 * Gets the elevation as a short integer at a given location.
	 * 
	 * @param phi   the phi location.
	 * @param theta the theta location.
	 * @return the elevation as an integer.
	 */
    public short getElevation(double phi, double theta) {
        return reader.getElevation(phi, theta);
	}

	public static void main(String[] args) throws IOException {
		runPerfTest(DIRECT_READER + ", " + MEGDRMapReader.DEFAULT_MEGDR_FILE);
		runPerfTest(ARRAY_READER + ", " + MEGDRMapReader.DEFAULT_MEGDR_FILE);
		runPerfTest(MEMORY_READER + ", " + MEGDRMapReader.DEFAULT_MEGDR_FILE);
	}

	private static void runPerfTest(String spec) {
		DecimalFormat formatter = new DecimalFormat("###,###,###");

		long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		MEGDRMapReader reader = createReader(spec);
		int size = 10000;
		double pi2 = Math.PI * 2;
		Instant start = Instant.now();
		for(int i = 0; i < size; i++) {
			double phi = RandomUtil.getRandomDouble(Math.PI);
			double theta = RandomUtil.getRandomDouble(pi2);
			reader.getElevation(phi, theta);
		}
		Instant finish = Instant.now();
		long finishMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("Reader " + spec + " Memory increase " + formatter.format(finishMemory - startMemory));
		System.out.println(size + " lookups in " + Duration.between(start, finish).toMillis());
	}
 }
