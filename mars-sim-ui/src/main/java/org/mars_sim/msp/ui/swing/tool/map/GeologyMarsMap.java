/*
 * Mars Simulation Project
 * GeologyMarsMap.java
 * @date 2022-08-02
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.map;

import javax.swing.JComponent;

import org.mars_sim.mapdata.MapDataUtil;

/**
 * The GeologyMarsMap class is a map showcasing geological characteristics 
 * on the surface of Mars that can be generated
 * for the map display. Map data is retrieved from a data file and stored in
 * memory.
 */
@SuppressWarnings("serial")
public class GeologyMarsMap extends CannedMarsMap {

	// The map type.
	public static final String TYPE = "geo";

	private static MapDataUtil mapDataUtil = MapDataUtil.instance();
	
	/**
	 * Constructor
	 *
	 * @param displayArea the component display area.
	 */
	public GeologyMarsMap(JComponent displayArea) {

		// Parent constructor
		super(displayArea, TYPE, mapDataUtil.getGeologyMapData());
	}
}
