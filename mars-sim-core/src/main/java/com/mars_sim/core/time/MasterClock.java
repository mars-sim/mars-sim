/*
 * Mars Simulation Project
 * MasterClock.java
 * @date 2023-09-08
 * @author Scott Davis
 */
package com.mars_sim.core.time;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.task.util.Task;

/**
 * The MasterClock represents the simulated time clock on virtual Mars and
 * delivers a clock pulse for each frame.
 */
public class MasterClock implements Serializable {

	/** default serial id. */
	static final long serialVersionUID = 1L;

	/** Initialized logger. */
	private static final SimLogger logger = SimLogger.getLogger(MasterClock.class.getName());
	/** The maximum speed allowed .*/
	public static final int MAX_SPEED = 20;
	/** The high speed setting. */
	public static final int HIGH_SPEED = 14;
	/** The mid speed setting. */
	public static final int MID_SPEED = 8;
	
	/** The CPU modifier for adjust the ref pulse width. */
	public static final int CPU_MODIFIER = 4;

	// 1x, 2x, 4x, 8x, 16x, 32x, 64x, 128x, 256x 
	public static final double MID_TIME_RATIO = Math.pow(2.0, 1.0 * MID_SPEED); 
	// 384x, 576x, 864x, 1296x, 1944x, 2916x
	public static final double HIGH_TIME_RATIO = MID_TIME_RATIO 
							* Math.pow(1.5, 1.0 * HIGH_SPEED - MID_SPEED);
	// 3645x, 7290x, 10935x, 14580x, 18225x, 21870x
	public static final double MAX_TIME_RATIO = HIGH_TIME_RATIO
							* Math.pow(1.25, 1.0 * MAX_SPEED - HIGH_SPEED);
	
	/** The Maximum number of pulses in the log .*/
	private static final int MAX_PULSE_LOG = 40;
	
	private static final int PULSE_STEPS = 90;
	
	// Note: What is a reasonable jump in the observed real time to be allow for 
	//       long simulation steps ? 15 seconds for debugging ? 
	//       How should it trigger in the next pulse ? 
	
	/** The maximum allowable elapsed time [in ms] before action is taken. */
	private static final int MAX_ELAPSED = 30_000; // 30,000 ms is 30 secs

	/** The maximum allowable sleep time [in ms] before action is taken. */
	private static final int MAX_SLEEP = 2_000;
	
	/** The sleep time [in ms] for letting other CPU tasks to get done. */
	private static final int NEW_SLEEP = 100;
	
	/** The maximum pulse time allowed in one frame for a task phase. */
	public static final double MAX_PULSE_WIDTH = .082;
	
	/** The number of milliseconds for each millisol.  */
	private static final double MILLISECONDS_PER_MILLISOL = MarsTime.SECONDS_PER_MILLISOL * 1000.0;

	
	// Transient members
	/** Pausing clock. */
	private transient boolean isPaused = false;
	/** Flag for ending the simulation program. */
	private transient boolean exitProgram;
	/** The last uptime in terms of number of pulses. */
	private transient long tLast;
	/** The thread for running the clock listeners. */
	private transient ExecutorService listenerExecutor;
	/** Thread for main clock */
	private transient ExecutorService clockExecutor;
	/** A list of clock listener tasks. */
	private transient Collection<ClockListenerTask> clockListenerTasks;
	/** The clock pulse. */
	private transient ClockPulse currentPulse;
	
	// Data members
	/** Is pausing millisol in use. */
	private boolean canPauseTime = false;

	/** The user's preferred simulation time ratio. */
	private int desiredTR = 0;
	/** The last sol on the last fireEvent. Need to set to -1. */
	private int lastSol = -1;
	/** The last millisol integer on the last fireEvent. Need to set to -1. */
	private int lastIntMillisol = -1;
	/** The maximum wait time between pulses in terms of milli-seconds. */
	private int maxWaitTimeBetweenPulses;
	/** Duration of last sleep in milliseconds per pulse. */
	private int sleepTime;
	/** The time taken to execute one frame in the game loop [in ms]. */
	private int executionTime;
	
	/** Next Clock Pulse ID. Start on 1 as all Unit are primed as 0 for the last. */
	private long nextPulseId = 1;
	/** Records the real milli time when a pulse is excited. */
	private long[] pulseLog = new long[MAX_PULSE_LOG];
	
	private double millisecPerPulse;
	/** The last millisol from the last pulse. */
	private double lastMillisol;
	/** The current simulation time ratio. */
	private double actualTR = 0;
	/** The number of millisols to be covered in the next pulse. */
	private double nextPulseTime;
	/** The minimum time span covered by each simulation pulse in millisols. */
	private final double minMilliSolPerPulse;
	/** The maximum time span covered by each simulation pulse in millisols. */
	private final double maxMilliSolPerPulse;
	/** The optimal time span covered by each simulation pulse in millisols. */
	private double optMilliSolPerPulse;
	/** The reference pulse in millisols. */
	private double referencePulse;
	/** The next pulse deviation in fraction. */
	private double nextPulseDeviation;
	/** The tick factor. */
	private double cpuFactor;
	
