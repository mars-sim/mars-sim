/**
 * Mars Simulation Project
 * MasterClock.java
 * @version 3.1.0 2017-01-12
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;

//import javafx.animation.Timeline;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The MasterClock represents the simulated time clock on virtual
 * Mars and delivers a clock pulse for each frame.
 */
public class MasterClock implements Serializable {

	/** default serial id. */
	static final long serialVersionUID = -1688463735489226493L;

	/** Initialized logger. */
	private static Logger logger = Logger.getLogger(MasterClock.class.getName());

	private static final double PERIOD_IN_MILLISOLS = 2500D / MarsClock.SECONDS_PER_MILLISOL;
	
	private static final int SEC_TO_MILLIS = 1000;
	
	// Data members
	/** Runnable flag. */
	private transient volatile boolean keepRunning = true;
	/** Pausing clock. */
	private transient volatile boolean isPaused = false;
	/** Simulation time ratio. */
	private volatile double currentTR = 0D;
	/** The Current time between updates (TBU). */
	private volatile long currentTBU_ns = 0L;
	/** Adjusted time ratio. */
	private volatile double adjustedTR = 0D;
	/** Adjusted time between updates in nanoseconds. */
	private volatile double adjustedTBU_ns = 0D;
	/** Adjusted time between updates in milliseconds. */
	private volatile double adjustedTBU_ms = 0;
	/** Adjusted time between updates in seconds. */
	private volatile double adjustedTBU_s = 0;
	/** Adjusted frame per sec */
	private volatile double adjustedFPS = 0;
	/** The maximum number of counts allowed in waiting for other threads to execute. */
	private int noDelaysPerYield = 0;
	/** The measure of tolerance of the maximum number of lost frames for saving a simulation. */
	private int maxFrameSkips = 0;
	/** Mode for saving a simulation. */
	private double tpfCache = 0;
	/** The UI refresh cycle. */
	private double refresh;	
	/** Mode for saving a simulation. */
	private transient volatile int saveType;
	/** Flag for ending the simulation program. */
	private transient volatile boolean exitProgram;
	/** Flag for getting ready for autosaving. */
	private transient volatile boolean autosave;
	/** The total number of pulses cumulated. */
	private long totalPulses = 1;
	/** The pulses since last elapsed. */
	private transient long elapsedLast;
	/** The cache for accumulating millisols up to a limit before sending out a clock pulse. */
	private transient double timeCache;
	/** Is FXGL is in use. */
	private boolean isFXGL = false;
	
	/** A list of clock listeners. */
	private transient List<ClockListener> clockListeners;
	/** A list of clock listener tasks. */
	private transient List<ClockListenerTask> clockListenerTasks =  new CopyOnWriteArrayList<>();


	/** The martian Clock. */
	private MarsClock marsTime;
	/** A copy of the initial martian clock at the start of the sim. */
	private static MarsClock initialMarsTime;
	/** The Earth Clock. */
	private EarthClock earthClock;
	/** The Uptime Timer. */
	private UpTimer uptimer;
	/** The file to save or load the simulation. */
	private transient volatile File file;
	/** The thread for running the game loop. */
	private ClockThreadTask clockThreadTask;

	//private transient ThreadPoolExecutor clockListenerExecutor;
	
	/** The thread for running the clock listeners. */
	private transient ExecutorService clockListenerExecutor;
	
	private static Simulation sim;
	
	private static SimulationConfig config;

