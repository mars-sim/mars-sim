/*
 * Mars Simulation Project
 * CheckSerializedSize.java
 * @date 2022-07-28
 * @author Manny Kung
 */

package org.mars_sim.msp.core.tool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

// https://stackoverflow.com/questions/3938122/how-to-get-amount-of-serialized-bytes-representing-a-java-object

public class CheckSerializedSize extends OutputStream {

	/** 
	 * Converts an object into a byte array using ObjectOutputStream and 
	 * ByteArrayOutputStream. More overhead with byte array.
	 * 
	 * @param obj
	 * @return
	 */
	public static long getSerializedSizeByteArray(Serializable obj) {
	    try {
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = new ObjectOutputStream(baos);
		    oos.writeObject(obj);
            oos.close();
		    return baos.size();//toByteArray();
	        } catch (Exception e) {
	            // Serialization failed
	            return 0;
	        }
		}

    /** 
     * Serializes an object and count the bytes. To avoid the overhead of a big byte 
     * array for large objects, extend OutputStream as a counter.
	 * 
	 * @param obj
	 * @return
	 */
    public static long getSerializedSize(Serializable obj) {
        try (CheckSerializedSize counter = new CheckSerializedSize();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(counter))
        {
            objectOutputStream.writeObject(obj);
            objectOutputStream.close();
            return counter.getNBytes();
        } catch (Exception e) {
            // Serialization failed
            return 0;
        }
    }

    private long nBytes = 0;

    private CheckSerializedSize() {}

    @Override
    public void write(int b) throws IOException {
        ++nBytes;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        nBytes += len;
    }

    public long getNBytes() {
        return nBytes;
    }
}