	/** The Martian Clock. */
	private MarsTime marsTime;
	/** A copy of the initial martian clock at the start of the sim. */
	private MarsTime initialMarsTime;
	/** The Earth Clock. */
	private LocalDateTime earthTime;
	private LocalDateTime initialEarthTime;
	/** The Uptime Timer. */
	private UpTimer uptimer;
	/** The thread for running the game loop. */
	private ClockThreadTask clockThreadTask;

	/**
	 * Constructor. 
	 *
	 * @param config The configuratino that cotnrols default clock settings
	 * @param userTimeRatio the time ratio defined by user
	 * @throws Exception if clock could not be constructed.
	 */
	public MasterClock(SimulationConfig config, int userTimeRatio) {
	
		// Create a martian clock
		marsTime = MarsTimeFormat.fromDateString(config.getMarsStartDateTime());

		// Save a copy of the initial mars time
		initialMarsTime = marsTime;

		// Create an Earth clock
		initialEarthTime = config.getEarthStartDate();
		earthTime = initialEarthTime;

		// Create an Uptime Timer
		uptimer = new UpTimer();

		// Calculate elapsedLast
		timestampPulseStart();

		// Create a dedicated thread for the Clock
		clockThreadTask = new ClockThreadTask();
		
		if (userTimeRatio > 0) {
			if (userTimeRatio <= MID_TIME_RATIO) {
				desiredTR = (int)MID_TIME_RATIO;
				while (desiredTR < userTimeRatio) {
					decreaseSpeed();
				}
			}
			else if (userTimeRatio <= HIGH_TIME_RATIO) {
				desiredTR = (int)HIGH_TIME_RATIO;
				while (desiredTR > userTimeRatio) {
					decreaseSpeed();
				}
			}
			else if (userTimeRatio <= MAX_TIME_RATIO) {
				desiredTR = (int)MAX_TIME_RATIO;
				while (desiredTR > userTimeRatio) {
					decreaseSpeed();
				}
			}	
		}
		else {
			desiredTR = config.getTimeRatio();
		}

		actualTR = desiredTR;
		
		minMilliSolPerPulse = config.getMinSimulatedPulse();
		maxMilliSolPerPulse = config.getMaxSimulatedPulse();
		
		// Set the optimal width of a pulse
		initReferencePulse();
		
		maxWaitTimeBetweenPulses = config.getDefaultPulsePeriod();

		
		// Safety check
		if (minMilliSolPerPulse > maxMilliSolPerPulse) {
			logger.severe("The min pulse millisol is higher than the max pulse.");
		}
		
		final String WHITESPACES = "-----------------------------------------------------";
		logger.config(WHITESPACES);
		logger.config("                Desired time-ratio : " + desiredTR + "x");
		logger.config("            Min millisol per pulse : " + Math.round(minMilliSolPerPulse * 10_000.0)/10_000.0);
		logger.config("        Optimal millisol per pulse : " + Math.round(optMilliSolPerPulse * 10_000.0)/10_000.0);
		logger.config("            Max millisol per pulse : " + Math.round(maxMilliSolPerPulse * 10_000.0)/10_000.0);
		logger.config(" Max elapsed time between 2 pulses : " + maxWaitTimeBetweenPulses + " ms");
		logger.config(WHITESPACES);
	}

	/**
	 * Initializes the reference pulse width and the optimal pulse width according to the desire TR.
	 */
	public void initReferencePulse() {
		
		int cores = SimulationRuntime.NUM_CORES;
		
		if (clockExecutor != null) {
			cores = ((ThreadPoolExecutor)clockExecutor).getActiveCount();
		}
		
		cpuFactor = Math.sqrt((double)cores + SimulationRuntime.NUM_CORES) * 2;
				
		// Re-evaluate the optimal width of a pulse
		computeReferencePulse();
	}

	/**
	 * Recomputes the reference pulse width and the optimal pulse width according to the desire TR.
	 */
	public void computeReferencePulse() {		
		// Re-evaluate the optimal width of a pulse
		referencePulse = minMilliSolPerPulse 
				+ ((maxMilliSolPerPulse / cpuFactor / CPU_MODIFIER - minMilliSolPerPulse) 
						* Math.pow(desiredTR, 1.2) / HIGH_TIME_RATIO);

		optMilliSolPerPulse = referencePulse;
	}
	
	/**
	 * Gets the CPU factor.
	 */
	public double getCpuFactor() {
		return cpuFactor;
	}
	
	/**
	 * Gets the CPU factor.
	 */
	public void setCPUFactor(double newTick) {
		cpuFactor = newTick;
		// Recompute the pulses
		computeReferencePulse();
	}
	
	/**
	 * Increments the tick factor.
	 */
	public void incrementTickFactor() {
		double tick = cpuFactor;
		tick *= 1.1;
		if (tick > 2 * cpuFactor)
			tick = 2 * cpuFactor;
		cpuFactor = tick;
	}
	
	/**
	 * Decrements the tick factor.
	 */
	public void decrementTickFactor() {
		double tick = cpuFactor;
		tick /= 1.1;
		if (tick < cpuFactor / 2)
			tick =  cpuFactor / 2;
		cpuFactor = tick;
	}
	
	/**
	 * Returns the current Martian time.
	 *
	 * @return Martian time
	 */
	public MarsTime getMarsTime() {
		return marsTime;
	}