    /**
     * Constructor
     * @param isFXGL true if FXGL is used for generating clock pulse
     * @param userTimeRatio the time ratio defined by user
     * @throws Exception if clock could not be constructed.
     */
    public MasterClock(boolean isFXGL, int userTimeRatio) {
    	this.isFXGL = isFXGL;
        //logger.info("MasterClock's constructor is on " + Thread.currentThread().getName() + " Thread");

    	sim = Simulation.instance();
        // Initialize data members
        config = SimulationConfig.instance();

        // Create a martian clock
        marsTime = new MarsClock(config.getMarsStartDateTime());
        
        initialMarsTime = (MarsClock) marsTime.clone();

//		testMarsTimeParams();
				
        // Create an Earth clock
        earthClock = new EarthClock(config.getEarthStartDateTime());
       
        // Create an Uptime Timer
        uptimer = new UpTimer(this);
     
        // Create listener list.
        clockListeners = Collections.synchronizedList(new CopyOnWriteArrayList<ClockListener>());
        elapsedLast = uptimer.getUptimeMillis();

        if (!isFXGL)
        	clockThreadTask = new ClockThreadTask();

        // Setting the initial time ratio.
        double tr = 0;
        if (userTimeRatio == -1)
        	tr = config.getTimeRatio();
        else {
        	tr = userTimeRatio;
        	logger.info("User-Defined Time Ratio is " + (int)tr + "x");
        }
        double tbu = config.getTimeBetweenUpdates();

        int threads = Simulation.NUM_THREADS;
     
        // Tune the time between update
        if (threads <= 32) {
        	adjustedTBU_ms =  12D / (Math.sqrt(threads) *2) * tbu;     	
        }
        else {
        	adjustedTBU_ms = 1.5 * tbu;
        }
    	
        // Tune the time ratio
        if (threads == 1) {
        	adjustedTR = tr/16D;
        }
        else if (threads == 2) {
        	adjustedTR = tr/8D;
        }
        else if (threads <= 3) {
        	adjustedTR = tr/4D;
        }
        else if (threads <= 4) {
           	adjustedTR = tr/4D;
        }
        else if (threads <= 6) {
        	adjustedTR = tr/2D;
        }
        else if (threads <= 8) {
        	adjustedTR = tr/2D;
        }
        else if (threads <= 12) {
        	adjustedTR = tr;
        }
        else if (threads <= 16) {
        	adjustedTR = tr;
        }
        else {
        	adjustedTR = tr;
        }

        adjustedTBU_ns = adjustedTBU_ms * 1_000_000.0; // convert from millis to nano
     	adjustedTBU_s = adjustedTBU_ms / 1_000.0;
     	adjustedFPS = 1.0/adjustedTBU_s;
        currentTBU_ns = (long) adjustedTBU_ns;

    	currentTR = adjustedTR;
    	
        // Added loading the values below from SimulationConfig
        setNoDelaysPerYield(config.getNoDelaysPerYield());
        setMaxFrameSkips(config.getMaxFrameSkips());

        logger.info("Based on # CPU cores/threads, the following parameters have been re-adjusted :");
        logger.info("Time Ratio (TR) : " + (int)adjustedTR + "x");
        logger.info("Time between Updates (TBU) : " + Math.round(adjustedTBU_ms * 100D)/100D + " ms");
        logger.info("Ticks Per Second (TPS) : " + Math.round(adjustedFPS*100D)/100D + " Hz");
		logger.info("*** Welcome to Mars and the beginning of the new adventure for humankind ***");
    }

