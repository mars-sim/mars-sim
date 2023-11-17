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
import java.util.logging.Level;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.task.util.Task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

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

	// 1x, 2x, 4x, 8x, 16x, 32x, 64x, 128x, 256x 
	public static final double MID_TIME_RATIO = (int)Math.pow(2, MID_SPEED); 
	// 384x, 576x, 864x, 1296x, 1944x, 2916x
	public static final double HIGH_TIME_RATIO = MID_TIME_RATIO 
							* Math.pow(1.5, HIGH_SPEED - MID_SPEED);
	// 3645x, 7290x, 10935x, 14580x, 18225x, 21870x
	public static final double MAX_TIME_RATIO = HIGH_TIME_RATIO
							* Math.pow(1.25, MAX_SPEED - HIGH_SPEED);
	
	/** The Maximum number of pulses in the log .*/
	private final int MAX_PULSE_LOG = 20;
	
	// Note: What is a reasonable jump in the observed real time to be allow for 
	//       long simulation steps ? 15 seconds for debugging ? 
	//       How should it trigger in the next pulse ? 
	
	/** The maximum allowable elapsed time [in ms] before action is taken. */
	private final long MAX_ELAPSED = 30_000;

	/** The maximum pulse time allowed in one frame for a task phase. */
	public static final double MAX_PULSE_WIDTH = .855;
	
	/** The multiplier for reducing the width of a pulse. */
//	public static final double MULTIPLIER = 3;
	/** The number of milliseconds for each millisol.  */
	private final double MILLISECONDS_PER_MILLISOL = MarsTime.SECONDS_PER_MILLISOL * 1000.0;

	// Transient members
	/** Runnable flag. */
	private transient boolean keepRunning = false;
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
	public boolean canPauseTime = false;

	/** The user's preferred simulation time ratio. */
	private int desiredTR = 0;
	/** Sol day on the last fireEvent. */
	private int lastSol = -1;
	/** The last millisol integer on the last fireEvent. */
	private int lastIntMillisol = 0;
	/** The maximum wait time between pulses in terms of milli-seconds. */
	private int maxWaitTimeBetweenPulses;
	
	/** The time taken to execute one frame in the game loop [in ms]. */
	private long executionTime;
	/** Next Clock Pulse ID. Start on 1 as all Unit are primed as 0 for the last. */
	private long nextPulseId = 1;
	/** Duration of last sleep. */
	public long sleepTime;
	/** Records the real milli time when a pulse is excited. */
	private long[] pulseLog = new long[MAX_PULSE_LOG];

	/** The current simulation time ratio. */
	private double actualTR = 0;
	/** The difference between desiredTR and actualTR. */
	private double deltaTR = 0;
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
	/** The optimal pulse deviation in fraction. */
	private double optPulseDeviation;
	
	/** The Martian Clock. */
	private MarsTime marsTime;
	/** A copy of the initial martian clock at the start of the sim. */
	private MarsTime initialMarsTime;
	/** The Earth Clock. */
	private LocalDateTime earthTime;
	/** The Uptime Timer. */
	private UpTimer uptimer;
	/** The thread for running the game loop. */
	private ClockThreadTask clockThreadTask;

	private SimulationConfig simulationConfig = SimulationConfig.instance();

	/**
	 * Constructor. 
	 *
	 * @param userTimeRatio the time ratio defined by user
	 * @throws Exception if clock could not be constructed.
	 */
	public MasterClock(int userTimeRatio) {
	
		// Create a martian clock
		marsTime = MarsTimeFormat.fromDateString(simulationConfig.getMarsStartDateTime());

		// Save a copy of the initial mars time
		initialMarsTime = marsTime;

		// Create an Earth clock
		earthTime = simulationConfig.getEarthStartDate();

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
			desiredTR = (int)simulationConfig.getTimeRatio();
		}

		actualTR = desiredTR;
		
		minMilliSolPerPulse = simulationConfig.getMinSimulatedPulse();
		maxMilliSolPerPulse = simulationConfig.getMaxSimulatedPulse();
		
		// Set the optimal width of a pulse
		recomputeReferencePulse();
		
		maxWaitTimeBetweenPulses = simulationConfig.getDefaultPulsePeriod();

		// Check pulse width
