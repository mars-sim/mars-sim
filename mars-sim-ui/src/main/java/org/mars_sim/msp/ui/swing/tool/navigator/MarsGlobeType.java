/**
 * Mars Simulation Project
 * PopUpMenu.java
 * @version 3.1.0 2018-07-23
 * @author stpa
 */
package org.mars_sim.msp.ui.swing.tool.navigator;

import org.mars_sim.msp.core.Msg;

public enum MarsGlobeType {

	//SURFACE (Msg.getString("img.mars.surface")), //$NON-NLS-1$
	//TOPO (Msg.getString("img.mars.topo")), //$NON-NLS-1$
	//SURFACE_2k (Msg.getString("img.mars.surface2k")), //$NON-NLS-1$
	//TOPO_2k (Msg.getString("img.mars.topo2k")), //$NON-NLS-1$
	SURFACE_MID (Msg.getString("img.mars.surfaceMid")), //$NON-NLS-1$
	TOPO_MID (Msg.getString("img.mars.topoMid")); //$NON-NLS-1$


	private String path;

	/** hidden constructor. */
	private MarsGlobeType(String path) {
		this.path = path;
	}

	/** gives back the path to the map image to be used. */
	public String getPath() {
		return this.path;
	}
}
