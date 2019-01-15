/**
 * Mars Simulation Project
 * MapUtils.java
  * @version 3.1.0 2017-10-05
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
//	private static final int MAP_OFFSET_X = 300;
//	private static final int MAP_OFFSET_Y = 300; //map's actual size is 900x900, but 300x300 is shown on the screen, 
	// upper left corner of view window starts at 301,301
	// see CannedMarsMap.createMapImageLarge()

	/**
	 * Private constructor for utility class.
	 */
	private MapUtils() {
	}

	/**
	 * Gets a coordinate x, y position on the map image.
	 * 
	 * @param coords  location of unit
	 * @param mapType the type of map.
	 * @return display point on map
	 */
	public static IntPoint getRectPosition(Coordinates coords, Coordinates mapCenter, String mapType) {

		int mapHeight = CannedMarsMap.MAP_HEIGHT;

		double rho = mapHeight / Math.PI;
		int halfMap = mapHeight / 2;
		int low_edge = halfMap - 150;
		// IntPoint p = Coordinates.findRectPosition(coords, mapCenter, rho, halfMap,
		// low_edge);
		// p.setLocation(p.getiX()+MAP_OFFSET_X, p.getiY()+MAP_OFFSET_Y);
		return Coordinates.findRectPosition(coords, mapCenter, rho, halfMap, low_edge);
	}

	public static int getPixelDistance(double distance, String mapType) {
		int mapWidth = CannedMarsMap.MAP_WIDTH;
		double distancePerPixel = Mars.MARS_CIRCUMFERENCE / mapWidth;
		return (int) Math.round(distance / distancePerPixel);
	}
}