/**
 * Mars Simulation Project
 * StreamConsumer.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 * $LastChangedDate$
 * $LastChangedRevision$
 */

package org.mars_sim.msp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A thread for consuming output from another stream.
 */
public class StreamConsumer extends Thread {

    // Data members
    InputStream in;
    String type;

    /**
     * Constructor
     * @param in the input stream to consume.
     * @param type the stream type.
     */
    public StreamConsumer(InputStream in, String type) {
        this.in = in;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
                System.out.println(type + ">" + line);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}