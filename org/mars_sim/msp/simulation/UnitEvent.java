/**
 * Mars Simulation Project
 * UnitEvent.java
 * @version 2.80 2006-09-08
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation;

import java.util.EventObject;

/**
 * A unit change event.
 */
public class UnitEvent extends EventObject {

	// Data members
	private String type; // The event type.
	
	/**
	 * Constructor
	 * @param source the object throwing the event.
	 * @param type the type of event.
	 */
	public UnitEvent(Unit source, String type) {
		// Use EventObject constructor
		super(source);
		
		this.type = type;
	}
	
	/**
	 * Gets the type of event.
	 * @return event type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Override toString() method.
	 */
	public String toString() {
		return getType();
	}
}