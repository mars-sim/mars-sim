/*
 * Mars Simulation Project
 * EntityListenerManager.java
 * @date 2026-06-11
 * @author Barry Evans
 */
package com.mars_sim.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import com.mars_sim.core.logging.SimLogger;

/**
 * This class manages the entity listeners for an entity and provides a method to fire events to those listeners.
 * It also includes metrics for monitoring the number of events fired and listeners notified.
 */
public class EntityListenerManager {
    private static final SimLogger logger = SimLogger.getLogger(EntityListenerManager.class.getName());

    // Used for synchronizing access to the metrics map and flush operations
    private static final Object metricsLock = new Object();

    private static Map<String, Integer> metrics = new TreeMap<>();
    private static long lastFlushTime = System.currentTimeMillis();
    private static long flushInternal;

    // Consumer to handle flushing of metrics
    private static Consumer<Map<String, Integer>> flushConsumer = null;

    // Accept the expensive cost of CopyOnWriteArraySet as updates are significantly less common than reads
    // and it allows us to avoid synchronisation on the listeners set when firing events.
	private Set<EntityListener> listeners = new CopyOnWriteArraySet<>();

    /**
	 * Checks if it has an entity listener.
	 * 
	 * @param listener
	 * @return
	 */
	public boolean hasEntityListener(EntityListener listener) {
		return listeners.contains(listener);
	}

    
	/**
	 * Adds an entity listener.
	 *
	 * @param newListener the listener to add.
	 */
	public void addEntityListener(EntityListener newListener) {
		if (newListener == null)
			throw new IllegalArgumentException();

		listeners.add(newListener);
	}

	/**
	 * Removes an entity listener.
	 *
	 * @param oldListener the listener to remove.
	 */
	public void removeEntityListener(EntityListener oldListener) {
		if (oldListener == null)
			throw new IllegalArgumentException();

		listeners.remove(oldListener);
	}

	/**
	 * Gets an unmodifiable set of the active listeners on this entity.
	 * 
	 * @return unmodifiable set of entity listeners.
	 */
	public Set<EntityListener> getEntityListeners() {
		return Collections.unmodifiableSet(listeners);
	}

    /**
     * Fire the event to all registers listeners,
     * @param newEvent Event to fire.
     */
    public void fireEvent(EntityEvent newEvent) {
        for(EntityListener i : listeners) {
            try {
                // Stop listeners breaking the update thread
                i.entityUpdate(newEvent);
            }
            catch(RuntimeException rte) {
                logger.severe(newEvent.getSource(), "Problem executing listener " + i + " for event " + newEvent, rte);
            }
        }

        // Update metrics
        if (flushConsumer != null)
            updateMetrics(newEvent);
    }

    /**
     * Registers a flush consumer and interval for updating metrics.
     * @param consumer the consumer to handle flushed metrics.
     * @param interval the interval in milliseconds at which to flush metrics.
     */
    public static void registerFlush(Consumer<Map<String, Integer>> consumer, long interval) {
        flushInternal = interval;
        flushConsumer = consumer;
        lastFlushTime = System.currentTimeMillis();
    }

    /**
     * This method updates the global metrics for the number of listeners and events fired.
     * If the valid period has passed then it will flush the metrics to the logger and reset the counts.
     * @param newEvent Event that was fired.
W     */
    private static void updateMetrics(EntityEvent newEvent) {
        synchronized (metricsLock) {
            metrics.merge(newEvent.getType(), 1, (a,b) -> a + b);

            // Flush metrics if the interval has passed
            if (System.currentTimeMillis() - lastFlushTime > flushInternal) {
                if (flushConsumer != null) {
                    // Flush the recorded events
                    flushConsumer.accept(metrics);
                }

                logger.info("Events Flushed after " + flushInternal / 1000 + " s, recorded " + metrics.size());

                // Reset, new map so the consumer can keep the old metrics if needed
                lastFlushTime = System.currentTimeMillis();
                metrics = new HashMap<>();
            }
        }
    }
}
