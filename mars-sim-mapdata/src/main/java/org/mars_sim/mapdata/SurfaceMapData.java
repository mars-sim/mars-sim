/**
 * Mars Simulation Project
 * SurfaceMapData.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */

package org.mars_sim.mapdata;

/**
 * Surface map data.
 */
public class SurfaceMapData extends IntegerMapData {

	// Static members.
	private static final String MAP_FILE = "/maps/surface2880x1440.jpg";

	/**
	 * Constructor
	 */
	public SurfaceMapData() {
		super(MAP_FILE);
	}
}
