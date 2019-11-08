/**
 * Mars Simulation Project
 * ReadTopo.java
 * @version 3.1.0 2019-11-07
 * @author Manny Kung
 */

package org.mars_sim.mapdata;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ReadTopo {

	private static final String FILE = "/maps/megt90n000cb.img";

	// Each number occupies 2 bytes
	private static final int BUFFER_SIZE = 2;

	private byte[] buffer = new byte[BUFFER_SIZE]; 
	
	public static final int HEIGHT = 720;
	public static final int WIDTH = 1440;
	
	private int[] elevation = new int[HEIGHT*WIDTH]; // has 1036800 values ; OR [720*2880] = 2073600 values
	
	public ReadTopo() {
		
		InputStream inputStream = ReadTopo.class.getResourceAsStream(FILE); //new BufferedInputStream(new FileInputStream(inputFile));
		
	    int i = 0;
	    
        try {
			while (inputStream.read(buffer) != -1) {
				// Combine the 2 bytes into a 16-bit number
				elevation[i] =  (buffer[0] << 8) | (buffer[1] & 0xff);
//				if (i % WIDTH == 0) System.out.println();
//				System.out.print(elevation[i] + " " + buffer[0] + " " + buffer[1] + " " + el2[i]);
				i++;
			}
			

//	        System.out.println(i);
//	        System.out.println(elevation.length);
			
	        inputStream.close();
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        
//        int[] nums = getIndex();
//        int maxIndex = nums[0];
//        int max = nums[1];
//        int minIndex = nums[2];
//        int min = nums[3];
//        
//        System.out.println(
//    		  "max : " + String.valueOf(max)
//      		+ "   maxIndex : " + String.valueOf(maxIndex)
//      		+ "   min : " + String.valueOf(min)
//      		+ "   minIndex : " + String.valueOf(minIndex)
//    		  ); 
//      
////      max : 21134   maxIndex : 418507   min : -8068   minIndex : 707288  
//        
//        int r = (int)(Math.round(1.0 * maxIndex / WIDTH)) - 1; // = 291 - 1
//      
//        int c = maxIndex - r * WIDTH ; // = 418507 - 290 * 1440 = 907
//        
//        double phi = Math.round(1.0 * r / HEIGHT * Math.PI * 100.0)/100.0;
//        
//        double theta = Math.round((1.0 * c / WIDTH * 2.0 * Math.PI -  Math.PI)* 100.0)/100.0;
//        
//        System.out.println(
//        		  "r : " + String.valueOf(r)
//          		+ "   c : " + String.valueOf(c)
//          		+ "  (" + String.valueOf(phi) 
//          		+ ", " + String.valueOf(theta)
//          		+ ")" //+ new Coordinates(phi, theta); 
//        		);
          
        // Note: new Coordinates(1.27, 0.82) --> 17.23° N 46.98° E
        
        
//        int[][] el = new int[HEIGHT][WIDTH];
//        
//        for (int k=0; k < HEIGHT * WIDTH; k++) {
//        	
//        	if (k % WIDTH == 0) {
//            	int [] row = new int[WIDTH];
//
//        		el = elevation[k]; 
//        		
//        	}
//        			
//        }
        	
//        List <Integer> list = new ArrayList <Integer> ();
//        for (int j : elevation)
//        	list.add(j);
        
//        Integer largest = Collections.max(list);
//        System.out.println("largest : " + String.valueOf(largest)); // largest : 21134
//        Integer least = Collections.min(list);
//        System.out.println("  least : " + String.valueOf(least)); // least : -8068
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
	
	public int[] getElevationArray() {
		return elevation;
	}
	
	public static void main(String[] args) throws IOException {
		new ReadTopo();
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
    public static void writeFile(File file, byte[] data)
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
}
