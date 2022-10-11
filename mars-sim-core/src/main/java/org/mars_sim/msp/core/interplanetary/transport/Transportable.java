/*
 * Mars Simulation Project
 * Transportable.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * An interface for an item that is transported between planets/moons/etc.
 */
public interface Transportable
extends Comparable<Transportable>{

	/**
	 * Gets the name of the transportable.
	 * 
	 * @return name string.
	 */
	public String getName();

	/**
	 * Gets the settlementName.
	 * 
	 * @return the settlement's name.
	 */
	public String getSettlementName();

	/**
	 * Get the Landing site
	 */
	public Coordinates getLandingLocation();

	/**
	 * Gets the current transit state.
	 * 
	 * @return {@link TransitState} transit state.
	 */
	public TransitState getTransitState();

	/**
	 * Sets the current transit state.
	 * 
	 * @param transitState {@link TransitState} the transit state.
	 */
	public void setTransitState(TransitState transitState);

	/**
	 * Gets the launch date from the launching location.
	 * 
	 * @return launch date as a MarsClock instance.
	 */
	public MarsClock getLaunchDate();

	/**
	 * Gets the arrival date at the destination.
	 * 
	 * @return arrival date as a MarsClock instance.
	 */
	public MarsClock getArrivalDate();

	/**
	 * Performs the arrival of the transportable.
	 */
	public void performArrival(SimulationConfig sc, Simulation sim);

	/**
	 * Reinitializes a loading item to the active UnitManager.
	 * 
	 * @param um
	 */
	public void reinit(UnitManager um);

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy();
}
