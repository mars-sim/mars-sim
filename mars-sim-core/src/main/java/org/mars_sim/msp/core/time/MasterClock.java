/*
 * Mars Simulation Project
 * MasterClock.java
 * @date 2022-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.time;

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

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.util.Task;

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
	public static final int MAX_SPEED = 13;
	/** The maximum time ratio allowed .*/
	public static final int MAX_TIME_RATIO = (int)Math.pow(2, MAX_SPEED);
	/** The Maximum number of pulses in the log .*/
	private static final int MAX_PULSE_LOG = 30;
	
	// Note: What is a reasonable jump in the observed real time to be allow for 
	//       long simulation steps ? 15 seconds for debugging ? 
	//       How should it trigger in the next pulse ? 
	
	/** The maximum allowable elapsed time [in ms] before action is taken. */
	private static final long MAX_ELAPSED = 30000;

	/** The maximum pulse time allowed in one frame for a task phase. */
	private static final double MAX_PULSE_TIME = .25;
	
	/** The multiplier for reducing the width of a pulse. */
	public static final double MULTIPLIER = 3;
	/** The number of milliseconds for each millisol.  */
	private static final double MILLISECONDS_PER_MILLISOL = MarsClock.SECONDS_PER_MILLISOL * 1000.0;

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
	
	/** The time taken to execute one frame in the game loop [in ms] */
	private long executionTime;
	/** Next Clock Pulse ID. Start on 1 as all Unit are primed as 0 for the last **/
	private long nextPulseId = 1;
	// Duration of last sleep
	public long sleepTime;
	// Records the real milli time when a pulse is execited
	private long[] pulseLog = new long[MAX_PULSE_LOG];
	
	/** The current simulation time ratio. */
	private double actualTR = 0;
	/** Number of millisols covered in the last pulse. */
	private double lastPulseTime;
	/** The minimum time span covered by each simulation pulse in millisols. */
	private double minMilliSolPerPulse;
	/** The maximum time span covered by each simulation pulse in millisols. */
	private double maxMilliSolPerPulse;
	/** The optimal time span covered by each simulation pulse in millisols. */
	private double optMilliSolPerPulse;

	/** The Martian Clock. */
	private MarsTime marsTime;
	/** The Martian Clock. */
	private MarsClock marsClock;
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
	 * Constructor
	 *
	 * @param userTimeRatio the time ratio defined by user
	 * @throws Exception if clock could not be constructed.
	 */
	public MasterClock(int userTimeRatio) {
	
		// Create a martian clock
		marsClock = MarsClockFormat.fromDateString(simulationConfig.getMarsStartDateTime());
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
		desiredTR = (int)simulationConfig.getTimeRatio();

		minMilliSolPerPulse = simulationConfig.getMinSimulatedPulse();
		maxMilliSolPerPulse = simulationConfig.getMaxSimulatedPulse();
		
		// Set the optimal width of a pulse
		adjustOptimalPulseWidth();
		
		maxWaitTimeBetweenPulses = simulationConfig.getDefaultPulsePeriod();

		// Safety check
		if (minMilliSolPerPulse > maxMilliSolPerPulse) {
			logger.severe("The min pulse millisol is higher than the max pule.");
		}
		
		String WHITESPACES = "-----------------------------------------------------";
		logger.config(WHITESPACES);
		logger.config("                 Base time-ratio : " + desiredTR + "x");
		logger.config("          Min millisol per pulse : " + minMilliSolPerPulse);
		logger.config("      Optimal millisol per pulse : " + optMilliSolPerPulse);
		logger.config("          Max millisol per pulse : " + maxMilliSolPerPulse);
		logger.config(" Max elapsed time between pulses : " + maxWaitTimeBetweenPulses + " ms");
		logger.config(WHITESPACES);
	}

	/**
	 * Adjusts the optimal pulse time according to the desire TR.
	 */
	private void adjustOptimalPulseWidth() {
		// Re-evaluate the optimal width of a pulse
		optMilliSolPerPulse = minMilliSolPerPulse 
				+ ((maxMilliSolPerPulse - minMilliSolPerPulse) * desiredTR / MAX_TIME_RATIO);
	}
	
	/**
	 * Returns the Martian clock.
	 *
	 * @return Martian clock instance
	 */
	public MarsClock getMarsClock() {
		return marsClock;
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
	 * Override the mars time. This must be used with caution
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
			logger.config("Time-ratio x" + desiredTR);
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
		// Get the desired millisols per second
		double desiredMsolPerSecond = (actualTR + desiredTR) / 2 / MarsClock.SECONDS_PER_MILLISOL;

		// Get the desired number of pulses
		double desiredPulses = desiredMsolPerSecond / (optMilliSolPerPulse + lastPulseTime) * 2;
		desiredPulses = Math.max(desiredPulses, 1D);
		
		// Get the milliseconds between each pulse
		double milliSecondsPerPulse = 1000D / desiredPulses;

		// Sleep time allows for the execution time
		sleepTime = (long)(milliSecondsPerPulse - executionTime);

		// Very useful but generates a LOT of log
//		String msg = String.format("Sleep calcs desiredTR=%d, actualTR=%.2f, msol/sec=%.2f, pulse/sec=%.2f, ms/Pulse=%.2f, exection=%d ms, sleep=%d ms",
//				desiredTR, actualTR, desiredMsolPerSecond, desiredPulses, milliSecondsPerPulse, executionTime, sleepTime);
//	    logger.info(msg);
	}
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
	 * Adds earth time and mars time.
	 *
	 * @return true if the pulse was accepted
	 */
	private boolean addTime() {
		boolean acceptablePulse = false;

		if (!isPaused) {
			// Find the new up time
			long tnow = System.currentTimeMillis();

			// Calculate the elapsed time in milli-seconds
			long realElapsedMillisec = tnow - tLast;
			
			// Make sure there is not a big jump; suggest power save so skip it
			if (realElapsedMillisec > MAX_ELAPSED) {
				// Reset the elapsed clock to ignore this pulse
				logger.warning("Elapsed real time is " + realElapsedMillisec + " ms, longer than the max time "
			                   + MAX_ELAPSED + " ms.");
				
				// Reset lastPulseTime
				lastPulseTime = optMilliSolPerPulse;
				// Reset realElaspedMilliSec back to its default time ratio
				realElapsedMillisec = (long) (optMilliSolPerPulse * MILLISECONDS_PER_MILLISOL / (int)simulationConfig.getTimeRatio());
			}
			
			else if (realElapsedMillisec == 0.0) {
				// At the start of the sim 
				realElapsedMillisec = (long) (optMilliSolPerPulse * MILLISECONDS_PER_MILLISOL / desiredTR);
				logger.warning("Zero elapsed real time. Resetting it back to " + realElapsedMillisec + " ms.");
			}
			
			else {
				// Adjust the actual TR
				checkActualTR();
				
				// NOTE: actualTR is just the ratio of the simulation's pulse time to the real elapsed time 
				
				// Obtain the latest time pulse width in millisols.
				lastPulseTime = (realElapsedMillisec * actualTR) / MILLISECONDS_PER_MILLISOL;
				
				// Adjust the time pulse
				checkPulseWidth();
			}

			// Gets the timestamp for the pulse
			timestampPulseStart();
			
			if (lastPulseTime > 0) {
				acceptablePulse = true;
			}
			
			// Elapsed time is acceptable
			if (keepRunning && acceptablePulse) {
				
				// The time elapsed for the EarthClock aligned to adjusted Mars time
				long earthMillisec = (long)(lastPulseTime * MILLISECONDS_PER_MILLISOL);

				// Calculate the actual rate for feedback
				actualTR = (double)earthMillisec / realElapsedMillisec;

				if (!listenerExecutor.isTerminated()
					&& !listenerExecutor.isShutdown()) {

					// Update the uptimer
					uptimer.updateTime(optMilliSolPerPulse * MILLISECONDS_PER_MILLISOL / desiredTR);

					// Add time to the Earth clock.
					earthTime = earthTime.plus(earthMillisec, ChronoField.MILLI_OF_SECOND.getBaseUnit());

					// Add time pulse to Mars clock.
					marsClock.addTime(lastPulseTime);
					marsTime = marsTime.addTime(lastPulseTime);

					// Run the clock listener tasks that are in other package
					fireClockPulse(lastPulseTime);
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
	 * Checks for the actual time ratio.
	 */
	private void checkActualTR() {
		double tr = actualTR;
		if (tr / desiredTR > 1.15) {
			double diff = tr - desiredTR;
			tr = tr - diff / 20;
		}
		
		else if (tr / desiredTR < .85) {
			double diff = desiredTR - tr;
			tr = tr + diff / 20;
		}
		
		actualTR = tr;
	}
	
	/**
	 * Checks for the value of pulse width. Adjust the pulse width accordingly. Let 
	 * it gradually catch up to the value of optMilliSolPerPulse.
	 */
	private void checkPulseWidth() {
		double time = lastPulseTime;
		if (time / maxMilliSolPerPulse > 1.15) {
			logger.config(20_000, "Pulse width " + Math.round(time*1_000.0)/1_000.0
					+ " clipped to a max of " + maxMilliSolPerPulse + ".");
			time = maxMilliSolPerPulse;
		}
		else if (time / minMilliSolPerPulse < .85) {
			logger.config(20_000, "Pulse width " + Math.round(time*1_000.0)/1_000.0
					+ " increased to a minimum of " + minMilliSolPerPulse + ".");
			time = minMilliSolPerPulse;
		}
		
		if (time / optMilliSolPerPulse > 1.15) {
			double diff = time - optMilliSolPerPulse;
			time = time - diff / 20;
		}
		
		else if (time / optMilliSolPerPulse < .85) {
			double diff = optMilliSolPerPulse - time;
			time = time + diff / 20;
		}	
		
		// Update the pulse time for use in tasks
		double oldPulseTime = Task.getStandardPulseTime();
		double newPulseTime = Math.min(time/MULTIPLIER, MAX_PULSE_TIME);
		if (newPulseTime != oldPulseTime) {
			Task.setStandardPulseTime(newPulseTime);
		}
		
		lastPulseTime = time;
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

		int currentIntMillisol = marsClock.getMillisolInt();
		// Checks if this pulse starts a new integer millisol
		boolean isNewIntMillisol = lastIntMillisol != currentIntMillisol;
		if (isNewIntMillisol) {
			lastIntMillisol = currentIntMillisol;
		}
	
		// Identify if it's a new Sol
		int currentSol = marsClock.getMissionSol();
		boolean isNewSol = ((lastSol >= 0) && (lastSol != currentSol));
		lastSol = currentSol;

		// Print the current sol banner
		if (isNewSol)
			printNewSol(currentSol);

		// Log the pulse
		long newPulseId = nextPulseId++;
		int logIndex = (int)(newPulseId % MAX_PULSE_LOG);
		pulseLog[logIndex] = System.currentTimeMillis();

		currentPulse = new ClockPulse(newPulseId, time, marsClock, marsTime, this, isNewSol, isNewIntMillisol);
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
	 * Increases the speed / time ratio.
	 */
	public void increaseSpeed() {
		int tr = desiredTR * 2;
		if (tr > MAX_TIME_RATIO) {
			return;
		}
		desiredTR = tr;
		adjustOptimalPulseWidth();
	}

	/**
	 * Decreases the speed / time ratio.
	 */
	public void decreaseSpeed() {
		desiredTR /= 2;
		if (desiredTR < 1) {
			desiredTR = 1;
		}
		adjustOptimalPulseWidth();
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
	 * Gets the pulse width, namely, the millisols covered in the last pulse.
	 *
	 * @return
	 */
	public double getMarsPulseTime() {
		return lastPulseTime;
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
	 * Returns the current # pulses per second.
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
	 * Returns the average # pulses per second.
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
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		marsClock = null;
		initialMarsTime = null;
		uptimer = null;
		clockThreadTask = null;
		listenerExecutor = null;
	}

}
