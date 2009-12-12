/**
 * Mars Simulation Project
 * UnitEvent.java
 * @version 2.80 2006-09-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.util.EventObject;

/**
 * A unit change event.
 */
public class UnitEvent extends EventObject {

	// Data members
	private String type; // The event type.
	private Object target; // The event target object, if any.
	
	/**
	 * Constructor
	 * @param source the object throwing the event.
	 * @param type the type of event.
	 */
	public UnitEvent(Unit source, String type, Object target) {
		// Use EventObject constructor
		super(source);
		
		this.type = type;
		this.target = target;
	}
	
	/**
	 * Gets the type of event.
	 * @return event type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Gets the target object of the event.
	 * @return target object or null if none.
	 */
	public Object getTarget() {
		return target;
	}
	
	/**
	 * Override toString() method.
	 */
	public String toString() {
		return getType();
	}
}