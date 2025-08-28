/*
 * Mars Simulation Project
 * ClockEventBus.java
 * @date 2025-08-28
 *
 * A hardened, thread-safe event bus for MasterClock listener callbacks.
 */
package com.mars_sim.core.time;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

import com.mars_sim.core.logging.SimLogger;

/**
 * Thread-safe listener bus for {@link ClockListener} callbacks.
 *
 * <p>Design:
 * <ul>
 *   <li>Listeners are stored in a {@link CopyOnWriteArraySet} to avoid
 *       {@code ConcurrentModificationException} during iteration.</li>
 *   <li>Each listener callback is exception-shielded so one faulty
 *       listener cannot break the dispatch loop.</li>
 *   <li>No locks are held while invoking listeners.</li>
 * </ul>
 */
public final class ClockEventBus {

    private static final SimLogger logger = SimLogger.getLogger(ClockEventBus.class.getName());

    private final CopyOnWriteArraySet<ClockListener> listeners = new CopyOnWriteArraySet<>();

    /** Adds a listener if non-null. */
    public boolean add(ClockListener l) {
        if (l == null) return false;
        return listeners.add(l);
    }

    /** Removes a listener if non-null. */
    public boolean remove(ClockListener l) {
        if (l == null) return false;
        return listeners.remove(l);
    }

    /** Number of registered listeners (snapshot, O(1) on COW set). */
    public int size() {
        return listeners.size();
    }

    /** Removes all listeners. */
    public void clear() {
        listeners.clear();
    }

    /** Dispatches a simulation clock pulse. */
    public void fireClockPulse(double time) {
        for (ClockListener l : listeners) {
            try {
                l.clockPulse(time);
            } catch (Throwable t) {
                logger.severe("ClockListener %s threw during clockPulse(%.4f): %s",
                        safeName(l), time, t);
            }
        }
    }

    /** Dispatches a UI refresh pulse. */
    public void fireUiPulse(double time) {
        for (ClockListener l : listeners) {
            try {
                l.uiPulse(time);
            } catch (Throwable t) {
                logger.severe("ClockListener %s threw during uiPulse(%.4f): %s",
                        safeName(l), time, t);
            }
        }
    }

    /** Dispatches a pause state change. */
    public void firePauseChange(boolean isPaused, boolean showPane) {
        for (ClockListener l : listeners) {
            try {
                l.pauseChange(isPaused, showPane);
            } catch (Throwable t) {
                logger.severe("ClockListener %s threw during pauseChange(paused=%s, showPane=%s): %s",
                        safeName(l), isPaused, showPane, t);
            }
        }
    }

    private static String safeName(ClockListener l) {
        try {
            return Objects.toString(l, "null");
        } catch (Throwable e) {
            return "listener";
        }
    }
}
