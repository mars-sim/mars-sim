/*
 * Mars Simulation Project
 * MapUtils.java
 * @date 2022-08-02
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
	 * @param baseMap the type of map.
	 * @return display point on map
	 */
	public static IntPoint getRectPosition(Coordinates coords, Coordinates mapCenter, Map baseMap) {
		return Coordinates.findRectPosition(coords, mapCenter, baseMap.getScale(), IntegerMapData.HALF_MAP, IntegerMapData.LOW_EDGE);
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
