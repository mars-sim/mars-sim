/**
 * Mars Simulation Project
 * Transportable.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport;

import org.mars_sim.msp.core.time.MarsClock;

/**
 * An interface for an item that is transported between planets/moons/etc.
 */
public interface Transportable extends Comparable<Transportable>{

	// Launching locations and destinations.
	public static final String MARS = "Mars";
	public static final String EARTH = "Earth";
	
	// Transit states.
    public final static String PLANNED = "planned";
    public final static String IN_TRANSIT = "in transit";
    public final static String ARRIVED = "arrived";
    public final static String CANCELED = "canceled";
	
    /**
     * Gets the name of the transportable.
     * @return name string.
     */
    public String getName();
    
	/**
	 * Gets the current transit state.
	 * @return transit state string.
	 */
	public String getTransitState();
	
	/**
	 * Sets the current transit state.
	 * @param transitState the transit state string.
	 */
	public void setTransitState(String transitState);
	
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