/*
 * Mars Simulation Project
 * CompressIntArray.java
 * @date 2022-10-22
 * @author Manny Kung
 */

package org.mars_sim.mapdata;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.io.ByteStreams;

import me.lemire.integercompression.differential.IntegratedBinaryPacking;
import me.lemire.integercompression.differential.IntegratedIntCompressor;
import me.lemire.integercompression.differential.IntegratedVariableByte;
import me.lemire.integercompression.differential.SkippableIntegratedComposition;

public class CompressIntArray {

	private static final String FILE = MEGDRMapReader.FILE;
	
    static IntegratedIntCompressor iic = new IntegratedIntCompressor(
            new SkippableIntegratedComposition(
                new IntegratedBinaryPacking(),
                new IntegratedVariableByte()));

	
	public static void main(String[] args) throws IOException {
		new CompressIntArray(FILE);
	}
	

    public CompressIntArray(String filename) {
    	
        System.out.println("Loading file " + filename + " as a bitmap...");
        
        int[] data = null;
		try {
			data = fromBitsetFileToArray(filename);
			
			if (data != null && data != new int[0]) {
		        System.out.println("Compressing " + data.length + " integers...");
		        
		        int[] compressed = iic.compress(data);
		        int[] recov = iic.uncompress(compressed);
		        
		        System.out.println("Compressing " + data.length*4/1024 + " KB to " + compressed.length*4/1024 + " KB.");
		        System.out.println("Recovering back to " + recov.length*4/1024 + " KB.");
		        System.out.println("Ratio: " + Math.round(data.length*1.0/compressed.length));
	
		        if(!Arrays.equals(recov,data)) throw new RuntimeException("Warning: Bug(s) encountered.");
	
		        long bef,aft;
		        bef = System.nanoTime();
		        recov = iic.uncompress(compressed);
		        aft = System.nanoTime();
	
		        System.out.println("Decoding speed: " + Math.round(data.length*1000.0/(aft-bef)) + " millions of integers per second.");
	
		        bef = System.nanoTime();
		        compressed = iic.compress(data);
		        aft = System.nanoTime();
	
		        System.out.println("Encoding speed: " + Math.round(data.length*1000.0/(aft-bef)) + " millions of integers per second.");
		        System.out.println("Note: with a bit of effort, speed can be much higher.");
		        System.out.println();
			}
			
	        ////////////////////////////////////////////
	        
	        
//	        try {
////				zipStats(filename);
//				toZip(filename);
//				
//			} catch (IOException e) {
//				System.out.println("Zipping issues: " + e.getMessage());
//			}

		} catch (Exception e1) {
			 System.out.println("Exception: " + e1.getMessage());
		}
        
    }

    public static int[] fromBitsetFileToArray(String filename) throws IOException {
    	InputStream inputStream = MEGDRMapReader.class.getResourceAsStream(filename);
        byte[] data = ByteStreams.toByteArray(inputStream);
        
        
        if (data != null) {
        	System.out.println("The byte array has a length of " + data.length + ".");
            // we determine cardinality
            int[] answer = new int[data.length/2];//720*1440];
            
            for(int i = 0 ; i < answer.length; ++i) {
    			// Combine the 2 bytes into a 16-bit number
            	answer[i] =  (data[2*i] << 8) | (data[2*i+1] & 0xff);
//    			if (i % WIDTH == 0) System.out.println();
//    			System.out.print(elevation[i] + " " + buffer[0] + " " + buffer[1] + " " + el2[i]);
    			i++;
    		}
    		
//            int card = 0;
//            for (int k = 0 ; k < data.length; ++k) {
//                int bv = data[k] & 0xFF;
//                card += Integer.bitCount(bv);
//            }
    //
//            int pos = 0;
//            for (int k = 0 ; k < data.length; ++k) {
//                int bv = data[k] & 0xFF;
//                for(int b = 0 ; b < 8; ++b)
//                    if ( ( (bv >> b) & 1 ) == 1) {
//                        answer[pos++] = b + k * 8;
//                    }
//            }
//            
//            if (pos != card) throw new RuntimeException("bug");
            

        	System.out.println("The int array has a length of " + answer.length + ".");
        	
            return answer;
        }
        else
        	System.out.println("The byte array is null.");
        
        return new int[0];
    }

	public void zipFile(File srcFile, File zipFile) throws IOException {
    	try (FileInputStream fis = new FileInputStream(srcFile);
    		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
    		zos.putNextEntry(new ZipEntry(srcFile.getName()));
    		int len;
    		byte[] buffer = new byte[1024];
    		while ((len = fis.read(buffer)) > 0) {
    			zos.write(buffer, 0, len);
    		}
    		zos.closeEntry();
    	}
	}
    
    
    public static void zipStats(String filename) throws IOException {     
    	InputStream inputStream = MEGDRMapReader.class.getResourceAsStream(filename);
        byte[] data = ByteStreams.toByteArray(inputStream);
        
        if (data != null) {
        	System.out.println("The byte array has a length of " + data.length + ".");
       	
            File fileToZip = new File(inputStream.toString());
            
            long bef = System.nanoTime();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            zos.setLevel(9);
            ZipEntry entry = new ZipEntry(fileToZip.getName());//.toString().replace(".img", ".zip"));
            entry.setSize(data.length);
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
            zos.close();
            
            byte[] result = baos.toByteArray();
            long aft = System.nanoTime();
            
            System.out.println("Zip encoding speed: " + data.length*1000.0/(aft-bef) + " million of bytes per second.");
            System.out.println("Zip compression ratio at best level: " + data.length * 1.0 / result.length);
        }
        else
        	System.out.println("The byte array is null.");
        
        System.out.println("Trying to compress the original bitmap using zip...");
    }
 
    public static byte[] readFileIntoByteArray(String filename) throws IOException {
    	File file = new File(filename);
    	byte[] bytes = null;
        try (InputStream is = new FileInputStream(file)) { //MEGDRMapReader.class.getResourceAsStream(filename)) {// 
            if (file.length() > Integer.MAX_VALUE) {
                throw new IOException("File is too large.");
            }

            int offset = 0;
            int bytesRead;
            bytes = new byte[(int) file.length()];
            while (offset < bytes.length
                    && (bytesRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += bytesRead;
            }
        }
        return bytes;
    }
}
