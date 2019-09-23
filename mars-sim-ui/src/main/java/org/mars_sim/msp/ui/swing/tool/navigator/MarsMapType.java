/**
 * Mars Simulation Project
 * MarsMapType.java
 * @version 3.1.0 2018-07-23
 * @author stpa
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import org.mars_sim.msp.core.Msg;

public enum MarsMapType {

	SURFACE_MID 	(Msg.getString("img.mars.surfaceMid")), //$NON-NLS-1$
	TOPO_MID 		(Msg.getString("img.mars.topoMid")), //$NON-NLS-1$
	GEO_MID 		(Msg.getString("img.mars.geoMid")); //$NON-NLS-1$

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
