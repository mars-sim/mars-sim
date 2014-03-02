package org.mars_sim.msp.ui.swing.tool.navigator;

import org.mars_sim.msp.core.Msg;

/**
 * @author stpa
 * 2014-03-02
 */
public enum MarsGlobeType {

	SURFACE (Msg.getString("img.mars.surface")), //$NON-NLS-1$
	TOPO (Msg.getString("img.mars.topo")); //$NON-NLS-1$

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