	/**
	 * Overrides the mars time. This must be used with caution.
	 * 
	 * @param newTime New Mars time
	 */
    public void setMarsTime(MarsTime newTime) {
		marsTime = newTime;
		earthTime = initialEarthTime.plus((long)(marsTime.getTotalMillisols() * MarsTime.MILLISOLS_PER_MINUTE),
									ChronoField.MINUTE_OF_HOUR.getBaseUnit());

    }
	
	/**
	 * Gets the initial Mars time at the start of the simulation.
	 *
	 * @return initial Mars time.
	 */
	public MarsTime getInitialMarsTime() {
		return initialMarsTime;
	}

	/**
	 * Returns the Earth date.
	 *
	 * @return Earth date
	 */
	public LocalDateTime getEarthTime() {
		return earthTime;
	}

	/**
	 * Returns uptime timer.
	 *
	 * @return uptimer instance
	 */
	public UpTimer getUpTimer() {
		return uptimer;
	}

	/**
	 * Adds a clock listener. A minimum duration can be specified which throttles how many
	 * pulses the listener receives. If the duration is set to zero then all Pulses are distributed.
	 *
	 * If the duration is positive then pulses will be skipped to ensure a pulse is not delivered any
	 * quicker than the min duration. The delivered Pulse will have the full elapsed times including
	 * the skipped Pulses.
	 *
	 *
	 * @param newListener the listener to add.
	 * @Param minDuration The minimum duration in milliseconds between pulses.
	 */
	public final void addClockListener(ClockListener newListener, long minDuration) {
		// Check if clockListenerTaskList already contain the newListener's task,
		// if it doesn't, create one
		if (clockListenerTasks == null)
			clockListenerTasks = Collections.synchronizedSet(new HashSet<>());
		if (!hasClockListenerTask(newListener)) {
			clockListenerTasks.add(new ClockListenerTask(newListener, minDuration));
		}
	}

	/**
	 * Removes a clock listener.
	 *
	 * @param oldListener the listener to remove.
	 */
	public final void removeClockListener(ClockListener oldListener) {
		ClockListenerTask task = retrieveClockListenerTask(oldListener);
		if (task != null) {
			clockListenerTasks.remove(task);
		}
	}

	/**
	 * Does it have this clock listener ?
	 *
	 * @param listener
	 * @return
	 */
	private boolean hasClockListenerTask(ClockListener listener) {
		Iterator<ClockListenerTask> i = clockListenerTasks.iterator();
		while (i.hasNext()) {
			ClockListenerTask c = i.next();
			if (c.getClockListener().equals(listener))
				return true;
		}
		return false;
	}

