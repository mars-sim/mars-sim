/**
 * Mars Simulation Project
 * MasterClock.java
 * @version 3.01 2011-05-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The MasterClock represents the simulated time clock on virtual
 * Mars. Virtual Mars has only one master clock. The master clock
 * delivers a clock pulse the virtual Mars every second or so, which
 * represents a pulse of simulated time.  All actions taken with
 * virtual Mars and its units are synchronized with this clock pulse.
 * <p/>
 * Update: The pulse is now tied to the system clock. This means that each time
 * a timePulse is generated, it is the following length:
 * <p/>
 * (realworldseconds since last call ) * timeRatio
 * <p/>
 * update: with regard to pauses..
 * <p/>
 * they work. the sim will completely pause when setPause(true) is called, and will
 * resume with setPause(false);
 * However ! Do not make any calls to System.currenttimemillis(), instead use
 * uptimer.getuptimemillis(), as this is "shielded" from showing any passed time
 * while the game is paused. Thank you.
 */
public class MasterClock implements Runnable, Serializable {

    private static String CLASS_NAME =
            "org.mars_sim.msp.simulation.time.MasterClock";

    private static Logger logger = Logger.getLogger(CLASS_NAME);

    // Data members
    private MarsClock marsTime;   // Martian Clock
    private MarsClock initialMarsTime; // Initial Martian time.
    private EarthClock earthTime; // Earth Clock
    private UpTimer uptimer; // Uptime Timer
    private transient volatile boolean keepRunning;  // Runnable flag
    private transient volatile boolean isPaused = false; // Pausing clock.
    private volatile double timeRatio = 1;     // Simulation/real-time ratio
    private transient volatile boolean loadSimulation; // Flag for loading a new simulation.
    private transient volatile boolean saveSimulation; // Flag for saving a simulation.
    private transient volatile File file;            // The file to save or load the simulation.
    private transient volatile boolean exitProgram;  // Flag for ending the simulation program.
    private transient List<ClockListener> listeners; // Clock listeners.
    private long totalPulses = 1;
    // private transient long pausestart=System.currentTimeMillis(),pauseend=System.currentTimeMillis(),pausetime=0;
    private transient long elapsedlast;// = uptimer.getUptimeMillis();//System.currentTimeMillis();;
    private transient long elapsedMilliseconds;
    // Sleep duration in milliseconds 
    //public final static long TIME_PULSE_LENGTH = 1000L;

    static final long serialVersionUID = -1688463735489226494L;

    /**
     * Constructor
     *
     * @throws Exception if clock could not be constructed.
     */
    public MasterClock() {
        // Initialize data members
        SimulationConfig config = SimulationConfig.instance();

        // Create a Martian clock
        marsTime = new MarsClock(config.getMarsStartDateTime());
        initialMarsTime = (MarsClock) marsTime.clone();

        // Create an Earth clock
        earthTime = new EarthClock(config.getEarthStartDateTime());

        // Create an Uptime Timer
        uptimer = new UpTimer();

        // Create listener list.
        listeners = Collections.synchronizedList(new ArrayList<ClockListener>());
        elapsedlast = uptimer.getUptimeMillis();
        elapsedMilliseconds = 0L;
    }

    /**
     * Returns the Martian clock
     *
     * @return Martian clock instance
     */
    public MarsClock getMarsClock() {
        return marsTime;
    }

    /**
     * Gets the initial Mars time at the start of the simulation.
     *
     * @return initial Mars time.
     */
    public MarsClock getInitialMarsTime() {
        return initialMarsTime;
    }

    /**
     * Returns the Earth clock
     *
     * @return Earth clock instance
     */
    public EarthClock getEarthClock() {
        return earthTime;
    }

    /**
     * Returns uptime timer
     *
     * @return uptimer instance
     */
    public UpTimer getUpTimer() {
        return uptimer;
    }

    /**
     * Adds a clock listener
     *
     * @param newListener the listener to add.
     */
    public final void addClockListener(ClockListener newListener) {
        if (listeners == null) listeners = Collections.synchronizedList(new ArrayList<ClockListener>());
        if (!listeners.contains(newListener)) listeners.add(newListener);
    }

    /**
     * Removes a clock listener
     *
     * @param oldListener the listener to remove.
     */
    public final void removeClockListener(ClockListener oldListener) {
        if (listeners == null) listeners = Collections.synchronizedList(new ArrayList<ClockListener>());
        if (listeners.contains(oldListener)) listeners.remove(oldListener);
    }

    /**
     * Sets the load simulation flag and the file to load from.
     *
     * @param file the file to load from.
     */
    public void loadSimulation(File file) {
        this.setPaused(false);
        loadSimulation = true;
        this.file = file;
    }

    /**
     * Checks if in the process of loading a simulation.
     *
     * @return true if loading simulation.
     */
    public boolean isLoadingSimulation() {
        return loadSimulation;
    }

    /**
     * Sets the save simulation flag and the file to save to.
     *
     * @param file save to file or null if default file.
     */
    public void saveSimulation(File file) {
        saveSimulation = true;
        this.file = file;
    }