    public void testMarsTimeParams() {
        // Create an Earth clock
        EarthClock c = new EarthClock("2043-09-30 00:00:00.000");//"2004-01-04 00:00:00.000"); // "2004-01-03 13:46:31.000"//"2000-01-06 00:00:00.000");
        
        ClockUtils.getFirstLandingDateTime();

		double millis = c.getMillis(c);
		logger.info("millis is " + millis); 
		double jdut = ClockUtils.getJulianDateUT(c);
		logger.info("jdut is " + jdut);    
		double T = ClockUtils.getT(c);
		logger.info("T is " + T);	
		double TT2UTC = ClockUtils.getDeltaUTC_TT(c);
		logger.info("TT2UTC is " + TT2UTC);	
		double jdtt = ClockUtils.getJulianDateTT(c);
		logger.info("jdtt is " + jdtt);
		double j2k = ClockUtils.getDaysSinceJ2kEpoch(c);
		logger.info("j2k is " + j2k);
		double M = ClockUtils.getMarsMeanAnomaly(c)%360;
		logger.info("M is " + M);
		double alpha = ClockUtils.getAlphaFMS(c)%360;
		logger.info("alpha is " + alpha);		
		double PBS = ClockUtils.getPBS(c)%360;
		logger.info("PBS is " + PBS);	
		double EOC = ClockUtils.getEOC(c)%360;
		logger.info("EOC is " + EOC);	
		double v = ClockUtils.getTrueAnomaly_Concise(c)%360;
		logger.info("v is " + v);
		double L_s = ClockUtils.getLs(c)%360;
		logger.info("L_s is " + L_s);

		double EOT = ClockUtils.getEOT_Concise(c);
		logger.info("EOT is " + EOT);
		double EOT_hr = ClockUtils.getEOTHour_Concise(c);
		logger.info("EOT_hr is " + EOT_hr);	
//		String EOTStr = ClockUtils.getEOTString(c);
//		logger.info("EOTStr is " + EOTStr);	
		
		double MTC = ClockUtils.getMTC(c);
		logger.info("MTC is " + MTC);
//		String MTCStr = ClockUtils.getMTCString(c);
//		logger.info("MTCStr is " + MTCStr);

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
        return earthClock;
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
     * @param newListener the listener to add.
     */
    public final void addClockListener(ClockListener newListener) {
        // if listeners list does not exist, create one
    	if (clockListeners == null) clockListeners = Collections.synchronizedList(new CopyOnWriteArrayList<ClockListener>());
        // if the listeners list does not contain newListener, add it to the list
    	if (!clockListeners.contains(newListener)) clockListeners.add(newListener);
    	// will check if clockListenerTaskList already contain the newListener's task, if it doesn't, create one
    	addClockListenerTask(newListener);
      }


    /**
     * Removes a clock listener
     * @param oldListener the listener to remove.
     */
    public final void removeClockListener(ClockListener oldListener) {
        if (clockListeners == null) clockListeners = Collections.synchronizedList(new CopyOnWriteArrayList<ClockListener>());
        if (clockListeners.contains(oldListener)) clockListeners.remove(oldListener);
       	// Check if clockListenerTaskList contain the newListener's task, if it does, delete it
        ClockListenerTask task = retrieveClockListenerTask(oldListener);
        if (task != null) clockListenerTasks.remove(task);
    }

    /**
     * Adds a clock listener task
     *
     * @param newListener the clock listener task to add.
     */
    public void addClockListenerTask(ClockListener listener) {
    	boolean hasIt = false;
    	if (clockListenerTasks == null)
    		clockListenerTasks =  new CopyOnWriteArrayList<ClockListenerTask>();
    	Iterator<ClockListenerTask> i = clockListenerTasks.iterator();
    	while (i.hasNext()) {
    		ClockListenerTask c = i.next();
    		if (c.getClockListener().equals(listener))
    			hasIt = true;
    	}
    	if (!hasIt) {
	    	clockListenerTasks.add(new ClockListenerTask(listener));
    	}
    }

    /**
     * Retrieve a clock listener task
     * @param oldListener the clock listener task to remove.
     */
    public ClockListenerTask retrieveClockListenerTask(ClockListener oldListener) {
/*     	ClockListenerTask c = null;
    	clockListenerTaskList.forEach(t -> {
    		ClockListenerTask l = c;
    		if (t.getClockListener().equals(oldListener))
    			l = t;
    	});
*/
    	ClockListenerTask t = null;
    	Iterator<ClockListenerTask> i = clockListenerTasks.iterator();
    	while (i.hasNext()) {
    		ClockListenerTask c = i.next();
    		if (c.getClockListener().equals(oldListener))
    		 t = c;
    	}
		return t;
    }

    /**
     * Sets the load simulation flag and the file to load from.
     *
     * @param file the file to load from.
     */
    public void loadSimulation(File file) {
        this.setPaused(false, false);
        //loadSimulation = true;
        this.file = file;
    }

    /**
     * Sets the save simulation flag and the file to save to.
     * @param file save to file or null if default file.
     */
    public void setSaveSim(int type, File file) {
        saveType = type;
        //System.out.println("file is "+ file);
        this.file = file;
    }

    /**
     * Sets the value of autosave
     * @param value
     */
    public void setAutosave(boolean value) {
    	autosave = value;
    }

    /**
     * Gets the value of autosave
     * @return autosave
     */
    public boolean getAutosave() {
    	return autosave;
    }

    /**
     * Checks if in the process of saving a simulation.
     * @return true if saving simulation.
     */
    public boolean isSavingSimulation() {
    	if (saveType != 0)
    		return true;
    	else
    		return false;
        //return saveSimulation || autosaveSimulation;
    }
    
    /**
     * Sets the exit program flag.
     */
    public void exitProgram() {
        this.setPaused(true, false);
        exitProgram = true;
    }

    /**
     * Computes the time pulse in millisols
     * in other words, the number of realworld seconds that have elapsed since it was last called
     * @return time pulse length in millisols
     * @throws Exception if time pulse length could not be determined.
     */
    public double computeTimePulseInMillisols(long elapsedMilliseconds) {
    	return computeTimePulseInSeconds(elapsedMilliseconds)/MarsClock.SECONDS_PER_MILLISOL;
    }

    /**
     * Computes the time pulse in seconds. It varies, depending on the time ratio
     * @return time pulse length in seconds
     */
    public double computeTimePulseInSeconds(long elapsedMilliseconds) {
    	return elapsedMilliseconds * currentTR / 1000D;
    }
    
    /*
     * Gets the total number of pulses since the start of the sim
     */
    public long getTotalPulses() {
        return totalPulses;
    }

    /**
     * Resets the total number of pulses using the default TBU value
     * @return totalPulses
     */
	public void resetTotalPulses() {
		totalPulses = (long) (1D/adjustedTBU_ms * uptimer.getLastUptime());
	}

    /**
     * Sets the simulation time ratio and adjust the value of time between update (TBU)
     * @param ratio
     */
    public void setTimeRatio(double ratio) {
        if (ratio >= 1D && ratio <= 65536D) {

        	if (ratio > currentTR)
        		currentTBU_ns = (long) (currentTBU_ns * 1.0025); // increment by .5%
        	else
        		currentTBU_ns = (long) (currentTBU_ns * .9975); // decrement by .5%

        	adjustedTBU_s = currentTBU_ns/1_000_000_000D;
            currentTR = ratio;
        }
        else throw new IllegalArgumentException("Time ratio is out of bounds ");
    }


    /**
     * Gets the simulation time ratio.
     * @return ratio
     */
    public double getTimeRatio() {
        return currentTR;
    }

    /**
     * Gets the default simulation time ratio.
     *
     * @return ratio
     */
    public double getCalculatedTimeRatio() {
        return adjustedTR;
    }


    /**
     * Gets the current time between update (TBU)
     * @return value in nanoseconds
     */
    public long getCurrentTBU() {
        return currentTBU_ns;
    }


    /**
     * Sets the value of no-delay-per-yield
     * @param value in number
     */
    public void setNoDelaysPerYield(int value) {
        if (value >= 1D && value <= 200D) {
        	noDelaysPerYield = value;
        }
        else throw new IllegalArgumentException("No-Delays-Per-Yield is out of bounds. Must be between 1 and 200");
    }

    /**
     * Gets the number of no-delay-per-yield
     * @return value in milliseconds
     */
    public int getNoDelaysPerYield() {
        return noDelaysPerYield;
    }

    /**
     * Sets the maximum number of skipped frames allowed
     * @param number of frames
     */
    public void setMaxFrameSkips(int value) {
        if (value >= 1 && value <= 200) {
        	maxFrameSkips = value;
        }
        else throw new IllegalArgumentException("max-frame-skips is out of bounds. Must be between 1 and 200");
    }

    /**
     * Gets the maximum number of skipped frames allowed
     * @return number of frames
     */
    public int getMaxFrameSkips() {
        return maxFrameSkips;
    }
    /**
     * Returns the instance of ClockThreadTask
     * @return ClockThreadTask
     */
    public ClockThreadTask getClockThreadTask() {
    	return clockThreadTask;
    }

    /**
     * Runs master clock's thread using ThreadPoolExecutor
     */
    class ClockThreadTask implements Runnable, Serializable {

		private static final long serialVersionUID = 1L;

		private ClockThreadTask() {
			//logger.info("MasterClock's ClockThreadTask's constructor is on " + Thread.currentThread().getName() + " Thread");
		}

		@Override
		public void run() {
	        elapsedLast = uptimer.getUptimeMillis();
	        // Keep running until told not to by calling stop()
	        keepRunning = true;

	        if (!isFXGL) {

				long t1, t2, sleepTime, overSleepTime = 0L, excess = 0L;
		        int noDelays = 0;
		        t1 = System.nanoTime();
		        
	        	 while (keepRunning) {
	 		        // Refactored codes for variable sleepTime
	        		t2 = System.nanoTime();

	        		addTime();
	 	           
	 		        // Benchmark CPU speed
//	 		        long diff = 0;
//
//	 		        if (count >= 1000)
//	 		        	count = 0;
//
//	 		        if (count >= 0) {
//	 			        diff = (long) ((t2 - t2Cache) / 1_000_000D);
//	 		        	diffCache = (diff * count + diffCache)/(count + 1);
//	 		        }

	 		        //if (count == 0) logger.info("Benchmarking this machine : " + diff + " per 1000 frames");
	 		        
	 	            //dt = t2 - t1;
	 	            sleepTime = currentTBU_ns - t2 + t1 - overSleepTime;
	 	            //System.out.print ("sleep : " + sleepTime/1_000_000 + "ms\t");

	 	            if (sleepTime > 0 && keepRunning) {
 			            // Pause simulation to allow other threads to complete.
 			            try {
 			                //Thread.yield();
 							TimeUnit.NANOSECONDS.sleep(sleepTime);
 			            }
 			            catch (InterruptedException e) {
 			            	Thread.currentThread().interrupt();
 				            //    logger.log(Level.WARNING, "program terminated while running sleep() in MasterClock.run() ", e);
 			            }

 			            overSleepTime = (System.nanoTime() - t2) - sleepTime;

 		            	//timeBetweenUpdates = (long) (timeBetweenUpdates * .999905); // decrement by .0005%
	 	            }

	 	            else { // last frame went beyond the PERIOD
	 	            	excess -= sleepTime;
	 	            	overSleepTime = 0L;

	 	            	if (++noDelays >= noDelaysPerYield) {
	 	            		Thread.yield();
	 	            		noDelays = 0;
	 	            	}

	 	            	//timeBetweenUpdates = (long) (timeBetweenUpdates * 1.0025); // increment by 0.25%
	 	            }

	 	            int skips = 0;

	 	            while ((excess > currentTBU_ns) && (skips < maxFrameSkips)) {
	 	            	excess -= currentTBU_ns;
	 	            	// Make up lost frames 
	 	            	skips++;

	 	            	if (skips >= maxFrameSkips) {
	 		            	logger.info("# of skips has reached the maximum # of frame skips. Resetting total pulse and slowing down (TBU).");
	 		            	resetTotalPulses();
	 		            	if (currentTBU_ns > (long) (adjustedTBU_ns * 1.25))
	 		            		currentTBU_ns = (long) (adjustedTBU_ns * 1.25);
	 		            	else
	 		            		currentTBU_ns = (long) (currentTBU_ns * .9925); // decrement by 2.5%
	 	            	}	
	 	            	
	 	            	addTime();
	 	            }
	 	            
	 	            // Set excess to zero to prevent getting stuck in the above while loop after waking up from power saving
	 	            excess = 0;
	 	            
	 	            t1 = System.nanoTime();
 	            
	 	            if (checkSave())
		 	            // Reset t1 time due to the long process of saving
	 	            	t1 = System.nanoTime();
	 	            
 	               // Exit program if exitProgram flag is true.
 	               if (exitProgram) {
 	            	   if (sim.getAutosaveTimer() != null)
 	            		   sim.getAutosaveTimer().shutdownNow();//.stop();
 	              
 	            	   System.exit(0);
 	               }
 	               
 	               // For performance benchmarking
//	 		       t2Cache = t2;
//	 		       count++;

	 	        } // end of while
	        } // if fxgl is not used
	    } // end of run
    }

   /*
    * Add earth time and mars time
    */
    private void addTime() {
        if (!isPaused) {
            // Update elapsed milliseconds.
            long millis = updateElapsedMilliseconds();
            // Get the sim millisecond for EarthClock
        	//double ms = millis * timeRatio;
            // Get the time pulse length in millisols.
            //double timePulse = millis / 1000 * timeRatio / MarsClock.SECONDS_IN_MILLISOL;
            // Get the time pulse length in millisols.
            double timePulse = computeTimePulseInMillisols(millis);
            // Incrementing total time pulse number.
            totalPulses++;

            if (timePulse > 0
            	&& keepRunning
	            //|| !clockListenerExecutor.isTerminating()
            	&& clockListenerExecutor != null
            	&& !clockListenerExecutor.isTerminated()
            	&& !clockListenerExecutor.isShutdown()) {

	                // Add time pulse length to Earth and Mars clocks.
	            	earthClock.addTime(millis * currentTR);
	            	marsTime.addTime(timePulse);
				  	fireClockPulse(timePulse);
            }   
        }
    }
    
    /**
     * Checks if it is on pause or a saving process has been requested. Keeps track of the time pulse 
     */
    private boolean checkSave() {
        //logger.info("MasterClock's checkSave() is on " + Thread.currentThread().getName() + " Thread");
    	
        if (saveType != 0) {
            try {
            	//logger.info("MasterClock's checkSave() is on " + Thread.currentThread().getName() + " Thread");
                sim.saveSimulation(saveType, file);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IOException. Could not save the simulation as "
                        + (file == null ? "null" : file.getPath()), e);
                e.printStackTrace();

	        } catch (Exception e1) {
	            logger.log(Level.SEVERE, "Exception. Could not save the simulation as "
	                    + (file == null ? "null" : file.getPath()), e1);
	            e1.printStackTrace();
	        }
            
            saveType = 0;
            
            return true;
        }

        else
        	return false; 
    }


