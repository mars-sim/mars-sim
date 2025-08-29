/*
 * Mars Simulation Project
 * MasterClock.java
 * @date 2025-08-05
 * @author Scott Davis
 */
package com.mars_sim.core.time;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.tool.MathUtils;

/**
 * The MasterClock represents the simulated time clock on virtual Mars and
 * delivers a clock pulse for each frame.
 */
public class MasterClock implements Serializable {

	/** default serial id. */
	static final long serialVersionUID = 1L;

	/** Initialized logger. */
	private static final SimLogger logger = SimLogger.getLogger(MasterClock.class.getName());

	/** Auto-drop threshold: listeners throwing this many times in a row are unregistered. */
	private static final int MAX_CONSECUTIVE_LISTENER_FAILURES = 5;

	/** The maximum speed allowed .*/
	public static final int TIER_3_TOP = 32;
	/** The maximum speed allowed .*/
	public static final int TIER_2_TOP = 24;
	/** The high speed setting. */
	public static final int TIER_1_TOP = 16;
	/** The mid speed setting. */
	public static final int TIER_0_TOP = 8;
	
	// 1x,    2x, 4x, 8x, 16x,    32x, 64x, 128x, 256x 
	public static final float LOW_SPEED_RATIO = (float)Math.pow(2.0, 1.0 * TIER_0_TOP); 
	// 384x, 576x, 864x, 1296x,    1944x, 2916x, 4374x, 6561x
	public static final float MID_SPEED_RATIO = LOW_SPEED_RATIO 
									* (float)Math.pow(1.5, 1.0 * TIER_1_TOP - TIER_0_TOP);
	// 8201x, 10,251x, 12,813x, 16,016x,    20,020x, 25,025x, 31,281x, 39,101x
	public static final float HIGH_SPEED_RATIO = MID_SPEED_RATIO
									* (float)Math.pow(1.25, 1.0 * TIER_2_TOP - TIER_1_TOP);
	// 48,876x, 54,985x, 61,858x, 69,590x,     78,288x, 88,074x, 99,083x, 111,468x
	public static final float SUPER_HIGH_SPEED_RATIO = HIGH_SPEED_RATIO
									* (float)Math.pow(1.125, 1.0 * TIER_3_TOP - TIER_2_TOP);
	
	/** The Maximum number of pulses in the log .*/
	private static final int MAX_PULSE_LOG = 40;
	
	private static final int PULSE_STEPS = 120;
	
	// Note: What is a reasonable jump in the observed real time to be allow for 
	//       long simulation steps ? 15 seconds for debugging ? 
	//       How should it trigger in the next pulse ? 
	
	/** The maximum allowable elapsed time [in ms] before action is taken. */
	private static final int MAX_ELAPSED = 30_000; // 30,000 ms is 30 secs

	/** The execution time limit [in ms] before action is taken. */
	private static final int EXE_UPPER_LIMIT = 9_000;
	
	/** The TPS lower limit before action is taken. */
	// private static final double TPS_LOWER_LIMIT = 0.1;
				
	/** The sleep time [in ms] for letting other CPU tasks to get done. */
	// private static final int NEW_SLEEP = 20;
	
	/** The initial task pulse dampener for controlling the speed of the task pulse width increase. */
	public static final int INITIAL_TASK_PULSE_DAMPER = 500;
	/** The initial ref pulse dampener for controlling the speed of the ref pulse width increase. */
	public static final int INITIAL_REF_PULSE_DAMPER = 500;
	/** The initial max pulse time allowed in one frame for a task to execute in its phase. */
	public static final float INITIAL_PULSE_WIDTH = .082f;
	/** The initial ratio between the next pulse width and the task pulse width. */
	public static final float INITIAL_TASK_PULSE_RATIO = .5f;
	/** The initial ratio between the minMilliSolPerPulse and the ref pulse width. */
	public static final float INITIAL_REF_PULSE_RATIO = .5f;
	
	/** The number of milliseconds for each millisol.  */
	private static final float MILLISECONDS_PER_MILLISOL = (float) (MarsTime.SECONDS_PER_MILLISOL * 1000f);

	
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
	/** A list of clock listener tasks. (CME-safe snapshot semantics) */
	private transient CopyOnWriteArrayList<ClockListenerTask> clockListenerTasks;
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
	
