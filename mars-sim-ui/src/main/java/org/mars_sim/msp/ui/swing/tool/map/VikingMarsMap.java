/*
 * Mars Simulation Project
 * VikingMarsMap.java
 * @date 2023-04-28
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.map;

import javax.swing.JComponent;

import org.mars_sim.mapdata.MapDataUtil;

/**
 * The VikingMarsMap class is a map of the viking surface of Mars that can be generated
 * for the map display. Map data is retrieved from a data file and stored in
 * memory.
 */
@SuppressWarnings("serial")
public class VikingMarsMap extends CannedMarsMap {

	// The map type.
	public static final String TYPE = "viking";

	private static MapDataUtil mapDataUtil = MapDataUtil.instance();
	
	/**
	 * Constructor
	 *
	 * @param displayArea the component display area.
	 */
	public VikingMarsMap(JComponent displayArea) {

		// Parent constructor
		super(displayArea, mapDataUtil.getVikingMapData());
	}
}
