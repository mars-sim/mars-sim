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
 * Mars. mars-sim has only one master clock and it delivers a clock pulse 
 * per frame.  All units are synchronized with this clock pulse.
 */
public class MasterClock implements Serializable {

	/** default serial id. */
	static final long serialVersionUID = -1688463735489226493L;

	/** Initialized logger. */
	private static Logger logger = Logger.getLogger(MasterClock.class.getName());

	private static final String	HOURS = "h ";
	private static final String	MINUTES = "m ";
	private static final String	ZERO_MINUTES = "00m ";
	private static final String	SECONDS = "s";

	private static final int SEC_TO_MILLIS = 1000;
	
	// Data members
	/** Runnable flag. */
	private transient volatile boolean keepRunning = true;
	/** Pausing clock. */
	private transient volatile boolean isPaused = false;
	/** Simulation time ratio. */
	private volatile double current_TR = 0D;
	/** The Current time between updates (TBU). */
	private volatile long current_tbu_ns = 0L;
	/** Default time ratio. */
	private volatile double cal_tr = 0D;
	/** Default time between updates in nanoseconds. */
	private volatile double cal_tbu_ns = 0D;
	/** Default time between updates in milliseconds. */
	private volatile double cal_tbu_ms = 0;
	/** Default time between updates in seconds. */
	private volatile double cal_tbu_s = 0;
	/** Default frame per sec */
	private volatile double cal_fps = 0;
	
	private int noDelaysPerYield = 0;
	private int maxFrameSkips = 0;
	private int count = -100;

	private double tpfCache = 0;
	
	/** Mode for saving a simulation. */
	private transient volatile int saveType;
	/** Flag for ending the simulation program. */
	private transient volatile boolean exitProgram;
	/** Flag for getting ready for autosaving. */
	private transient volatile boolean autosave;

	private long totalPulses = 1;
	private long t2Cache = 0;
	private long diffCache = 0;
	private transient long elapsedLast;

	/** Clock listeners. */
	private transient List<ClockListener> clockListeners;
	private transient List<ClockListenerTask> clockListenerTasks =  new CopyOnWriteArrayList<>();

	//private double time_ratio;
	private boolean isFXGL = false;
	
	/** Martian Clock. */
	private MarsClock marsTime;
	/** Initial Martian time. */
	private static MarsClock initialMarsTime;
	/** Earth Clock. */
	private EarthClock earthTime;
	/** Uptime Timer. */
	private UpTimer uptimer;
	/** The file to save or load the simulation. */
	private transient volatile File file;

	private ClockThreadTask clockThreadTask;

	//private transient ThreadPoolExecutor clockListenerExecutor;
	private transient ExecutorService clockListenerExecutor;
	
	private static Simulation sim;
	
	private static SimulationConfig config;

	//private List<Double> TPFList = new ArrayList<>();
	
    /**
     * Constructor
     *
     * @throws Exception if clock could not be constructed.
     */
    public MasterClock(boolean isFXGL, int userTimeRatio) {
    	this.isFXGL = isFXGL;
        //logger.info("MasterClock's constructor is on " + Thread.currentThread().getName() + " Thread");

    	sim = Simulation.instance();
        // Initialize data members
        config = SimulationConfig.instance();

        // Create a Martian clock
        marsTime = new MarsClock(config.getMarsStartDateTime());
        
        initialMarsTime = (MarsClock) marsTime.clone();

        // Create an Earth clock
        earthTime = new EarthClock(config.getEarthStartDateTime());

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
        	config.getTimeRatio();
        else {
        	tr = userTimeRatio;
        	logger.info("User-Defined Time Ratio is " + (int)tr + "x");
        }
        double tbu = config.getTimeBetweenUpdates();

        int threads = Simulation.NUM_THREADS;
     
        // Tune the time between update
        if (threads <= 32) {
        	cal_tbu_ms =  12D / (Math.sqrt(threads) *2) * tbu;     	
        }
        else {
        	cal_tbu_ms = 1.5 * tbu;
        }
    	
        // Tune the time ratio
        if (threads == 1) {
        	cal_tr = tr/16D;
        }
        else if (threads == 2) {
        	cal_tr = tr/8D;
        }
        else if (threads <= 3) {
        	cal_tr = tr/4D;
        }
        else if (threads <= 4) {
           	cal_tr = tr/4D;
        }
        else if (threads <= 6) {
        	cal_tr = tr/2D;
        }
        else if (threads <= 8) {
        	cal_tr = tr/2D;
        }
        else if (threads <= 12) {
        	cal_tr = tr;
        }
        else if (threads <= 16) {
        	cal_tr = tr;
        }
        else {
        	cal_tr = tr;
        }

        cal_tbu_ns = cal_tbu_ms * 1_000_000.0; // convert from millis to nano
     	cal_tbu_s = cal_tbu_ms / 1_000.0;
     	cal_fps = 1.0/cal_tbu_s;
        current_tbu_ns = (long) cal_tbu_ns;

    	current_TR = cal_tr;
    	
        // Added loading the values below from SimulationConfig
        setNoDelaysPerYield(config.getNoDelaysPerYield());
        setMaxFrameSkips(config.getMaxFrameSkips());

        logger.info("Based on # CPU cores/threads, we re-adjust the parameters as follows :");
        logger.info("Time Ratio (TR) : " + (int)cal_tr + "x");
        logger.info("Time between Updates (TBU) : " + Math.round(cal_tbu_ms * 10D)/10D + " ms");
        logger.info("Ticks Per Second (TPS) : " + Math.round(cal_fps*10D)/10D + " Hz");
		logger.info("*** Welcome to Mars and the beginning of the new adventure of humankind. ***");
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
    	return computeTimePulseInSeconds(elapsedMilliseconds)/MarsClock.SECONDS_IN_MILLISOL;
    }