    /**
     * Looks at the clock listener list and checks if each listener has already had a corresponding task in the clock listener task list.
     */
    public void setupClockListenerTask() {
		clockListeners.forEach(t -> {
			// Check if it has a corresponding task or not, if it doesn't, create a task for t
			addClockListenerTask(t);
		});
    }

    public double getRefresh() {
    	return refresh;
    }
    
    /**
     * Prepares clock listener tasks for setting up threads.
     */
	public class ClockListenerTask implements Runnable {

		private double time;
		private ClockListener listener;

		public ClockListener getClockListener() {
			return listener;
		}

		private ClockListenerTask(ClockListener listener) {
			this.listener = listener;
		}

		public void insertTime(double time) {
			this.time = time;
		}

		@Override
		public void run() {
			try {

  		  		// The most important job for CLockListener is to send a clock pulse to Simulation's clockPulse()
  		  		// so that UpTimer, Mars, UnitManager, ScientificStudyManager, TransportManager gets updated.
				listener.clockPulse(time);
				refresh = PERIOD_IN_MILLISOLS * time;
	  		  	timeCache += time;
				if (timeCache > refresh) {
					// The secondary job of CLockListener is to send uiPulse() out to MainDesktopPane,
					// which in terms sends a clock pulse out to update all unit windows and tool windows
					//
					// It also sends an ui pulse out to the following class and map related panels:
					// 1. SettlementMapPanel 
					// 2. ArrivingSettlementDetailPanel
					// 3. GlobeDisplay
					// 4. MapPanel
					// 5. ResupplyDetailPanel
					// 6. Telegraph
					// 7. TimeWindow
					// 8. EventTableModel
					// 9. NotificationWindow
					listener.uiPulse(time);
		  			timeCache = 0;
				}
			
			} catch (ConcurrentModificationException e) {
				e.printStackTrace();
			}
		}
	}


