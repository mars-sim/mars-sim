/**
 * Mars Simulation Project
 * MasterClock.java
 * @version 2.82 2007-11-09
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;

/** The MasterClock represents the simulated time clock on virtual
 *  Mars. Virtual Mars has only one master clock. The master clock
 *  delivers a clock pulse the virtual Mars every second or so, which
 *  represents a pulse of simulated time.  All actions taken with
 *  virtual Mars and its units are synchronized with this clock pulse.
 *  
 *  Update: The pulse is now tied to the system clock. This means that each time
 *  a timePulse is generated, it is the following length: 
 *  
 *  	(realworldseconds since last call ) * timeRatio
 *  
 *  update: with regard to pauses.. 
 *  
 *  they work. the sim will completely pause when setPause(true) is called, and will
 *  resume with setPause(false);
 *  However ! Do not make any calls to System.currenttimemillis(), instead use 
 *  uptimer.getuptimemillis(), as this is "shielded" from showing any passed time
 *  while the game is paused. Thank you. 
 *  
 *  
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
    private volatile double timeRatio=1;     // Simulation/real-time ratio
    private transient volatile boolean loadSimulation; // Flag for loading a new simulation.
    private transient volatile boolean saveSimulation; // Flag for saving a simulation.
    private transient volatile File file;            // The file to save or load the simulation.
    private transient volatile boolean exitProgram;  // Flag for ending the simulation program.
    private transient List<ClockListener> listeners; // Clock listeners.
    private transient volatile long totalpulses=1;
    private transient volatile double pulsespersec=0.0;
   // private transient long pausestart=System.currentTimeMillis(),pauseend=System.currentTimeMillis(),pausetime=0;
    private transient long elapsedlast;// = uptimer.getUptimeMillis();//System.currentTimeMillis();;
    // Sleep duration in milliseconds 
    //public final static long TIME_PULSE_LENGTH = 1000L;
    
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
        initialMarsTime = (MarsClock) marsTime.clone();
	
        // Create an Earth clock
        earthTime = new EarthClock(config.getEarthStartDateTime());

        // Create an Uptime Timer
        uptimer = new UpTimer();
        
        // Create listener list.
        listeners = Collections.synchronizedList(new ArrayList<ClockListener>());
        elapsedlast = uptimer.getUptimeMillis();//System.currentTimeMillis();
//        elapsedlast = System.currentTimeMillis();
    }

    /** Returns the Martian clock
     *  @return Martian clock instance
     */
    public MarsClock getMarsClock() {
        return marsTime;
    }
    
    /**
     * Gets the initial Mars time at the start of the simulation.
     * @return initial Mars time.
     */
    public MarsClock getInitialMarsTime() {
    	return initialMarsTime;
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
    	if (listeners == null) listeners = Collections.synchronizedList(new ArrayList<ClockListener>());
        if (!listeners.contains(newListener)) listeners.add(newListener);
    }
    
    /**
     * Removes a clock listener
     * @param oldListener the listener to remove.
     */
    public final void removeClockListener(ClockListener oldListener) {
    	if (listeners == null) listeners = Collections.synchronizedList(new ArrayList<ClockListener>());
    	if (listeners.contains(oldListener)) listeners.remove(oldListener);
    }
    
    /**
     * Sets the load simulation flag and the file to load from.
     * @param file the file to load from.
     */
    public void loadSimulation(File file) {
    	this.setPaused(false);
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
    	this.setPaused(true);
    	exitProgram = true;
    }

    /** 
     * Gets the time pulse length
     * in other words, the number of realworld seconds that have elapsed since it was last called 
     * @return time pulse length in millisols
     * @throws Exception if time pulse length could not be determined.
     */
    public double getTimePulse() throws Exception {

		// Get time ratio from simulation configuration.
    	
		if (timeRatio == 0) setTimeRatio((int)SimulationConfig.instance().getSimulationTimeRatio());

        double timePulse;
        if (timeRatio > 0D) {
           double timePulseSeconds = ((double)this.getElapsedmillis() * timeRatio/1000);// * (TIME_PULSE_LENGTH / 1000D);
            timePulse = MarsClock.convertSecondsToMillisols(timePulseSeconds);
        }
        else timePulse = 1D;
    
        totalpulses++;
        return timePulse;
    }
    public long gettotalpulses() 
    {
    	return totalpulses;
    }
    
    /** 
     * Sets the simulation/real-time ratio.
     * accepts input in the range 1..100. It will do the rest. 
     * @param ratio the simulation/real-time ratio.
     * @throws Exception if parameter is invalid.
     */
    public void setTimeRatio(int slidervalue) throws Exception {
    	// ratio should be in the range 1..100 inclusive
    	/*
    	 * the numbers below have been tweaked with some care. At 20, the realworld:sim ratio is 1:1
    	 * above 20, the numbers start climbing logarithmically maxing out at around 100K this is really fast
    	 * Below 20, the simulation goes in slow motion, 1:0.0004 is around the slowest. The increments may be 
    	 * so small at this point that events can't progress at all. When run too quickly, lots of accidents occur,
    	 * and lots of settlers die. 
    	 * */
    	
    	if ( (slidervalue > 0)&&(slidervalue <= 100) )
    	{if (slidervalue >= 20 ) 
    		{timeRatio = Math.round( Math.pow(1.135, (slidervalue-20)*1.2)  );

    		} 
    		else 
    		{
    		 timeRatio = Math.pow(1.232, (slidervalue-19)*1.8);	
    		 if (timeRatio < 0.001) timeRatio = 0.001;
    		}
    	} 
    	else {
    		timeRatio = 15;
    		throw new Exception("Time ratio should be in 1..100");
    		} 
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
        elapsedlast = uptimer.getUptimeMillis();// System.currentTimeMillis();
        // Keep running until told not to
        while (keepRunning) {
        	
        	//long pauseTime = TIME_PULSE_LENGTH - lastTimeDiff;
        	//if (pauseTime < 10L) pauseTime = 10L;
        	
        	try {
//        		Thread.sleep(pauseTime);
        		Thread.sleep(50);
        		//Thread.yield();
        	} 
        	catch (InterruptedException e) {}
            
        	if (!isPaused()) {
        		try {
        			// Get the time pulse length in millisols.
        			double timePulse = getTimePulse();
        	//		System.out.println("gettimePulse() "+timePulse);
        			long startTime = System.nanoTime();

        			// Add time pulse length to Earth and Mars clocks. 
        			earthTime.addTime(MarsClock.convertMillisolsToSeconds(timePulse));
        			marsTime.addTime(timePulse);
				
        			synchronized(listeners) {
        				// Send clock pulse to listeners.
        				Iterator<ClockListener> i = listeners.iterator();
//        				while (i.hasNext()) i.next().clockPulse(timePulse);
        				while (i.hasNext()) {
            				ClockListener cl = i.next();
        					cl.clockPulse(timePulse);
        					//System.out.println("Master clock sending pulse to object: "+cl.toString());
        				}
        			}
				
        			long endTime = System.nanoTime();
        			lastTimeDiff = (endTime - startTime) / 1000000L;
        			
        			if(logger.isLoggable(Level.FINEST)) {
        			    logger.finest("time: " + lastTimeDiff);
        			}
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
    	uptimer.setPaused(isPaused);
    	this.isPaused = isPaused;
    }
    
    /**
     * Checks if the simulation is paused or not.
     * @return true if paused.
     */
    public boolean isPaused() {
    	return isPaused;
    }
    

    public double getPulsesPerSecond()
    	{
    	//System.out.println("pulsespersecond: "+((double) totalpulses / (uptimer.getUptimeMillis()/1000 ) ));
    	return ((double) totalpulses / (uptimer.getUptimeMillis()/1000 ) );}
 
    private long getElapsedmillis() {        
    	long tnow = uptimer.getUptimeMillis();// System.currentTimeMillis();
    	long jelapsed = tnow - elapsedlast ;
    	elapsedlast = tnow;
    //	System.out.println("getElapsedmillis "+jelapsed);
    	return jelapsed;
    	}

    /**
     * the following is a utility. It may be slow. It returns a string in YY:DDD:HH:MM:SS.SSS format
     * note: it is set up currently to only return hh:mm:ss.s
     * */
    public String gettimestring(double seconds) 
    {
        final int secspmin = 60, secsphour = 3600, secspday = 86400, secsperyear = 31536000;
    	long years,days,hours,minutes;
    	double secs;
    	String YY="",DD="",HH="",MM="",SS="";
    	
    	years = (int)Math.floor(seconds/secsperyear);
		days = (int)((seconds%secsperyear)/secspday);		
		hours=(int)((seconds%secspday)/secsphour);
		minutes=(int)((seconds%secsphour)/secspmin);
		secs=(double)((seconds%secspmin));

	
	if (years > 0) {YY=""+years+":";} else {YY="";};	
	
	if (days > 0 ) {DD=String.format("%03d",days)+":";} else {DD="0:";} 
	
	if (hours > 0) {HH = String.format("%02d",hours)+":";} else {HH = "00:";}
	
	if (minutes > 0){ MM = String.format("%02d",minutes)+":";}else {MM="00:";} 

	SS = String.format("%5.3f", secs);
   	//******* change here for more complete string *****
	return /*YY+*/DD+HH+MM+SS;
    	
    }
}