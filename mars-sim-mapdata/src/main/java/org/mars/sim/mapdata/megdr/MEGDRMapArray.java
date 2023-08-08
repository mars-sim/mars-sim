/*
 * Mars Simulation Project
 * MEGDRMapArray.java
 * @date 2023-08-06
 * @author Barry Evans
 */

package org.mars.sim.mapdata.megdr;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import org.mars.sim.mapdata.common.FileLocator;

import com.google.common.io.ByteStreams;

public class MEGDRMapArray extends MEGDRMapReader {

    private static final Logger logger = Logger.getLogger(MEGDRMapArray.class.getName());
	private static final double DIAMETER = 2 * Math.PI * 3376.2;
    	
	// Future: switch to using JavaFastPFOR to save memory.
	private short[] elevation;

    public MEGDRMapArray(String filename) {
		loadElevation(FileLocator.locateFile(filename));
	}

	/**
	 * Gets the elevation from the array.
	 */
    protected short getElevation(int index) {
		return elevation[index];
	}
		
	/**
	 * Loads the elevation data into short array.
	 * 
	 * @return
	 */
	private void loadElevation(File file) {
		
	    try (InputStream inputStream = new FileInputStream(file)) {

			// Use ByteStreams to convert to byte array
			byte[] bytes = ByteStreams.toByteArray(inputStream);
			
			elevation = convertByteArrayToShortIntArray(bytes);
			
			short mapHeight = (short) Math.sqrt(elevation.length / 2);
			short mapWidth = (short) (mapHeight * 2);
			
			logger.info("Reading elevation dataset from '" + file + "' (" + mapWidth + " by " + mapHeight + ").");
	        
			double resolution = Math.round(DIAMETER / mapWidth * 100.0)/100.0; 
			
			logger.info("Horizontal resolution is " + resolution + " km between two pixels at the equator.");
			
            setSize(mapWidth, mapHeight);
			
		} catch (Exception e) {
			 logger.severe("Problems in inputStream: " + e.getMessage());
		}
	}
	
    /**
	 * Converts two bytes to a short int.
	 * 
	 * @param data
	 * @return
	 */
	private short convert2ByteToShortInt(byte[] data) {
	    if (data == null || data.length != 2) return 0x0;
	    // ----------
	    return (short)( // NOTE: type cast not necessary for int
	            (0xff & data[0]) << 8  |
	            (data[0]) << 8  |
	            (0xff & data[1])
	            );
	}
	
	/**
	 * Converts byte array to short array.
	 * 
	 * @param data
	 * @return
	 */
	private short[] convertByteArrayToShortIntArray(byte[] data) {
        if (data == null || data.length % 2 != 0) return null;
        int size = data.length / 2;
        short[] shorts = new short[size];
        
        for (int i = 0; i < shorts.length; i++) {
        	
        	shorts[i]  = convert2ByteToShortInt(new byte[] {
                    data[(i*2)],
                    data[(i*2)+1]
            	});
        }
        return shorts;
    }
}