    /**
     * Checks if in the process of saving a simulation.
     *
     * @return true if saving simulation.
     */
    public boolean isSavingSimulation() {
        return saveSimulation;
    }

    /**
     * Sets the exit program flag.
     */
    public void exitProgram() {
        this.setPaused(true);
        exitProgram = true;
    }

    /**
     * Gets the time pulse length
     * in other words, the number of realworld seconds that have elapsed since it was last called
     *
     * @return time pulse length in millisols
     * @throws Exception if time pulse length could not be determined.
     */
    public double getTimePulse() {

        // Get time ratio from simulation configuration.

        if (timeRatio == 0) setTimeRatio((int) SimulationConfig.instance().getSimulationTimeRatio());

        double timePulse;
        if (timeRatio > 0D) {
            double timePulseSeconds = ((double) getElapsedmillis() * (timeRatio / 1000D));// * (TIME_PULSE_LENGTH / 1000D);
            timePulse = MarsClock.convertSecondsToMillisols(timePulseSeconds);
        } else timePulse = 1D;

        totalPulses++;
        return timePulse;
    }

    public long getTotalPulses() {
        return totalPulses;
    }


    /*
    	 * the numbers below have been tweaked with some care. At 20, the realworld:sim ratio is 1:1
    	 * above 20, the numbers start climbing logarithmically maxing out at around 100K this is really fast
    	 * Below 20, the simulation goes in slow motion, 1:0.0004 is around the slowest. The increments may be
    	 * so small at this point that events can't progress at all. When run too quickly, lots of accidents occur,
    	 * and lots of settlers die.
    	 * */
    //you can change these to suit:
    private static final double ratioatmid = 1000.0D, //the "default" ratio that will be set at 50, the middle of the scale
            maxratio = 10800.0D, //the max ratio the sim can be set at
            minfracratio = 0.001D, //the minimum ratio the sim can be set at
            maxfracratio = 0.98D, //the largest fractional ratio the sim can be set at

            //don't recommend changing these:
            minslider = 20.0D,
            midslider = (50.0D - minslider),
            maxslider = 100D - minslider,
            minfracpos = 1D,
            maxfracpos = minslider - 1D;


    /**
     * Sets the simulation/real-time ratio.
     * accepts input in the range 1..100. It will do the rest.
     *
     * @param sliderValue the simulation/real-time ratio.
     * @throws Exception if parameter is invalid.
     */
    public void setTimeRatio(int sliderValue) {
        // sliderValue should be in the range 1..100 inclusive, if not it defaults to
        // 1:15 real:sim ratio
        
        //double base;
        double slope, offset;
        //double e1, e2;

        if ((sliderValue > 0) && (sliderValue <= 100)) {
/*
            if (sliderValue >= minslider) //generates ratios >= 1
            {
                offset = Math.pow(Math.E, ((Math.log(ratioatmid)) / midslider));
                e1 = Math.pow(Math.E, ((Math.log(maxratio)) / maxslider));
                e2 = Math.pow(Math.E, ((Math.log(ratioatmid)) / midslider));
                slope = (e1 - e2) / (maxslider - midslider);
                base = (sliderValue - minslider - 30) * slope + offset;

                timeRatio = Math.pow(base, (sliderValue - minslider));
                timeRatio = Math.round(timeRatio);
*/
            if (sliderValue >= (midslider + minslider)) {
                // Creates exponential curve between ratioatmid and maxratio.
                double a = ratioatmid;
                double b = maxratio / ratioatmid;
                double T = maxslider - midslider;
                double expo = (sliderValue - minslider - midslider) / T;
                timeRatio = a * Math.pow(b, expo);
            }
            else if (sliderValue >= minslider) {
                // Creates exponential curve between 1 and ratioatmid.
                double a = 1D;
                double b = ratioatmid;
                double T = midslider;
                double expo = (sliderValue - minslider) / T;
                timeRatio = a * Math.pow(b, expo);
            } else //generates ratios < 1
            {
                offset = minfracratio;
                slope = (maxfracratio - minfracratio) / (maxfracpos - minfracpos);
                timeRatio = (sliderValue - minfracpos) * slope + offset;
            }
        } else {
            timeRatio = 15D;
            throw new IllegalArgumentException("Time ratio should be in 1..100");
        }
    }

    /**
     * setTimeRatio is for setting the Masterclock's time ratio directly. It is a double
     * indicating the simetime:realtime ratio. 1000 means 1000 sim time minutes elapse for
     * each real-world minute.
     */
    public void setTimeRatio(double ratio) {
        if (ratio >= 0.0001D && ratio <= 500000D) {
            timeRatio = ratio;
            //need to set slider bar in the correct position.

        } else throw new IllegalArgumentException("Time ratio out of bounds ");
    }

    /**
     * Gets the real-time/simulation ratio.
     *
     * @return ratio
     */
    public double getTimeRatio() {
        return timeRatio;
    }