    /**
     * Fires the clock pulse to each clock listener
     */
	public void fireClockPulse(double time) {
		for (ClockListenerTask task : clockListenerTasks) {
	  		if (task != null) {
  		  		task.insertTime(time);		  		
		  		clockListenerExecutor.execute(task);
	  		}
	  		
	  		else
	  			return;
        }
    }

    /**
     * Stop the clock
     */
	// called by stop() in Simulation.java
    public void stop() {
        keepRunning = false;
    }

    public void restart() {
        keepRunning = true;
    }

    /**
     * Set if the simulation is paused or not.
     *
     * @param isPaused true if simulation is paused.
     */
    public void setPaused(boolean isPaused, boolean showPane) {
        //logger.info("MasterClock's setPaused() is on " + Thread.currentThread().getName());
    	//System.out.println("MasterClock : calling setPaused()");
        uptimer.setPaused(isPaused);
        
        if (isPaused
        	&& sim.getAutosaveTimer() != null
        	&& !sim.getAutosaveTimer().isShutdown()
            && !sim.getAutosaveTimer().isTerminated()
            ) {
        		sim.getAutosaveTimer().shutdown();//.pause(); // note: using sim (instead of Simulation.instance()) won't work when loading a saved sim.
        }
        else
			sim.startAutosaveTimer();//getAutosaveTimer().restart();//.play();
        
    	//if (isPaused) System.out.println("MasterClock.java : setPaused() : isPause is true");
        this.isPaused = isPaused;
        // Fire pause change to all clock listeners.
        firePauseChange(showPane);
    }

