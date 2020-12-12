/**
 * Mars Simulation Project
 * MasterClock.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */

package org.mars_sim.msp.core.time;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

	private static final int INITIAL_DEFAULT_SLEEP = 500;

	/** default serial id. */
	static final long serialVersionUID = 1L;

	/** Initialized logger. */
	private static Logger logger = Logger.getLogger(MasterClock.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	private static final int FACTOR = 4;
	public static final int MAX_SPEED = 10;
	
	
	/** The number of milliseconds for each millisols.  */	
	private static final double MILLISECONDS_PER_MILLISOL = MarsClock.SECONDS_PER_MILLISOL * 1000.0;

	private static final double MAX_MSOL_PULSE = 10;

	// Maximum number of pulses in the log
	private static final int MAX_PULSE_LOG = 10;
	
	
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
	private long defaultSleepTime;
	/** The last uptime in terms of number of pulses. */
	private transient long tLast;
	/** The cache for accumulating millisols up to a limit before sending out a clock pulse. */
	private transient double timeCache;
	
//	/** The time between two ui pulses. */	
//	private float pulseTime = .5F;
	/** The counts for ui pulses. */	
	private transient int count;

	// Records the real milli time when a pulse is execited
	private long[] pulseLog = new long[MAX_PULSE_LOG];
	
	/** Is FXGL is in use. */
	public boolean isFXGL = false;
	/** Is pausing millisol in use. */
	public boolean canPauseTime = false;
	private double pausingMillisols;
	
	/** The maximum number of counts allowed in waiting for other threads to execute. */
	private int noDelaysPerYield = 0;
	/** The measure of tolerance of the maximum number of lost frames for saving a simulation. */
	private int maxFrameSkips = 0;
	
	/** The total number of pulses cumulated. */
	private long totalPulses = 1;
	/** The cache for the last nano time of an ui pulse. */	
	private long t01 = 0;
	/** Next Clock Pulse ID. Start on 1 as all Unit are primed as 0 for the last **/
	private long nextPulseId = 1;
	
	/** Mode for saving a simulation. */
	private double tpfCache = 0;

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
	
	/** The Martian Clock. */
	private MarsClock marsClock;
	/** A copy of the initial martian clock at the start of the sim. */
	private MarsClock initialMarsTime;
	/** The Earth Clock. */
	private EarthClock earthClock;
	/** The Uptime Timer. */
	private UpTimer uptimer;
	/** The thread for running the game loop. */
	private ClockThreadTask clockThreadTask;
	/** Sol day on the last fireEvent */
	private int lastSol = -1;

	// The default sleep per pulse; is adjusted
	public long defaultSleep = INITIAL_DEFAULT_SLEEP;

	// Duration of last sleep
	public long sleepTime;

	private static Simulation sim = Simulation.instance();

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
		SimulationConfig simulationConfig = SimulationConfig.instance();

		// Create a martian clock
		marsClock = new MarsClock(simulationConfig.getMarsStartDateTime());
		// Save a copy of the initial mars time
		initialMarsTime = (MarsClock) marsClock.clone();
		
//		testNewMarsLandingDayTime();

		// Create an Earth clock
		earthClock = new EarthClock(simulationConfig.getEarthStartDateTime());

		// Create an Uptime Timer
		uptimer = new UpTimer();

		// Create listener list.
		clockListeners = new CopyOnWriteArrayList<ClockListener>();
		timeIntervals = new CopyOnWriteArrayList<>();
		
		// Calculate elapsedLast
		timestampPulseStart();

		// Check if FXGL is used
		if (!isFXGL)
			clockThreadTask = new ClockThreadTask();

		logger.config("-----------------------------------------------------");
		
		// Setting the initial time ratio.
		double tr = 0;
		if (userTimeRatio == -1)
			tr = simulationConfig.getTimeRatio();
		else {
			tr = userTimeRatio;
//			logger.config("   User-Defined Time Ratio (TR) : " + (int) tr + "x");
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

		logger.config("         User Defined Time Ratio : " + (int) tr + "x");
		logger.config("            Base Time Ratio (TR) : " + (int) baseTR + "x");
		logger.config("     Base Ticks Per Second (TPS) : " + Math.round(baseFPS * 100D) / 100D + " Hz");
		logger.config(" Base Time between Updates (TBU) : " + Math.round(baseTBU_ms * 100D) / 100D + " ms");
		logger.config("-----------------------------------------------------");
		logger.config("Note: parameters are tuned to # available CPU threads");

		//		logger.config(" - - - Welcome to Mars - - -");
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
		return nextPulseId;
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
	
	
	/**
	 * Sets the simulation time ratio and adjust the value of time between update
	 * (TBU)
	 * 
	 * @param ratio
	 */
	public void setTimeRatio(int ratio) {
		if (ratio >= 0D && ratio <= Math.pow(2, MAX_SPEED) && currentTR != ratio) {

			if (ratio > currentTR)
				currentTBU_ns = (long) (currentTBU_ns * 1.0025); // increment by .5%
			else
				currentTBU_ns = (long) (currentTBU_ns * .9975); // decrement by .5%

			logger.config("Time-ratio : " + (int)currentTR + "x -> " + (int)ratio + "x");
				
			currentTR = ratio;
			
		}
		
//		else {
//			logger.config("Not possible to change the time-ratio from " + (int)currentTR + "x -> " + (int)ratio + "x");		
//		}
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
			
			// Keep running until told not to by calling stop()
			keepRunning = true;

			if (sim.isDoneInitializing() && !isFXGL) {
				while (keepRunning) {
					long startTime = System.currentTimeMillis();
					
					// Call addTime() to increment time in EarthClock and MarsClock
					if (addTime()) {
						// If a can was applied then potentially adjust the sleep
						executionTime = System.currentTimeMillis() - startTime;
					
						// sleepTime varies, depending on the remaining time
						sleepTime = defaultSleep - executionTime;
					}
					else {
						logger.info("AddTime not accepted: lastPulse " + tLast);
						sleepTime = defaultSleep;
					}
					
					// If still going then wait
					if (keepRunning) {
						if (sleepTime > 0) {
							// Pause simulation to allow other threads to complete.
							try {
								Thread.sleep(sleepTime);;
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}
						else {
							logger.warning("Sleep skipped: too short");
						}
					}

					// Exit program if exitProgram flag is true.
					if (exitProgram) {
						AutosaveScheduler.cancel();
						System.exit(0);
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
	private boolean addTime() {
		boolean acceptablePulse = false;
		
		if (!isPaused) {	
			// Find the new up time
			long tnow = System.currentTimeMillis();
		
			// Calculate the elapsed time in milli-seconds
			long realElaspedMilliSec = tnow - tLast;	

			// The time elapsed for the EarthClock
			long earthMillisec = realElaspedMilliSec * currentTR;
			// Get the time pulse length in millisols.
			double marsMSol = earthMillisec / MILLISECONDS_PER_MILLISOL; 

			// Pulse must be less than the max and positive
			if (marsMSol > 0) {
				acceptablePulse = true;
				if (marsMSol > MAX_MSOL_PULSE) {
					logger.warning("Proposed pulse " + marsMSol + " clipped to max " + MAX_MSOL_PULSE);
					marsMSol = MAX_MSOL_PULSE;
				}
			}
			
			if (acceptablePulse && keepRunning) {
				// Elapsed time is acceptable
				if (!clockExecutor.isTerminated()
					&& !clockExecutor.isShutdown()) {	
					// Do the pulse
					timestampPulseStart();
					
					uptimer.updateTime(realElaspedMilliSec);
					
					// Add time to the Earth clock.
					earthClock.addTime(earthMillisec);
					
					// Add time pulse to Mars clock.
					marsClock.addTime(marsMSol);
					
					// Run the clock listener tasks that are in other package
					fireClockPulse(marsMSol);
				}
				else {
					// NOTE: when resuming from power saving, timePulse becomes zero
					LogConsolidated.flog(Level.CONFIG, 0, sourceName, "The clockListenerExecutor has died. Restarting...");
					resetClockListeners();
				}
			}
			// NOTE: when resuming from power saving, timePulse becomes zero
		}
		
		return acceptablePulse;
	}
		   
	/**
	 * Checks if it is on pause or a saving process has been requested. Keeps track
	 * of the time pulse
	 * 
	 * @return true if it's saving
	 */
	private boolean checkSave() {

		if (saveType != SaveType.NONE) {
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

	/**
	 * Prepares clock listener tasks for setting up threads.
	 */
	public class ClockListenerTask implements Callable<String>{

		private ClockPulse currentPulse;
		private ClockListener listener;

		public ClockListener getClockListener() {
			return listener;
		}

		private ClockListenerTask(ClockListener listener) {
			this.listener = listener;
		}

		public void setCurrentPulse(ClockPulse pulse) {
			this.currentPulse = pulse;
		}
		
		@Override
		//public void run() {
		public String call() throws Exception {
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
					listener.clockPulse(currentPulse);
					timeCache += currentPulse.getElapsed();
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
			return "done";
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

		// Identify if it's a new Sol
		int currentSol = marsClock.getMissionSol();
		boolean isNewSol = ((lastSol >= 0) && (lastSol != currentSol));
		lastSol  = currentSol;
		
		// Log the pulse
		long newPulseId = nextPulseId++;
		int logIndex = (int)(newPulseId % MAX_PULSE_LOG);
		pulseLog[logIndex] = System.currentTimeMillis();
		
		ClockPulse pulse = new ClockPulse(sim, newPulseId, time, marsClock, earthClock, this, isNewSol);
		clockListenerTasks.forEach(s -> {
			s.setCurrentPulse(pulse);
			Future<String> result = clockExecutor.submit(s);
			// Wait for it to complete so the listeners doesn't get queued up if the MasterClock races ahead
			try {
				result.get();
			} catch (ExecutionException e) {
				logger.log(Level.SEVERE, "Problem in clock listener", e);
			} catch (InterruptedException e) {
				// Program closing down
				Thread.currentThread().interrupt();
			}
		});
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
		timestampPulseStart();
	}

	/**
	 * Timestamps the last pulse, used to calculate elapsed pulse time
	 */
	private void timestampPulseStart() {
		tLast = System.currentTimeMillis();
	}

	/**
	 * Starts the clock
	 */
	public void start() {
		keepRunning = true;
		timestampPulseStart();
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
	
			if (value) {
				AutosaveScheduler.cancel();		
			}
			else {
				AutosaveScheduler.start();
				
				// Reset the last pulse time
				timestampPulseStart();
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
				
				addTime();
				
				// Set tpfCache back to zero
				tpfCache = 0;
			}

			checkSave();

			// Exit program if exitProgram flag is true.
			if (exitProgram) {
				AutosaveScheduler.cancel();
				System.exit(0);
			}
		}
	}

	/**
	 * Gets the sleep time in milliseconds
	 * 
	 * @return
	 */
	public long getSleepTime() {
		return sleepTime;
	}
	
	/**
	 * Gets the residual time in milliseconds
	 * 
	 * @return
	 */
	public long getResidualTime() {
		return defaultSleep - executionTime;
	}
	
	/** 
	 * Gets the time [in microseconds] taken to execute one frame in the game loop 
	 * 
	 * @return
	 */
	public long getExecutionTime() {
		return executionTime;	
	}
	
	/** 
	 * Gets the base time between update [in microseconds] in the game loop 
	 * 
	 * @return
	 */
	public double getBaseTBU() {
		return baseTBU_ms;	
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 */
	public static void initializeInstances(Simulation s) {
		sim = s;//Simulation.instance();
		timeIntervals = new CopyOnWriteArrayList<>();
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
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

	/**
	 * How many pulses per second
	 * @return
	 */
	public double getPulsesPerSecond() {
		double ticksPerSecond = 0;
	
		// Make sure enough pulses have passed
		if (nextPulseId >= MAX_PULSE_LOG) {
			// Recent idx will be the previous pulse id but check it is not negative
			int recentIdx = (int)((nextPulseId-1) % MAX_PULSE_LOG);
			recentIdx = (recentIdx < 0 ? (MAX_PULSE_LOG-1) : recentIdx);
			
			// Oldest id will be the next pulse as it will be overwrite on next tick
			int oldestIdx = (int)(nextPulseId % MAX_PULSE_LOG);
			long elaspsedMilli = (pulseLog[recentIdx] - pulseLog[oldestIdx]);
			ticksPerSecond = (MAX_PULSE_LOG * 1000D)/elaspsedMilli;
		}
		
		return ticksPerSecond;
	}
}
