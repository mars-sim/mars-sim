/**
 * Mars Simulation Project
 * SurfMarsMap.java
 * @version 2.75 2003-08-03
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard.tool.navigator;

import java.io.*;
import java.net.URL;
import javax.swing.JComponent;

/**
 *  The SurfMarsMap class is a map of the surface of Mars that can be generated
 *  for the MapDisplay.
 */
public class SurfMarsMap extends CannedMarsMap {

    // Data members
    private RandomAccessFile map;

    /** Constructs a SurfMarsMap object
     *  @param displayArea the component display area
     */
    public SurfMarsMap(JComponent displayArea) {

        // User CannedMarsMap constructor
        super(displayArea);

        try {
            URL found = getClass().getClassLoader().getResource("map_data/SurfaceMarsMap.dat");
            map = new RandomAccessFile(found.getFile(), "r");
        }
        catch (Exception e) {
            System.out.println("Could not find SurfaceMarsMap.dat");
            System.out.println("  You can find it at: http://mars-sim.sourceforge.net/SurfaceDat.zip");
            System.out.println("  Download and then unzip in the directory mars-sim/map_data");
            System.exit(0);
        }
    }

    /** Gets the surface map
     *  @return the map file
     */
    public RandomAccessFile getMapFile() {
        return map;
    }
}

