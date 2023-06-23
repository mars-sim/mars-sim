/*
 * Mars Simulation Project
 * MEGDRMapReader.java
 * @date 2023-06-17
 * @author Manny Kung
 */

package org.mars_sim.mapdata;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Logger;

import org.mars_sim.msp.common.FileLocator;

import com.google.common.io.ByteStreams;

/**
 * This class reads the topographical or elevation data of MOLA Mission Experiment 
 * Gridded Data Records (MEGDRs) acquired by MGS mission. 
 * @See https://pds-geosciences.wustl.edu/missions/mgs/megdr.html.
 */
public class MEGDRMapReader {
	
	private static final Logger logger = Logger.getLogger(MEGDRMapReader.class.getName());
	
	static final int LEVEL = 1;
	
//	NOTE: (Do not delete)
//	
//	LEVEL 0 :
// `megt90n000cb.img` provides 
//	map resolution of 1440x720
//	4 pixels per degree (or 0.25 by 0.25 degrees)
//	map scale of 14.818 km per pixel
//	
//	LEVEL 1 :
// `megt90n000eb.img` provides
//	map resolution of 5760x2880
//	16 pixels per degree (or 0.0625by 0.0625 degrees)
//	map scale of 3.705 km per pixel	
//	
//	LEVEL 2 :
// `megt90n000fb.img` provides
//	map resolution of 11520x5760
//	32 pixels per degree (or 0.03125 by 0.03125 degrees)
//	map scale of 1.853 km per pixel		
	 
	/** meg004 is Low resolutions. */
	private static final String meg004 = "megt90n000cb.img";

	/** meg004 is mid resolutions. */
	private static final String meg016 = "megt90n000eb.img";

	/** meg032 is high resolutions. */
	private static final String meg032 = "megt90n000fb.img";
	
	private static final String PATH = "/maps/";
	
	static final String FILE = PATH + meg004;
	
	private String[] maps = {PATH + meg004, PATH + meg016, PATH + meg032};
	
	// Future: switch to using JavaFastPFOR to save memory.
	private short[] elevation;
	
	private static short mapHeight;
	private static short mapWidth;
	
	public static void main(String[] args) throws IOException {
		new MEGDRMapReader(LEVEL);
	}
	
	/**
	 * This class loads NASA's MEGDR elevation data set. 
	 * 
	 * @see <a href="https://github.com/mars-sim/mars-sim/issues/225">mars-sim Issue #225</a>
	 */
	public MEGDRMapReader(int level) {
		loadElevation(level);
	}
	
	/**
	 * Loads the elevation data into short array.
	 * 
	 * @return
	 */
	public short[] loadElevation(int level) {
		// Select the map resolution
		String file = maps[level];
		
	    try (InputStream inputStream = new FileInputStream(FileLocator.locateFile(file))) {

			// Use ByteStreams to convert to byte array
			byte[] bytes = ByteStreams.toByteArray(inputStream);
			
			elevation = convertByteArrayToShortIntArray(bytes);
			
			mapHeight = (short) Math.sqrt(elevation.length / 2);
			mapWidth = (short) (mapHeight * 2);
			
			logger.info("Reading elevation dataset from '" + file + "' (" + mapWidth + " by " + mapHeight + ").");
	            
		} catch (Exception e) {
			 System.out.println("Problems in inputStream: " + e.getMessage());
		}
	    
        return elevation;
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
	public short[] convertByteArrayToShortIntArray(byte[] data) {
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
	
	public int[] getIndex() {
		int min = 0;
		int max = 0; 

		int minIndex = 0;
		int maxIndex = 0;

		for (int i = 0; i < elevation.length; i++)  {
			if (max < elevation[i]) {
				max = elevation[i];
				maxIndex = i;
			}
			
			if (min > elevation[i]) {
				min = elevation[i];
				minIndex = i;
			}
		}

		return new int[] {maxIndex, max, minIndex, min};
	}
	
	public short[] getElevationArray() {
		return elevation;
	}
	
	/**
     * This method returns the byte array that represent the contents of 
     * {@code file}.
     * 
     * @param  file the file to read.
     * @return the array of bytes representing the contents of the input file.
     */
    public static byte[] readFile(File file) 
    throws IOException, FileNotFoundException {
        Objects.requireNonNull(file, "The input file is null.");
        long size = file.length();
        checkSize(size);

        byte[] data;
        int bytesRead;

        try (FileInputStream stream = new FileInputStream(file)) {
            data = new byte[(int) size];
            bytesRead = stream.read(data);
        }

        if (bytesRead != size) {
            throw new IllegalStateException(
                    "File size and read count mismatch. File size: " +
                    size + ", bytes read: " + bytesRead);
        }

        return data;
    }

    /**
     * Writes the byte array {@code data} to the file {@code file}. After 
     * successful operation of this method, the input file will contain exactly
     * the contents of the input data.
     * 
     * @param file the file to write to.
     * @param data the data array to write.
     * @throws java.io.IOException           if file IO fails.
     * @throws java.io.FileNotFoundException if file does not exist.
     */
    public void writeFile(File file, byte[] data)
    throws IOException, FileNotFoundException {
        Objects.requireNonNull(file, "The input file is null.");
        Objects.requireNonNull(data, "The input data to write is null.");

        try (BufferedOutputStream stream = new BufferedOutputStream(
                                           new FileOutputStream(file))) {
            stream.write(data);
        }
    }

    // This method ensures that file size is small enough to be represented 
    // using a variable of type 'int'.
    private static final void checkSize(long size) {
        if (size > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                    "The target file is too large: " + size + " bytes. " +
                    "Maximum allowed size is " + Integer.MAX_VALUE + 
                    "bytes.");
        }
    }
    
    public void writeIntArray(String filename, int[]x) throws IOException{
    	try (FileWriter fr = new FileWriter(filename)) {
    	  BufferedWriter outputWriter = new BufferedWriter(fr);
    	      	  
    	  for (int i = 0; i < x.length; i++) {
    	    outputWriter.write(Integer.toString(x[i]));
    	    outputWriter.newLine();
    	  }
    	  
    	  outputWriter.flush();  
    	  outputWriter.close();  
    	}
    }

	public short getHeight() {
		return mapHeight;
	}

	public short getWidth() {
		return mapWidth;
	}
}
