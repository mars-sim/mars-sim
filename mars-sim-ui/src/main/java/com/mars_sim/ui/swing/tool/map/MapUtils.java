/*
 * Mars Simulation Project
 * MapUtils.java
 * @date 2023-04-28
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.map;

import java.awt.Dimension;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;

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
	 * Gets a coordinate x, y position on the ma(p image.
	 * 
	 * @param coords location of position to convert
	 * @param mapCenter Center of the map in the displau
	 * @param baseMap the type of map.
	 * @param displaySize Size of the target display
	 * @return display point on map
	 */
	public static IntPoint getRectPosition(Coordinates coords, Coordinates mapCenter, MapDisplay baseMap,
					Dimension displaySize) {
		var xHalf = baseMap.getPixelWidth()/2;
		var yHalf = baseMap.getPixelHeight()/2;
		return mapCenter.findRectPosition(coords, baseMap.getRho(),
						xHalf, (int)(xHalf - (displaySize.getWidth()/2)),
						yHalf, (int)(yHalf - (displaySize.getHeight()/2)));
	}

	/**
	 * Gets the distance in terms of the number of pixels.
	 * 
	 * @param distance
	 * @param mapType
	 * @return
	 */
	public static int getPixelDistance(double distance, MapDisplay baseMap) {
		return (int) Math.round(distance / Coordinates.MARS_CIRCUMFERENCE * baseMap.getPixelWidth());
	}
}
