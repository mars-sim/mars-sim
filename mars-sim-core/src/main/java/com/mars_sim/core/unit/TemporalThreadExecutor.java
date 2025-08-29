/*
 * Mars Simulation Project
 * TemporalThreadExecutor.java
 * Patched: 2025-08-29
 *
 * Per-target lanes (ordered per target, parallel across targets).
 * - Targets are held in CopyOnWriteArrayList (snapshot-safe).
 * - Each target has a SerialExecutor lane; lanes run on a shared pool.
 * - applyPulse() submits one task per target and awaits all (barrier).
 */
package com.mars_sim.core.unit;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

public class TemporalThreadExecutor implements TemporalExecutor {

    private static final SimLogger LOG = SimLogger.getLogger(TemporalThreadExecutor.class.getName());

    /** Snapshot-safe target list; writes copy the backing array. */
    private final CopyOnWriteArrayList<Temporal> targets = new CopyOnWriteArrayList<>();

    /** Shared pool used by all lanes. */
    private final ExecutorService pool;

    /** Per-target serialized lanes. */
    private final ConcurrentHashMap<Temporal, SerialExecutor> lanes = new ConcurrentHashMap<>();

    /** Liveness flag. */
    private final AtomicBoolean running = new AtomicBoolean(true);

    public TemporalThreadExecutor() {
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        int parallelism = Math.max(2, cores); // at least 2 to get some overlap
        this.pool = Executors.newFixedThreadPool(parallelism, namedFactory("TemporalLane-"));
        LOG.config("TemporalThreadExecutor started with parallelism = " + parallelism);
    }

    private static ThreadFactory namedFactory(String prefix) {
        return r -> {
            Thread t = new Thread(r);
            t.setName(prefix + t.getId());
            t.setDaemon(true);
            return t;
        };
    }

    /** A serializing executor that runs tasks one-at-a-time on a backing executor. */
    private static final class SerialExecutor implements Executor {
        private final Executor backend;
        private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
        private final AtomicBoolean running = new AtomicBoolean(false);

        SerialExecutor(Executor backend) {
            this.backend = backend;
        }

        @Override
        public void execute(Runnable command) {
            queue.add(command);
            schedule();
        }

        private void schedule() {
            if (running.compareAndSet(false, true)) {
                backend.execute(() -> {
                    try {
                        Runnable r;
                        while ((r = queue.poll()) != null) {
                            try {
                                r.run();
                            } catch (Throwable ex) {
                                // Isolate failures; continue draining
                                SimLogger.getLogger(SerialExecutor.class.getName())
                                         .severe("SerialExecutor task threw: ", ex);
                            }
                        }
                    } finally {
                        running.set(false);
                        // Check for races where a task arrived after we stopped
                        if (!queue.isEmpty()) schedule();
                    }
                });
            }
        }
    }

    @Override
    public void addTarget(Temporal t) {
        if (t == null) return;
        targets.addIfAbsent(t);
        // Lazily create lane on first pulse for this target
    }

    @Override
    public void removeTarget(Temporal t) {
        if (t == null) return;
        targets.remove(t);
        lanes.remove(t); // release lane
    }

    /**
     * Deliver the pulse to all targets:
     *  - each target enqueued to its own serial lane (preserves per-target order),
     *  - lanes run concurrently on the shared pool,
     *  - wait for all to complete (barrier) to keep the world in sync per pulse.
     */
    @Override
    public void applyPulse(ClockPulse pulse) {
        Objects.requireNonNull(pulse, "pulse");
        if (!running.get()) return;

        final int n = targets.size();
        if (n == 0) return;

        final CountDownLatch latch = new CountDownLatch(n);

        for (Temporal t : targets) {
            // One lane per target
            SerialExecutor lane = lanes.computeIfAbsent(t, k -> new SerialExecutor(pool));
            lane.execute(() -> {
                try {
                    t.timePassing(pulse);
                } catch (Throwable ex) {
                    LOG.severe("Temporal target threw during pulse #" + pulse.getId() + ": ", ex);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean interrupted = false;
        try {
            latch.await();
        } catch (InterruptedException ie) {
            interrupted = true;
            Thread.currentThread().interrupt();
        }
        if (interrupted) {
            LOG.warning("applyPulse interrupted while waiting for target lanes to drain.");
        }
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) return;
        pool.shutdownNow();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                LOG.warning("TemporalThreadExecutor did not terminate within 5s; forcing shutdown.");
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        lanes.clear();
    }
}
