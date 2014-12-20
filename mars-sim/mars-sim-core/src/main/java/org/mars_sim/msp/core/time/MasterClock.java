/**
 * Mars Simulation Project
 * MasterClock.java
 * @version 3.07 2014-11-05
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

	/** default serial id. */
	static final long serialVersionUID = -1688463735489226493L;

	/** Initialized logger. */
	private static Logger logger = Logger.getLogger(MasterClock.class.getName());

	/** Clock thread sleep time (milliseconds) --> 25Hz should be sufficient. */
	private static long SLEEP_TIME = 40L;

	// Data members
	/** Martian Clock. */
	private MarsClock marsTime;
	/** Initial Martian time. */
	private MarsClock initialMarsTime;
	/** Earth Clock. */
	private EarthClock earthTime;
	/** Uptime Timer. */
	private UpTimer uptimer;
	/** Runnable flag. */
	private transient volatile boolean keepRunning;
	/** Pausing clock. */
	private transient volatile boolean isPaused = false;
	/** Simulation/real-time ratio. */
	private volatile double timeRatio = 0D;
	/** Flag for loading a new simulation. */
	private transient volatile boolean loadSimulation;
	/** Flag for saving a simulation. */
	private transient volatile boolean saveSimulation;
	/** The file to save or load the simulation. */
	private transient volatile File file;
	/** Flag for ending the simulation program. */
	private transient volatile boolean exitProgram;
	/** Clock listeners. */
	private transient List<ClockListener> listeners;
	private long totalPulses = 1;
	private transient long elapsedlast;
	private transient long elapsedMilliseconds;

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
        if (timeRatio == 0) setTimeRatio(SimulationConfig.instance().getSimulationTimeRatio());

        double timePulse;
        if (timeRatio > 0D) {
            double timePulseSeconds = ((double) getElapsedmillis() * (timeRatio / 1000D));
            timePulse = MarsClock.convertSecondsToMillisols(timePulseSeconds);
        } 
        else timePulse = 1D;

        return timePulse;
    }

    public long getTotalPulses() {
        return totalPulses;
    }

    /**
     * setTimeRatio is for setting the Masterclock's time ratio directly. It is a double
     * indicating the simetime:realtime ratio. 1000 means 1000 sim time minutes elapse for
     * each real-world minute.
     */
    public void setTimeRatio(double ratio) {
        if (ratio >= 0.0001D && ratio <= 500000D) {
            timeRatio = ratio;
        } 
        else throw new IllegalArgumentException("Time ratio out of bounds ");
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

            // Pause simulation to allow other threads to complete.
            try {
                Thread.yield();
                Thread.sleep(SLEEP_TIME);
            } 
            catch (Exception e) {
                logger.log(Level.WARNING, "Problem with Thread.yield() in MasterClock.run() ", e);
            }

            if (!isPaused) {
                
                // Update elapsed milliseconds.
                updateElapsedMilliseconds();
                
                // Get the time pulse length in millisols.
                double timePulse = getTimePulse();
                
                // Incrementing total time pulse number.
                totalPulses++;
                
                long startTime = System.nanoTime();

                // Add time pulse length to Earth and Mars clocks.
                double earthTimeDiff = getElapsedmillis() * timeRatio / 1000D;
                earthTime.addTime(earthTimeDiff);
                marsTime.addTime(timePulse);

                // Fire clock pulse to all clock listeners.
                fireClockPulse(timePulse);
                
                long endTime = System.nanoTime();
                lastTimeDiff = (long) ((endTime - startTime) / 1000000D);

                logger.finest("Pulse #" + totalPulses + " time: " + lastTimeDiff + " ms");
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
            } 
            else if (loadSimulation) {
                // Load the simulation from a file.
                if (file.exists() && file.canRead()) {
                    Simulation.instance().loadSimulation(file);
                    Simulation.instance().start();
                } 
                else {
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
     * Send a clock pulse to all clock listeners.
     * @param time the amount of time (millisols) in the pulse.
     */
    public void fireClockPulse(double time) {
        
        synchronized (listeners) {
            Iterator<ClockListener> i = listeners.iterator();
            while (i.hasNext()) {
                ClockListener cl = i.next();
                try {
                    cl.clockPulse(time);
                    try {
                        Thread.yield();
                    } 
                    catch (Exception e) {
                        logger.log(Level.WARNING, "Problem with Thread.yield() in MasterClock.run() ", e);
                    }
                } catch (Exception e) {
            		throw new IllegalStateException("Error while firing clock pulse", e);
                }
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
    	//if (isPaused) System.out.println("MasterClock.java : setPaused() : isPause is true");
        this.isPaused = isPaused;
        // Fire pause change to all clock listeners.
        firePauseChange();
    }

    /**
     * Checks if the simulation is paused or not.
     *
     * @return true if paused.
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Send a pulse change event to all clock listeners.
     */
    public void firePauseChange() {
        
        synchronized (listeners) {
            Iterator<ClockListener> i = listeners.iterator();
            while (i.hasNext()) {
                ClockListener cl = i.next();
                try {
                    cl.pauseChange(isPaused);
                } catch (Exception e) {
                    throw new IllegalStateException("Error while firing pase change", e);
                }
            }
        }
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

        if (days > 0) {
            b.append(String.format("%03d", days)).append(":");
        } else {
            b.append("0:");
        }

        if (hours > 0) {
            b.append(String.format("%02d", hours)).append(":");
        } else {
            b.append("00:");
        }

        if (minutes > 0) {
            b.append(String.format("%02d", minutes)).append(":");
        } else {
            b.append("00:");
        }

        b.append(String.format("%5.3f", secs));

        return b.toString();
    }
    
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
       marsTime = null;
       initialMarsTime = null;
       earthTime = null;
       uptimer = null;
       listeners.clear();
       listeners = null;
    }
}