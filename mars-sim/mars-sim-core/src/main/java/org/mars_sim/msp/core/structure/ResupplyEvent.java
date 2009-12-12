/**
 * Mars Simulation Project
 * ResupplyEvent.java
 * @version 2.76 2004-07-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.events.*;

/**
 * An historical event for the arrival of a settlement 
 * resupply mission from Earth.
 */
public class ResupplyEvent extends HistoricalEvent {

	/**
	 * Constructor
	 * @param settlement the name of the settlement getting the supplies.
	 * @param resupplyName the name of the resupply mission.
	 */
	public ResupplyEvent(Settlement settlement, String resupplyName) {
		super(HistoricalEventManager.SUPPLY, "Supplies delivered", settlement, 
			resupplyName + " arrive at " + settlement.getName());
	}
}