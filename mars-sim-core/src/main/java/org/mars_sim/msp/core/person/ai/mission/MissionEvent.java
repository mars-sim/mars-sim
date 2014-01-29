/**
 * Mars Simulation Project
 * MissionEvent.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.EventObject;

/**
 * A mission change event.
 */
public class MissionEvent extends EventObject {
	
	// Data members
	private String type; // The event type.
	private Object target; // The event target object or null if none.
	
	/**
	 * Constructor
	 * @param source the object throwing the event.
	 * @param type the event type.
	 * @param target the event target object (or null if none)
	 */
	public MissionEvent(Mission source, String type, Object target) {
		// Use EventObject constructor.
		super(source);
		
		this.type = type;
		this.target = target;
	}
	
	/**
	 * Gets the event type.
	 * @return event type.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Gets the event target object.
	 * @return target object or null if none.
	 */
	public Object getTarget() {
		return target;
	}
}