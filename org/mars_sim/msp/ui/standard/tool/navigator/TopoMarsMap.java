/**
 * Mars Simulation Project
 * TopoMarsMap.java
 * @version 2.75 2003-12-20
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import javax.swing.JComponent;

/**
 *  The TopoMarsMap class is a map of the topography of Mars that can be generated
 *  for the MapDisplay.
 */
public class TopoMarsMap extends CannedMarsMap {

	private static final String INDEX_FILE = "TopoMarsMap.index";
	private static final String MAP_FILE = "TopoMarsMap.dat";

    /** Constructs a TopoMarsMap object
     *  @param displayArea the display component
     */
    public TopoMarsMap(JComponent displayArea) {
        super(displayArea, MAP_FILE, INDEX_FILE);
    }
}
