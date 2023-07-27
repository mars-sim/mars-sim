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
 	
	private static final double PI = Math.PI;
 	private static final double TWO_PI = Math.PI * 2D;
 	private static final double DEG_PER_RADIAN = 180/Math.PI;

	// Singleton instance.
	private static MapDataUtil instance;
	
	private static MapDataFactory mapDataFactory;
	private static MEGDRMapReader reader;
	
 	private static short height;
 	private static short width;
 	
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
         reader = new MEGDRMapReader(MEGDRMapReader.LEVEL);
         
         height = reader.getHeight();
         width = reader.getWidth();
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
		reader = null;
	}
 	
 }
