/*
 * Mars Simulation Project
 * EntityListenerManager.java
 * @date 2026-06-11
 * @author Barry Evans
 */
package com.mars_sim.core;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.mars_sim.core.logging.SimLogger;

/**
 * This class manages the entity listeners for an entity and provides a method to fire events to those listeners.
 * It also includes metrics for monitoring the number of events fired and listeners notified.
 */
public class EntityListenerManager {
    private static final SimLogger logger = SimLogger.getLogger(EntityListenerManager.class.getName());

    // Metrics for monitoring the number of events fired and listeners notified
    private static class EventMetric {
        private long eventFired = 0;
        private long listenersNotified = 0;

        public void recordEvent(int listenersCount) {
            eventFired++;
            listenersNotified += listenersCount;
        }

        public long getEventsFired() {
            return eventFired;
        }

        public long getListenersNotified() {
            return listenersNotified;
        }
    }

    private static Map<String, EventMetric> metrics = new TreeMap<>();
    private static long lastFlushTime = System.currentTimeMillis();
    private static final long FLUSH_INTERVAL_MS = 60000; // Flush metrics every 60 seconds

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
        // Uncomment only when needed as this is an expensive operation
        //updateMetrics(newEvent, listeners.size());
    }

    /**
     * This method updates the global metrics for the number of listeners and events fired.
     * If the valid period has passed then it will flush the metrics to the logger and reset the counts.
     * @param newEvent Event that was fired.
     * @param listenersCount Number of listeners the event was fired to.
     */
    private static void updateMetrics(EntityEvent newEvent, int listenersCount) {
        synchronized (metrics) {
            var key = newEvent.getSource().getClass().getSimpleName() + ":" + newEvent.getType();
            EventMetric metric = metrics.computeIfAbsent(key, k -> new EventMetric());
            metric.recordEvent(listenersCount);

            // Flush metrics if the interval has passed
            if (System.currentTimeMillis() - lastFlushTime > FLUSH_INTERVAL_MS) {
                int totalEvents = 0;
                int totalListeners = 0;
                for (Map.Entry<String, EventMetric> entry : metrics.entrySet()) {
                    EventMetric recordedMetric = entry.getValue();

                    totalEvents += recordedMetric.getEventsFired();
                    totalListeners += recordedMetric.getListenersNotified();
                    logger.info("Events - Source: " + entry.getKey() + ", Fired: " + recordedMetric.getEventsFired()
                                + ", Notified: " + recordedMetric.getListenersNotified());
                }
                logger.info("Events - Total over " + FLUSH_INTERVAL_MS / 1000 + " s, Fired: " + totalEvents
                                        + ", Notified: " + totalListeners);

                // Reset
                lastFlushTime = System.currentTimeMillis();
                metrics.clear();
            }
        }
    }
}