	/** The task pulse damper - the higher the number, the slower the task pulse width will increase. */
	private int taskPulseDamper = INITIAL_TASK_PULSE_DAMPER;
	/** The ref pulse damper - the higher the number, the slower the ref pulse width will increase. */
	private int refPulseDamper = INITIAL_REF_PULSE_DAMPER;
	
	/** The time taken to execute one frame in the game loop [in ms]. */
	private short executionTime;
	
	/** Next Clock Pulse ID. Start on 1 as all Unit are primed as 0 for the last. */
	private long nextPulseId = 1;
	/** Records the real milli time when a pulse is excited. */
	private long[] pulseLog = new long[MAX_PULSE_LOG];
	
	/** Duration of last sleep in milliseconds per pulse. */
	private float sleepTime;
	/** The millisol per pulse. */
	private float millisecPerPulse;
	/** The last millisol from the last pulse. */
	private float lastMillisol;
	/** The current simulation time ratio. */
	private float actualTR = 0;

	/** The minimum time span covered by each simulation pulse in millisols. */
	private final float minMilliSolPerPulse;
	/** The maximum time span covered by each simulation pulse in millisols. */
	private final float maxMilliSolPerPulse;
	/** The original CPU util. */
	private float originalCPUUtil;
	/** The current CPU util. */
	private float cpuUtil;
	/** The player adjustable task pulse ratio. */
	private float taskPulseRatio = INITIAL_TASK_PULSE_RATIO; 
	/** The player adjustable ref pulse ratio. */
	private float refPulseRatio = INITIAL_REF_PULSE_RATIO; 
	
	/** The number of millisols to be covered in the leading pulse. */
	private float leadPulseTime;
	/** The optimal time span covered by each simulation pulse in millisols. */
	private float optMilliSolPerPulse;
	/** The reference pulse in millisols. */
	private float referencePulse;
	/** The next pulse deviation in fraction. */
	private float pulseDeviation;
	/** The adjustable task pulse time allowed in one frame for a task phase. */
	private float taskPulseWidth = INITIAL_PULSE_WIDTH;

	
	/** The Martian Clock. */
	private MarsTime marsTime;
	/** A copy of the initial Martian clock at the start of the sim. */
	private MarsTime initialMarsTime;
	/** The Earth Clock. */
	private LocalDateTime earthTime;
	/** The starting Earth time. */	
	private LocalDateTime initialEarthTime;
	/** The Uptime Timer. */
	private UpTimer uptimer;
	/** The thread for running the game loop. */
	private ClockThreadTask clockThreadTask;

