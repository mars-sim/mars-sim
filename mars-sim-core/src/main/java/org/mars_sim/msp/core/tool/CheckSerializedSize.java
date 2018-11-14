package org.mars_sim.msp.core.tool;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

// see https://stackoverflow.com/questions/3938122/how-to-get-amount-of-serialized-bytes-representing-a-java-object

public class CheckSerializedSize extends OutputStream {

    /** Serialize obj and count the bytes */
    public static long getSerializedSize(Serializable obj) {
        try {
            CheckSerializedSize counter = new CheckSerializedSize();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(counter);
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