    /**
     * Computes the time pulse in seconds. It varies, depending on the time ratio
     * @return time pulse length in seconds
     */
    public double computeTimePulseInSeconds(long elapsedMilliseconds) {
    	return elapsedMilliseconds * current_TR / 1000D;
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
		totalPulses = (long) (1D/cal_tbu_ms * uptimer.getLastUptime());
	}

    /**
     * Sets the simulation time ratio and adjust the value of time between update (TBU)
     * @param ratio
     */
    public void setTimeRatio(double ratio) {
        if (ratio >= 1D && ratio <= 65536D) {

        	if (ratio > current_TR)
        		current_tbu_ns = (long) (current_tbu_ns * 1.0025); // increment by .5%
        	else
        		current_tbu_ns = (long) (current_tbu_ns * .9975); // decrement by .5%

        	cal_tbu_s = current_tbu_ns/1_000_000_000D;
            current_TR = ratio;
        }
        else throw new IllegalArgumentException("Time ratio is out of bounds ");
    }


    /**
     * Gets the simulation time ratio.
     * @return ratio
     */
    public double getTimeRatio() {
        return current_TR;
    }

    /**
     * Gets the default simulation time ratio.
     *
     * @return ratio
     */
    public double getCalculatedTimeRatio() {
        return cal_tr;
    }


