/**
 * Mars Simulation Project
 * Transportable.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport;

import org.mars_sim.msp.core.time.MarsClock;

/**
 * An interface for an item that is transported between planets/moons/etc.
 */
public interface Transportable
extends Comparable<Transportable>{

	// Launching locations and destinations.
	public static final String MARS = "Mars";
	public static final String EARTH = "Earth";

	/**
	 * Gets the name of the transportable.
	 * @return name string.
	 */
	public String getName();

	/**
	 * Gets the settlementName.
	 * @return the settlement's name.
	 */
	public String getSettlementName();

	/**
	 * Gets the current transit state.
	 * @return {@link TransitState} transit state.
	 */
	public TransitState getTransitState();

	/**
	 * Sets the current transit state.
	 * @param transitState {@link TransitState} the transit state.
	 */
	public void setTransitState(TransitState transitState);

	/**
	 * Gets the launch date from the launching location.
	 * @return launch date as a MarsClock instance.
	 */
	public MarsClock getLaunchDate();

	/**
	 * Gets the arrival date at the destination.
	 * @return arrival date as a MarsClock instance.
	 */
	public MarsClock getArrivalDate();

	/**
	 * Perform the arrival of the transportable.
	 */
	public void performArrival();

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy();
}