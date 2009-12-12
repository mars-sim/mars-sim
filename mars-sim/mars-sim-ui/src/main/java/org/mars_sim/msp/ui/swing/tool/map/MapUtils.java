/**
 * Mars Simulation Project
 * MapUtils.java
 * @version 2.80 2006-10-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.mars.Mars;

/**
 * Static class for map utilities. 
 */
public class MapUtils {

	/**
	 * Private constructor for utility class.
	 */
	private MapUtils() {
	}
	
    /** 
     * Gets a coordinate x, y position on the map image.
     * @param coords location of unit
     * @param mapType the type of map.
     * @return display point on map
     */
    public static IntPoint getRectPosition(Coordinates coords, Coordinates mapCenter, String mapType) {

        int mapHeight = 0;        
        if (USGSMarsMap.TYPE.equals(mapType)) mapHeight = USGSMarsMap.MAP_HEIGHT;
        else mapHeight = CannedMarsMap.MAP_HEIGHT;
        
        double rho = mapHeight / Math.PI;
        int halfMap = mapHeight / 2;
        int low_edge = halfMap - 150;

        return Coordinates.findRectPosition(coords, mapCenter, rho, halfMap, low_edge);
    }
    
    public static int getPixelDistance(double distance, String mapType) {
        int mapWidth = 0;        
        if (USGSMarsMap.TYPE.equals(mapType)) mapWidth = USGSMarsMap.MAP_WIDTH;
        else mapWidth = CannedMarsMap.MAP_WIDTH;
        double distancePerPixel = Mars.MARS_CIRCUMFERENCE / mapWidth;
        return (int) Math.round(distance / distancePerPixel);
    }
}