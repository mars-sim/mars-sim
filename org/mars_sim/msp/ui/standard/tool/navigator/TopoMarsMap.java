/**
 * Mars Simulation Project
 * TopoMarsMap.java
 * @version 2.76 2004-06-02
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import org.mars_sim.msp.simulation.Simulation;
import javax.swing.JComponent;

/**
 *  The TopoMarsMap class is a map of the topography of Mars that can be generated
 *  for the MapDisplay.
 */
public class TopoMarsMap extends CannedMarsMap {

    /** 
     * Constructor
     * @param displayArea the display component
     */
    public TopoMarsMap(JComponent displayArea) {
    	super(displayArea, Simulation.instance().getMars().getSurfaceFeatures().getSurfaceTerrain().getTopoColors());
    }
}
