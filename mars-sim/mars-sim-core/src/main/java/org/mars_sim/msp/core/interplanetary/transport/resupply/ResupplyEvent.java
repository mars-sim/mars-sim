/**
 * Mars Simulation Project
 * ResupplyEvent.java
 * @version 3.02 2012-04-09
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

	/**
	 * Constructor
	 * @param settlement the name of the settlement getting the supplies.
	 * @param description of the event.
	 */
	public ResupplyEvent(Resupply resupply, String description) {
		super(HistoricalEventManager.SUPPLY, resupply.getState(), resupply, 
			description);
	}
}