	/**
	 * Constructor. 
	 *
	 * @param config The configuration that controls default clock settings
	 * @param userTimeRatio the time ratio defined by user
	 * @throws Exception if clock could not be constructed.
	 */
	public MasterClock(SimulationConfig config, int userTimeRatio) {
	
		// Create a Martian clock
		marsTime = MarsTimeFormat.fromDateString(config.getMarsStartDateTime());

		// Save a copy of the initial Mars time
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
			if (userTimeRatio <= LOW_SPEED_RATIO) {
				desiredTR = (int)LOW_SPEED_RATIO;
				while (desiredTR > userTimeRatio) {
					decreaseSpeed();
				}
			}
			else if (userTimeRatio <= MID_SPEED_RATIO) {
				desiredTR = (int)MID_SPEED_RATIO;
				while (desiredTR > userTimeRatio) {
					decreaseSpeed();
				}
			}
			else if (userTimeRatio <= HIGH_SPEED_RATIO) {
				desiredTR = (int)HIGH_SPEED_RATIO;
				while (desiredTR > userTimeRatio) {
					decreaseSpeed();
				}
			}	
			else if (userTimeRatio <= SUPER_HIGH_SPEED_RATIO) {
				desiredTR = (int)SUPER_HIGH_SPEED_RATIO;
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
		
		// Calculate the original cpu load
		computeOriginalCPULoad();

		// Set the reference pulse width
		computeReferencePulse();
		
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
	 * Computes the original cpu utjl or load.
	 */
	public void computeOriginalCPULoad() {
	
		int cores = SimulationRuntime.NUM_CORES;	
		Simulation sim = Simulation.instance();
		
		if (sim.getUnitManager() != null) {
			float objLoad = sim.getUnitManager().getObjectsLoad();
			float load = .4f * (float)Math.sqrt(Math.max(1, objLoad/30.0));
			
			// Save the original pulse load
			originalCPUUtil = cores / load;	
		}
		else {
			originalCPUUtil = cores / .4f;
		}
			
		cpuUtil = originalCPUUtil;
	}
	
	/**
	 * Computes the new cpu util or load.
	 */
	public void computeNewCpuLoad() {
		cpuUtil = (float)(Math.round((.5 * cpuUtil + .5 * originalCPUUtil) * 100.0)/100.0); 
	}

	/**
	 * Computes the reference pulse width and the optimal pulse width according to the desire TR.
	 */
	public void computeReferencePulse() {
		// Re-evaluate the optimal width of a pulse
		referencePulse = (float) (refPulseRatio * minMilliSolPerPulse 
						+ (1 - refPulseRatio) * Math.pow(desiredTR, 1.2) / cpuUtil / refPulseDamper);
		
		optMilliSolPerPulse = referencePulse;
	}
	
	/**
	 * Gets the CPU util.
	 */
	public float getCPUUtil() {
		return cpuUtil;
	}
		
	/**
	 * Sets the CPU util.
	 */
	public void setCPUUtil(float value) {
		cpuUtil = value;
		// Recompute the ref and opt pulses
		computeReferencePulse();
	}

	public void setTaskPulseDamper(int value) {
		taskPulseDamper = value;
	}
	
	public int getTaskPulseDamper() {
		return taskPulseDamper;
	}

	public void setRefPulseDamper(int value) {
		refPulseDamper = value;
		// Recompute the ref and opt pulses
		computeReferencePulse();
	}
	
	public int getRefPulseDamper() {
		return refPulseDamper;
	}
	
	public void setTaskPulseRatio(float value) {
		taskPulseRatio = value;
	}
	
	public float getTaskPulseRatio() {
		return taskPulseRatio;
	}
	
	public void setRefPulseRatio(float value) {
		refPulseRatio = value;
		// Recompute the ref and opt pulses
		computeReferencePulse();
	}
	
	public float getRefPulseRatio() {
		return refPulseRatio;
	}
	
	public void resetTaskPulseDamper() {
		taskPulseDamper = INITIAL_TASK_PULSE_DAMPER;
	}
	
	public void resetRefPulseDamper() {
		refPulseDamper = INITIAL_REF_PULSE_DAMPER;
	}
	
	public void resetTaskPulseRatio() {
		taskPulseRatio = INITIAL_TASK_PULSE_RATIO;
	}
	
	public void resetRefPulseRatio() {
		refPulseRatio = INITIAL_REF_PULSE_RATIO;
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
		if (newListener == null) return;
		if (clockListenerTasks == null)
			clockListenerTasks = new CopyOnWriteArrayList<>();
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
		if (task != null && clockListenerTasks != null) {
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
		if (clockListenerTasks == null) return false;
		for (ClockListenerTask c : clockListenerTasks) {
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
			for (ClockListenerTask c : clockListenerTasks) {
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
				logger.config(10_000, "Elapsed real time is " + realElapsedMillisec/1000.0 
						+ " secs, exceeding the max time of " + MAX_ELAPSED/1000.0 + " secs.");	
				// Reset optMilliSolPerPulse
				optMilliSolPerPulse = referencePulse;
				// Reset the lead pulse
				leadPulseTime = optMilliSolPerPulse;
				// Reset realElaspedMilliSec back to its default time ratio
				realElapsedMillisec = (long) (leadPulseTime * MILLISECONDS_PER_MILLISOL 
						/ desiredTR);
			}
			// At the start of the sim, realElapsedMillisec is also zero
			// Note: find out when the zero realElapsedMillisec will also occur. Probably due to simulation pause ?!
			else if (realElapsedMillisec == 0.0) {
				// Reset optMilliSolPerPulse
				optMilliSolPerPulse = referencePulse;
				// Reset the lead pulse
				leadPulseTime = optMilliSolPerPulse;
				// Reset realElaspedMilliSec back to its default time ratio
				if (leadPulseTime > 0)
					realElapsedMillisec = (long) (leadPulseTime * MILLISECONDS_PER_MILLISOL / desiredTR);
				// Reset the elapsed clock to ignore this pulse
				logger.config("Skipping this frame. Elapsed real time is zero. Setting it to the expected " + realElapsedMillisec + " ms.");
			}
			
			else {
				// Adjust the time pulses and get the deviation
				pulseDeviation = computePulseDev();
			}
		
			if (pulseDeviation > -10 && pulseDeviation < 10) {
				// If not deviating too much
				acceptablePulse = true;
			}
			
			// Elapsed time is acceptable
			if (leadPulseTime > 0 && clockThreadTask.getRunning() && acceptablePulse) {
				
				// Calculate the time elapsed for EarthClock, based on latest Mars time pulse
				float earthMillisec = leadPulseTime * MILLISECONDS_PER_MILLISOL;

				// Allows actualTR to gradually catch up with desiredTR
				// Note that the given value of actualTR is the ratio of Earth time to real time elapsed
				if (realElapsedMillisec > 0.0)
					actualTR = (float)(0.9 * actualTR + 0.1 * earthMillisec / realElapsedMillisec);

				if (!listenerExecutor.isTerminated() && !listenerExecutor.isShutdown()) {
					// Update the uptimer
					uptimer.updateTime(realElapsedMillisec);
					// Gets the timestamp for the pulse
					timestampPulseStart();				
					// Add time to the Earth clock.
					earthTime = earthTime.plus((long)(earthMillisec * 1000), ChronoField.MICRO_OF_SECOND.getBaseUnit());
					// Add time pulse to Mars clock.
					marsTime = marsTime.addTime(leadPulseTime);
					// Run the clock listener tasks that are in other package
					fireClockPulse(leadPulseTime);
				}
			}
			else if (!acceptablePulse) {
				logger.severe(20_000, "acceptablePulse is false. Pulse width deviated too much: " 
						+ pulseDeviation + ".");
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
	 * Adjusts the optimal pulse and the lead pulse to gradually 
	 * catch up with the reference pulse.
	 * 
	 * @return ratio between the lead pulse and the reference pulse
	 */
	private float computePulseDev() {
		
		float leadPulse = leadPulseTime;
		float refPulse = referencePulse;
		float ratio = 0;
		
		// Get the ratio of sleep time to execution time [dimension-less]
		float d = sleepTime / (1 + executionTime);
	
		if (d > 3 && pulseDeviation > -2) {
			// If sleepTime is +ve, then there's surplus of CPU, decrease leadPulse
			leadPulse = (float) (MathUtils.between((1 - d / 3_000), .999_999, 1.000_001) * leadPulse);
		}
		else if (d < -3 && pulseDeviation < 2) {
			// If sleepTime is -ve, then there's lack of CPU, increase leadPulse			
			leadPulse = (float) (MathUtils.between((1 - d / 3_000), .999_999, 1.000_001) * leadPulse);
		}


		///////////////////////////

		// Between actualTR and desiredTR
		ratio = actualTR / desiredTR;

		if (ratio > 1.001) {
			leadPulse = leadPulse + (1 - ratio) * leadPulse / PULSE_STEPS / 2;
			if (leadPulse > refPulse) {
				leadPulse = refPulse;
			}					
				
			// Update the lead pulse time
			leadPulseTime = leadPulse;
		}
		else if (ratio < 0.999) {
			leadPulse = leadPulse - (ratio - 1) * leadPulse / PULSE_STEPS * 5;
			if (leadPulse < refPulse) {
				leadPulse = refPulse;
			}				
				
			// Update the lead pulse time
			leadPulseTime = leadPulse;
		}		

		ratio = leadPulse / refPulse;

		if (ratio < .3) {
			leadPulse = leadPulse + (1 - ratio) * leadPulse / PULSE_STEPS / 2;
			if (leadPulse > refPulse) {
				leadPulse = refPulse;
			}					
				
			// Update the lead pulse time
			leadPulseTime = leadPulse;
		}
		else if (ratio > 3) {
			leadPulse = leadPulse - (ratio - 1) * leadPulse / PULSE_STEPS * 5;
			if (leadPulse < refPulse) {
				leadPulse = refPulse;
			}				
				
			// Update the lead pulse time
			leadPulseTime = leadPulse;
		}		
		
		// Update the pulse time for use in tasks
		float newTaskPulseWidth = (float) (taskPulseRatio * INITIAL_PULSE_WIDTH 
				+ (1 - taskPulseRatio) * leadPulse / taskPulseDamper / cpuUtil * 5_000);

		if (taskPulseWidth != newTaskPulseWidth) {
			taskPulseWidth = newTaskPulseWidth;
			Task.setStandardPulseTime(newTaskPulseWidth);
		}

		// Returns the deviation
		return (leadPulse - refPulse) / refPulse;
	}
	
	/**
	 * Prepares clock listener tasks for setting up threads.
	 */
	public class ClockListenerTask implements Callable<String>{
		private double msolsSkipped = 0;
		private long lastPulseDelivered = 0;
		private ClockListener listener;
		private long minDuration;

		/** Count of consecutive exceptions from this listener. */
		private int consecutiveFailures = 0;

		public ClockListener getClockListener() {
			return listener;
		}

		private ClockListenerTask(ClockListener listener, long minDuration) {
			this.listener = listener;
			this.minDuration = minDuration;
			this.lastPulseDelivered = System.currentTimeMillis();
		}

		/** Resets the consecutive failure counter (on success). */
		public void resetFailures() {
			consecutiveFailures = 0;
		}

		/** Increments failure counter (on exception). */
		public void recordFailure(Throwable t) {
			consecutiveFailures++;
		}

		/** Returns current consecutive failure count. */
		public int getConsecutiveFailures() {
			return consecutiveFailures;
		}

		@Override
		public String call() throws Exception {
			if (!isPaused) {
				try {
					// The most important job for ClockListener is to send a clock pulse to listener
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

					// Success: reset failure streak
					resetFailures();
					return "done";
				}
				catch (Throwable e) {
					// Failure: increment streak and log
					recordFailure(e);
					logger.severe(
						"Can't send out clock pulse to listener: " 
							+ (listener != null ? listener.getClass().getName() : "null")
							+ " (consecutive failures=" + getConsecutiveFailures() + "): ",
						e
					);

					// Decide whether to drop this listener
					if (getConsecutiveFailures() >= MAX_CONSECUTIVE_LISTENER_FAILURES) {
						return "drop";
					}
					return "fail";
				}
			}
			return "done";
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ClockListenerTask other)) return false;
			return listener != null && listener.equals(other.listener);
		}

		@Override
		public int hashCode() {
			return (listener == null ? 0 : listener.hashCode());
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
		double currentMillisol = (float) marsTime.getMillisol();
		// Get the current sol
		int currentSol = marsTime.getMissionSol();
				
		////////////////////////////////////////////////////////////////////////////////////
		// Part 1: Update isNewSol and isNewHalfSol
		////////////////////////////////////////////////////////////////////////////////////

		// Identify if this pulse crosses a sol
		final boolean isNewSol = (lastSol != currentSol);
		
		// Note : variable = (condition) ? expressionTrue : expressionFalse
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
		final boolean isNewIntMillisol = (lastIntMillisol != currentIntMillisol);
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
		this.lastMillisol = (float) currentMillisol;
	
		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 4: Print the current sol banner
		////////////////////////////////////////////////////////////////////////////////////

		if (isNewSol)
			printNewSol(currentSol);
		
		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 5: Log the pulse
		////////////////////////////////////////////////////////////////////////////////////

		final long newPulseId = nextPulseId++;
		int logIndex = (int)(newPulseId % MAX_PULSE_LOG);
		pulseLog[logIndex] = System.currentTimeMillis();

		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 6: Create a clock pulse
		////////////////////////////////////////////////////////////////////////////////////

		currentPulse = new ClockPulse(newPulseId, time, marsTime, this, 
				isNewSol, isNewHalfSol, isNewIntMillisol, isNewHalfMillisol);
		
		// Execute all listeners: submit all, then wait for all to complete (CME-safe via COW list)
		if (clockListenerTasks != null && !clockListenerTasks.isEmpty()) {
			final List<ClockListenerTask> submitted = new ArrayList<>(clockListenerTasks);
			final List<Future<String>> futures = new ArrayList<>(submitted.size());

			for (ClockListenerTask task : submitted) {
				try {
					futures.add(listenerExecutor.submit(task));
				}
				catch (RejectedExecutionException ree) {
					// Application shutting down
					Thread.currentThread().interrupt();
					logger.severe("RejectedExecutionException when submitting clock listener task: ", ree);
				}
			}

			// Wait for completion and handle possible "drop" instructions
			for (int i = 0; i < futures.size(); i++) {
				Future<String> f = futures.get(i);
				ClockListenerTask task = submitted.get(i);
				try {
					String status = f.get();
					if ("drop".equals(status)) {
						ClockListener listener = task.getClockListener();
						clockListenerTasks.remove(task);
						logger.warning(
							"Auto-unregistering ClockListener "
								+ (listener != null ? listener.getClass().getName() : "null")
								+ " after " + task.getConsecutiveFailures() + " consecutive failures."
						);
					}
				}
				catch (ExecutionException ee) {
					logger.severe("ExecutionException. Problem with clock listener tasks: ", ee);
				}
				catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					logger.severe("Interrupted during clock listener tasks: ", ie);
				}
			}
		}
	}

	/**
	 * Executes the clock listener task. (kept for compatibility; not used by fireClockPulse anymore)
	 *
	 * @param task
	 */
	public void executeClockListenerTask(ClockListenerTask task) {
		Future<String> result = listenerExecutor.submit(task);

		try {
			// Wait for it to complete so the listeners doesn't get queued up if the MasterClock races ahead
			String status = result.get();

			// If a listener repeatedly failed, drop it automatically.
			if ("drop".equals(status)) {
				ClockListener listener = task.getClockListener();
				clockListenerTasks.remove(task);
				logger.warning(
					"Auto-unregistering ClockListener "
						+ (listener != null ? listener.getClass().getName() : "null")
						+ " after " + task.getConsecutiveFailures() + " consecutive failures."
				);
			}
		} catch (ExecutionException ee) {
			logger.severe("ExecutionException. Problem with clock listener tasks: ", ee);
		} catch (RejectedExecutionException ree) {
			// Application shutting down
			Thread.currentThread().interrupt();
			// Executor is shutdown and cannot complete queued tasks
			logger.severe("RejectedExecutionException. Problem with clock listener tasks: ", ree);
		} catch (InterruptedException ie) {
			// Program closing down
			Thread.currentThread().interrupt();
			logger.severe("InterruptedException. Problem with clock listener tasks: ", ie);
		}
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

		startClockExecutor();

		timestampPulseStart();
		
		logger.info(3_000, "Simulation started.");
	}
	
	/**
	 * Stops the clock.
	 */
	public void stop() {
		clockThreadTask.stopRunning();
		
		logger.info(3_000, "Simulation paused.");
	}

	/**
	 * Starts the listener thread pool executor.
	 */
	private void startListenerExecutor() {
		if (listenerExecutor == null 
				|| listenerExecutor.isShutdown()
				|| listenerExecutor.isTerminated()) {
			logger.config(10_000, "Setting up thread(s) for clock listener.");
			listenerExecutor = Executors.newFixedThreadPool(1,
					new ThreadFactoryBuilder().setNameFormat("clockListener-%d").build());
		}
	}
	
	/**
	 * Starts the clock thread pool executor.
	 */
	private void startClockExecutor() {
		if (clockExecutor == null
				|| clockExecutor.isShutdown()
				|| clockExecutor.isTerminated()) {

			logger.config(10_000, "Setting up the master clock thread executor.");
			clockExecutor = Executors.newSingleThreadExecutor(
					new ThreadFactoryBuilder().setNameFormat("masterclock-%d").build());

			// Recompute pulse load
			// computeNewCpuLoad();
			// Redo the pulses
			computeReferencePulse();
		}
		clockExecutor.execute(clockThreadTask);
	}
	
	/**
	 * Increases the speed or time ratio.
	 */
	public synchronized void increaseSpeed() {
		int tr = desiredTR;
		
		if (tr >= SUPER_HIGH_SPEED_RATIO) {
			return;
		}
		else if (tr >= HIGH_SPEED_RATIO) {
			tr = (int)(tr * 1.125);
		}
		else if (tr >= MID_SPEED_RATIO) {
			tr = (int)(tr * 1.25);
		}
		else if (desiredTR >= LOW_SPEED_RATIO) {
			tr = (int)(tr * 1.5);
		}
		else {
			tr = tr * 2;
		}
		
		logger.config("Speed increased from " + desiredTR + " to " + tr + ".");
		
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
		
		if (tr > HIGH_SPEED_RATIO) {
			tr = (int)Math.round(tr / 1.125);
		}
		else if (tr > MID_SPEED_RATIO) {
			tr = (int)Math.round(tr / 1.25);
		}
		else if (tr > LOW_SPEED_RATIO) {
			tr = (int)Math.round(tr / 1.5);
		}
		else if (tr > 1) {
			tr = (int)Math.round(tr / 2D);
		}
		else {
			return;
		}
		
		logger.config("Speed decreased from " + desiredTR + " to " + tr + ".");
		
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
			// Iterate over snapshot semantics (COW list); removal is safe during iteration.
			for (ClockListenerTask cl : clockListenerTasks) {
				try {
					cl.getClockListener().pauseChange(isPaused, showPane);
					// Success: reset failure streak
					cl.resetFailures();
				}
				catch (Throwable t) {
					// Failure: increment streak and log
					cl.recordFailure(t);
					ClockListener listener = cl.getClockListener();
					logger.severe(
						"ClockListener "
							+ (listener != null ? listener.getClass().getName() : "null")
							+ " threw during pauseChange(paused=" + isPaused + ", showPane=" + showPane
							+ ") [consecutive failures=" + cl.getConsecutiveFailures() + "]: ",
						t
					);

					// Auto-remove if past threshold
				 if (cl.getConsecutiveFailures() >= MAX_CONSECUTIVE_LISTENER_FAILURES) {
						clockListenerTasks.remove(cl);
						logger.warning(
							"Auto-unregistering ClockListener "
								+ (listener != null ? listener.getClass().getName() : "null")
								+ " after " + cl.getConsecutiveFailures()
								+ " consecutive failures during pauseChange."
						);
					}
				}
			}
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
	public float getSleepTime() {
		return sleepTime;
	}

	/**
	 * Gets the next pulse width, namely, the millisols covered in the next pulse.
	 *
	 * @return
	 */
	public float getLeadPulseTime() {
		return leadPulseTime;
	}

	/**
	 * Gets the task pulse width.
	 *
	 * @return
	 */
	public float geTaskPulseWidth() {
		return taskPulseWidth;
	}
	
	/**
	 * Gets the optimal pulse width.
	 *
	 * @return
	 */
	public float getOptPulseTime() {
		return optMilliSolPerPulse;
	}
	
	/**
	 * Gets the reference pulse.
	 *
	 * @return
	 */
	public float getReferencePulse() {
		return referencePulse;
	}
	
	/**
	 * Gets the deviation between the optimal pulse width and the next pulse width.
	 *
	 * @return
	 */
	public float getNextPulseDeviation() {
		return pulseDeviation;
	}
	
	/**
	 * Gets the time [in microseconds] taken to execute one frame in the game loop.
	 *
	 * @return
	 */
	public short getExecutionTime() {
		return executionTime;
	}

	public float getMillisecPerPulse() {
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
		
		float delta = (float) Math.min(deltaTR / 10, 5 * Math.abs(desiredTR/(1 + actualTR)));				
				
		// Get the desired millisols per second
		float desiredMsolPerSec = (float) ((desiredTR - delta) / MarsTime.SECONDS_PER_MILLISOL);

		// Get the desired number of pulses per second
		float desiredPulsesPerSec = (float) (desiredMsolPerSec / 
				(0.1 * optMilliSolPerPulse + 0.6 * referencePulse + 0.3 * leadPulseTime));
		
		// Get the milliseconds between each pulse
		millisecPerPulse = 1000f / desiredPulsesPerSec;
	
		// Update the sleep time that will allow room for the execution time (ms per pulse)
		sleepTime = (float)(Math.round((millisecPerPulse - executionTime) * 10.0) / 10.0);
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
					executionTime = (short) (System.currentTimeMillis() - startTime);
					// Get the sleep time
					calculateSleepTime();				

					// NOTE: When resuming from power save, executionTime is often very high
					if (executionTime > EXE_UPPER_LIMIT) {
				    	logger.severe(EXE_UPPER_LIMIT, String.format("Abnormal execution time: %d ms.", executionTime));
					}
					else if (executionTime > EXE_UPPER_LIMIT / 3) {
				    	logger.severe(EXE_UPPER_LIMIT / 3, String.format("Abnormal execution time: %d ms.", executionTime));
					}
				}
				else {
					// Case 2: acceptablePulse is false
				    if (!isPaused) {
						logger.warning(20_000, "Time Pulse deviated too much. Pause & unpause the sim. "
								+ "Lower the TR or adjust the pulse parameters.");
						// Test if this can restore the simulation
						leadPulseTime = referencePulse;
					}
				}
				
				// If still going then wait
				if (keepRunning && !isPaused && sleepTime > 0) {
					try {
						Thread.sleep((long)sleepTime);
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
