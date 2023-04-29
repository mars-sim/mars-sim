/*
 * Mars Simulation Project
 * TopoMarsMap.java
 * @date 2022-08-02
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.swing.tool.map;

import javax.swing.JComponent;

import org.mars_sim.mapdata.MapDataUtil;

/**
 * The TopoMarsMap class is a map of the topography of Mars that can be
 * generated for the MapDisplay.
 */
@SuppressWarnings("serial")
public class TopoMarsMap extends CannedMarsMap {

	// The map type.
	public static final String TYPE = "topo";

	private static MapDataUtil mapDataUtil = MapDataUtil.instance();
	
	/**
	 * Constructor
	 * 
	 * @param displayArea the display component
	 */
	public TopoMarsMap(JComponent displayArea) {
		super(displayArea, TYPE, mapDataUtil.getTopoMapData());
	}
}
