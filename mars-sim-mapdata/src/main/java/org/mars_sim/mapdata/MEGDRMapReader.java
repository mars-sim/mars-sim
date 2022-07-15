/*
 * Mars Simulation Project
 * MEGDRMapReader.java
 * @date 2021-08-28
 * @author Manny Kung
 */

package org.mars_sim.mapdata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

import com.google.common.io.ByteStreams;

import me.lemire.integercompression.Composition;
import me.lemire.integercompression.FastPFOR;
import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.IntegerCODEC;
import me.lemire.integercompression.VariableByte;

/**
 * This class reads the topographical or elevation data of MOLA Mission Experiment 
 * Gridded Data Records (MEGDRs) acquired by MGS mission. 
 * See https://pds-geosciences.wustl.edu/missions/mgs/megdr.html.
 */
public class MEGDRMapReader {

	public static final int HEIGHT = 720; //2880;
	public static final int WIDTH = 1440; //5760;

	// megt90n000cb.img has a resolution of 4 pixels per degree (or 0.25 by 0.25 degrees)
	// map scale : 14.818 km per pixel
	private static final String FILE = "/maps/megt90n000cb.img";
	private static final String COMPRESSED = "720x1440_JavaFastPFOR_compressed";
//	private static final String UNCOMPRESSED = "720x1440_uncompressed";

	// megt90n000eb.img has a resolution of 16 pixels per degree (or 0.0625 by 0.0625 degrees)
	// map scale : 3.705 km per pixel
//	private static final String FILE = "/maps/megt90n000eb.img";
//	private static final String COMPRESSED = "2880x5760_JavaFastPFOR_compressed";
//	private static final String UNCOMPRESSED = "2880x5760_uncompressed";
	
//	public static final int HEIGHT = 2880;
//	public static final int WIDTH = 5760;
	
	// Each number occupies 2 bytes
	private static final int BUFFER_SIZE = 2;
	private static final byte[] buffer = new byte[BUFFER_SIZE]; 
	
	// Each number occupies ? bytes
	private static final int COMPRESSED_BUFFER_SIZE = 4;
	private static final byte[] cBuffer = new byte[COMPRESSED_BUFFER_SIZE]; 
	
	private static int COMPRESSED_N;
	
	// Future: switch to using JavaFastPFOR to save memory.
	private int[] elevation = new int[HEIGHT*WIDTH]; // has 1036800 values ; OR [720*2880] = 2073600 values
	
	public static void main(String[] args) throws IOException {
		new MEGDRMapReader().loadElevation();
	}
	
	/**
	 * THis class loads NASA's MEGDR elevation dataset 
	 * 
	 * @see <a href="https://github.com/mars-sim/mars-sim/issues/225">GitHub Discussion #225</a>
	 */
	public MEGDRMapReader() {
	}
	
	private int convert2ByteToInt(byte[] data) {
	    if (data == null || data.length != 2) return 0x0;
	    // ----------
	    return (int)( // NOTE: type cast not necessary for int
	            (0xff & data[0]) << 8  |
	            (data[0]) << 8  |
	            (0xff & data[1]) << 0
	            );
	}
	
	/**
	 * Converts btye array to int array.
	 * 
	 * @param data
	 * @return
	 */
	public int[] convertByteArrayToIntArray(byte[] data) {
        if (data == null || data.length % 2 != 0) return null;
        // ----------
        int[] ints = new int[data.length / 2];
        for (int i = 0; i < ints.length; i++)
            ints[i] = ( convert2ByteToInt(new byte[] {
                    data[(i*2)],
                    data[(i*2)+1],
            } ));
        return ints;
    }
	
