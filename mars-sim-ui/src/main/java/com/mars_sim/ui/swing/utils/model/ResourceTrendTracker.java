/*
 * Mars Simulation Project
 * ResourceTrendTracker.java
 * @date 2026-09-26
 * @author Shalini H R
 */
package com.mars_sim.ui.swing.utils.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mars_sim.ui.swing.components.TrendValue.Trend;

/**
 * Remembers the last value of a resource per owner so the direction of change can be derived.
 * The trend is only recalculated when a real change is reported via {@link #update}, so repeated
 * repaints of the table do not reset it. Updates arrive on the simulation thread whilst reads
 * happen on the Swing thread, hence the concurrent map.
 *
 * @param <T> Type of the owner of the resources
 */
class ResourceTrendTracker<T> {

	/**
	 * Minimum change from the reference value before a new trend is reported.
	 * This stops tiny fluctuations flipping the trend on every update.
	 */
	static final double THRESHOLD = 0.1;

	private record Key(Object owner, int resourceID) {}

	private record Entry(double reference, Trend trend) {}

	private final Map<Key, Entry> entries = new ConcurrentHashMap<>();

	/**
	 * Records a new value for a resource and recalculates its trend.
	 * The first value recorded has a trend of SAME.
	 *
	 * @param owner Owner of the resource
	 * @param resourceID Resource being updated
	 * @param value New value
	 */
	void update(T owner, int resourceID, double value) {
		entries.compute(new Key(owner, resourceID), (k, previous) -> {
			if (previous == null) {
				return new Entry(value, Trend.SAME);
			}
			double diff = value - previous.reference();
			if (Math.abs(diff) < THRESHOLD) {
				// Not significant; keep the reference so slow changes accumulate
				return previous;
			}
			return new Entry(value, (diff > 0) ? Trend.UP : Trend.DOWN);
		});
	}

	/**
	 * Gets the last calculated trend of a resource.
	 *
	 * @param owner Owner of the resource
	 * @param resourceID Resource
	 * @return Trend; SAME if nothing has been recorded
	 */
	Trend getTrend(T owner, int resourceID) {
		var e = entries.get(new Key(owner, resourceID));
		return (e != null ? e.trend() : Trend.SAME);
	}

	/**
	 * Forgets all values of an owner.
	 *
	 * @param owner Owner to remove
	 */
	void remove(T owner) {
		entries.keySet().removeIf(k -> k.owner().equals(owner));
	}

	/**
	 * Forgets all values.
	 */
	void clear() {
		entries.clear();
	}
}
