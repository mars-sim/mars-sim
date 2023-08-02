/*
 * Mars Simulation Project
 * UpTimer.java
 * @date 2021-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.IOException;
import java.io.Serializable;


/**
 * The UpTimer class keeps track of how long an instance of the simulation
 * has been running in real time.
 */
public class UpTimer implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = -4992839027918134952L;

	/** Initialized logger. */
//  private static final Logger logger = Logger.getLogger(UpTimer.class.getName());

//	private static final long NANOSECONDS_PER_MILLISECONDS = 1_000_000L;

	/** The time limit (ms) allowed between time pulses. */
//	private static final long TIME_LIMIT = 1_000L;

	private static final String DAY = "d ";
//	private static final String HR = "h ";
//	private static final String MIN = "m ";
//	private static final String SEC = "s ";
//	private static final String ZERO = "0";

	private static final long SECS_PER_MIN = 60;
	private static final long HOURS_PER_DAY = 24;
	private static final long MINS_PER_HOUR = 60;
	private static final long SECS_PER_HOUR = MINS_PER_HOUR * SECS_PER_MIN;
	private static final long SECS_PER_DAY = HOURS_PER_DAY * SECS_PER_HOUR;

	// Data members
	/** The last up time. Sets to 1 in case it gets divided by 0 right away. */
	private long uptime = 1;

    public UpTimer() {
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    /**
     * Updates the simulation uptime.
     * 
     * TODO: May switch to using System.currentTimeMillis() to get the start time and end time and subtract the pause time.
     * 
     * @param elapsedMilli
     */
    public void updateTime(double elapsedMilli) {
        uptime += elapsedMilli;
    }

    /**
     * Reports the amount of time the simulation has been running, as a String.
     *
     * @return simulation running time formatted in a string. ex "6 days 5:32:58"
     */
    public String getUptime() {
    	long uptimeSec = uptime/1000;
       	StringBuilder result = new StringBuilder();
       	long days = uptimeSec / SECS_PER_DAY;
       	long hours = (uptimeSec / SECS_PER_HOUR) % HOURS_PER_DAY;
       	long mins = (uptimeSec / SECS_PER_MIN) % MINS_PER_HOUR;
       	long secs = uptimeSec % SECS_PER_MIN;
       	
        if (days > 0) {
        	result.append(days);
        	result.append(DAY);
        }

        result.append(String.format("%02dh %02dm %02ds", hours, mins, secs));

        return result.toString();
     }
}