    /**
     * Checks if the simulation is paused or not.
     *
     * @return true if paused.
     */
    public boolean isPaused() {
    	//System.out.println("MasterClock : isPause is " + isPaused);
        return isPaused;
    }

    /**
     * Send a pulse change event to all clock listeners.
     */
    public void firePauseChange(boolean showPane) {

        clockListeners.forEach(cl -> cl.pauseChange(isPaused, showPane));
/*
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
*/
    }

    public double getPulsesPerSecond() {
        //System.out.println("totalPulses : " + Math.round(totalPulses*100.0)/100.0 
        //		+ "    uptimeMillis: " + Math.round(uptimer.getUptimeMillis()*100.0)/100.0
        //		+ "    tps: " + 1000L * totalPulses / uptimer.getUptimeMillis());
        return 1000.0 / elapsedLast * totalPulses ;//uptimer.getUptimeMillis();
    }


    /**
     * Update the milliseconds elapsed since last time pulse.
     */
    private long updateElapsedMilliseconds() {
    	if (uptimer == null) {
    		uptimer = new UpTimer(this);
    	}
        long tnow = uptimer.getUptimeMillis();
        long elapsedMilliseconds = tnow - elapsedLast;
        elapsedLast = tnow;
        return elapsedMilliseconds;
    }


 

