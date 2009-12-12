/**
 * Mars Simulation Project
 * TopoMarsMap.java
 * @version 2.80 2006-11-21
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.swing.tool.map;

import org.mars_sim.msp.core.Simulation;

import javax.swing.JComponent;

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
    	super(displayArea, Simulation.instance().getMars().getSurfaceFeatures().getSurfaceTerrain().getTopoColors());
    }
}
