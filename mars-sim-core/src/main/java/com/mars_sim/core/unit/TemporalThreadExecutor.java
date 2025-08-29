/*
 * Mars Simulation Project
 * TemporalThreadExecutor.java
 * Patched: 2025-08-28
 *
 * Per-target lanes executor:
 * - Each target (Temporal) is assigned a dedicated single-thread "lane"
 *   to preserve strict in-order delivery for that target across pulses.
 * - Lanes run in parallel across different targets for higher throughput.
 * - Targets are stored in a CopyOnWriteArrayList for CME-safe snapshots.
 * - Each pulse is barriered: we wait until all lanes finish the pulse.
 */
package com.mars_sim.core.unit;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
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

    /** Snapshot-safe target list. */
    private final CopyOnWriteArrayList<Temporal> targets = new CopyOnWriteArrayList<>();

    /**
     * One dedicated single-thread executor per target ("lane") to ensure
     * in-order delivery for that target while allowing parallelism across targets.
     */
    private final ConcurrentHashMap<Temporal, ExecutorService> lanes = new ConcurrentHashMap<>();

    /** Liveness flag. */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /** Thread name prefix for lanes. */
    private final String lanePrefix = "TemporalLane-";

    public TemporalThreadExecutor() { }

    @Override
    public void addTarget(Temporal t) {
        if (t == null) return;
        targets.addIfAbsent(t);
        // Lazily create a per-target single-thread lane if missing or terminated.
        lanes.compute(t, (key, svc) -> {
            if (svc == null || svc.isShutdown() || svc.isTerminated()) {
                return Executors.newSingleThreadExecutor(namedFactory(lanePrefix));
            }
            return svc;
        });
    }

    @Override
    public void removeTarget(Temporal t) {
        if (t == null) return;
        targets.remove(t);
        ExecutorService lane = lanes.remove(t);
        if (lane != null) {
            lane.shutdownNow();
            try {
                lane.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Delivers the pulse to targets in parallel (one lane per target) while preserving
     * ordering per target. We block until all lanes finish the pulse to maintain
     * pulse-boundary semantics.
     */
    @Override
    public void applyPulse(ClockPulse pulse) {
        Objects.requireNonNull(pulse, "pulse");
        if (!running.get()) return;

        // Snapshot of targets (COW avoids CME)
        final int n = targets.size();
        if (n == 0) return;

        final CountDownLatch latch = new CountDownLatch(n);

        for (Temporal t : targets) {
            // Fetch lane; if absent (e.g., removed concurrently), skip but count down
            final ExecutorService lane = lanes.get(t);
            if (lane == null || lane.isShutdown() || lane.isTerminated()) {
                latch.countDown();
                continue;
            }

            lane.submit(() -> {
                try {
                    t.timePassing(pulse);
                } catch (Throwable ex) {
                    // Never let one bad target kill the entire pulse fanout
                    LOG.severe("Temporal target threw during pulse #" + pulse.getId() + ": ", ex);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Barrier: wait until all lanes submitted above finish this pulse
        boolean interrupted = false;
        try {
            latch.await();
        } catch (InterruptedException ie) {
            interrupted = true;
            Thread.currentThread().interrupt();
        }
        if (interrupted) {
            LOG.warning("applyPulse interrupted while waiting for per-target lanes to complete.");
        }
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) return;

        // Shut down all lanes
        for (ExecutorService lane : lanes.values()) {
            try {
                lane.shutdownNow();
            } catch (Throwable t) {
                // ignore
            }
        }
        for (ExecutorService lane : lanes.values()) {
            try {
                lane.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        lanes.clear();
        targets.clear();
    }

    /** Simple named thread factory with daemon threads. */
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
}
