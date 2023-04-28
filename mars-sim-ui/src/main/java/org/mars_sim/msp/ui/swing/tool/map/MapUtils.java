/*
 * Mars Simulation Project
 * MapUtils.java
 * @date 2023-04-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import org.mars_sim.mapdata.IntegerMapData;
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
		return Coordinates.findRectPosition(coords, mapCenter, IntegerMapData.RHO, IntegerMapData.HALF_MAP, IntegerMapData.LOW_EDGE);
	}

	/**
	 * Gets the distance in terms of the number of pixels.
	 * 
	 * @param distance
	 * @param mapType
	 * @return
	 */
	public static int getPixelDistance(double distance, String mapType) {
		return (int) Math.round(distance / Coordinates.MARS_CIRCUMFERENCE * IntegerMapData.MAP_PIXEL_WIDTH);
	}
}
