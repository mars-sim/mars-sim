/*
 * Mars Simulation Project
 * MapUtils.java
 * @date 2022-08-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import org.mars_sim.mapdata.MapDataUtil;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;

/**
 * Static class for map utilities.
 */
public class MapUtils {

	/**
	 * Private constructor for utility class.
	 */
	private MapUtils() {
		// nothing
	}

	/**
	 * Gets a coordinate x, y position on the map image.
	 * 
	 * @param coords  location of unit
	 * @param mapType the type of map.
	 * @return display point on map
	 */
	public static IntPoint getRectPosition(Coordinates coords, Coordinates mapCenter, String mapType) {
		int mapHeight = MapDataUtil.MAP_HEIGHT;
		int halfMap = mapHeight / 2;
		int lowEdge = halfMap - MapDataUtil.GLOBE_BOX_WIDTH / 2; 
		double rho = mapHeight / Math.PI;
		return Coordinates.findRectPosition(coords, mapCenter, rho, halfMap, lowEdge);
	}

	/**
	 * Gets the distance in terms of the number of pixels.
	 * 
	 * @param distance
	 * @param mapType
	 * @return
	 */
	public static int getPixelDistance(double distance, String mapType) {
		int mapWidth = MapDataUtil.MAP_WIDTH;
		double distancePerPixel = Coordinates.MARS_CIRCUMFERENCE / mapWidth;
		return (int) Math.round(distance / distancePerPixel);
	}
}
