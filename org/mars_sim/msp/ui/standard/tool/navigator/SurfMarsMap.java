/**
 * Mars Simulation Project
 * SurfMarsMap.java
 * @version 2.75 2003-12-20
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import javax.swing.JComponent;

/**
 *  The SurfMarsMap class is a map of the surface of Mars that can be generated
 *  for the map display.  Map data is retrieved from a data file and stored in memory.
 */
public class SurfMarsMap extends CannedMarsMap {

    private static final String INDEX_FILE = "SurfaceMarsMap.index";
    private static final String MAP_FILE = "SurfaceMarsMap.dat";

    /** 
     * Constructor
     *
     * @param displayArea the component display area.
     */
    public SurfMarsMap(JComponent displayArea) {

		// Parent constructor
		super(displayArea, MAP_FILE, INDEX_FILE);
    }
}