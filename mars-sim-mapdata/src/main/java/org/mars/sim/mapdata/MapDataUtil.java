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
 		short row = (short) Math.round(phi * height / PI);
 		
 		if (row == height) 
 			row--;
 		
 		short column = (short) (width / 2 + Math.round(theta * width / TWO_PI));

 		if (column == width)
 			column--;

 		int index = row * width + column;
 		
 		if (index > height * width)
 			index = height * width - 1;

 		return elevationArray()[index];
 	}
     
     /**
      * Gets the surface map data.
      * 
      * @return surface map data.
      */
     public MapData getMapData(String mapType) {
         return mapDataFactory.getMapData(mapType);
     }

     /**
      * Gets the map types available.
      * 
      * @return
      */
    public Collection<MapMetaData> getMapTypes() {
        return mapDataFactory.getLoadedTypes();
    }
 }