	/**
	 * Retrieves the clock listener task instance, given its clock listener.
	 *
	 * @param listener the clock listener
	 */
	private ClockListenerTask retrieveClockListenerTask(ClockListener listener) {
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
	 * Sets the exit program flag.
	 */
	public void exitProgram() {
		this.setPaused(true, false);
		exitProgram = true;
	}

	/*
	 * Gets the total number of pulses since the start of the sim.
	 */
	public long getTotalPulses() {
		return nextPulseId;
	}

	/**
	 * Resets the listener executor thread.
	 */
	private void resetListenerExecutor() {
		// If the clockListenerExecutor is not working, need to restart it
		logger.severe(10_000, "The Clock Thread has died. Restarting...");

		// Re-instantiate clockListenerExecutor
		if (listenerExecutor != null) {
			listenerExecutor.shutdown();
			listenerExecutor = null;
		}

		// Restart executor, listener tasks are still in place
		startListenerExecutor();
	}


	/**
	 * Sets the preferred time ratio.
	 *
	 * @param ratio
	 */
	public void setDesiredTR(int ratio) {
		if (ratio > 0D && desiredTR != ratio) {
			desiredTR = ratio;
			logger.config("Setting desired time-ratio to " + desiredTR + ".");
		}
	}

	/**
	 * Gets the preferred time ratio. It stays at one value.
	 *
	 * @return ratio
	 */
	public int getDesiredTR() {
		return desiredTR;
	}
	
	/**
	 * Gets the actual time ratio. The value varies over time.
	 *
	 * @return
	 */
	public double getActualTR() {
		return actualTR;
	}


	/**
	 * Adds earth time and mars time.
	 *
	 * @return true if the pulse was accepted
	 */
	private boolean addTime() {
		boolean acceptablePulse = false;

		if (!isPaused) {
			// Ensure listenerExecutor is working
			if (listenerExecutor.isTerminated() 
					|| listenerExecutor.isShutdown()) {
				// NOTE: check if resuming from power saving can cause this
				logger.config("ListenerExecutor has died. Restarting listener executor thread.");
				
				resetListenerExecutor();
			}
			
			// Find the new up time
			long tnow = System.currentTimeMillis();

			// Calculate the real time elapsed [in milliseconds]
			// Note: this should not include time for rendering UI elements
			long realElapsedMillisec = tnow - tLast;

			// Note: Catch the large realElapsedMillisec below. Probably due to power save
			if (realElapsedMillisec > MAX_ELAPSED) {
				// Reset the elapsed clock to ignore this pulse
				logger.config(10_000, "Elapsed real time is " + realElapsedMillisec 
						+ " ms (longer than the max time of " + MAX_ELAPSED + " ms).");	
				// Reset optMilliSolPerPulse
				optMilliSolPerPulse = referencePulse;
				// Reset nextPulseTime
				nextPulseTime = optMilliSolPerPulse;
				// Reset realElaspedMilliSec back to its default time ratio
				realElapsedMillisec = (long) (nextPulseTime * MILLISECONDS_PER_MILLISOL 
						/ desiredTR);
			}
			// Note: Catch the zero realElapsedMillisec below. Probably due to simulation pause
			else if (realElapsedMillisec == 0.0) {
				// Reset optMilliSolPerPulse
				optMilliSolPerPulse = referencePulse;
				// Reset nextPulseTime
				nextPulseTime = optMilliSolPerPulse;
				// Reset realElaspedMilliSec back to its default time ratio
				if (nextPulseTime > 0)
					realElapsedMillisec = (long) (nextPulseTime * MILLISECONDS_PER_MILLISOL / desiredTR);
				// Reset the elapsed clock to ignore this pulse
				logger.config(10_000, "Elapsed real time is zero. Resetting it back to " + realElapsedMillisec + " ms.");
			}
			
			else {
				// Compute the delta TR
				double deltaTR = calculateDeltaTR();

				// NOTE: actualTR is just the ratio of the simulation's pulse time to the real elapsed time

				// Obtain the delta time, given the ratio of realElapsedMillisec to the diff between actualTR and desiredTR 
				double deltaPulseTime = (realElapsedMillisec * deltaTR) 
						/ MILLISECONDS_PER_MILLISOL / PULSE_STEPS;
				
				// Update the next time pulse width [in millisols]
				nextPulseTime -= deltaPulseTime;
				if (nextPulseTime < minMilliSolPerPulse)
					nextPulseTime = minMilliSolPerPulse;
				// Adjust the time pulses and get the deviation
				nextPulseDeviation = adjustPulseWidth();
			}
		
			if (nextPulseDeviation > -2.0 ||  nextPulseDeviation < 2.0) {
				acceptablePulse = true;
			}
			
			// Elapsed time is acceptable
			if (clockThreadTask.getRunning() && acceptablePulse) {
				
				// Calculate the time elapsed for EarthClock, based on latest Mars time pulse
				long earthMillisec = (long) (nextPulseTime * MILLISECONDS_PER_MILLISOL);

				// Allows actualTR to gradually catch up with desiredTR
				// Note that the given value of actualTR is the ratio of Earth time to real time elapsed
				if (realElapsedMillisec != 0.0)
					actualTR = 0.9 * actualTR + 0.1 * earthMillisec / realElapsedMillisec;

				if (!listenerExecutor.isTerminated() && !listenerExecutor.isShutdown()) {
					// Update the uptimer
					uptimer.updateTime(realElapsedMillisec);
					// Gets the timestamp for the pulse
					timestampPulseStart();				
					// Add time to the Earth clock.
					earthTime = earthTime.plus(earthMillisec, ChronoField.MILLI_OF_SECOND.getBaseUnit());
					// Add time pulse to Mars clock.
					marsTime = marsTime.addTime(nextPulseTime);
					// Run the clock listener tasks that are in other package
					fireClockPulse(nextPulseTime);
				}
			}
			else if (!acceptablePulse) {
				logger.severe(10_000, "Pulse width deviated too much: " + nextPulseDeviation
						+ "  acceptablePulse is false.");
			}
			else {
				// NOTE: check if resuming from power saving can cause this
				logger.severe(10_000, "ClockThreadTask is NOT running. Restarting listener executor thread.");
				
				resetListenerExecutor();
			}
		}
		
		return acceptablePulse;
	}

	/**
	 * Calculate the difference between the actualTR and the desiredTR.
	 * 
	 * @return delta TR
	 */
	private double calculateDeltaTR() {
		return actualTR - desiredTR;
	}

			
	/**
	 * Adjusts the optimal pulse and the next pulse time. Allows it to 
	 * gradually catch up with the reference pulse.
	 * 
	 * @return deviation of the next pulse width from the optimal pulse in ratio
	 */
	private double adjustPulseWidth() {
		
		double nextPulse = nextPulseTime;
		double optPulse = optMilliSolPerPulse;
		double refPulse = referencePulse;
		
		if (sleepTime < 0) {
			// Increase the optimal pulse width proportionally so as to avoid negative sleep time 
			// value would be > 1
			double value = 1.0 - sleepTime / 1000.0;
			if (value > 1.1)
				value = 1.1;
			else if (value < 1.0)
				value = 1.0;
			optPulse = value * optPulse;
		}

		else {
			boolean goOn = true;
			
			optPulse = .99999 * optPulse;
			
			// Between refPulse and optPulse
			double ratio = refPulse / optPulse;
			
			// Adjust optPulse
			if (ratio > 1.1) {
				double diff = refPulse - optPulse;
				optPulse = optPulse + diff / refPulse / PULSE_STEPS;
				if (optPulse > maxMilliSolPerPulse * 1.05) {
					optPulse = maxMilliSolPerPulse * 1.05;
					logger.warning(30_000L, "refPulse / optPulse = " + ratio + ". Set optPulse to max.");
				}
				goOn = false;
			}
			
			else if (ratio < .9) {
				double diff = optPulse - refPulse;
				optPulse = optPulse - diff / refPulse / PULSE_STEPS;
				if (optPulse < minMilliSolPerPulse) optPulse = minMilliSolPerPulse;
				goOn = false;
			}

			///////////////////////////

			if (goOn) {
				// Between actualTR and desiredTR
				ratio = actualTR / desiredTR;

				// Adjust next pulse
				if (ratio < 0.99) {
					// Increase the optimal pulse width
					nextPulse = nextPulse + (1 - ratio) * nextPulse / PULSE_STEPS / 2;
					if (nextPulse > maxMilliSolPerPulse * 1.05) {
						nextPulse = maxMilliSolPerPulse * 1.05;
						logger.warning(30_000L, "actualTR / desiredTR = " + ratio + ". Set nextPulse to max.");
					}
					goOn = false;
				}
				else if (ratio > 1.01) {
					// Decrease the optimal pulse width
					nextPulse = nextPulse - (ratio - 1) * nextPulse / PULSE_STEPS / 2;
					if (nextPulse < minMilliSolPerPulse) nextPulse = minMilliSolPerPulse;
					goOn = false;
				}
			}
					
			///////////////////////////

			if (goOn) {
				// Between refPulse and nextPulse
				ratio = refPulse / nextPulse;
				
				// Adjust optPulse
				if (ratio > 1.1) {
					double diff = refPulse - nextPulse;
					nextPulse = nextPulse + diff / refPulse / PULSE_STEPS / 2;
					if (nextPulse > maxMilliSolPerPulse * 1.05) {
						nextPulse = maxMilliSolPerPulse * 1.05;
						logger.warning(30_000L, "refPulse / nextPulse = " + ratio + ". Set nextPulse to max.");
					}
					goOn = false;
				}
				
				else if (ratio < .9) {
					double diff = nextPulse - refPulse;
					nextPulse = nextPulse - diff / refPulse / PULSE_STEPS / 2;
					if (nextPulse < minMilliSolPerPulse) nextPulse = minMilliSolPerPulse;
					goOn = false;
				}
			}
			
			// Update the next pulse time
			nextPulseTime = nextPulse;
			
		}
		
		// Update the optimal pulse time
		optMilliSolPerPulse = optPulse;
		
		if (optPulse > 10 * refPulse) 
			logger.warning(30_000L, "optPulse is " + optPulse + ", 10x the ref pulse.");
			
		// Update the pulse time for use in tasks
		double oldPulse = Task.getStandardPulseTime();
		double newPulse = Math.max(Math.min(nextPulse, maxMilliSolPerPulse), minMilliSolPerPulse);
		if (newPulse > MAX_PULSE_WIDTH) {
			newPulse = MAX_PULSE_WIDTH;
		}
		if (newPulse != oldPulse) {
			Task.setStandardPulseTime(newPulse);
//			logger.info(5_000L, "New standard pulse time is " + Math.round(newPulse * 1000.0)/1000.0);
		}

		// Returns the deviation ratio
		return (nextPulse - optPulse) / optPulse;
	}
	
	/**
	 * Prepares clock listener tasks for setting up threads.
	 */
	public class ClockListenerTask implements Callable<String>{
		private double msolsSkipped = 0;
		private long lastPulseDelivered = 0;
		private ClockListener listener;
		private long minDuration;

		public ClockListener getClockListener() {
			return listener;
		}

		private ClockListenerTask(ClockListener listener, long minDuration) {
			this.listener = listener;
			this.minDuration = minDuration;
			this.lastPulseDelivered = System.currentTimeMillis();
		}

		@Override
		public String call() throws Exception {
			if (!isPaused) {
				try {
					// The most important job for ClockListener is to send a clock pulse to listener
					// gets updated.
					ClockPulse activePulse = currentPulse;

					// Handler is collapsing pulses so check the passed time
					if (minDuration > 0) {
						// Compare elapsed real time to the minimum
						long timeNow = System.currentTimeMillis();
						if ((timeNow - lastPulseDelivered) < minDuration) {
							// Less than the minimum so record elapse and skip
							msolsSkipped += currentPulse.getElapsed();
							return "skip";
						}

						// Build new pulse to include skipped time
						activePulse = currentPulse.addElapsed(msolsSkipped);

						// Reset count
						lastPulseDelivered = timeNow;
						msolsSkipped = 0;
					}

					// Call handler
					listener.clockPulse(activePulse);
				}
				catch (Exception e) {
					logger.severe( "Can't send out clock pulse: ", e);
				}
			}
			return "done";
		}
	}

	public long getNextPulse() {
		return nextPulseId;
	}

	/**
	 * Prints the new mission sol.
	 */
	private void printNewSol(int currentSol) {
		logger.config(" - - - - - - - - - - - - - - Sol " 
				+ currentSol
				+ " - - - - - - - - - - - - - - ");
	}
	
	/**
	 * Fires the clock pulse to each clock listener.
	 *
	 * @param time
	 */
	private void fireClockPulse(double time) {
		////////////////////////////////////////////////////////////////////////////////////		
		// NOTE: Any changes (Part 0 to Part 3) made below may need to be brought to ClockPulse's fireClockPulse()
		////////////////////////////////////////////////////////////////////////////////////
		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 0: Retrieve values
		////////////////////////////////////////////////////////////////////////////////////
		
		// Get the current millisol integer
		int currentIntMillisol = marsTime.getMillisolInt();
		// Get the current millisol
		double currentMillisol = marsTime.getMillisol();
		// Get the current sol
		int currentSol = marsTime.getMissionSol();
				
		////////////////////////////////////////////////////////////////////////////////////
		// Part 1: Update isNewSol and isNewHalfSol
		////////////////////////////////////////////////////////////////////////////////////

		// Identify if this pulse crosses a sol
		boolean isNewSol = (lastSol != currentSol);
		boolean isNewHalfSol = false;
		
		// Updates lastSol
		if (isNewSol) {
			this.lastSol = currentSol;
			isNewHalfSol = true;
		}
		else {
			// Identify if it just passes half a sol
			isNewHalfSol = lastMillisol < 500 && currentMillisol >= 500;
		}


		////////////////////////////////////////////////////////////////////////////////////
		// Part 2: Update isNewIntMillisol and isNewHalfMillisol
		////////////////////////////////////////////////////////////////////////////////////

		// Checks if this pulse starts a new integer millisol
		boolean isNewIntMillisol = (lastIntMillisol != currentIntMillisol);
		boolean isNewHalfMillisol = false;
		
		// Updates lastSol
		if (isNewIntMillisol) {
			this.lastIntMillisol = currentIntMillisol;
			isNewHalfMillisol = true;
		}
		else {
			// Find the decimal part of the past millisol and current millisol
			int intPartLast = (int)lastMillisol;
			double decimalPartLast = lastMillisol - intPartLast;
			int intPartCurrent = (int)currentMillisol;
			double decimalPartCurrent = currentMillisol - intPartCurrent;
			
			// Identify if it just passes half a millisol
			isNewHalfMillisol = decimalPartLast < .5 && decimalPartCurrent >= .5;
		}
		
		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 3: Update lastMillisol
		////////////////////////////////////////////////////////////////////////////////////

		// Update the lastMillisol
		this.lastMillisol = currentMillisol;
	
		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 4: Print the current sol banner
		////////////////////////////////////////////////////////////////////////////////////

		if (isNewSol)
			printNewSol(currentSol);
		
		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 5: Log the pulse
		////////////////////////////////////////////////////////////////////////////////////

		long newPulseId = nextPulseId++;
		int logIndex = (int)(newPulseId % MAX_PULSE_LOG);
		pulseLog[logIndex] = System.currentTimeMillis();

		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 6: Create a clock pulse
		////////////////////////////////////////////////////////////////////////////////////

		currentPulse = new ClockPulse(newPulseId, time, marsTime, this, 
				isNewSol, isNewHalfSol, isNewIntMillisol, isNewHalfMillisol);
		
		// Note: for-loop may handle checked exceptions better than forEach()
		// See https://stackoverflow.com/questions/16635398/java-8-iterable-foreach-vs-foreach-loop?rq=1

		// May do it using for loop

		// Note: Using .parallelStream().forEach() in a quad cpu machine would reduce TPS and unable to increase it beyond 512x
		// Not using clockListenerTasks.forEach(s -> { }) for now

		// Execute all listener concurrently and wait for all to complete before advancing
		// Ensure that Settlements stay synch'ed and some don't get ahead of others as tasks queue
		// May use parallelStream() after it's proven to be safe
		if (clockListenerTasks != null) {
			Collections.synchronizedSet(new HashSet<>(clockListenerTasks)).stream().forEach(this::executeClockListenerTask);
		}
	}

	/**
	 * Executes the clock listener task.
	 *
	 * @param task
	 */
	public void executeClockListenerTask(ClockListenerTask task) {
		Future<String> result = listenerExecutor.submit(task);

		try {
			// Wait for it to complete so the listeners doesn't get queued up if the MasterClock races ahead
			result.get();
		} catch (ExecutionException ee) {
			logger.severe( "ExecutionException. Problem with clock listener tasks: ", ee);
		} catch (RejectedExecutionException ree) {
			// Application shutting down
			Thread.currentThread().interrupt();
			// Executor is shutdown and cannot complete queued tasks
			logger.severe( "RejectedExecutionException. Problem with clock listener tasks: ", ree);
		} catch (InterruptedException ie) {
			// Program closing down
			Thread.currentThread().interrupt();
			logger.severe("InterruptedException. Problem with clock listener tasks: ", ie);
		}
	}

	/**
	 * Stops the clock.
	 */
	public void stop() {
		clockThreadTask.stopRunning();
		logger.info("Simulation put on pause.");
	}

	/**
	 * Timestamps the last pulse, used to calculate elapsed pulse time.
	 */
	private void timestampPulseStart() {
		tLast = System.currentTimeMillis();
	}

	/**
	 * Starts the clock.
	 */
	public void start() {
		clockThreadTask.startRunning();

		startListenerExecutor();

		if (clockExecutor == null) {
			int num = 1; // Should only have 1 thread updating the time
			logger.config("Setting up " + num + " thread for clock executor.");
			clockExecutor = Executors.newFixedThreadPool(num,
					new ThreadFactoryBuilder().setNameFormat("masterclock-%d").build());
			
			// Redo the pulses
			initReferencePulse();
		}
		clockExecutor.execute(clockThreadTask);

		timestampPulseStart();
		
		logger.info("Starting or restarting simulation...");
	}
	
	/**
	 * Increases the speed or time ratio.
	 */
	public synchronized void increaseSpeed() {
		int tr = desiredTR;
		if (tr >= MAX_TIME_RATIO) {
			return;
		}
		else if (tr >= HIGH_TIME_RATIO) {
			tr = (int)(tr * 1.25);
		}
		else if (desiredTR >= MID_TIME_RATIO) {
			tr = (int)(tr * 1.5);
		}
		else {
			tr = tr * 2;
		}
		
		desiredTR = tr;
		
		// Recompute the optimal pulse width
		computeReferencePulse();
		// Recompute the delta TR
		calculateDeltaTR();
	}

	/**
	 * Decreases the speed or time ratio.
	 */
	public synchronized void decreaseSpeed() {
		int tr = desiredTR;
		if (tr > HIGH_TIME_RATIO) {
			tr = (int)Math.round(tr / 1.25);
		}
		else if (tr > MID_TIME_RATIO) {
			tr = (int)Math.round(tr / 1.5);
		}
		else if (tr > 1) {
			tr = (int)Math.round(tr / 2D);
		}
		else {
			return;
		}
		
		desiredTR = tr;

		// Recompute the reference pulse width and optimal pulse width
		computeReferencePulse();
		// Compute the delta TR
		calculateDeltaTR();
	}


	/**
	 * Sets if the simulation is paused or not.
	 *
	 * @param value the state to be set.
	 * @param showPane true if the pane should be shown.
	 */
	public void setPaused(boolean value, boolean showPane) {
		if (this.isPaused != value) {
			this.isPaused = value;

			if (!value) {
				// Reset the last pulse time
				timestampPulseStart();
			}

			if (isPaused) {
				stop();
			}
			else {
				start();
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
	 * Sends a pulse change event to all clock listeners.
	 *
	 * @param isPaused
	 * @param showPane
	 */
	private void firePauseChange(boolean isPaused, boolean showPane) {
		if (clockListenerTasks != null) {
			clockListenerTasks.forEach(cl -> cl.listener.pauseChange(isPaused, showPane));
		}
	}

	/**
	 * Starts the listener thread pool executor.
	 */
	private void startListenerExecutor() {
		if (listenerExecutor == null) {
			int num = Math.min(1, SimulationRuntime.NUM_CORES - SimulationConfig.instance().getUnusedCores());
			if (num <= 0) num = 1;
			logger.config("Setting up " + num + " thread(s) for clock listener.");
			listenerExecutor = Executors.newFixedThreadPool(num,
					new ThreadFactoryBuilder().setNameFormat("clockListener-%d").build());
		}
	}

	/**
	 * Shuts down clock listener thread pool executor.
	 */
	public void shutdown() {
		if (listenerExecutor != null)
			listenerExecutor.shutdownNow();
		if (clockExecutor != null)
			clockExecutor.shutdownNow();
	}

	/**
	 * Gets the sleep time in milliseconds.
	 *
	 * @return
	 */
	public int getSleepTime() {
		return sleepTime;
	}

	/**
	 * Gets the next pulse width, namely, the millisols covered in the next pulse.
	 *
	 * @return
	 */
	public double getNextPulseTime() {
		return nextPulseTime;
	}

	/**
	 * Gets the optimal pulse width.
	 *
	 * @return
	 */
	public double getOptPulseTime() {
		return optMilliSolPerPulse;
	}
	
	/**
	 * Gets the reference pulse.
	 *
	 * @return
	 */
	public double getReferencePulse() {
		return referencePulse;
	}
	
	/**
	 * Gets the next pulse deviation.
	 *
	 * @return
	 */
	public double getNextPulseDeviation() {
		return nextPulseDeviation;
	}
	
	/**
	 * Gets the time [in microseconds] taken to execute one frame in the game loop.
	 *
	 * @return
	 */
	public int getExecutionTime() {
		return executionTime;
	}

	public double getMillisecPerPulse() {
		return millisecPerPulse;
	}
	
	/**
	 * Returns the current # pulses per second, namely, current ticks per sec (TPS).
	 *
	 * @return
	 */
	public double getCurrentPulsesPerSecond() {
		double ticksPerSecond = 0;

		// Make sure enough pulses have passed
		if (nextPulseId >= 0) {
			// Recent idx will be the previous pulse id but check it is not negative
			int recentIdx = (int)((nextPulseId-1) % MAX_PULSE_LOG);
			recentIdx = (recentIdx < 0 ? (MAX_PULSE_LOG-1) : recentIdx);

			// Penultimate pulse id will be one before the recent
			int penIdx = (int)((recentIdx-1) % MAX_PULSE_LOG);
			penIdx = (penIdx < 0 ? (MAX_PULSE_LOG-1) : penIdx);
			long elapsedMilli = (pulseLog[recentIdx] - pulseLog[penIdx]);
			ticksPerSecond = 1000D/elapsedMilli;
		}

		return ticksPerSecond;
	}
	
	/**
	 * Returns the average # pulses per second, namely, average ticks per sec (TPS).
	 *
	 * @return
	 */
	public double getAveragePulsesPerSecond() {
		double ticksPerSecond = 0;

		// Make sure enough pulses have passed
		if (nextPulseId >= MAX_PULSE_LOG) {
			// Recent idx will be the previous pulse id but check it is not negative
			int recentIdx = (int)((nextPulseId-1) % MAX_PULSE_LOG);
			recentIdx = (recentIdx < 0 ? (MAX_PULSE_LOG-1) : recentIdx);

			// Oldest id will be the next pulse as it will be overwrite on next tick
			int oldestIdx = (int)(nextPulseId % MAX_PULSE_LOG);
			long elapsedMilli = (pulseLog[recentIdx] - pulseLog[oldestIdx]);
			ticksPerSecond = (MAX_PULSE_LOG * 1000D)/elapsedMilli;
		}

		return ticksPerSecond;
	}

	/**
	 * Gets the clock pulse.
	 *
	 * @return
	 */
	public ClockPulse getClockPulse() {
		return currentPulse;
	}

	/**
	 * Sets the pause time for the Command Mode.
	 *
	 * @param value0
	 * @param value1
	 */
	public void setCommandPause(boolean value0, double value1) {
		// Check GameManager.mode == GameMode.COMMAND ?
		canPauseTime = value0;
		// Note: will need to re-implement the auto pause time for command mode
		logger.info("Auto pause time: " + value1);
	}

	/**
	 * Determines the sleep time for this frame.
	 */
	private void calculateSleepTime() {
		// Question: how should the difference between actualTR and desiredTR relate to or affect the sleepTime ?

		// Compute the delta TR
		double deltaTR = calculateDeltaTR();
		
		double delta = Math.min(deltaTR / 10, 5 * Math.abs(desiredTR/Math.min(1, actualTR)));				
				
		// Note: actualTR is greater or less than desiredTR, then our goal is to see a increase or decrease 
		// on actualTR by adjusting the sleepTime. May need to adjust the pulse width as well.
		
		// Get the desired millisols per second
		double desiredMsolPerSec = (desiredTR - delta) / MarsTime.SECONDS_PER_MILLISOL;

		// Get the desired number of pulses per second
		double desiredPulsesPerSec = desiredMsolPerSec / 
				(0.2 * optMilliSolPerPulse + 0.3 * nextPulseTime + 0.5 * referencePulse);
		
		// Get the milliseconds between each pulse
		// // Limit the desired pulses to be the minimum of 1 (or at least 1)
		millisecPerPulse = 1000 / desiredPulsesPerSec;
	
		// Update the sleep time that will allow room for the execution time
		sleepTime = (int) millisecPerPulse - executionTime;
		
		// if sleepTime is negative continuously, will consider calling Thread.sleep() 
		// to pause the execution of this thread and allow other threads to complete.

		// NOTE: When resuming from power save, executionTime is often very high
		// Do NOT delete the followings. Very useful for debugging.
		if (executionTime > 1000) {
			String msg = String.format(
				// "sleep=%d ms, desiredTR=%d, actualTR=%.2f, "
				"Abnormal execution time detected : %d ms.", 
//				+ "millisol/sec=%.2f, pulse/sec=%.2f, mspp=%.2f, ms/pulse=%.2f, ",
				executionTime
//				sleepTime, desiredTR, actualTR, 
//				desiredMsolPerSec, desiredPulsesPerSec, mspp, millisecPerPulse 
				);
	    	logger.severe(msg);
		}
	    
	    if (sleepTime < 0)
	    	sleepTime = 0;
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		initialMarsTime = null;
		uptimer = null;
		clockThreadTask = null;
		listenerExecutor = null;
		marsTime = null;
		earthTime = null;
	}

	/**
	 * Runs master clock's thread using ThreadPoolExecutor.
	 */
	private class ClockThreadTask implements Runnable, Serializable {

		private static final long serialVersionUID = 1L;

		private volatile boolean keepRunning = true;
		
		private ClockThreadTask() {
		}

		public boolean getRunning() {
			return keepRunning;
		}
		
		/**
		 * Runs the clock.
		 */
		public void startRunning() {
			keepRunning = true;
		}
		
		/**
		 * Stops the clock.
		 */
		public void stopRunning() {
			keepRunning = false;
		}
		
		@Override
		public void run() {
			// Keep running until told not to by calling stop()
			while (keepRunning) {
				
				long startTime = System.currentTimeMillis();

				// Call addTime() to increment time in EarthClock and MarsClock
				if (addTime()) {
					// Case 1: Normal Operation: acceptablePulse is true
					// Gauge the total execution time
					executionTime = (int) (System.currentTimeMillis() - startTime);
					// Get the sleep time
					calculateSleepTime();
					
					if (executionTime > MAX_SLEEP && getAveragePulsesPerSecond() < 3) {

						logger.warning(30_000, 
								// "sleepTime: " + sleepTime + " ms.  "
								"executionTime: " + executionTime
								+ "  optMilliSolPerPulse: " + Math.round(optMilliSolPerPulse * 1000.0)/1000.0
								+ "  ave pulse: " + getAveragePulsesPerSecond());
						// Set the sleep time in proportion to the anomalous executionTime
						sleepTime = executionTime/30;
					}
				}
				else if (!isPaused) {
					// Case 2: acceptablePulse is false
					logger.warning(30_000, "Time Pulse not within range. "
							+ "Set sleepTime to " + NEW_SLEEP 
							+ " ms to allow other CPU tasks get done first.");
					// Set the sleep time
					sleepTime = NEW_SLEEP;
				}
				
				// If still going then wait
				if (keepRunning && !isPaused && sleepTime > 0) {
					// Pause the execution of this thread 
					// and allow other threads to complete.
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}

				// Exit program if exitProgram flag is true.
				if (exitProgram) {
					System.exit(0);
				}
			} // end of while
		} // end of run
	}
}
