/**
 * Mars Simulation Project
 * SurfMarsMap.java
 * @version 2.70 2000-09-04
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard;  
 
import java.io.*;
import javax.swing.JComponent;

public class SurfMarsMap extends CannedMarsMap {

    private RandomAccessFile map;

    public SurfMarsMap(JComponent displayArea) {
	super(displayArea);

	try {
	    map = new RandomAccessFile("map_data/SurfaceMarsMap.dat", "r");
	} catch (FileNotFoundException ex) {
	    System.out.println("Could not find SurfaceMarsMap.dat");
	    System.out.println("  You can find it at: http://mars-sim.sourceforge.net/SurfaceDat.zip");
	    System.out.println("  Download and then unzip in the directory mars-sim/map_data");
	    System.exit(0);
	}
    }

    public RandomAccessFile getMapFile() {
	return map;
    }
}
