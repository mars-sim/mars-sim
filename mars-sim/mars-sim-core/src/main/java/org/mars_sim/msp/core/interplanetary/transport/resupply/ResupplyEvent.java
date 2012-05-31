/**
 * Mars Simulation Project
 * ResupplyEvent.java
 * @version 3.02 2012-05-31
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventManager;

/**
 * An historical event for the arrival of a settlement 
 * resupply mission from Earth.
 */
public class ResupplyEvent extends HistoricalEvent {

    // Resupply event types.
    public static final String RESUPPLY_CREATED = "Resupply Mission Created";
    public static final String RESUPPLY_CANCELLED = "Resupply Mission Canceled";
    public static final String RESUPPLY_LAUNCHED = "Resupply Mission Launched";
    public static final String RESUPPLY_ARRIVED = "Resupply Mission Arrived";
    public static final String RESUPPLY_MODIFIED = "Resupply Mission Modified";
    
	/**
	 * Constructor
	 * @param resupply the resupply mission.
	 * @param eventType the event type string.
	 * @param description of the event.
	 */
	public ResupplyEvent(Resupply resupply, String eventType, String description) {
		super(HistoricalEventManager.SUPPLY, eventType, resupply, 
			description);
	}
}