/*
 * Mars Simulation Project
 * TemporalThreadExecutor.java
 * @date 2025-07-27
 * @author Barry Evans
 */
package com.mars_sim.core.unit;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

/**
 * This class implement a TemporalExecutor that uses a blocking worker Thread per Temporal
 * managed.
 */
public class TemporalThreadExecutor implements TemporalExecutor {
	/**
	 * Prepares the Settlement task for setting up its own thread.
	 */
	private static class TemporalRunnable implements Runnable {
		private Temporal target;
		private ClockPulse currentPulse;
		private Semaphore startLock = new Semaphore(0);  // Sets as zero so initially thread is blocked
		private Semaphore doneLock = new Semaphore(0);  // Sets as zero so initially caller is blocked

		private boolean keepRunning = true;

		
		private TemporalRunnable(Temporal target) {
			this.target = target;
		}

		void applyPulse(ClockPulse pulse) {
			this.currentPulse = pulse;
			startLock.release();	// Release the worker element and processes in the background
		}

		/**
		 * Waits for the current pulse to be applied
		 */
		private void awaitPulse() {
			try {
				doneLock.acquire();
			} catch (InterruptedException e) {
				logger.severe(target + ": Problem waitng for pulse", e);
				Thread.currentThread().interrupt();
			}
		}

		private void stop() {
			keepRunning = false;
            startLock.release();
		}

		@Override
		public void run() {
			// Keep running as will break once keepRunnign flag changes
			while(true) {
				try {
					startLock.acquire();
				} catch (InterruptedException e) {
				    logger.severe(target + ": Problem waiting for startLock", e);
					Thread.currentThread().interrupt();
				} // Wait for the pulse

				// Graceful closedown
				if (!keepRunning) {
					break;
				}

				try {
					target.timePassing(currentPulse);
					doneLock.release();  // Notify parent pulse has been applied
				}
				catch (RuntimeException rte) {
					logger.severe(target + ": Problem with Pulse", rte);
				}	
			}
		}
	}
    
    private static final SimLogger logger = SimLogger.getLogger(TemporalThreadExecutor.class.getName());
    private Set<TemporalRunnable> tasks = new HashSet<>();

    @Override
    public void applyPulse(ClockPulse pulse) {

		// Wakeup each thread by applying the latest Pulse
		tasks.stream().forEach(s -> s.applyPulse(pulse));

		// Waits for all to apply the new pulse as this is a blocking operation
		// order of completion does not matter
		// These must be seperate to applow the Thread to process
		tasks.stream().forEach(s -> s.awaitPulse());
    }

    /**
     * Stops all threads.
     */
    @Override
    public void stop() {
        logger.info("Stopping executor");
        tasks.stream().forEach(s -> s.stop());
    }

    /**
     * Adds a new Temporal to the executor. This will create a new Thread.
     * 
     * @param s Temporal to add
     */
    @Override
    public void addTarget(Temporal s) {
        var task = new TemporalRunnable(s);
        tasks.add(task);

        var t = new Thread(task, s.toString() + " Pulse");
        t.start();
    }
}
