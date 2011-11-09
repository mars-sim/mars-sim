/**
 * Mars Simulation Project
 * TopoMarsMap.java
 * @version 3.02 2011-11-08
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.swing.tool.map;

import org.mars_sim.msp.mapdata.MapDataUtil;

import javax.swing.*;

/**
 *  The TopoMarsMap class is a map of the topography of Mars that can be generated
 *  for the MapDisplay.
 */
public class TopoMarsMap extends CannedMarsMap {

	// The map type.
	public static final String TYPE = "topographical map";
    
    /** 
     * Constructor
     * @param displayArea the display component
     */
    public TopoMarsMap(JComponent displayArea) {
    	super(displayArea, MapDataUtil.instance().getTopoMapData());
    }
}
