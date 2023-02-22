/*
 * Mars Simulation Project
 * MarsMapType.java
 * @date 2022-08-02
 * @author stpa
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

public enum MarsMapType {

	SURFACE_MID 	("map/mars_surfaceMid"),
	TOPO_MID 		("map/mars_topoMid"),
	GEO_MID 		("map/mars_geoMid"); 

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
