/*
 * Mars Simulation Project
 * MarsMapType.java
 * @date 2023-04-28
 * @author stpa
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

public enum MarsMapType {

	SURFACE_MID 	("map/surface600"),
	TOPO_MID 		("map/topo600"),
	GEO_MID 		("map/geo600"),
	REGION_MID		("map/region600"),
	VIKING_MID		("map/viking600")
	; 

	private String path;

	/** hidden constructor. */
	private MarsMapType(String path) {
		this.path = path;
	}

	/** gives back the path to the map image to be used. */
	public String getPath() {
		return this.path;
	}
}
