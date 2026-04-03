/*
 * Mars Simulation Project
 * HistoricalEventListener.java
 * @date 2024-08-10
 * @author Barry Evans
 */

package com.mars_sim.core.events;

/**
 * This interface is implemented by any object that is to receive notification
 * of the registration or removal of an HistoricalEvent.
 */
public interface HistoricalEventListener {

	/**
	 * A new event has been added at the specified manager.
	 *
	 * @param event The new {@link HistoricalEvent} added.
	 */
	public void eventAdded(HistoricalEvent event);
}