    /**
     * Starts clock listener thread pool executor
     */
    public void startClockListenerExecutor() {
    	//if ( clockListenerExecutor.isTerminated() || clockListenerExecutor.isShutdown() )
    	if (clockListenerExecutor == null)
    		//clockListenerExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    		clockListenerExecutor = Executors.newSingleThreadExecutor();

    }

    /**
     * Shuts down clock listener thread pool executor
     */
    public void endClockListenerExecutor() {
    	if (clockListenerExecutor != null)
    		clockListenerExecutor.shutdownNow();
    }


	//public ThreadPoolExecutor getClockListenerExecutor() {
	//	return clockListenerExecutor;
	//}

    // To be called by TransportWizard and ConstructionWizard
	public ExecutorService getClockListenerExecutor() {
		return clockListenerExecutor;
	}
	
//	public long getDiffCache() {
//		return diffCache;
//	}

	public double getFPS() {		
//		List<Double> list = new ArrayList<>(TPFList);
//		double sum = 0;
//		int size = list.size();
//		for (int i = 0; i < size; i++) {
//			sum += list.get(i);
//		}
//		return size/sum;
		return Math.round(10.0/adjustedTBU_s)/10.0;//1_000_000/tbu_ns;
	}
	
    /**
     * Sends out a clock pulse if using FXGL
     */
    public void onUpdate(double tpf) {
//        logger.info("MasterClock onUpdate() is on " + Thread.currentThread().getName() + " Thread");
    	if (!isPaused) {
	    	tpfCache += tpf;
	    	//System.out.println("tpfCache: " + Math.round(tpfCache *1000.0)/1000.0 
	    	//		+ "   tpf: " + Math.round(tpf *1000.0)/1000.0 
	    	//		+ "   cal_tbu_s : " + Math.round(cal_tbu_s *1000.0)/1000.0);
	    	if (tpfCache >= adjustedTBU_s) {
/*   		
	    		TPFList.add(tpfCache);
	    		// Remove the first 5 
	    		if (TPFList.size() > 20) {
	    			List<Double> list = new ArrayList<>(TPFList);
	    			for (int i = 0; i < 5; i++) {
	    				list.remove(i);
	    			}
	    			TPFList = list;
	    		}
*/   		
	        //elapsedLast = uptimer.getUptimeMillis();
    		//	System.out.println("tpfCache >= default_tbu_ms");
    		//if (isPaused) {
    			//resetTotalPulses();
    		//}
    		//else {
		        double t = tpfCache * currentTR;
		        // Get the time pulse length in millisols.
		        double timePulse = t / MarsClock.SECONDS_PER_MILLISOL;
	        	//System.out.println("tpfCache : " + Math.round(tpfCache *1000.0)/1000.0 
	        	//		+ "   tpfCache * timeRatio : " + Math.round(t *1000.0)/1000.0 
	        	//		+ "   elapsedLast : " + Math.round(elapsedLast *1000.0)/1000.0
	        	//		+ "   timePulse : " + Math.round(timePulse *1000.0)/1000.0);
	        	// tpfCache : 0.117   tpfCache * timeRatio : 14.933   elapsedLast : 9315.0(inc)   timePulse : 0.168
	        	
		        // Incrementing total time pulse number.
		        //totalPulses++;
		        //if (totalPulses > 100_000) resetTotalPulses();
		        //System.out.println(totalPulses);
		        if (timePulse > 0
		        	&& keepRunning
		        	&& !isPaused
		            //|| !clockListenerExecutor.isTerminating()
		        	&& clockListenerExecutor != null
		        	&& !clockListenerExecutor.isTerminated()
		        	&& !clockListenerExecutor.isShutdown()) {
		            	//logger.info(millis + "");
		                // Add time pulse length to Earth and Mars clocks.
		        		//System.out.println(Math.round(timePulse *10000.0)/10000.0);
		            	earthClock.addTime(SEC_TO_MILLIS * t);//millis*timeRatio);
		            	marsTime.addTime(timePulse);
		            	fireClockPulse(timePulse);
		        }
		        
		        // Set tpfCache back to zero
		        tpfCache = 0;
	        }
   
	      	if (saveType != 0) {
	            try {
	                sim.saveSimulation(saveType, file);
	            } catch (IOException e) {
	                logger.log(Level.SEVERE, "Could not save the simulation as "
	                        + (file == null ? "null" : file.getPath()), e);
	                e.printStackTrace();
	            }

	            saveType = 0;
	        }

	        // Exit program if exitProgram flag is true.
	        if (exitProgram) {
	        	if (sim.getAutosaveTimer() != null)
	        		sim.getAutosaveTimer().shutdownNow();//.stop();
	            System.exit(0);
	        }
	        

    	}
 
    }
    
    /**
     * Prepare object for garbage collection.
     */
	public void destroy() {
		config = null;
    	sim = null;
    	marsTime = null;
    	initialMarsTime = null;
    	earthClock = null;
    	uptimer = null;
    	clockThreadTask = null;
    	clockListenerExecutor = null;
    	file = null;
    	
    	clockListeners = null;
    	clockListenerExecutor = null;
    }
}