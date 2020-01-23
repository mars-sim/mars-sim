/**
 * Mars Simulation Project
 * MasterClock.java
 * @version 3.1.0 2017-01-12
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Simulation.SaveType;

/**
 * The MasterClock represents the simulated time clock on virtual Mars and
 * delivers a clock pulse for each frame.
 */
public class MasterClock implements Serializable {

	/** default serial id. */
	static final long serialVersionUID = 1L;

	/** Initialized logger. */
	private static Logger logger = Logger.getLogger(MasterClock.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	private static final int FACTOR = 4;
	
//	private static final double SMALL_NUMBER = 0.0028;
	
	/** The number of milliseconds for each millisols.  */	
	private static final double MILLISECONDS_PER_MILLISOL = MarsClock.SECONDS_PER_MILLISOL * 1000.0;
	/** The number of econds for each millisols.  */	
	private static final double SECONDS_PER_MILLISOL = MarsClock.SECONDS_PER_MILLISOL;
	
	/** (For Command Mode) the maximum number of millisols for pausing. */
	private static final double MAX_SOLS = 7_000;
	
	
	// Data members
	/** Runnable flag. */
	private transient volatile boolean keepRunning = false;
	/** Pausing clock. */
	private transient volatile boolean isPaused = false;
	/** Flag for ending the simulation program. */
	private transient volatile boolean exitProgram;
	/** Flag for getting ready for autosaving. */
//	private transient volatile boolean autosave;
	/** Mode for saving a simulation. */
	private transient volatile SaveType saveType = SaveType.NONE;
//	private AtomicInteger saveType;
	
	/** The Current time between updates (TBU). */
	private volatile long currentTBU_ns = 0L;
	/** Simulation time ratio. */
	private volatile int currentTR = 0;
	/** Adjusted time ratio. */
	private volatile int baseTR = 0;
	/** Adjusted time between updates in nanoseconds. */
	private volatile double baseTBU_ns = 0D;
	/** Adjusted time between updates in milliseconds. */
	private volatile double baseTBU_ms = 0;
	/** Adjusted time between updates in seconds. */
	private volatile double baseTBU_s = 0;
	/** Adjusted frame per sec */
	private volatile double baseFPS = 0;
	/** The pulse per seconds */
	private volatile double pps = 0;
	
	/** The time taken to execute one frame in the game loop */
	private volatile long executionTime;	
	/** The sleep time */
	private long sleepTime;
	/** The residual time */
	private long residualTime;
	/** The last uptime in terms of number of pulses. */
	private transient long tLast;
	/** The cache for accumulating millisols up to a limit before sending out a clock pulse. */
	private transient double timeCache;

	private static boolean justReloaded = false;
	
//	/** The time between two ui pulses. */	
//	private float pulseTime = .5F;
	/** The counts for ui pulses. */	
	private transient int count;
	/** The total number of counts between two ui pulses. */
	private transient int totalCount = 40;
	/** The average of the last working millis and the current one. */
//	private long millisCache;
	
	/** Is FXGL is in use. */
	public boolean isFXGL = false;
	/** Is pausing millisol in use. */
	public boolean canPauseTime = false;

	/** The maximum number of counts allowed in waiting for other threads to execute. */
	private int noDelaysPerYield = 0;
	/** The measure of tolerance of the maximum number of lost frames for saving a simulation. */
	private int maxFrameSkips = 0;
	
	/** The total number of pulses cumulated. */
	private long totalPulses = 1;
	/** The cache for the last nano time of an ui pulse. */	
	private long t01 = 0;
	
	/** Mode for saving a simulation. */
	private double tpfCache = 0;
	/** The total amount of millisols for Commander Mode. */
	private double millisols = 0;
	/** For Command Mode, the sim will pause every x millisols. */
	private double pausingMillisols = 1000;

	/** The file to save or load the simulation. */
	private transient volatile File file;
	/** The thread for running the clock listeners. */
	private transient ExecutorService clockExecutor;
	
	/** A list of clock listeners. */
	private transient List<ClockListener> clockListeners;
	/** A list of clock listener tasks. */
	private transient List<ClockListenerTask> clockListenerTasks;
	/** A list of past UI refresh rate. */
	private static List<Float> timeIntervals;
	
	
	/** The martian Clock. */
	private MarsClock marsClock;
	/** A copy of the initial martian clock at the start of the sim. */
	private MarsClock initialMarsTime;
	/** The Earth Clock. */
	private EarthClock earthClock;
	/** The Uptime Timer. */
	private UpTimer uptimer;
	/** The thread for running the game loop. */
	private ClockThreadTask clockThreadTask;

	// Note: ExecutorService may not stop after the program exits.
	// see https://netopyr.com/2017/03/13/surprising-behavior-of-cached-thread-pool/

	private static Simulation sim = Simulation.instance();
	private static SimulationConfig simulationConfig;

	/**
	 * Constructor
	 * 
	 * @param isFXGL        true if FXGL is used for generating clock pulse
	 * @param userTimeRatio the time ratio defined by user
	 * @throws Exception if clock could not be constructed.
	 */
	public MasterClock(boolean isFXGL, int userTimeRatio) {
		this.isFXGL = isFXGL;
		// logger.config("MasterClock's constructor is on " + Thread.currentThread().getName() + " Thread");
		
		// Gets an instance of the SimulationConfig singleton 
		simulationConfig = SimulationConfig.instance();

		// Create a martian clock
		marsClock = new MarsClock(simulationConfig.getMarsStartDateTime());
		// Save a copy of the initial mars time
		initialMarsTime = (MarsClock) marsClock.clone();
		
//		testNewMarsLandingDayTime();

		// Create an Earth clock
		earthClock = new EarthClock(simulationConfig.getEarthStartDateTime());

		// Create an Uptime Timer
		uptimer = new UpTimer(this);

		// Create listener list.
		clockListeners = Collections.synchronizedList(new CopyOnWriteArrayList<ClockListener>());
		timeIntervals = new CopyOnWriteArrayList<>();
		
		// Calculate elapsedLast
		tLast = uptimer.getUptimeMillis();

		// Check if FXGL is used
		if (!isFXGL)
			clockThreadTask = new ClockThreadTask();

		logger.config("   -------------------------------------------");
		
		// Setting the initial time ratio.
		double tr = 0;
		if (userTimeRatio == -1)
			tr = simulationConfig.getTimeRatio();
		else {
			tr = userTimeRatio;
			logger.config("   User-Defined Time Ratio (TR) : " + (int) tr + "x");
		}
		
		// Gets the time between updates
		double tbu = simulationConfig.getTimeBetweenUpdates();

		// Gets the machine's # of threads
		int threads = Simulation.NUM_THREADS;

		// Tune the time between update
		if (threads <= 32) {
			baseTBU_ms = 12D / (Math.sqrt(threads) * 2) * tbu;
		} else {
			baseTBU_ms = 1.5 * tbu;
		}

		// Tune the time ratio
		if (threads == 1) {
			baseTR = (int) (tr / 32D);
		} else if (threads == 2) {
			baseTR = (int) (tr / 16D);
		} else if (threads <= 3) {
			baseTR = (int) (tr / 8D);
		} else if (threads <= 4) {
			baseTR = (int) (tr / 6D);
		} else if (threads <= 6) {
			baseTR = (int) (tr / 4D);
		} else if (threads <= 8) {
			baseTR = (int) (tr / 2D);
		} else if (threads <= 12) {
			baseTR = (int) tr;
		} else if (threads <= 16) {
			baseTR = (int) (tr * 2D);
		} else {
			baseTR = (int) (tr * 4D);
		}

		baseTBU_ns = baseTBU_ms * 1_000_000.0; // convert from millis to nano
		baseTBU_s = baseTBU_ms / 1_000.0;
		baseFPS = 1.0 / baseTBU_s;
		
		// Set the current TBU
		currentTBU_ns = (long) baseTBU_ns;

		// Set the current time ratio
		currentTR = baseTR;

		// Added loading the values below from SimulationConfig
		setNoDelaysPerYield(simulationConfig.getNoDelaysPerYield());
		setMaxFrameSkips(simulationConfig.getMaxFrameSkips());

//		logger.config("Based on # CPU cores/threads, the following parameters have been re-adjusted as follows :");
		logger.config("            Base Time Ratio (TR) : " + (int) baseTR + "x");
		logger.config("     Base Ticks Per Second (TPS) : " + Math.round(baseFPS * 100D) / 100D + " Hz");
		logger.config(" Base Time between Updates (TBU) : " + Math.round(baseTBU_ms * 100D) / 100D + " ms");
		logger.config("   -------------------------------------------");
//		logger.config(" - - - Welcome to Mars - - -");
	}

	public void testNewMarsLandingDayTime() {
		// Create an Earth clock
		EarthClock c = new EarthClock("1609-03-12 19:19:06.000"); 
		// "2004-01-04 00:00:00.000"
		// "2004-01-03 13:46:31.000" degW = 84.702 , <-- used this
		// "2000-01-06 00:00:00.000" degW = 0 ,

		// 0015-Adir-01 corresponds to Wednesday, September 30, 2043 at 12:00:00 AM
		// Coordinated Universal Time
		// "2043-09-30 00:00:00.000"

		// 0000-Adir-01 corresponds to Tuesday, July 14, 2015 at 9:53:18 AM Coordinated
		// Universal Time
		// "2015-07-14 09:53:18.0"

		// "2028-08-17 15:23:13.740"

		// Use the EarthClock instance c from above for the computation below :
//		ClockUtils.getFirstLandingDateTime();

		double millis = EarthClock.getMillis(c);
		logger.config("millis is " + millis);
		double jdut = ClockUtils.getJulianDateUT(c);
		logger.config("jdut is " + jdut);
		double T = ClockUtils.getT(c);
		logger.config("T is " + T);
		double TT2UTC = ClockUtils.getDeltaUTC_TT(c);
		logger.config("TT2UTC is " + TT2UTC);
		double jdtt = ClockUtils.getJulianDateTT(c);
		logger.config("jdtt is " + jdtt);
		double j2k = ClockUtils.getDaysSinceJ2kEpoch(c);
		logger.config("j2k is " + j2k);
		double M = ClockUtils.getMarsMeanAnomaly(c) % 360;
		logger.config("M is " + M);
		double alpha = ClockUtils.getAlphaFMS(c) % 360;
		logger.config("alpha is " + alpha);
		double PBS = ClockUtils.getPBS(c) % 360;
		logger.config("PBS is " + PBS);
		double EOC = ClockUtils.getEOC(c) % 360;
		logger.config("EOC is " + EOC);
		double v = ClockUtils.getTrueAnomaly0(c) % 360;
		logger.config("v is " + v);
		double L_s = ClockUtils.getLs(c) % 360;
		logger.config("L_s is " + L_s);

		double EOT = ClockUtils.getEOTDegree(c);
		logger.config("EOTDegree is " + EOT);
		double EOT_hr = ClockUtils.getEOTHour(c);
		logger.config("EOTHour is " + EOT_hr);
		String EOT_Str = ClockUtils.getFormattedTimeString(EOT_hr);
		logger.config("EOT_Str is " + EOT_Str);

		double MTC0 = ClockUtils.getMTC0(c);
		logger.config("MTC0 is " + MTC0);
		double MTC1 = ClockUtils.getMTC1(c);
		logger.config("MTC1 is " + MTC1);
		String MTCStr = ClockUtils.getFormattedTimeString(MTC1);
		logger.config("MTC_Str is " + MTCStr);

		String millisols = ClockUtils.getFormattedMillisolString(MTC1);
		logger.config("millisols is " + millisols);
		
		double LMST = ClockUtils.getLMST(c, 0);// 184.702);
		logger.config("LMST is " + LMST);
		String LMST_Str = ClockUtils.getFormattedTimeString(LMST);
		logger.config("LMST_Str is " + LMST_Str);

		double LTST = ClockUtils.getLTST(c, 0);// 184.702);
		logger.config("LTST is " + LTST);
		String LTST_Str = ClockUtils.getFormattedTimeString(LTST);
		logger.config("LTST_Str is " + LTST_Str);

		double sl = ClockUtils.getSubsolarLongitude(c);
		logger.config("sl is " + sl);

		double r = ClockUtils.getHeliocentricDistance(c);
		logger.config("r is " + r);
	}

	/**
	 * Returns the Martian clock
	 *
	 * @return Martian clock instance
	 */
	public MarsClock getMarsClock() {
		return marsClock;
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
	 * 
	 * @param newListener the listener to add.
	 */
	public final void addClockListener(ClockListener newListener) {
		// if listeners list does not exist, create one
		if (clockListeners == null)
			clockListeners = Collections.synchronizedList(new CopyOnWriteArrayList<ClockListener>());
		// if the listeners list does not contain newListener, add it to the list
		if (!clockListeners.contains(newListener))
			clockListeners.add(newListener);
		// will check if clockListenerTaskList already contain the newListener's task,
		// if it doesn't, create one
		addClockListenerTask(newListener);
	}

	/**
	 * Removes a clock listener
	 * 
	 * @param oldListener the listener to remove.
	 */
	public final void removeClockListener(ClockListener oldListener) {
//		if (clockListeners == null)
//			clockListeners = Collections.synchronizedList(new CopyOnWriteArrayList<ClockListener>());
		if (clockListeners != null && clockListeners.contains(oldListener))
			clockListeners.remove(oldListener);
//		 logger.config("just called clockListeners.remove(oldListener)");
		// Check if clockListenerTaskList contain the newListener's task, if it does,
		// delete it
		ClockListenerTask task = retrieveClockListenerTask(oldListener);
//		 logger.config("just get task");
		if (task != null)
			clockListenerTasks.remove(task);
	}

	/**
	 * Adds a clock listener task
	 *
	 * @param newListener the clock listener task to add.
	 */
	public void addClockListenerTask(ClockListener listener) {
		boolean hasIt = false;
		if (clockListenerTasks == null)
			clockListenerTasks = new CopyOnWriteArrayList<ClockListenerTask>();
		Iterator<ClockListenerTask> i = clockListenerTasks.iterator();
		while (i.hasNext()) {
			ClockListenerTask c = i.next();
			if (c.getClockListener().equals(listener))
				hasIt = true;
		}
		if (!hasIt) {
			ClockListenerTask clt = new ClockListenerTask(listener);
			clockListenerTasks.add(clt);
//			logger.config(clt.getClockListener().getClass().getSimpleName() + "'s clock listener added.");
		}
	}

	/**
	 * Retrieve the clock listener task instance, given its clock listener
	 * 
	 * @param listener the clock listener
	 */
	public ClockListenerTask retrieveClockListenerTask(ClockListener listener) {	
		if (clockListenerTasks != null) {
			Iterator<ClockListenerTask> i = clockListenerTasks.iterator();
			while (i.hasNext()) {
				ClockListenerTask c = i.next();
				if (c.getClockListener().equals(listener))
					return c;
			}
		}
		return null;
	}

	/**
	 * Sets the load simulation flag and the file to load from.
	 *
	 * @param file the file to load from.
	 */
	public void loadSimulation(File file) {
		this.setPaused(false, false);
		this.file = file;
	}

	/**
	 * Sets the save simulation flag and the file to save to.
	 * 
	 * @param file save to file or null if default file.
	 */
	public void setSaveSim(SaveType type, File file) {
		saveType = type;
		this.file = file;
//		logger.config("setSaveSim(" + type + ", " + file + ");  saveType is " + saveType);
	}

//	/**
//	 * Sets the value of autosave
//	 * 
//	 * @param value
//	 */
//	public void setAutosave(boolean value) {
//		autosave = value;
//	}

//	/**
//	 * Gets the value of autosave
//	 * 
//	 * @return autosave
//	 */
//	public boolean getAutosave() {
//		return autosave;
//	}

	/**
	 * Checks if in the process of saving a simulation.
	 * 
	 * @return true if saving simulation.
	 */
	public boolean isSavingSimulation() {
		if (saveType == SaveType.NONE)
			return false;
		else
			return true;
	}

	public void setSaveType() {
		saveType = SaveType.NONE;
	}
	
	/**
	 * Sets the exit program flag.
	 */
	public void exitProgram() {
		this.setPaused(true, false);
		exitProgram = true;
	}

	/**
	 * Computes the time pulse in millisols in other words, the number of realworld
	 * seconds that have elapsed since it was last called
	 * 
	 * @param elapsedMilliseconds time elapsed in milliseconds
	 * @return time pulse length in millisols
	 * @throws Exception if time pulse length could not be determined.
	 */
	public double computeTimePulseInMillisols(long elapsedMilliseconds) {
//		return computeTimePulseInSeconds(elapsedMilliseconds) / MarsClock.SECONDS_PER_MILLISOL;
		return elapsedMilliseconds * currentTR / 1000D / MarsClock.SECONDS_PER_MILLISOL;
	}

//	/**
//	 * Computes the time pulse in seconds. It varies, depending on the time ratio
//	 * 
//	 * @param elapsedMilliseconds time elapsed in milliseconds
//	 * @return time pulse length in seconds
//	 */
//	public double computeTimePulseInSeconds(long elapsedMilliseconds) {
//		return elapsedMilliseconds * currentTR / 1000D;
//	}

	/*
	 * Gets the total number of pulses since the start of the sim
	 */
	public long getTotalPulses() {
		return totalPulses;
	}

	/**
	 * Resets the total number of pulses using the default TBU value
	 * 
	 * @return totalPulses
	 */
	public void resetTotalPulses() {
//		long old = totalPulses;
		totalPulses = 1;// (long) (1D / adjustedTBU_ms * uptimer.getLastUptime());
		// At the start of the sim, totalPulses is zero
//		if (totalPulses > 1)
//			logger.config("Resetting the pulse count from " + old + " to " + totalPulses + ".");
	}

	/**
	 * Resets the clock listener thread
	 */
	public void resetClockListeners() {
		// If the clockListenerExecutor is not working, need to restart it
//		LogConsolidated.log(Level.CONFIG, 0, sourceName, "The Clock Thread has died. Restarting...");
		
		// Re-instantiate clockListenerExecutor
		clockExecutor = Executors.newSingleThreadExecutor();
		// Re-instantiate clockListeners
		clockListeners = Collections.synchronizedList(new CopyOnWriteArrayList<ClockListener>());

		setupClockListenerTask();
			
		addClockListenerTask(sim);

//		sim.restartClockExecutor();
	}
	
//	public long getDefaultTotalPulses() {
//		return (long)(1D / adjustedTBU_ms * uptimer.getLastUptime());
//	}
	
	/**
	 * Sets the simulation time ratio and adjust the value of time between update
	 * (TBU)
	 * 
	 * @param ratio
	 */
	public void setTimeRatio(int ratio) {
		if (ratio >= 1D && ratio <= 65536D && currentTR != ratio) {

			if (ratio > currentTR)
				currentTBU_ns = (long) (currentTBU_ns * 1.0025); // increment by .5%
			else
				currentTBU_ns = (long) (currentTBU_ns * .9975); // decrement by .5%

			logger.config("Time-ratio : " + (int)currentTR + "x -> " + (int)ratio + "x");
				
			currentTR = ratio;
			
		}
	}

	/**
	 * Gets the simulation time ratio.
	 * 
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
		return baseTR;
	}

	/**
	 * Gets the current time between update (TBU)
	 * 
	 * @return value in nanoseconds
	 */
	public long getCurrentTBU() {
		return currentTBU_ns;
	}

	/**
	 * Sets the value of no-delay-per-yield
	 * 
	 * @param value in number
	 */
	public void setNoDelaysPerYield(int value) {
		if (value >= 1D && value <= 200D) {
			noDelaysPerYield = value;
		} else
			throw new IllegalArgumentException("No-Delays-Per-Yield is out of bounds. Must be between 1 and 200");
	}

	/**
	 * Gets the number of no-delay-per-yield
	 * 
	 * @return value in milliseconds
	 */
	public int getNoDelaysPerYield() {
		return noDelaysPerYield;
	}

	/**
	 * Sets the maximum number of skipped frames allowed
	 * 
	 * @param number of frames
	 */
	public void setMaxFrameSkips(int value) {
		if (value >= 1 && value <= 200) {
			maxFrameSkips = value;
		} else
			throw new IllegalArgumentException("max-frame-skips is out of bounds. Must be between 1 and 200");
	}

	/**
	 * Gets the maximum number of skipped frames allowed
	 * 
	 * @return number of frames
	 */
	public int getMaxFrameSkips() {
		return maxFrameSkips;
	}

	/**
	 * Returns the instance of ClockThreadTask
	 * 
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
		}

		@Override
		public void run() {
			tLast = uptimer.getUptimeMillis();
			// Keep running until told not to by calling stop()
			keepRunning = true;

			if (sim.isDoneInitializing() && !isFXGL) {

				long t1 = 0;
				long t2 = 0;
				long t3 = 0;
				long overSleepTime = 0L;
				long excess = 0L;
				int noDelays = 0;

				while (keepRunning) {
					// Gets the new t1
					t1 = System.nanoTime();
					
					// Call addTime() to increment time in EarthClock and MarsClock
					addTime();
		
					// Gets t2 after a time pulse has been sent to EarthClock and MarsClock
					t2 = System.nanoTime();
					
					executionTime = t2 - t1;
					
					// Note: dt = t2 - t1. It's the time where all the logics are done

					// sleepTime varies, depending on the remaining time
					sleepTime = currentTBU_ns - executionTime;
//					logger.config("sleepTime : " + sleepTime/1_000_000 + " ms");

					if (sleepTime > 0 && keepRunning) {
						// Pause simulation to allow other threads to complete.
						try {
							TimeUnit.NANOSECONDS.sleep(sleepTime);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						residualTime = t1 - t3;
						overSleepTime = currentTBU_ns - executionTime - sleepTime - residualTime;
//						logger.config("overSleepTime : " + overSleepTime/1_000_000 + " ms");
					}

					else {
						// if sleepTime <= 0 ( if t2 is way bigger than t1
						// last frame went beyond the PERIOD
						int secs = 0;
						int overSleepSeconds = (int) (overSleepTime/1_000_000_000);
						int mins = 0;
						
						String s = "";
						
						if (overSleepSeconds > 60) {
							mins = overSleepSeconds / 60;
						}
						secs = overSleepSeconds % 60;
						
						if (mins > 0) {
							s = mins + " mins " + secs + " secs";
						}
						else {
							s = secs + " secs";
						}
						
						if (overSleepSeconds > 0 && keepRunning)
							logger.config("This machine seems to be on power saving for " + s); 
						
						excess -= sleepTime;
						overSleepTime = 0L;
						sleepTime = 0;
						
						if (++noDelays >= noDelaysPerYield) {
							noDelays = 0;
						}

						if (excess/1_000_000 > 500) {
							// If the pause is more than .5 seconds, this is most likely due to the machine 
							// just recovering from a power saving event
							
							// Reset the pulse count
							resetTotalPulses();
						}
						
						else {
							for (int i = 1; i <= maxFrameSkips; i++) {
								boolean value = !justReloaded && (Math.abs(excess) > currentTBU_ns);
								justReloaded = false;	
								
								if (!value) {
									excess = 0;
									break;
								}
									
								logger.warning("excess: " + excess/1_000_000 
										 + " ms. currentTBU : " + currentTBU_ns/1_000_000
										 + " ms. executionTime : " + executionTime/1_000_000
										 + " ms. Recovering from a lost frame (Skips # " + i + ")."
										); // e.g. excess : -118289082
								
								excess -= currentTBU_ns;
		
								// Reset the pulse count
								resetTotalPulses();
								
								// Call addTime once to get back the time lost in a frame
								addTime();
								
							}
							
//							if (skips >= maxFrameSkips) {
////								logger.config("# of skips (" + skips + ") is at the max skips (" + maxFrameSkips + ")."); 
//								// Reset the pulse count
//								resetTotalPulses();
//								// Adjust the time between update
//								if (currentTBU_ns > (long) (baseTBU_ns * 1.25))
//									currentTBU_ns = (long) (baseTBU_ns * 1.25);
//								else
//									currentTBU_ns = (long) (currentTBU_ns * .9925); // decrement by 2.5%
//								
//								logger.config("Reset total pulses and set TBU to " + Math.round(100.0 * currentTBU_ns/1_000_000.0)/100.0 + " ms");
//								
//								addTime();
//							}
						}
						
						// Gets t3
						t3 = System.nanoTime();
					}
					
					// Set excess to zero to prevent getting stuck in the above while loop after
					// waking up from power saving
//					logger.config("Setting excess to zero");
					excess = 0;

					// Exit program if exitProgram flag is true.
					if (exitProgram) {
						AutosaveScheduler.cancel();
						System.exit(0);
					}
					
					// Gets t3
					t3 = System.nanoTime();
					
					if (canPauseTime && millisols >= pausingMillisols) {
						
						double m = marsClock.getMillisolOneDecimal();
						
						millisols = m % pausingMillisols;
						
						setPaused(true, false);
					}
					
					// Check to see if the simulation should be saved at this point.
					checkSave();
					
				} // end of while
			} // if fxgl is not used
		} // end of run
	}

	public void setCommandPause(boolean value0, double value1) {
		// Check GameManager.mode == GameMode.COMMAND ?
		canPauseTime = value0;
		pausingMillisols = value1;
	}
	
	/*
	 * Add earth time and mars time
	 */
	private void addTime() {
		if (!isPaused) {
			// Update elapsed milliseconds.
			long millis = calculateElapsedTime();
			if (millis < baseTBU_ms * .75) {
				// Note: this usually happens when recovering from power saving
				// Since millis is too far off, use millisCache instead to compute the correct timePulse 
				
				// Reset currentTR
				if (currentTR == baseTR)
					currentTR = baseTR/2;
				else
					currentTR = baseTR;
				
				// reset millis back to its original value
				millis = (long) (baseTBU_ms);
			}
				
//			logger.config("millis : " + millis + "     currentTR : " + currentTR);

			// The time elapsed for the EarthClock
			long earthMillis = millis * currentTR;
			// Get the time pulse length in millisols.
			double timePulse = earthMillis / MILLISECONDS_PER_MILLISOL; 
//			logger.config("timePulse : " + Math.round(timePulse*1000.0)/1000.0);

			// Incrementing total time pulse number.
			totalPulses++;

			// Note: after recovering from a power save
			// millis = 0 or 1
			// timePulse = 0.0 or 0.002883686808002465
			 
			if (timePulse > 0 && keepRunning) {
				if (clockExecutor != null
					&& !clockExecutor.isTerminated()
					&& !clockExecutor.isShutdown()) {	
					
					// Add time to the Earth clock.
					earthClock.addTime(earthMillis);
					
					// Add time pulse to Mars clock.
					marsClock.addTime(timePulse);
					
//					long t0 = System.nanoTime();
					// Run the clock listener tasks that are in other package
					fireClockPulse(timePulse);
					
//					long t1 = System.nanoTime();
									
					millisols += timePulse;
					if (millisols > MAX_SOLS)
						millisols = millisols - MAX_SOLS;
//					logger.info("dt : " + (t1- t0)/1_000 + " us");
				}
				else {
					// NOTE: when resuming from power saving, timePulse becomes zero
					LogConsolidated.log(Level.CONFIG, 0, sourceName, "The clockListenerExecutor has died. Restarting...");
					resetClockListeners();
				}
			}
			// NOTE: when resuming from power saving, timePulse becomes zero

		}
	}

//	private void shutdownAndAwaitTermination(ExecutorService pool) {
//		pool.shutdown(); // Disable new tasks from being submitted
//		try {
//		// Wait a while for existing tasks to terminate
//		if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
//			pool.shutdownNow(); // Cancel currently executing tasks
//		// Wait a while for tasks to respond to being cancelled
//			if (!pool.awaitTermination(2, TimeUnit.SECONDS))
//				System.err.println("Pool did not terminate");
//			}
//		} catch (InterruptedException ie) {
//			// (Re-)Cancel if current thread also interrupted
//			pool.shutdownNow();
//			// Preserve interrupt status
//			Thread.currentThread().interrupt();
//		}
//	}
		   
	/**
	 * Checks if it is on pause or a saving process has been requested. Keeps track
	 * of the time pulse
	 * 
	 * @return true if it's saving
	 */
	private boolean checkSave() {
//		logger.config("checkSave() is on " + Thread.currentThread().getName()); // pool-4-thread-1
//		logger.config("1. checkSave() : saveType is " + saveType); 
		if (saveType != SaveType.NONE) {
//			logger.config("checkSave() is on " + Thread.currentThread().getName());
//			logger.config("2. checkSave() : saveType is " + saveType + "     file is " + file); 
			try {
				sim.saveSimulation(saveType, file);
			} catch (NullPointerException e) {
				logger.log(Level.SEVERE,
						"NullPointerException. Could not save the simulation.", e);// as " + (file == null ? "null" : file.getPath()), e);
				e.printStackTrace();	
			} catch (IOException e) {
				logger.log(Level.SEVERE,
						"IOException. Could not save the simulation.", e);//  as " + (file == null ? "null" : file.getPath()), e);
				e.printStackTrace();

			} catch (Exception e) {
				logger.log(Level.SEVERE,
						"Exception. Could not save the simulation.", e);//  as " + (file == null ? "null" : file.getPath()), e1);
				e.printStackTrace();
			}
			
			// Reset saveType back to zero
			saveType = SaveType.NONE;

			return true;
		}

		else
			return false;
	}

	/**
	 * Looks at the clock listener list and checks if each listener has already had
	 * a corresponding task in the clock listener task list.
	 */
	public void setupClockListenerTask() {
		clockListeners.forEach(t -> {
			// Check if it has a corresponding task or not, 
			// if it doesn't, create a task for t
			addClockListenerTask(t);
		});
	}

	/**
	 * Returns the refresh rate
	 * 
	 * @return the refresh rate
	 */
	public float getRefresh() {
//		return 1/(DoubleStream.of(refreshRates).average().orElse(0d));
		float sum = 0;
		int size = timeIntervals.size();
		for (float r : timeIntervals) {
			sum += r;
		}	
		// Note: average refresh rate = 1/timeInterval
		return size/sum;
	}

//	/** 
//	 * Gets the time between two ui pulses.
//	 * 
//	 * @return the pulse time
//	 */
//	public float getPulseTime( ) {
//		return pulseTime;
//	}
	
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

		public double getTime() {
			return time;
		}
		
		@Override
		public void run() {
			if (sim.isDoneInitializing()) {
				try {
					// The most important job for CLockListener is to send a clock pulse to
					// 1. Simulation
	
					// so that 
					// 1. UpTimer,
					// 2. Mars, 
					// 3. MissionManager,
					// 4. UnitManager, 
					// 5. ScientificStudyManager, 
					// 6. TransportManager,
				
					// gets updated.
					listener.clockPulse(time);
					timeCache += time;
					count++;
	//				long t02 = System.currentTimeMillis();//.nanoTime();
	//				// Discard the very first pulseTime since it's not invalid
	//				// at the start of the sim
	//				if (t01 != 0) {
	//					timeIntervals.add((t02-t01)/1_000F);
	//				}
	//				t01 = t02;
	//				if (timeIntervals.size() > 15)
	//					timeIntervals.remove(0);
					
					// period is in seconds
	//				double period = timeCache / currentTR * MarsClock.SECONDS_PER_MILLISOL;
	
					if (count > FACTOR) {
	//					int speed = getCurrentSpeed();
	//					if (speed == 0)
	//						speed = 1;
	//					totalCount = speed * speed / FACTOR ;
	//					System.out.println(totalCount);
	//					if (totalCount < 4)
	//						totalCount = 4;
	//					totalCount = (int) (3D/computePulsesPerSecond());
						count = 0;
						
	//					long t02 = System.nanoTime();
						long t02 = System.currentTimeMillis();//.nanoTime();
						// Discard the very first pulseTime since it's not invalid
						// at the start of the sim
						if (t01 != 0) {
							timeIntervals.add((t02-t01)/1_000F);
						}
						t01 = t02;
	
						if (timeIntervals.size() > 30)
							timeIntervals.remove(0);
	
	//					System.out.println(
	//							"time : " + Math.round(time*100.0)/100.0 
	//							+ "   timeCache : " + Math.round(timeCache*100.0)/100.0 
	//							  "   r : " + Math.round(r*100.0)/100.0 
	//							+ "   refresh : " + Math.round(getRefresh()*100.0)/100.0 
	//							"   time between pulses : " + pulseTime
	//							+ "   currentTR : " + currentTR
	//							+ "   Period : " + Math.round(period*1000.0)/1000.0
	//							);
	
						// The secondary job of CLockListener is to send uiPulse() out to
						// 0. MainDesktopPane,
						// which in terms sends a clock pulse out to update all unit windows and tool
						// windows
						//
						// It also sends an ui pulse out to the following class and map related panels:
	
						// 1. MarsTerminal
						// 2. AudioPlayer
						// 3. MainDesktopPane
						// 4. GlobeDisplay
						// 5. MapPanel (2x)
						// 6. TimeWindow
						// 7. SettlementMapPanel
						// 8. NotificationWindow
						// 9. ResupplyDetailPanel
						//10. ArrivingSettlementDetailPanel
						
						
						// Note: on a typical PC, approximately one ui pulse is sent out each second
						listener.uiPulse(timeCache);
						timeCache = 0;
	
					}
	
				} catch (ConcurrentModificationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	   /**
     * Gets the simulation speed
     * 
     * @return
     */
    public int getCurrentSpeed() {
    	int speed = 0;
    	int tr = (int) currentTR;	
        int base = 2;

        while (tr != 1) {
            tr = tr/base;
            --speed;
        }
        
    	return -speed;
    }
    
	/**
	 * Fires the clock pulse to each clock listener
	 * 
	 * @param time
	 */
	public void fireClockPulse(double time) {
		for (ClockListenerTask task : clockListenerTasks) {
			if (task != null) {
				task.insertTime(time);
				clockExecutor.execute(task);
			}

			else
				return;
		}
	}

	/**
	 * Stop the clock
	 */
	public void stop() {
		keepRunning = false;
	}

	/**
	 * Restarts the clock
	 */
	public void restart() {
		keepRunning = true;
	}

	/**
	 * Starts the clock
	 */
	public void start() {
		keepRunning = true;
	}

	
	/**
	 * Set if the simulation is paused or not.
	 *
	 * @param value the state to be set.
	 * @param showPane true if the pane should be shown.
	 */
	public void setPaused(boolean value, boolean showPane) {
		if (this.isPaused != value) {
			this.isPaused = value;
			uptimer.setPaused(value);
	
			if (value) {
//				stop();
				AutosaveScheduler.cancel();		
//				logger.config("The simulation is paused.");
			}
			else {
//				restart();
				AutosaveScheduler.start();
//				logger.config("The simulation is unpaused.");
			}
			
			// Fire pause change to all clock listeners.
			firePauseChange(value, showPane);
		}
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
	 * 
	 * @param isPaused
	 * @param showPane
	 */
	public void firePauseChange(boolean isPaused, boolean showPane) {

		clockListeners.forEach(cl -> cl.pauseChange(isPaused, showPane));
		
//		 synchronized (listeners) { 
//			 Iterator<ClockListener> i = listeners.iterator();
//			 while (i.hasNext()) { 
//				 ClockListener cl = i.next(); try {
//			 cl.pauseChange(isPaused); 
//				 } catch (Exception e) { 
//					 throw new
//				 IllegalStateException("Error while firing pase change", e); 
//				 } 
//			} 
//		 }
		 
	}

	/**
	 * Gets the pulse per second
	 * 
	 * @return
	 */
	public double getPulsesPerSecond() {
		return 1000.0 / tLast * totalPulses;
	}
	
	/**
	 * Update the milliseconds elapsed since last time pulse.
	 */
	private long calculateElapsedTime() {
//		if (uptimer == null) {
//			uptimer = new UpTimer(this);
//		}		
		// Find the new up time
		long tnow = uptimer.getUptimeMillis();		
		// Calculate the elapsed time in milli-seconds
		long elapsedMilliseconds = tnow - tLast;		
		// Update the last up time
		tLast = tnow;
		// return the elapsed time;
		return elapsedMilliseconds;
	}

	/**
	 * Starts clock listener thread pool executor
	 */
	public void startClockListenerExecutor() {
		if (clockExecutor == null)
			clockExecutor = Executors.newSingleThreadExecutor(); //(ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	}

	/**
	 * Shuts down clock listener thread pool executor
	 */
	public void endClockListenerExecutor() {
		if (clockExecutor != null)
			clockExecutor.shutdownNow();
	}


	/**
	 * Gets the clock listener executor. To be called by TransportWizard and ConstructionWizard
	 * 
	 * @return
	 */
	public ExecutorService getClockListenerExecutor() {
		return clockExecutor;
	}


	/**
	 * Gets the Frame per second
	 * 
	 * @return
	 */
	public double getFPS() {
//		List<Double> list = new ArrayList<>(TPFList);
//		double sum = 0;
//		int size = list.size();
//		for (int i = 0; i < size; i++) {
//			sum += list.get(i);
//		}
//		return size/sum;
		return Math.round(10.0 / baseTBU_s) / 10.0;// 1_000_000/tbu_ns;
	}

	/**
	 * Sends out a clock pulse if using FXGL
	 * 
	 * @param tpf
	 */
	public void onUpdate(double tpf) {
		if (!isPaused) {
			tpfCache += tpf;
			if (tpfCache >= baseTBU_s) {
				
				double t = tpfCache * currentTR;
				// Get the time pulse length in millisols.
				double timePulse = t / SECONDS_PER_MILLISOL;
				// tpfCache : 0.117 tpfCache * timeRatio : 14.933 elapsedLast : 9315.0(inc)
				// timePulse : 0.168

				if (timePulse > 0 && keepRunning && !isPaused
						&& clockExecutor != null 
						&& !clockExecutor.isTerminated()
//						&& !clockExecutor.isTerminating()
						&& !clockExecutor.isShutdown()) {
					// Add time pulse length to Earth and Mars clocks.
					earthClock.addTime((int)(1000 * t));
					marsClock.addTime(timePulse);
					fireClockPulse(timePulse);
				}

				// Set tpfCache back to zero
				tpfCache = 0;
			}

			checkSave();
			
//			if (saveType != 0) {
//				try {
//					sim.saveSimulation(saveType, file);
//				} catch (IOException e) {
//					logger.log(Level.SEVERE,
//							"Could not save the simulation as " + (file == null ? "null" : file.getPath()), e);
//					e.printStackTrace();
//				}
//
//				saveType = 0;
//			}

			// Exit program if exitProgram flag is true.
			if (exitProgram) {
				AutosaveScheduler.cancel();
				System.exit(0);
			}
		}
	}

	public double getTime() {
		return clockListenerTasks.get(0).getTime();
	}
	
	/**
	 * Gets the sleep time in milliseconds
	 * 
	 * @return
	 */
	public long getSleepTime() {
		return sleepTime/1_000_000;
	}
	
	/**
	 * Gets the residual time in milliseconds
	 * 
	 * @return
	 */
	public long getResidualTime() {
		return residualTime/1_000_000;
	}
	
	/** 
	 * Gets the time [in microseconds] taken to execute one frame in the game loop 
	 * 
	 * @return
	 */
	public long getExecutionTime() {
		return executionTime/1_000;	
	}
	
	/** 
	 * Gets the base time between update [in microseconds] in the game loop 
	 * 
	 * @return
	 */
	public double getBaseTBU() {
		return baseTBU_ms;	
	}
	
	public void increaseTimeRatio() {
        int currentSpeed = getCurrentSpeed();
        int newSpeed = currentSpeed + 1;
		if (newSpeed >= 0 && newSpeed <= 13) {
        	double ratio = Math.pow(2, newSpeed);
        	setTimeRatio((int)ratio);  
		}
	}
	
	public void decreaseTimeRatio() {
        int currentSpeed = getCurrentSpeed();
        int newSpeed = currentSpeed - 1;
		if (newSpeed >= 0 && newSpeed <= 13) {
        	double ratio = Math.pow(2, newSpeed);
        	setTimeRatio((int)ratio);  
		}
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public static void initializeInstances(Simulation s) {
		sim = s;//Simulation.instance();
		timeIntervals = new ArrayList<>();
		justReloaded = true;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		simulationConfig = null;
		sim = null;
		marsClock.destroy();
		marsClock = null;
		initialMarsTime = null;
		earthClock.destroy();
		earthClock = null;
		uptimer = null;
		clockThreadTask = null;
		clockExecutor = null;
		file = null;

		clockListeners = null;
		clockExecutor = null;
	}
}