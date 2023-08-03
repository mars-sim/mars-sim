/*
 * Mars Simulation Project
 * Hash.java
 * @date 2021-08-28
 * @author Manny Kung
 */

package org.mars_sim.msp.core.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public enum Hash {

    MD5("MD5"),
    SHA1("SHA1"),
    SHA256("SHA-256"),
    SHA512("SHA-512");

    private String name;

    Hash(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public byte[] checksum(File input) {
        try (InputStream in = new FileInputStream(input)) {
            MessageDigest digest = MessageDigest.getInstance(getName());
            byte[] block = new byte[4096];
            int length;
            while ((length = in.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            return digest.digest();
        } catch (Exception e) {
            System.out.println("Problem in Hash's checksum(): " + e.getMessage());
        }
        return null;
    }

    public String getChecksumString(File file) throws IOException {
        //Get file input stream for reading the file content
    	try (FileInputStream fis = new FileInputStream(file)) {
            return getChecksumString(fis);
        }
    }

    /**
     * Generate a checksum string for a input stream of text.
     * @param source
     * @return
     * @throws IOException
     */
    public String getChecksumString(InputStream source) throws IOException {
        try {
    		//Create byte array to read data in chunks
    		byte[] byteArray = new byte[1024];
    		int bytesCount = 0; 
        
    		MessageDigest digest  = MessageDigest.getInstance(getName());
	        //Read file data and update in message digest
	        while ((bytesCount = source.read(byteArray)) != -1) {
	            digest.update(byteArray, 0, bytesCount);
	        };
 

	        //Get the hash's bytes
	        byte[] bytes = digest.digest();
	         
	        //This bytes[] has bytes in decimal format;
	        //Convert it to hexadecimal format
	        StringBuilder sb = new StringBuilder();
	        for(int i=0; i< bytes.length ;i++) {
	            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
	        }
	         
	       return sb.toString();
		} catch (NoSuchAlgorithmException e) {
            System.out.println("Problem in Hash's getChecksumString(): " + e.getMessage());
		}
    	
    	// Return empty hash
    	return "";
    }
}
