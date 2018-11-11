/**
 * Mars Simulation Project
 * UpTimer.java
 * @version 3.07 2015-01-12
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;


/**
 * The UpTimer class keeps track of how long an instance of the simulation
 * has been running in real time.
 */
public class UpTimer implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = -4992839027918134952L;

	/** Initialized logger. */
    private static Logger logger = Logger.getLogger(UpTimer.class.getName());

	private static final long NANOSECONDS_PER_MILLISECONDS = 1_000_000L;

	/** The time limit (ms) allowed between time pulses. */
	private static final long TIME_LIMIT = 1000L;

	private transient long thiscall = System.nanoTime() / NANOSECONDS_PER_MILLISECONDS;
	private transient long lastcall = System.nanoTime() / NANOSECONDS_PER_MILLISECONDS;

	private static final int secspmin = 60, secsphour = 3600, secspday = 86400, secsperyear = 31536000;
	private long days, hours, minutes, seconds;

	// Data members
	/** The last up time. Sets to 1 in case it gets divided by 0 right away. */
	private long uptime = 1;
	private long utsec = 0;

	private transient boolean paused = true;

	private static MasterClock masterClock;// = Simulation.instance().getMasterClock();

    public UpTimer(MasterClock masterclock) {
    	this.masterClock = masterclock;
        this.setPaused(false);
        lastcall = System.nanoTime() / NANOSECONDS_PER_MILLISECONDS;
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        lastcall = System.nanoTime() / NANOSECONDS_PER_MILLISECONDS;
    }

    public void updateTime() {
        utsec = getUptimeMillis() / 1000;
        days = (long) ((utsec % secsperyear) / secspday);
        hours = (long) ((utsec % secspday) / secsphour);
        minutes = (long) ((utsec % secsphour) / secspmin);
        seconds = (long) ((utsec % secspmin));
    }

    /**
     * Reports the amount of time the simulation has been running, as a String.
     *
     * @return simulation running time formatted in a string. ex "6 days 5:32:58"
     */
    public String getUptime() {
    	StringBuilder result = new StringBuilder();

        if (days > 0) {
        	result.append(days);
        	result.append("d ");
        }

        if (hours < 10) {
        	result.append("0");
        }

    	result.append(hours);
    	result.append("h ");

        if (minutes < 10) {
        	result.append("0");
        }

    	result.append(minutes);
    	result.append("m ");

        if (seconds < 10) {
        	result.append("0");
        }

    	result.append(seconds);
    	result.append("s ");

        return result.toString();
/*
        String minstr = "" + minutes;
        if (minutes < 10)
        	minstr = "0" + minutes;

        String secstr = "" + seconds;
        if (seconds < 10) secstr = "0" + seconds;

        String daystr = "";
        if (days == 1) daystr = "" + days + " Day ";
        else {
            daystr = "" + days + " Days ";
        }

        String hourstr = "" + hours;
        return daystr + hourstr + ":" + minstr + ":" + secstr;
*/


    }

    /**
     * Gets the uptime in milliseconds
     */
    public long getUptimeMillis() {
        thiscall = System.nanoTime() / NANOSECONDS_PER_MILLISECONDS;
        if (paused) {
            return uptime;
        }
        else {
            if ((thiscall - lastcall) < TIME_LIMIT) {
                uptime = uptime + (thiscall - lastcall);
                lastcall = thiscall;
                return uptime;
            }
            else {
                thiscall = lastcall = System.nanoTime() / NANOSECONDS_PER_MILLISECONDS;
                masterClock.resetTotalPulses();
                //logger.warning("Time limit exceeded between the last and this call, resetting the total # of pules");
                return uptime;
            }
        }
    }

    /**
     * Gets the last uptime
     * @return uptime
     */
    public long getLastUptime() {
    	return uptime;
    }

    /**
     * Checks if the simulation is paused
     * @return true if the simulation is paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Sets the simulation pause mode
     * @param value
     */
    public void setPaused(boolean value) {
        paused = value;
        if (value) {

        } else {
            thiscall = lastcall = System.nanoTime() / NANOSECONDS_PER_MILLISECONDS;
        }
    }
    
    /**
     * Prepare object for garbage collection.
     */
	public void destroy() {
		masterClock = null;
	}
}