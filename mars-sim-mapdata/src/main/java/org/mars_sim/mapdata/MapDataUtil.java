/**
 * Mars Simulation Project
 * MapDataUtility.java
 * @version 3.1.0 2018-10-04
 * @author Scott Davis
 */

package org.mars_sim.mapdata;

/**
 * Static utility class for accessing Mars map data.
 */
public final class MapDataUtil {
	
	public static final int IMAGE_WIDTH = 300;
	public static final int IMAGE_HEIGHT = IMAGE_WIDTH;
	
	private static final double TWO_PI = Math.PI * 2D;

    // Singleton instance.
    private static MapDataUtil instance;
    private static MapDataFactory mapDataFactory;
    
    private static MEGDRMapReader reader;

	private static int[] elevationArray;
	
//	static {
//		reader = new MEGDRMapReader();
//		elevationArray = reader.getElevationArray();
//	}
	
    /**
     * Private constructor for static utility class.
     */
    private MapDataUtil() {
        mapDataFactory = new MapDataFactory();
		reader = new MEGDRMapReader();
		elevationArray = reader.getElevationArray();
    }
    
    public static int[] getElevationArray() {
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
		while (phi > Math.PI)
			phi -= Math.PI;
		while (phi < 0)
			phi += Math.PI;

		// Adjust theta with PI for the map offset.
		// Note: the center of the map is when theta = 0
		if (theta > Math.PI)
			theta -= Math.PI;
		else
			theta += Math.PI;
		
		// Make sure theta is between 0 and 2 PI.
		while (theta > TWO_PI)
			theta -= TWO_PI;
		while (theta < 0)
			theta += TWO_PI;

		int row = (int) Math.round(phi * MEGDRMapReader.HEIGHT / Math.PI);
		if (row == MEGDRMapReader.HEIGHT) 
			row--;
		
		int column = MEGDRMapReader.WIDTH /2 + (int) Math.round(theta * MEGDRMapReader.WIDTH / TWO_PI);
//		if (column < 0)
//			column = 0;		
		if (column == MEGDRMapReader.WIDTH)
			column--;

		return MapDataUtil.getElevationArray()[row * MEGDRMapReader.WIDTH + column];
	}
	
    /**
     * Get the singleton instance of MapData.
     * @return instance.
     */
    public static MapDataUtil instance() {
        if (instance == null) {
            instance = new MapDataUtil();
        }
        return instance;
    }
    
    /**
     * Get the surface map data.
     * @return surface map data.
     */
    public MapData getSurfaceMapData() {
        return mapDataFactory.getMapData(MapDataFactory.SURFACE_MAP_DATA);
    }
    
    /**
     * Get the topographical map data.
     * @return topographical map data.
     */
    public MapData getTopoMapData() {
        return mapDataFactory.getMapData(MapDataFactory.TOPO_MAP_DATA);
    }
    
    /**
     * Get the geology map data.
     * @return geology map data.
     */
    public MapData getGeologyMapData() {
        return mapDataFactory.getMapData(MapDataFactory.GEOLOGY_MAP_DATA);
    }
    
}