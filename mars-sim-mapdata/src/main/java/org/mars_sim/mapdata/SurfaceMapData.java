/*
 * Mars Simulation Project
 * SurfaceMapData.java
 * @date 2022-08-02
 * @author Scott Davis
 */

package org.mars_sim.mapdata;

/**
 * Surface map data.
 */
public class SurfaceMapData extends IntegerMapData {

	// Static members.
	private static final String MAP_FILE = "/maps/surface2880x1440.jpg"; // 5760x2880.jpg"; // 

	/**
	 * Constructor
	 */
	public SurfaceMapData() {
		super(MAP_FILE);
	}
}
