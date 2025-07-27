/*
 * Mars Simulation Project
 * TemporalExecutorService.java
 * @date 2025-07-27
 * @author Barry Evans
 */
package com.mars_sim.core.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

/**
 * This is an implementation of a Temporal Excutor that uses a ExecutorService coupled with Callable 
 * to apply a clock pulse in parallel.
 */
public class TemporalExecutorService implements TemporalExecutor {

	/**
	 * Prepares the Temporal task for setting up its own thread.
	 */
	private static class TemporalTask implements Callable<String> {
		private Temporal target;
		private ClockPulse currentPulse;

		TemporalTask(Temporal target) {
			this.target = target;
		}

		void setCurrentPulse(ClockPulse pulse) {
			this.currentPulse = pulse;
		}

		@Override
		public String call() throws Exception {
			try {
				target.timePassing(currentPulse);
			}
			catch (RuntimeException rte) {
				String msg = "Problem with pulse on " + target
        					  + ": " + rte.getMessage();
	            logger.severe(msg, rte);
	            return msg;
			}
			return target + " completed pulse #" + currentPulse.getId();
		}
	}

    private static final SimLogger logger = SimLogger.getLogger(TemporalExecutorService.class.getName());

    private ExecutorService executor;
    private List<TemporalTask> tasks = new ArrayList<>();

    /**
     * Create a blank temporal executor
     */
    public TemporalExecutorService(String name) {
        logger.config("Setting up a caching thread factory wuth a caching strategy");

        // Use a standard cached thread pool
        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(name + "-%d").build());
    }

    /**
     * Apply a new pulse to all the registered temporals.
     */
    @Override
    public void applyPulse(ClockPulse pulse) {

		// May use parallelStream() after it's proven to be safe
		tasks.stream().forEach(s -> s.setCurrentPulse(pulse));

		// Execute all listener concurrently and wait for all to complete before advancing
		// Ensure that Settlements stay synch'ed and some don't get ahead of others as tasks queue
		try {
			List<Future<String>> results = executor.invokeAll(tasks);
			for (Future<String> future : results) {
				future.get();
			}
		}
		catch (ExecutionException ee) {
			// Problem running the pulse
			logger.severe("Problem running the settlement task pulses : ", ee);
		}
		catch (InterruptedException ie) {
			// Program probably exiting
			if (executor.isShutdown()) {
				Thread.currentThread().interrupt();
			}
		}
    }

    @Override
    public void stop() {
        executor.shutdown();
    }

    @Override
    public void addTarget(Temporal s) {
        tasks.add(new TemporalTask(s));
    }
}
