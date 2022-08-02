/*
 * Mars Simulation Project
 * MapDataUtil.java
 * @date 2022-08-02
 * @author Scott Davis
 */

 package org.mars_sim.mapdata;

/**
  * Static utility class for accessing Mars map data.
  */
 public final class MapDataUtil {
 	
 	public static final int IMAGE_WIDTH = 300;
 	public static final int IMAGE_HEIGHT = IMAGE_WIDTH;
 	private static final int ELEVATION_MAP_HEIGHT = MEGDRMapReader.HEIGHT;
 	private static final int ELEVATION_MAP_WIDTH = MEGDRMapReader.WIDTH;
 	
 	private static final double PI = Math.PI;
 	private static final double TWO_PI = Math.PI * 2D;

     // Singleton instance.
     private static MapDataUtil instance;
     private static MapDataFactory mapDataFactory;
     private static MEGDRMapReader reader;

 	private static int[] elevationArray;
 	
     /**
      * Private constructor for static utility class.
      */
     private MapDataUtil() {
         mapDataFactory = new MapDataFactory();
         reader = new MEGDRMapReader();
     }
     
     public int[] getElevationArray() {
     	if (elevationArray == null)	
     		elevationArray = reader.loadElevation();
  
 		return elevationArray;
 	}
 	
     /**
 	 * Gets the elevation as an integer at a given location.
 	 * 
 	 * @param phi   the phi location.
 	 * @param theta the theta location.
 	 * @return the elevation as an integer.
 	 */
 	public int getElevationInt(double phi, double theta) {
 		// Make sure phi is between 0 and PI.
 		while (phi > PI)
 			phi -= PI;
 		while (phi < 0)
 			phi += PI;

 		// Adjust theta with PI for the map offset.
 		// Note: the center of the map is when theta = 0
 		if (theta > PI)
 			theta -= PI;
 		else
 			theta += PI;
 		
 		// Make sure theta is between 0 and 2 PI.
 		while (theta > TWO_PI)
 			theta -= TWO_PI;
 		while (theta < 0)
 			theta += TWO_PI;

 		int row = (int) Math.round(phi * ELEVATION_MAP_HEIGHT / PI);
 		
 		if (row == ELEVATION_MAP_HEIGHT) 
 			row--;
 		
 		int column = ELEVATION_MAP_WIDTH /2 + (int) Math.round(theta * ELEVATION_MAP_WIDTH / TWO_PI);

 		if (column == ELEVATION_MAP_WIDTH)
 			column--;

 		int index = row * ELEVATION_MAP_WIDTH + column;
 		
 		if (index > ELEVATION_MAP_HEIGHT * ELEVATION_MAP_WIDTH)
 			index = ELEVATION_MAP_HEIGHT * ELEVATION_MAP_WIDTH - 1;
 		
 		return getElevationArray()[index];
 	}
 	
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
      * Gets the surface map data.
      * 
      * @return surface map data.
      */
     public MapData getSurfaceMapData() {
         return mapDataFactory.getMapData(MapDataFactory.SURFACE_MAP_DATA);
     }
     
     /**
      * Gets the topographical map data.
      * 
      * @return topographical map data.
      */
     public MapData getTopoMapData() {
         return mapDataFactory.getMapData(MapDataFactory.TOPO_MAP_DATA);
     }
     
     /**
      * Gets the geology map data.
      * 
      * @return geology map data.
      */
     public MapData getGeologyMapData() {
         return mapDataFactory.getMapData(MapDataFactory.GEOLOGY_MAP_DATA);
     }
     
 }
