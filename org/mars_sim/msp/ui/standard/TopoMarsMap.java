/**
 * Mars Simulation Project
 * TopoMarsMap.java
 * @version 2.71 2000-10-23
 * @author Scott Davis
 * @author Greg Whelan
 */

package org.mars_sim.msp.ui.standard;

import java.io.*;
import javax.swing.JComponent;

/**
 *  The TopoMarsMap class is a map of the topography of Mars that can be generated
 *  for the MapDisplay.
 */
public class TopoMarsMap extends CannedMarsMap {

    // Data members
    private RandomAccessFile map;

    /** Constructs a TopoMarsMap object 
     *  @param displayArea the display component
     */
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
    
    /** Gets the topographical map 
     *  @return the topographical map file
     */
    public RandomAccessFile getMapFile() {
        return map;
    }

    /** Loads the index and sum arrays 
     *  @param indexFile the name path of the index file
     *  @param sumFile the name path of the sum file
     */
    private void loadArrays(String indexFile, String sumFile) {
        try {
            // Load index array
            BufferedInputStream indexBuff =
                    new BufferedInputStream(new FileInputStream(indexFile));
            DataInputStream indexReader = new DataInputStream(indexBuff);
            index = new int[mapHeight];
            for (int x = 0; x < mapHeight; x++)
                index[x] = indexReader.readInt();
            indexReader.close();
            indexBuff.close();

            // Load sum array
            BufferedInputStream sumBuff =
                    new BufferedInputStream(new FileInputStream(sumFile));
            DataInputStream sumReader = new DataInputStream(sumBuff);
            sum = new long[mapHeight];
            for (int x = 0; x < mapHeight; x++)
                sum[x] = sumReader.readLong();
            sumReader.close();
            sumBuff.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
