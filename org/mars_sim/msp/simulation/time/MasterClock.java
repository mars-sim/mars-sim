/**
 * Mars Simulation Project
 * MasterClock.java
 * @version 2.76 2004-06-01
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.time;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;

/** The MasterClock represents the simulated time clock on virtual
 *  Mars. Virtual Mars has only one master clock. The master clock
 *  delivers a clock pulse the virtual Mars every second or so, which
 *  represents a pulse of simulated time.  All actions taken with
 *  virtual Mars and its units are synchronized with this clock pulse.
 */
public class MasterClock implements Runnable, Serializable {

    // Data members
    private MarsClock marsTime;   // Martian Clock
    private EarthClock earthTime; // Earth Clock
    private UpTimer uptimer;      // Uptime Timer
    private transient volatile boolean keepRunning;  // Runnable flag
    private transient volatile boolean isPaused; // Pausing clock.
    private volatile double timeRatio;     // Simulation/real-time ratio
    private transient volatile boolean loadSimulation; // Flag for loading a new simulation.
    private transient volatile boolean saveSimulation; // Flag for saving a simulation.
    private transient volatile File file;            // The file to save or load the simulation.
    private transient volatile boolean exitProgram;  // Flag for ending the simulation program.
    private transient List listeners; // Clock listeners.

    // Sleep duration in milliseconds 
    public final static long TIME_PULSE_LENGTH = 1000L;
    
    static final long serialVersionUID = -1688463735489226494L;

    /** 
     * Constructor
     * @throws Exception if clock could not be constructed.
     */
    public MasterClock() throws Exception {
        // Initialize data members
		SimulationConfig config = SimulationConfig.instance();

        // Create a Martian clock
        marsTime = new MarsClock(config.getMarsStartDateTime());
	
        // Create an Earth clock
        earthTime = new EarthClock(config.getEarthStartDateTime());

        // Create an Uptime Timer
        uptimer = new UpTimer();
        
        // Create listener list.
        listeners = Collections.synchronizedList(new ArrayList());
    }

    /** Returns the Martian clock
     *  @return Martian clock instance
     */
    public MarsClock getMarsClock() {
        return marsTime;
    }

    /** Returns the Earth clock
     *  @return Earth clock instance
     */
    public EarthClock getEarthClock() {
        return earthTime;
    }

    /** Returns uptime timer
     *  @return uptimer instance
     */
    public UpTimer getUpTimer() {
        return uptimer;
    }
    
    /**
     * Adds a clock listener
     * @param newListener the listener to add.
     */
    public final void addClockListener(ClockListener newListener) {
    	if (listeners == null) listeners = Collections.synchronizedList(new ArrayList());
        if (!listeners.contains(newListener)) listeners.add(newListener);
    }
    
    /**
     * Removes a clock listener
     * @param oldListener the listener to remove.
     */
    public final void removeClockListener(ClockListener oldListener) {
    	if (listeners == null) listeners = Collections.synchronizedList(new ArrayList());
    	if (listeners.contains(oldListener)) listeners.remove(oldListener);
    }
    
    /**
     * Sets the load simulation flag and the file to load from.
     * @param file the file to load from.
     */
    public void loadSimulation(File file) {
    	loadSimulation = true;
    	this.file = file;
    }
    
    /**
     * Checks if in the process of loading a simulation.
     * @return true if loading simulation.
     */
    public boolean isLoadingSimulation() {
    	return loadSimulation;
    }
    
    /**
     * Sets the save simulation flag and the file to save to.
     * @param file save to file or null if default file.
     */
    public void saveSimulation(File file) {
    	saveSimulation = true;
    	this.file = file;
    }
    
    /**
     * Checks if in the process of saving a simulation.
     * @return true if saving simulation.
     */
    public boolean isSavingSimulation() {
    	return saveSimulation;
    }
    
    /**
     * Sets the exit program flag.
     */
    public void exitProgram() {
    	exitProgram = true;
    }

    /** 
     * Gets the time pulse length
     * @return time pulse length in millisols
     * @throws Exception if time pulse length could not be determined.
     */
    public double getTimePulse() throws Exception {

		// Get time ratio from simulation configuration.
		if (timeRatio == 0) setTimeRatio(SimulationConfig.instance().getSimulationTimeRatio());

        double timePulse;
        if (timeRatio > 0D) {
            double timePulseSeconds = timeRatio * (TIME_PULSE_LENGTH / 1000D);
            timePulse = MarsClock.convertSecondsToMillisols(timePulseSeconds);
        }
        else timePulse = 1D;
    
        return timePulse;
    }
    
    /** 
     * Sets the simulation/real-time ratio.
     * Value cannot be 0 or less.
     * @param ratio the simulation/real-time ratio.
     * @throws Exception if parameter is invalid.
     */
    public void setTimeRatio(double ratio) throws Exception {
    	if (ratio > 0D) timeRatio = ratio;
    	else throw new Exception("Time ratio cannot be zero or less.");
    }
    
    /**
     * Gets the simulation/real-time ratio.
     * @return ratio
     */
    public double getTimeRatio() {
    	return timeRatio;
    }

    /** Run clock */
    public void run() {
        keepRunning = true;
        long lastTimeDiff = 1000L;

        // Keep running until told not to
        while (keepRunning) {
        	
        	long pauseTime = TIME_PULSE_LENGTH - lastTimeDiff;
        	if (pauseTime < 10L) pauseTime = 10L;
        	
        	try {
        		Thread.sleep(pauseTime);
        	} 
        	catch (InterruptedException e) {}
            
        	if (!isPaused()) {
        		try {
        			//Increment the uptimer
        			uptimer.addTime(TIME_PULSE_LENGTH);

        			// Get the time pulse length in millisols.
        			double timePulse = getTimePulse();

        			long startTime = System.nanoTime();
        		
        			// Add time pulse length to Earth and Mars clocks. 
        			earthTime.addTime(MarsClock.convertMillisolsToSeconds(timePulse));
        			marsTime.addTime(timePulse);
				
        			synchronized(listeners) {
        				// Send clock pulse to listeners.
        				Iterator i = listeners.iterator();
        				while (i.hasNext()) ((ClockListener) i.next()).clockPulse(timePulse);
        			}
				
        			long endTime = System.nanoTime();
        			lastTimeDiff = (endTime - startTime) / 1000000L;
        			// System.out.println("time: " + lastTimeDiff);
        		}
        		catch (Exception e) {
        			e.printStackTrace(System.err);
        			stop();
        		}
        	}
			
			try {
        		if (saveSimulation) {
        			// Save the simulation to a file.
					Simulation.instance().saveSimulation(file);
					saveSimulation = false;
				}
				else if (loadSimulation) {
					// Load the simulation from a file.
					Simulation.instance().loadSimulation(file);
					loadSimulation = false;
				}
        	}
        	catch (Exception e) {
        		e.printStackTrace(System.err);
        		saveSimulation = false;
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
     * @param isPaused true if simulation is paused.
     */
    public void setPaused(boolean isPaused) {
    	this.isPaused = isPaused;
    }
    
    /**
     * Checks if the simulation is paused or not.
     * @return true if paused.
     */
    public boolean isPaused() {
    	return isPaused;
    }
}