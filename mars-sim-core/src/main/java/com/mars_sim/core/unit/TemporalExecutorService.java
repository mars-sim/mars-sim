/*
 * Mars Simulation Project
 * TemporalExecutorService.java
 * Patched: 2025-08-28
 *
 * Parallel fan-out executor using a fixed thread pool. CME-safe:
 * - Targets are held in CopyOnWriteArrayList and iterated via snapshot.
 * - Each target runs in its own task; we wait for all to finish per pulse.
 * - Uses invokeAll() as a barrier per pulse delivery.
 */
package com.mars_sim.core.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

public class TemporalExecutorService implements TemporalExecutor {

    private static final SimLogger LOG = SimLogger.getLogger(TemporalExecutorService.class.getName());

    /** Safe to iterate without CME; write operations copy the backing array. */
    private final CopyOnWriteArrayList<Temporal> targets = new CopyOnWriteArrayList<>();

    /** Backing pool for parallel fan-out. */
    private final ExecutorService pool;

    /** Liveness flag. */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /** Optional prefix for worker thread names. */
    private final String threadPrefix;

    /**
     * @param threadPrefix prefix for worker thread names, may be null
     */
    public TemporalExecutorService(String threadPrefix) {
        this.threadPrefix = (threadPrefix == null ? "Temporal-" : threadPrefix);
        int cpu = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        int parallelism = Math.max(2, cpu); // reasonable default, avoids tiny pools

        this.pool = Executors.newFixedThreadPool(parallelism, namedFactory(this.threadPrefix));
        LOG.config("TemporalExecutorService started with parallelism = " + parallelism);
    }

    private static ThreadFactory namedFactory(String prefix) {
        return new ThreadFactory() {
            private volatile int idx = 0;
            @Override public Thread newThread(Runnable r) {
                Thread t = new Thread(r, prefix + idx++);
                t.setDaemon(true);
                return t;
            }
        };
    }

    @Override
    public void addTarget(Temporal t) {
        if (t == null) return;
        targets.addIfAbsent(t);
    }

    @Override
    public void removeTarget(Temporal t) {
        if (t == null) return;
        targets.remove(t);
    }

    @Override
    public void applyPulse(ClockPulse pulse) {
        Objects.requireNonNull(pulse, "pulse");
        if (!running.get()) return;

        // Snapshot size once; CopyOnWrite ensures this is stable for the loop
        final int n = targets.size();
        if (n == 0) return;

        // Build a barriered batch using invokeAll()
        final List<Callable<Void>> batch = new ArrayList<>(n);
        for (Temporal t : targets) {
            batch.add(() -> {
                try {
                    // Deliver pulse to target
                    t.timePassing(pulse);
                } catch (Throwable ex) {
                    // Never let one bad target kill the pulse
                    LOG.severe("Temporal target threw during pulse #" + pulse.getId() + ": ", ex);
                }
                return null;
            });
        }

        try {
            // invokeAll() blocks until all tasks complete (barrier semantics)
            pool.invokeAll(batch);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOG.warning("applyPulse interrupted while waiting for tasks to complete.");
        } catch (RuntimeException re) {
            // Defensive: in practice, invokeAll doesn't throw RejectedExecutionException here unless pool is broken
            LOG.severe("Unexpected runtime exception during applyPulse invokeAll: ", re);
        }
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) return;
        pool.shutdownNow();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                LOG.warning("TemporalExecutorService did not terminate within 5s; forcing shutdown.");
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
