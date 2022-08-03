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

	private static final int MAP_HEIGHT = MapDataUtil.MAP_HEIGHT;
	private static final int HALF_MAP = MAP_HEIGHT / 2;
	private static final int LOW_EDGE = HALF_MAP - MapDataUtil.GLOBE_BOX_WIDTH / 2; 
	private static final double RHO = MAP_HEIGHT / Math.PI;
	
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
		return Coordinates.findRectPosition(coords, mapCenter, RHO, HALF_MAP, LOW_EDGE);
	}

	/**
	 * Gets the distance in terms of the number of pixels.
	 * 
	 * @param distance
	 * @param mapType
	 * @return
	 */
	public static int getPixelDistance(double distance, String mapType) {
		return (int) Math.round(distance / Coordinates.MARS_CIRCUMFERENCE * MapDataUtil.MAP_WIDTH);
	}
}
