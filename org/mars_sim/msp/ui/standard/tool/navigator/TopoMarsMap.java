/**
 * Mars Simulation Project
 * TopoMarsMap.java
 * @version 2.75 2003-12-20
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import org.mars_sim.msp.simulation.Mars;
import javax.swing.JComponent;

/**
 *  The TopoMarsMap class is a map of the topography of Mars that can be generated
 *  for the MapDisplay.
 */
public class TopoMarsMap extends CannedMarsMap {

    /** Constructs a TopoMarsMap object
     *  @param displayArea the display component
     *  @param mars the Mars instance.
     */
    public TopoMarsMap(JComponent displayArea, Mars mars) {
    	super(displayArea, mars.getSurfaceFeatures().getSurfaceTerrain().getTopoColors());
    }
}
