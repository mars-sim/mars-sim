/**
 * Mars Simulation Project
 * TopoMarsMap.java
 * @version 3.1.0 2017-10-05
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
public class TopoMarsMap extends CannedMarsMap {

	// The map type.
	public static final String TYPE = "topographical map";

	private static MapDataUtil mapDataUtil = MapDataUtil.instance();
	
	/**
	 * Constructor
	 * 
	 * @param displayArea the display component
	 */
	public TopoMarsMap(JComponent displayArea) {
		super(displayArea, mapDataUtil.getTopoMapData());
	}
}