	public int[] loadElevation() {	 
		InputStream inputStream = MEGDRMapReader.class.getResourceAsStream(FILE); //new BufferedInputStream(new FileInputStream(inputFile));

	    try {
	    		
			byte[] bytes = ByteStreams.toByteArray(inputStream);
			
			elevation = convertByteArrayToIntArray(bytes);
//			int size = elevation.length;
//			for (int j=0; j<size; j++) {
//				if (j % WIDTH == 0) System.out.println();
//				System.out.print(elevation[j] + " ");
//			}
			
//		    int i = 0;  
//			while (inputStream.read(buffer) != -1) {
//				// Combine the 2 bytes into a 16-bit number
//				//				elevation[i] =  (0xff & buffer[0] << 8) | (0xff & buffer[1]);
//				elevation[i] = (buffer[0] << 8 ) | (buffer[1] & 0xff);
//				if (i % WIDTH == 0) System.out.println();
//				System.out.print(elevation[i] + " ");// + buffer[0] + " " + buffer[1]);
//				i++;
//			}
//        
	        inputStream.close();
	        
		} catch (IOException e) {
			 System.out.println("Problems in inputStream: " + e.getMessage());
		}

        return elevation;
        
//        try {
//			write(OUTPUT, elevation);
//		} catch (IOException e) {
//		}
//        
//        write2ByteArray(UNCOMPRESSED, elevation);
//        
//        int[] uncompressed = read2ByteArray(UNCOMPRESSED);
//        
//        if(Arrays.equals(elevation, uncompressed)) {
//            System.out.println("Uncompressed elevation data is recovered from file without loss");
//        }
//        else
//            throw new RuntimeException("bug"); // could use assert
        		
//        useJavaFastPFOR();
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
    
    /**
     * Uses Java Fast PFOR library to compress and uncompress arrays of integers. 
     * See https://github.com/lemire/JavaFastPFOR.
     */
    public void useJavaFastPFOR() {
//        int ChunkSize = 8192 ; //16384; //32768; // size of each chunk, choose a multiple of 128
        final int N = elevation.length;
//        final int TotalSize = N; // some arbitrary number
        int[] data = elevation;
        
        // output vector should be large enough...
//      int [] compressed = new int[TotalSize + 4096];//1024]; 
        int[] compressed = new int [N+1024];// could need more

        System.out.println("Compressing " + elevation.length + " integers using friendly interface");
   
        IntWrapper inputoffset = new IntWrapper(0);
        IntWrapper outputoffset = new IntWrapper(0);
        
        // CODEC type 1
        IntegerCODEC codec =  new Composition(
            new FastPFOR(),
            new VariableByte());
        
        // Compressing
        codec.compress(data,inputoffset,data.length,compressed,outputoffset);

//        // CODEC type 2
//        System.out.println("Compressing "+TotalSize+" integers using chunks of "+ChunkSize+" integers ("+ChunkSize*4/1024+"KB)");
//        System.out.println("(It is often better for applications to work in chunks fitting in CPU cache.)");
//        
//        // Most of the processing
//        // will be done with binary packing, and leftovers will
//        // be processed using variable byte, using variable byte
//        // only for the last chunk!
//        IntegratedIntegerCODEC regularcodec =  new IntegratedBinaryPacking();
//        IntegratedVariableByte ivb = new IntegratedVariableByte();
//        IntegratedIntegerCODEC lastcodec =  new IntegratedComposition(regularcodec,ivb);
//        
//        // Compressing
//        for(int k = 0; k < TotalSize / ChunkSize; ++k)
//            regularcodec.compress(data,inputoffset,ChunkSize,compressed,outputoffset);
//        
//        lastcodec.compress(data, inputoffset, TotalSize % ChunkSize, compressed, outputoffset);
            
        System.out.println("Reduce size of unsorted integers from "
        		+ data.length*4/1024+"KB to " 
        		+ outputoffset.intValue()*4/1024+"KB");
        System.out.println("compressed.length : " + compressed.length + "    N + 1024 : " + (N + 1024));

        // we can repack the data: (optional)
        compressed = Arrays.copyOf(compressed,outputoffset.intValue());
        
        COMPRESSED_N = compressed.length;
        System.out.println("Repacking compressed int[], size of COMPRESSED_N : " + COMPRESSED_N);

        // CODEC type 1
        int[] recovered = new int[N];
        IntWrapper recoffset = new IntWrapper(0);
        codec.uncompress(compressed,
        		new IntWrapper(0),
        		compressed.length,
        		recovered,
        		recoffset);
        
//        // CODEC type 2
//        // We are *not* assuming that the original array length is known, however
//        // we assume that the chunk size (ChunkSize) is known.
//        int[] recovered = new int[ChunkSize]; // TotalSize];//
//        IntWrapper compoff = new IntWrapper(0);
//        IntWrapper recoffset;
//        int currentpos = 0;
//
//        while(compoff.get()<compressed.length) {
//            recoffset = new IntWrapper(0);
//            regularcodec.uncompress(compressed,compoff,compressed.length - compoff.get(),recovered,recoffset);
//
//            if(recoffset.get() < ChunkSize) {// last chunk detected
//                ivb.uncompress(compressed,compoff,compressed.length - compoff.get(),recovered,recoffset);
//            }
//            for(int i = 0; i < recoffset.get(); ++i) {
//                if(data[currentpos+i] != recovered[i]) throw new RuntimeException("bug"); // could use assert
//            }
//            currentpos += recoffset.get();
//        }
        
        System.out.println("recovered.length : " + recovered.length);
        
        if(Arrays.equals(data, recovered)) {
            System.out.println("Elevation data is recovered in memory without loss");
            write4ByteArray(COMPRESSED, compressed);
        }
        else
            throw new RuntimeException("bug"); // could use assert
  
//        IntWrapper outputoffset2 = new IntWrapper(0);
        
        int[] compressed2 = read4ByteArray(COMPRESSED);
        System.out.println("compressed2.length : " + compressed2.length);
        		
        // we can repack the data: (optional)
//        compressed2 = Arrays.copyOf(compressed2, outputoffset2.intValue());
//        System.out.println("Repacking compressed2, compressed2.length : " + compressed2.length);
        
        int[] recovered2 = new int[N];
        IntWrapper recoffset2 = new IntWrapper(0);
        codec.uncompress(compressed2,
        		new IntWrapper(0),
        		compressed2.length,
        		recovered2,
        		recoffset2);
        
//        // CODEC type 2
//        // We are *not* assuming that the original array length is known, however
//        // we assume that the chunk size (ChunkSize) is known.
//        int[] recovered2 = new int[ChunkSize];
//        IntWrapper compoff2 = new IntWrapper(0);
//        IntWrapper recoffset2;
//        int currentpos2 = 0;
//
//        while(compoff.get( )< compressed2.length) {
//            recoffset2 = new IntWrapper(0);
//            regularcodec.uncompress(compressed2,
//            		compoff2,
//            		compressed2.length - compoff2.get(),
//            		recovered2,
//            		recoffset2);
//
//            if(recoffset2.get() < ChunkSize) {// last chunk detected
//                ivb.uncompress(compressed2,
//                		compoff2,
//                		compressed2.length - compoff2.get(),
//                		recovered2,
//                		recoffset2);
//            }
//            for(int i = 0; i < recoffset2.get(); ++i) {
//                if(data[currentpos2+i] != recovered2[i]) throw new RuntimeException("bug"); // could use assert
//            }
//            currentpos2 += recoffset2.get();
//        }
         
        System.out.println("recovered2.length : " + recovered2.length);
        
//        if(Arrays.equals(recovered, recovered2)) {       
        if(Arrays.equals(data, recovered2)) {
            System.out.println("Elevation data is recovered from the file without loss");
        }
        else
            throw new RuntimeException("bug"); // could use assert

    }
    
    public void write4ByteArray(String filename, int[] array) {
    	int size = array.length;
    	
    	byte[] data = new byte[size * COMPRESSED_BUFFER_SIZE];
    	
        System.out.println("byte[] to be written. length : " + data.length + "   (4 * COMPRESSED_N : " + 4 * COMPRESSED_N + ")");
        
    	for (int i=0; i<size; i++) {

//    		data[i*2]     = (byte)((array[i] >> 8) & 0xff);
//    		data[i*2 + 1] = (byte)((array[i] >> 0) & 0xff);
 		
    		data[i*4]     = (byte)((array[i] >> 24) & 0xff);
    		data[i*4 + 1] = (byte)((array[i] >> 16) & 0xff);
    		data[i*4 + 2] = (byte)((array[i] >> 8) & 0xff);
    		data[i*4 + 3] = (byte)((array[i] >> 0) & 0xff);
  		
//    	    ByteBuffer bb = ByteBuffer.allocate(4); 
//    	    bb.putInt(i); 
//    	    data[i*4] = bb.array()[0];
//    	    data[i*4 + 1] = bb.array()[1];
//    	    data[i*4 + 2] = bb.array()[2];
//    	    data[i*4 + 3] = bb.array()[3];
    	}

    	try (BufferedOutputStream stream = new BufferedOutputStream(
                new FileOutputStream(filename))) {
    		stream.write(data);
		} catch (IOException e) {
			 System.out.println("Problems in write4ByteArray's FileOutputStream: " + e.getMessage());
		}
    }
    
    public void write2ByteArray(String filename, int[] array) {
    	int size = array.length;
    	
    	byte[] data = new byte[size * BUFFER_SIZE];
    	
        System.out.println("byte[] to be written. length : " + data.length);// + "   (2 * COMPRESSED_N : " + 2 * COMPRESSED_N + ")");
         		
    	for (int i=0; i<size; i++) {
//    		elevation[i] =  (buffer[0] << 8) | (0xff & buffer[1]);
    		
    		data[i*2]     = (byte)((array[i] >> 8) & 0xff);
//    		data[i*2]     = (byte)((array[i] >> 8));
    		data[i*2 + 1] = (byte)((array[i] >> 0) & 0xff);
 		
//    		data[i*4]     = (byte)((array[i] >> 24) & 0xff);
//    		data[i*4 + 1] = (byte)((array[i] >> 16) & 0xff);
//    		data[i*4 + 2] = (byte)((array[i] >> 8) & 0xff);
//    		data[i*4 + 3] = (byte)((array[i] >> 0) & 0xff);
  		
//    	    ByteBuffer bb = ByteBuffer.allocate(4); 
//    	    bb.putInt(i); 
//    	    data[i*4] = bb.array()[0];
//    	    data[i*4 + 1] = bb.array()[1];
//    	    data[i*4 + 2] = bb.array()[2];
//    	    data[i*4 + 3] = bb.array()[3];
    	}

    	try (BufferedOutputStream stream = new BufferedOutputStream(
                new FileOutputStream(filename))) {
    		stream.write(data);
		} catch (IOException e) {
			 System.out.println("Problems in write2ByteArray's FileOutputStream: " + e.getMessage());
		}
    }

    
    public int[] read4ByteArray(String filename) {

    	int[] compressed = new int[COMPRESSED_N];
//    	byte[] data = new byte[compressed.length * 4];
//    	int size = data.length;
    	
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(filename))) {
		
			int i = 0;
	    
			while (inputStream.read(cBuffer) != -1) {
				// Combine the ? bytes into a 16-bit integer
//				compressed[i] =  (0xff & cBuffer[0] << 8) | (0xff & cBuffer[1] );
				
				// NOTE: type cast not necessary for int
				compressed[i] =  
		            (0xff & cBuffer[0]) << 24  |
		            (0xff & cBuffer[1]) << 16  |
		            (0xff & cBuffer[2]) << 8   |
		            (0xff & cBuffer[3]) << 0;
				
//				if (i % WIDTH == 0) System.out.println();
//				System.out.print(compressed[i] + " " + cBuffer[0] + " " + cBuffer[1] + " " + el2[i]);
				i++;
			}
			
	        inputStream.close();
	        
		}
        catch (IOException e) {
			 System.out.println("Problems in read4ByteArray's FileInputStream: " + e.getMessage());
		}
   
        System.out.println("int[] reassembled from file. length : " + compressed.length);
        
        return compressed;
    }
    
//    static IntegratedIntCompressor iic = new IntegratedIntCompressor(
//            new SkippableIntegratedComposition(
//                new IntegratedBinaryPacking(),
//                new IntegratedVariableByte()));
}
