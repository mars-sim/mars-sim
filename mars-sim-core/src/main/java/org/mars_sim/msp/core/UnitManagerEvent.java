/**
 * Mars Simulation Project
 * UnitManagerEvent.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.util.EventObject;

public class UnitManagerEvent
extends EventObject {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// UnitManager event strings.
    public static final String ADD_UNIT = "add unit";
    public static final String REMOVE_UNIT = "remove unit";
	
	// Data members
	private String eventType;
	private Unit unit;
	
	/**
	 * Constructor
	 * @param unitManager the unit manager throwing this event.
	 * @param eventType the type of event.
	 * @param unit the target of the event.
	 */
	UnitManagerEvent(UnitManager unitManager, String eventType, Unit unit) {
		// User EventObject constructor.
		super(unitManager);
		
		this.eventType = eventType;
		this.unit = unit;
	}

	/**
	 * Gets the event type.
	 * @return type string.
	 */
	public String getEventType() {
		return eventType;
	}
	
	/**
	 * Gets the unit target of this event.
	 * @return unit
	 */
	public Unit getUnit() {
		return unit;
	}
}