    /**
     * Run clock
     */
    public void run() {

        keepRunning = true;
        long lastTimeDiff;
        elapsedlast = uptimer.getUptimeMillis();
        
        // Keep running until told not to
        while (keepRunning) {

            try {
                Thread.yield();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Problem with Thread.yield() in MasterClock.run() ", e);
            }

            if (!isPaused) {
                
                // Update elapsed milliseconds.
                updateElapsedMilliseconds();
                
                // Get the time pulse length in millisols.
                double timePulse = getTimePulse();
                
                long startTime = System.nanoTime();

                // Add time pulse length to Earth and Mars clocks.
                double earthTimeDiff = getElapsedmillis() * timeRatio / 1000D;
                earthTime.addTime(earthTimeDiff);
                marsTime.addTime(timePulse);

                synchronized (listeners) {
                    // Send clock pulse to listeners.
                    Iterator<ClockListener> i = listeners.iterator();
                    while (i.hasNext()) {
                        ClockListener cl = i.next();
                        try {
                            cl.clockPulse(timePulse);
                        } catch (Exception e) {
                            throw new IllegalStateException("Error while looping master clock",e);
                        }
                    }
                }

                long endTime = System.nanoTime();
                lastTimeDiff = (endTime - startTime) / 1000000L;

                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("time: " + lastTimeDiff);
                }
                
                try {
                    Thread.yield();
                } catch (Exception e) {
                    logger.fine("Problem with Thread.yield() in MasterClock.run() ");
                }
            }
            
            if (saveSimulation) {
                // Save the simulation to a file.
                try {
                    Simulation.instance().saveSimulation(file);
                } catch (IOException e) {

                    logger.log(Level.SEVERE, "Could not save the simulation with file="
                            + (file == null ? "null" : file.getPath()), e);
                    e.printStackTrace();
                }
                saveSimulation = false;
            } else if (loadSimulation) {
                // Load the simulation from a file.
                if (file.exists() && file.canRead()) {
                    Simulation.instance().loadSimulation(file);
                } else {
                    logger.warning("Cannot access file " + file.getPath() + ", not reading");
                }
                loadSimulation = false;
            }

            // Exit program if exitProgram flag is true.
            if (exitProgram) {
                exitProgram = false;
                System.exit(0);
            }
        }
    }

    /**
     * Stop the clock
     */
    public void stop() {
        keepRunning = false;
    }

    /**
     * Set if the simulation is paused or not.
     *
     * @param isPaused true if simulation is paused.
     */
    public void setPaused(boolean isPaused) {
        uptimer.setPaused(isPaused);
        this.isPaused = isPaused;
    }

    /**
     * Checks if the simulation is paused or not.
     *
     * @return true if paused.
     */
    public boolean isPaused() {
        return isPaused;
    }


    public double getPulsesPerSecond() {
        //System.out.println("pulsespersecond: "+((double) totalPulses / (uptimer.getUptimeMillis()/1000 ) ));
        return ((double) totalPulses / (uptimer.getUptimeMillis() / 1000D));
    }

    private void updateElapsedMilliseconds() {
        long tnow = uptimer.getUptimeMillis();
        elapsedMilliseconds = tnow - elapsedlast;
        elapsedlast = tnow;
        //System.out.println("getElapsedmilliseconds " + elapsedMilliseconds);
    }
    
    private long getElapsedmillis() {
        return elapsedMilliseconds;
    }

    public static final int secspmin = 60, secsphour = 3600, secspday = 86400, secsperyear = 31536000;


    /**
     * the following is a utility. It may be slow. It returns a string in YY:DDD:HH:MM:SS.SSS format
     * note: it is set up currently to only return hh:mm:ss.s
     */
    public String getTimeString(double seconds) {

        logger.info("timestring for " + seconds);

//        long years, days, hours, minutes;
//        double secs;
//        String YY = "", DD = "", HH = "", MM = "", SS = "";

        long years = (int) Math.floor(seconds / secsperyear);
        long days = (int) ((seconds % secsperyear) / secspday);
        long hours = (int) ((seconds % secspday) / secsphour);
        long minutes = (int) ((seconds % secsphour) / secspmin);
        double secs = (seconds % secspmin);

        StringBuilder b = new StringBuilder();

        b.append(years);
        if(years>0){
            b.append(":");
        }


//        if (years > 0) {
//            YY = "" + years + ":";
//        } else {
//            YY = "";
//        }
//        ;

        if (days > 0) {
            b.append(String.format("%03d", days)).append(":");
//            DD = String.format("%03d", days) + ":";
        } else {
            b.append("0:");
//            DD = "0:";
        }

        if (hours > 0) {
            b.append(String.format("%02d", hours)).append(":");
//            HH = String.format("%02d", hours) + ":";
        } else {
            b.append("00:");
//            HH = "00:";
        }

        if (minutes > 0) {
            b.append(String.format("%02d", minutes)).append(":");
//            MM = String.format("%02d", minutes) + ":";
        } else {
            b.append("00:");
//            MM = "00:";
        }

        b.append(String.format("%5.3f", secs));
//        SS = String.format("%5.3f", secs);
        //******* change here for more complete string *****
//        return /*YY+*/DD + HH + MM + SS;

        return b.toString();
    }
}