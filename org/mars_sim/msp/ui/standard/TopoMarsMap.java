/**
 * Mars Simulation Project
 * TopoMarsMap.java
 * @version 2.70 2000-09-04
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard; 
 
import java.io.*;
import javax.swing.JComponent;

public class TopoMarsMap extends CannedMarsMap {

    private RandomAccessFile map;

    public TopoMarsMap(JComponent displayArea) {
	super(displayArea);

	loadArrays("map_data/TopoMarsMap.index", "map_data/TopoMarsMap.sum");

	try {
	    map = new RandomAccessFile("map_data/TopoMarsMap.dat", "r");
	} catch (FileNotFoundException ex) {
	    System.out.println("Could not find TopoMarsMap.dat");
	    System.exit(0);
	}
    }

    public RandomAccessFile getMapFile() {
	return map;
    }

    private void loadArrays(String indexFile, String sumFile) {
	try {
	    // Load index array
	    BufferedInputStream indexBuff =
		new BufferedInputStream(new FileInputStream(indexFile));
	    DataInputStream indexReader = new DataInputStream(indexBuff);
	    index = new int[mapHeight];
	    for (int x=0; x < mapHeight; x++) index[x] = indexReader.readInt();
	    indexReader.close();
	    indexBuff.close();
			
	    // Load sum array
	    BufferedInputStream sumBuff =
		new BufferedInputStream(new FileInputStream(sumFile));
	    DataInputStream sumReader = new DataInputStream(sumBuff);
	    sum = new long[mapHeight];
	    for (int x=0; x < mapHeight; x++) sum[x] = sumReader.readLong();
	    sumReader.close();
	    sumBuff.close();
	}
	catch(IOException e) {
	    System.out.println(e);
	}
    }
}