    /**
     * Gets the current time between update (TBU)
     * @return value in nanoseconds
     */
    public long getCurrentTBU() {
        return current_tbu_ns;
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

	 		        // Benchmark CPU speed
	 		        long diff = 0;

	 		        if (count >= 1000)
	 		        	count = 0;

	 		        if (count >= 0) {
	 			        diff = (long) ((t2 - t2Cache) / 1_000_000D);
	 		        	diffCache = (diff * count + diffCache)/(count + 1);
	 		        }

	 		        //if (count == 0) logger.info("Benchmarking this machine : " + diff + " per 1000 frames");

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
	 			            	earthTime.addTime(millis * current_TR);
	 			            	marsTime.addTime(timePulse);
	 						  	fireClockPulse(timePulse);
	 		            }   
	 		        }
	 		        
	 	            //dt = t2 - t1;
	 	            sleepTime = current_tbu_ns - t2 + t1 - overSleepTime;
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

	 	            t1 = System.nanoTime();

	 	            // Skip the sleep time if the statusUpdate() or other processes take too long
	 	            int skips = 0;

	 	            while ((excess > current_tbu_ns) && (skips < maxFrameSkips)) {
	 	            	excess -= current_tbu_ns;
	 	            	//logger.warning("Making up a lost frame by calling statusUpdate() again. skips :" + skips);
	 	            	// Make up a lost frame by calling addTime() in MarsClock and EarthClock via statusUpdate()
	 	            	skips++;

	 	            	if (skips >= maxFrameSkips) {
	 		            	logger.info("# of skips has reached the maximum # of frame skips. Resetting total pulse and slowing down (TBU).");
	 		            	resetTotalPulses();
	 		            	if (current_tbu_ns > (long) (cal_tbu_ns * 1.25))
	 		            		current_tbu_ns = (long) (cal_tbu_ns * 1.25);
	 		            	else
	 		            		current_tbu_ns = (long) (current_tbu_ns * .9925); // decrement by 2.5%
	 	            	}
	 	            	
	 	            }

 	            	checkSave();
 	            	
 	               // Exit program if exitProgram flag is true.
 	               if (exitProgram) {
 	            	   if (sim.getAutosaveTimer() != null)
 	            		   sim.getAutosaveTimer().shutdownNow();//.stop();
 	              
 	            	   System.exit(0);
 	               }
 	               
	 	            // Set excess to zero to prevent getting stuck in the above while loop after waking up from power saving
	 	            excess = 0;

	 		        t2Cache = t2;

	 		        count++;

	 	        } // end of while
	        } // if fxgl is not used
	    } // end of run
    }

    /**
     * Checks if it is on pause or a saving process has been requested. Keeps track of the time pulse 
     */
    private void checkSave() {
        //logger.info("MasterClock's statusUpdate() is on " + Thread.currentThread().getName() + " Thread");
    	
        if (saveType != 0) {
            try {
            	logger.info("MasterClock's statusUpdate() is on " + Thread.currentThread().getName() + " Thread");
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
        }
        

        
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

		public void addTime(double time) {
			this.time = time;
		}

		@Override
		public void run() {
			try {
				listener.clockPulse(time);
			} catch (ConcurrentModificationException e) {}
		}
	}


    /**
     * Fires the clock pulse to each clock listener
     */
	public void fireClockPulse(double time) {
		for (ClockListenerTask task : clockListenerTasks) {
	  		if (task != null) {
  		  		task.addTime(time);
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


    public static final int secspmin = 60, secsphour = 3600, secspday = 86400, secsperyear = 31536000;


    /**
     * Returns a date time string in HHh MMm SS.SSs format
     * @param ratio
     * @return a date time string
     */
    public String getTimeString(double ratio) {

        //long years = (int) Math.floor(seconds / secsperyear);
        //long days = (int) ((seconds % secsperyear) / secspday);
        int hours = (int) ((ratio % secspday) / secsphour);
        int minutes = (int) ((ratio % secsphour) / secspmin);
        double secs = (ratio % secspmin);

        StringBuilder b = new StringBuilder();
/*
        b.append(years);
        if(years>0){
            b.append("yr:");
        }

        if (days > 0) {
            b.append(String.format("%03d", days)).append("mon:");
        } else {
            b.append("0mon:");
        }
*/
        if (hours > 0) {
            b.append(String.format("%02d", hours)).append(HOURS);
        }
        //} else {
        //    b.append("00h ");
        //}

        if (minutes > 0) {
            b.append(String.format("%02d", minutes)).append(MINUTES);
        } else {
            b.append(ZERO_MINUTES);
        }

        //b.append(String.format("%5.3f", secs));
        b.append(String.format("%05.2f", secs) + SECONDS);

        return b.toString();
    }

    /**
     * Returns a truncated string in HHh MMm SSs format
     * @param ratio
     * @return a date time string
     */
    public String getTimeTruncated(double ratio) {

        //long years = (int) Math.floor(seconds / secsperyear);
        //long days = (int) ((seconds % secsperyear) / secspday);
        int hours = (int) ((ratio % secspday) / secsphour);
        int minutes = (int) ((ratio % secsphour) / secspmin);
        double secs = (ratio % secspmin);

        StringBuilder b = new StringBuilder();
/*
        b.append(years);
        if(years>0){
            b.append("yr:");
        }

        if (days > 0) {
            b.append(String.format("%03d", days)).append("mon:");
        } else {
            b.append("0mon:");
        }
*/
        if (hours > 0) {
            b.append(String.format("%02d", hours)).append(HOURS);
        }
        //} else {
        //    b.append("00h ");
        //}

        if (minutes > 0) {
            b.append(String.format("%02d", minutes)).append(MINUTES);
        } else {
            b.append(ZERO_MINUTES);
        }

        //b.append(String.format("%5.3f", secs));
        b.append(String.format("%02.0f", secs) + SECONDS);

        return b.toString();
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
	
	public long getDiffCache() {
		return diffCache;
	}

	public double getFPS() {
/*		
		List<Double> list = new ArrayList<>(TPFList);
		double sum = 0;
		int size = list.size();
		for (int i = 0; i < size; i++) {
			sum += list.get(i);
		}
		return size/sum;
*/
		return Math.round(10.0/cal_tbu_s)/10.0;//1_000_000/tbu_ns;
	}
	
    public void onUpdate(double tpf) {
//        logger.info("MasterClock onUpdate() is on " + Thread.currentThread().getName() + " Thread");
    	if (!isPaused) {
	    	tpfCache += tpf;
	    	//System.out.println("tpfCache: " + Math.round(tpfCache *1000.0)/1000.0 
	    	//		+ "   tpf: " + Math.round(tpf *1000.0)/1000.0 
	    	//		+ "   cal_tbu_s : " + Math.round(cal_tbu_s *1000.0)/1000.0);
	    	if (tpfCache >= cal_tbu_s) {
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
		        double t = tpfCache * current_TR;
		        // Get the time pulse length in millisols.
		        double timePulse = t / MarsClock.SECONDS_IN_MILLISOL;
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
		            	earthTime.addTime(SEC_TO_MILLIS * t);//millis*timeRatio);
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
    	earthTime = null;
    	uptimer = null;
    	clockThreadTask = null;
    	clockListenerExecutor = null;
    	file = null;
    	
    	clockListeners = null;
    	clockListenerExecutor = null;
    }
}