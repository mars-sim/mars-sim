/**
 * Mars Simulation Project
 * UpTimer.java
 * @version 2.72 2001-02-21
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.util.*;

/** The UpTimer class keeps track of how long a instance of the simulation 
 *  has been running in real time.
 */
public class UpTimer {

    // Data members
    Date startTime;

    /** Constructs an UpTimer object */
    public UpTimer() {
    
        startTime = new Date();
    }

    /** Determines the amount of time the simulation has been running. 
     *  @return simulation running time formatted in a string. ex "5:32:58"
     */
    public String getUptime() {
        
        Date currentTime = new Date();

        long uptime = currentTime.getTime() - startTime.getTime();

        int hoursInt = (int) ((double) uptime / 1000D / 60D / 60D);
        String hours = "" + hoursInt;
        
        int minutesInt = (int) ((double) uptime / 1000D / 60D) - (hoursInt * 60);
        String minutes = "" + minutesInt;
        if (minutesInt < 10) minutes = "0" + minutesInt;

        int secondsInt = (int) ((double) uptime / 1000D) - (hoursInt * 60 * 60) - (minutesInt * 60);
        String seconds = "" + secondsInt;
        if (secondsInt < 10) seconds = "0" + secondsInt;

        return hours + ":" + minutes + ":" + seconds;
    }
}
