/*
 * Mars Simulation Project
 * MapDataUtil.java
 * @date 2023-04-28
 * @author Scott Davis
 */

 package org.mars_sim.mapdata;

/**
  * Static utility class for accessing Mars map data.
  */
 public final class MapDataUtil {
 	
	private static final double PI = Math.PI;
 	private static final double TWO_PI = Math.PI * 2D;

	// Singleton instance.
	private static MapDataUtil instance;
	private static MapDataFactory mapDataFactory;
	private static MEGDRMapReader reader;

 	private static int[] elevationArray;
 	
 	private static int height;
 	private static int width;
 	
     /**
      * Private constructor for static utility class.
      */
     private MapDataUtil() {
         mapDataFactory = new MapDataFactory();
         reader = new MEGDRMapReader();
         
         height = reader.getHeight();
         width = reader.getWidth();
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
	
//		System.out.println("0. height: " + height
//				+ "  0. width: " + width);
		
// 		// Make sure phi is between 0 and PI.
// 		while (phi > PI)
// 			phi -= PI;
// 		while (phi < 0)
// 			phi += PI;
//
// 		// Adjust theta with PI for the map offset.
// 		// Note: the center of the map is when theta = 0
// 		if (theta > PI)
// 			theta -= PI;
// 		else
// 			theta += PI;
// 		
// 		// Make sure theta is between 0 and 2 PI.
// 		while (theta > TWO_PI)
// 			theta -= TWO_PI;
// 		while (theta < 0)
// 			theta += TWO_PI;

 		int row = (int) Math.round(phi * height / PI);
 		
 		if (row == height) 
 			row--;
 		
 		int column = width / 2 + (int) Math.round(theta * width / TWO_PI);

 		if (column == width)
 			column--;

 		int index = row * width + column;
 		
 		if (index > height * width)
 			index = height * width - 1;
 		
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
       
     /**
      * Gets the region map data.
      * 
      * @return region map data.
      */
     public MapData getRegionMapData() {
         return mapDataFactory.getMapData(MapDataFactory.REGION_MAP_DATA);
     }
     
     /**
      * Gets the viking map data.
      * 
      * @return viking map data.
      */
     public MapData getVikingMapData() {
         return mapDataFactory.getMapData(MapDataFactory.VIKING_MAP_DATA);
     }
 }
