/**
 * Mars Simulation Project
 * MasterClock.java
 * @version 2.76 2004-06-01
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;

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
    private boolean keepRunning;  // Runnable flag
    private double timeRatio;     // Simulation/real-time ratio

    // Sleep duration in milliseconds 
    private final static int SLEEP_DURATION = 1000;
    
    static final long serialVersionUID = -1688463735489226494L;

    /** 
     * Constructor
     * @throws Exception if clock could not be constructed.
     */
    public MasterClock() throws Exception {
        // Initialize data members
		SimulationConfig config = Simulation.instance().getSimConfig();

        // Create a Martian clock
        marsTime = new MarsClock(config.getMarsStartDateTime());
	
        // Create an Earth clock
        earthTime = new EarthClock(config.getEarthStartDateTime());

        // Create an Uptime Timer
        uptimer = new UpTimer();
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
     * Gets the time pulse length
     * @return time pulse length in millisols
     * @throws Exception if time pulse length could not be determined.
     */
    public double getTimePulse() throws Exception {

		// Get time ratio from simulation configuration.
		if (timeRatio == 0) setTimeRatio(Simulation.instance().getSimConfig().getSimulationTimeRatio());

        double timePulse;
        if (timeRatio > 0D) {
            double timePulseSeconds = timeRatio * (SLEEP_DURATION / 1000D);
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

        // Keep running until told not to
        while (keepRunning) {
            try {
                Thread.sleep(SLEEP_DURATION);
            } 
            catch (InterruptedException e) {}

			try {
            	//Increment the uptimer
            	uptimer.addTime(SLEEP_DURATION);

            	// Get the time pulse length in millisols.
            	double timePulse = getTimePulse();

            	// Send simulation a clock pulse representing the time pulse length (in millisols).
            	Simulation.instance().clockPulse(timePulse);

            	// Add time pulse length to Earth and Mars clocks. 
            	earthTime.addTime(MarsClock.convertMillisolsToSeconds(timePulse));
            	marsTime.addTime(timePulse);
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				stop();
			}
        }
    }

    /**
     * Stop the clock 
     */
    public void stop() {
        keepRunning = false;
    }
}