//		adjustOptPulseWidth();
		
		// Safety check
		if (minMilliSolPerPulse > maxMilliSolPerPulse) {
			logger.severe("The min pulse millisol is higher than the max pulse.");
		}
		
		String WHITESPACES = "-----------------------------------------------------";
		logger.config(WHITESPACES);
		logger.config("                Desired time-ratio : " + desiredTR + "x");
		logger.config("            Min millisol per pulse : " + minMilliSolPerPulse);
		logger.config("        Optimal millisol per pulse : " + optMilliSolPerPulse);
		logger.config("            Max millisol per pulse : " + maxMilliSolPerPulse);
		logger.config(" Max elapsed time between 2 pulses : " + maxWaitTimeBetweenPulses + " ms");
		logger.config(WHITESPACES);
	}

	/**
	 * Recomputes the reference pulse width and the optimal pulse width according to the desire TR.
	 */
	private void recomputeReferencePulse() {
		// Re-evaluate the optimal width of a pulse
		referencePulse = minMilliSolPerPulse 
				+ ((maxMilliSolPerPulse / Math.sqrt(Simulation.NUM_THREADS) / 5 - minMilliSolPerPulse) 
						* Math.pow(desiredTR, 1.2) / HIGH_TIME_RATIO);

		optMilliSolPerPulse = referencePulse;
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
	 * Resets the clock listener thread.
	 */
	private void resetClockListeners() {
		// If the clockListenerExecutor is not working, need to restart it
		logger.warning("The Clock Thread has died. Restarting...");

		// Re-instantiate clockListenerExecutor
		if (listenerExecutor != null) {
			listenerExecutor.shutdown();
			listenerExecutor = null;
		}

		// Restart executor, listener tasks are still in place
		startClockListenerExecutor();
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
	 * Runs master clock's thread using ThreadPoolExecutor.
	 */
	private class ClockThreadTask implements Runnable, Serializable {

		private static final long serialVersionUID = 1L;

		private ClockThreadTask() {
		}

		@Override
		public void run() {
			// Keep running until told not to by calling stop()
			keepRunning = true;

			if (!isPaused) {

				while (keepRunning) {
					long startTime = System.currentTimeMillis();

					// Call addTime() to increment time in EarthClock and MarsClock
					if (addTime()) {

						// If a can was applied then potentially adjust the sleep
						executionTime = System.currentTimeMillis() - startTime;
						// Get the sleep time
						calculateSleepTime();	
					}
					
					else {
						// If on pause or acceptablePulse is false
						sleepTime = maxWaitTimeBetweenPulses;
					}

					// If still going then wait
					if (keepRunning) {
						if (sleepTime > MAX_ELAPSED) {
							// This should not happen
							logger.warning("Sleep too long: clipped to " + maxWaitTimeBetweenPulses);
							sleepTime = maxWaitTimeBetweenPulses;
						}
						if (sleepTime > 0) {
							// Pause simulation to allow other threads to complete.
							try {
								Thread.sleep(sleepTime);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}
					}

					// Exit program if exitProgram flag is true.
					if (exitProgram) {
						System.exit(0);
					}

				} // end of while
			} // if fxgl is not used

			logger.warning("Clock Thread stopping");

		} // end of run

	/**
	 * Determines the sleep time for this frame.
	 */
	private void calculateSleepTime() {
		// Question: how should the difference between actualTR and desiredTR relate to or affect the sleepTime ?

		// Note: actualTR is greater or less than desiredTR, then our goal is to see a increase or decrease 
		// on actualTR by adjusting the sleepTime. May need to adjust the pulse width as well.
		
		// Get the desired millisols per second
		// Note: make deltaTR (= actualTR - desiredTR) affect the sleepTime
		double desiredMsolPerSecond = (actualTR + desiredTR - deltaTR * 3) / 2 / MarsTime.SECONDS_PER_MILLISOL;

		// Get the desired number of pulses
		double desiredPulses = desiredMsolPerSecond / 
				(0.3 * optMilliSolPerPulse + 0.6 * nextPulseTime + 0.1 * referencePulse);
		
		// Limit the desired pulses to at least 1
		desiredPulses = Math.max(desiredPulses, 1D);
		
		// Get the milliseconds between each pulse
		double milliSecondsPerPulse = 1000 / desiredPulses;

		// Update the sleep time that will allow room for the execution time
		sleepTime = (long)(milliSecondsPerPulse - executionTime);

		// if sleepTime is negative, will increase pulse width in checkPulseWidth() 
		// temporarily to relieve the long execution time

		// Very useful but generates a LOT of log
//		String msg = String.format("Sleep calcs desiredTR=%d, actualTR=%.2f, msol/sec=%.2f, pulse/sec=%.2f, ms/Pulse=%.2f, exection=%d ms, sleep=%d ms",
//				desiredTR, actualTR, desiredMsolPerSecond, desiredPulses, milliSecondsPerPulse, executionTime, sleepTime);
//	    logger.info(msg);
	}
}

	/**
	 * Adds earth time and mars time.
	 *
	 * @return true if the pulse was accepted
	 */
	private boolean addTime() {
		boolean acceptablePulse = false;

		if (!isPaused) {
			// Find the new up time
			long tnow = System.currentTimeMillis();

			// Calculate the real time elapsed [in milliseconds]
			// Note: this should not include time for rendering UI elements
			long realElapsedMillisec = tnow - tLast;

			// Make sure there is not a big jump; suggest power save so skip it
			if (realElapsedMillisec > MAX_ELAPSED) {
//				if (optMilliSolPerPulse > maxMilliSolPerPulse * 1.05) optMilliSolPerPulse = maxMilliSolPerPulse * 1.05;
//				else if (optMilliSolPerPulse < minMilliSolPerPulse) optMilliSolPerPulse = minMilliSolPerPulse;
				optMilliSolPerPulse = referencePulse;
				// Reset nextPulseTime
				nextPulseTime = optMilliSolPerPulse;
				// Reset realElaspedMilliSec back to its default time ratio
				realElapsedMillisec = (long) (nextPulseTime * MILLISECONDS_PER_MILLISOL 
						/ (int)simulationConfig.getTimeRatio());
				// Reset the elapsed clock to ignore this pulse
				logger.warning(10_000, "Elapsed real time is " + realElapsedMillisec 
						+ " ms, longer than the max time " + MAX_ELAPSED + " ms.");				
			}
			
			else if (realElapsedMillisec == 0.0) {
//				if (optMilliSolPerPulse > maxMilliSolPerPulse * 1.05) optMilliSolPerPulse = maxMilliSolPerPulse * 1.05;
//				else if (optMilliSolPerPulse < minMilliSolPerPulse) optMilliSolPerPulse = minMilliSolPerPulse;		
				optMilliSolPerPulse = referencePulse;
				// Reset nextPulseTime
				nextPulseTime = optMilliSolPerPulse;
				// Reset realElaspedMilliSec back to its default time ratio
				if (nextPulseTime > 0)
					realElapsedMillisec = (long) (nextPulseTime * MILLISECONDS_PER_MILLISOL / desiredTR);
				// Reset the elapsed clock to ignore this pulse
				logger.warning(10_000, "Zero elapsed real time. Resetting it back to " + realElapsedMillisec + " ms.");
			}
			
			else {
				// Compute the delta TR
				calculateDeltaTR();

				// NOTE: actualTR is just the ratio of the simulation's pulse time to the real elapsed time

				// Obtain the delta time, given the ratio of realElapsedMillisec to the diff between actualTR and desiredTR 
				double deltaPulseTime = (realElapsedMillisec * deltaTR) 
						/ MILLISECONDS_PER_MILLISOL / 100;
				// Update the next time pulse width [in millisols]
				nextPulseTime -= deltaPulseTime;
				if (nextPulseTime < 0)
					nextPulseTime = minMilliSolPerPulse;

				// Adjust the optimal time pulse and get the deviation
				optPulseDeviation = adjustPulseWidth();
			}
		
			if (nextPulseTime > 0) {
				acceptablePulse = true;
			}
			
			// Elapsed time is acceptable
			if (keepRunning && acceptablePulse) {
				
				// Calculate the time elapsed for EarthClock, based on latest Mars time pulse
				long earthMillisec = (long) (nextPulseTime * MILLISECONDS_PER_MILLISOL);

				// Allows actualTR to gradually catch up with desiredTR
				// Note that the given value of actualTR is the ratio of Earth time to real time elapsed
				if (realElapsedMillisec != 0)
					actualTR = 0.9 * actualTR + 0.1 * (double) earthMillisec / realElapsedMillisec;

				if (!listenerExecutor.isTerminated()
					&& !listenerExecutor.isShutdown()) {

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
				else {
					// NOTE: when resuming from power saving, timePulse becomes zero
					logger.config("The clockListenerExecutor has died. Restarting...");
					resetClockListeners();
				}
			}
		}
		
		return acceptablePulse;
	}

	/**
	 * Checks for the delta time ratio.
	 * 
	 * @return deviation of the optimal pulse width
	 */
	private void calculateDeltaTR() {
		deltaTR = actualTR - desiredTR;
	}
	
	/**
	 * Adjusts the optimal pulse and the next pulse time. Allows it to 
	 * gradually catch up with the reference pulse.
	 */
	private double adjustPulseWidth() {
		
		double nextPulse = nextPulseTime;
		double optPulse = optMilliSolPerPulse;
		
		if (sleepTime < 0) {
			// Increase the optimal pulse width proportionally so as to avoid negative sleep time 
			optPulse = Math.max(1.15, (1 - sleepTime / 1000)) * optPulse;
		}

		else {
			optPulse = .999 * optPulse;
			
			double ratio = actualTR / desiredTR;

			// Adjust optPulse
			if (ratio < 0.999) {
				double diff = desiredTR - actualTR;
				// Increase the optimal pulse width
				optPulse = optPulse + diff / desiredTR / 60;
				if (optPulse > maxMilliSolPerPulse * 1.05) optPulse = maxMilliSolPerPulse * 1.05;
//				logger.config(20_000, "Setting optimal pulse width " + Math.round(optMilliSolPerPulse * 10_000.0) / 10_000.0 + " based on TR deviation to " + Math.round(optPulse * 10_000.0) / 10_000.0 + ".");
			}
			else if (ratio > 1.001) {
				double diff = actualTR - desiredTR;
				// Decrease the optimal pulse width
				optPulse = optPulse - diff / desiredTR / 60;
				if (optPulse < minMilliSolPerPulse) optPulse = minMilliSolPerPulse;
//				logger.config(20_000, "Setting optimal pulse width " + Math.round(optMilliSolPerPulse * 10_000.0) / 10_000.0 + " based on TR deviation to " + Math.round(optPulse * 10_000.0) / 10_000.0 + ".");
			}
			
			///////////////////////////
			ratio = referencePulse / optPulse;
			
			// Adjust optPulse
			if (ratio > 1.05) {
				double diff = referencePulse - optPulse;
				optPulse = optPulse + diff / referencePulse / 50;
				if (optPulse > maxMilliSolPerPulse * 2) optPulse = maxMilliSolPerPulse * 2;
//				logger.config(20_000, "Increasing optimal pulse width " + Math.round(optMilliSolPerPulse * 10_000.0) / 10_000.0 + " toward reference pulse " + Math.round(referencePulse * 10_000.0) / 10_000.0 + ".");
			}
			
			else if (ratio < .95) {
				double diff = optPulse - referencePulse;
				optPulse = optPulse - diff / referencePulse / 50;
				if (optPulse < minMilliSolPerPulse) optPulse = minMilliSolPerPulse;
//				logger.config(20_000, "Decreasing optimal pulse width " + Math.round(optMilliSolPerPulse * 10_000.0) / 10_000.0 + " toward reference pulse " + Math.round(referencePulse * 10_000.0) / 10_000.0 + ".");
			}
					
			///////////////////////////
			ratio = nextPulse / (.8 * optPulse + .2 * referencePulse);
			
			// Adjust nextPulse
			if (ratio > 1.05) {
				double diff = nextPulse - (.8 * optPulse + .2 * referencePulse);
				nextPulse = nextPulse - diff / (.8 * optPulse + .2 * referencePulse) / 60;
				if (nextPulse < minMilliSolPerPulse) nextPulse = minMilliSolPerPulse;
//				logger.config(20_000, "Increasing optimal pulse width " + Math.round(optMilliSolPerPulse * 10_000.0) / 10_000.0 + " toward " + Math.round(optPulse * 10_000.0) / 10_000.0 + ".");
			}
			
			else if (ratio < .95) {
				double diff = (.8 * optPulse + .2 * referencePulse) - nextPulse;
				nextPulse = nextPulse + diff / (.8 * optPulse + .2 * referencePulse) / 60;
				if (nextPulse > maxMilliSolPerPulse * 2) nextPulse = maxMilliSolPerPulse * 2;
//				logger.config(20_000, "Decreasing optimal pulse width " + Math.round(optMilliSolPerPulse * 10_000.0) / 10_000.0 + " toward " + Math.round(optPulse * 10_000.0) / 10_000.0 + ".");
			}
			
			// Update the next pulse time
			nextPulseTime = nextPulse;
		}
				
		// Update the optimal pulse time
		optMilliSolPerPulse = optPulse;
			
		// Update the pulse time for use in tasks
		double oldPulse = Task.getStandardPulseTime();
		double newPulse = Math.max(Math.min(nextPulse, maxMilliSolPerPulse), minMilliSolPerPulse);
		if (newPulse != oldPulse) {
			Task.setStandardPulseTime(newPulse);
		}

		// Returns the deviation
		return (optPulse - referencePulse) / referencePulse;
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
					logger.log(Level.SEVERE, "Can't send out clock pulse: ", e);
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

		int currentIntMillisol = marsTime.getMillisolInt();
		// Checks if this pulse starts a new integer millisol
		boolean isNewIntMillisol = lastIntMillisol != currentIntMillisol;
		if (isNewIntMillisol) {
			lastIntMillisol = currentIntMillisol;
		}
	
		// Identify if it's a new Sol
		int currentSol = marsTime.getMissionSol();
		boolean isNewSol = ((lastSol >= 0) && (lastSol != currentSol));

		// Identify if it's half a sol
		boolean isNewHalfSol = isNewSol || (lastSol <= 500 && currentSol > 500);		
		
		// Update the lastSol
		lastSol = currentSol;
		
		// Print the current sol banner
		if (isNewSol)
			printNewSol(currentSol);

		// Log the pulse
		long newPulseId = nextPulseId++;
		int logIndex = (int)(newPulseId % MAX_PULSE_LOG);
		pulseLog[logIndex] = System.currentTimeMillis();

		currentPulse = new ClockPulse(newPulseId, time, marsTime, this, isNewSol, isNewHalfSol, isNewIntMillisol);
		// Note: for-loop may handle checked exceptions better than forEach()
		// See https://stackoverflow.com/questions/16635398/java-8-iterable-foreach-vs-foreach-loop?rq=1

		// May do it using for loop

		// Note: Using .parallelStream().forEach() in a quad cpu machine would reduce TPS and unable to increase it beyond 512x
		// Not using clockListenerTasks.forEach(s -> { }) for now

		// Execute all listener concurrently and wait for all to complete before advancing
		// Ensure that Settlements stay synch'ed and some don't get ahead of others as tasks queue
		// May use parallelStream() after it's proven to be safe
		Collections.synchronizedSet(new HashSet<>(clockListenerTasks)).stream().forEach(this::executeClockListenerTask);
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
			logger.log(Level.SEVERE, "ExecutionException. Problem with clock listener tasks: ", ee);
		} catch (RejectedExecutionException ree) {
			// Application shutting down
			Thread.currentThread().interrupt();
			// Executor is shutdown and cannot complete queued tasks
			logger.log(Level.SEVERE, "RejectedExecutionException. Problem with clock listener tasks: ", ree);
		} catch (InterruptedException ie) {
			// Program closing down
			Thread.currentThread().interrupt();
			logger.log(Level.SEVERE, "InterruptedException. Problem with clock listener tasks: ", ie);
		}
	}

	/**
	 * Stops the clock.
	 */
	public void stop() {
		keepRunning = false;
	}

	/**
	 * Restarts the clock.
	 */
	public void restart() {
		keepRunning = true;
		timestampPulseStart();
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
		keepRunning = true;

		startClockListenerExecutor();

		if (clockExecutor == null) {
			int num = 1; // Should only have 1 thread updating the time
			logger.config("Setting up " + num + " thread(s) for clock executor.");
			clockExecutor = Executors.newFixedThreadPool(num,
					new ThreadFactoryBuilder().setNameFormat("masterclock-%d").build());
		}
		clockExecutor.execute(clockThreadTask);

		timestampPulseStart();
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
		recomputeReferencePulse();
		// Recompute the delta TR
		calculateDeltaTR();
		// Adjust the optimal time pulse and get the deviation
//		optPulseDeviation = adjustOptPulseWidth();
	}

	/**
	 * Decreases the speed or time ratio.
	 */
	public synchronized void decreaseSpeed() {
		int tr = desiredTR;
		if (tr > HIGH_TIME_RATIO) {
			tr = (int)(tr / 1.25);
		}
		else if (tr > MID_TIME_RATIO) {
			tr = (int)(tr / 1.5);
		}
		else if (tr > 1) {
			tr = (int)(tr / 2);
		}
		else {
			return;
		}
		
		desiredTR = tr;

		// Recompute the reference pulse width and optimal pulse width
		recomputeReferencePulse();
		// Compute the delta TR
		calculateDeltaTR();
		// Adjust the optimal time pulse and get the deviation
//		optPulseDeviation = adjustOptPulseWidth();
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
	 * Starts clock listener thread pool executor.
	 */
	private void startClockListenerExecutor() {
		if (listenerExecutor == null) {
			int num = Math.min(1, Simulation.NUM_THREADS - simulationConfig.getUnusedCores());
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
	 * Gets the Frame per second.
	 *
	 * @return
	 */
	public double getFPS() {
		// How to check xFGL version ?
		return 0;
	}

	/**
	 * Gets the sleep time in milliseconds.
	 *
	 * @return
	 */
	public long getSleepTime() {
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
	 * Gets the optimal pulse deviation.
	 *
	 * @return
	 */
	public double getOptPulseDeviation() {
		return optPulseDeviation;
	}
	
	/**
	 * Gets the time [in microseconds] taken to execute one frame in the game loop.
	 *
	 * @return
	 */
	public long getExecutionTime() {
		return executionTime;
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

}
