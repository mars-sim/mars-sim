/**
 * Mars Simulation Project
 * MissionEvent.java
 * @version 2.80 2006-09-06
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.EventObject;

/**
 * A mission change event.
 */
public class MissionEvent extends EventObject {
	
	// Data members
	private String type; // The event type.
	
	/**
	 * Constructor
	 * @param source the object throwing the event.
	 * @param type the event type.
	 */
	public MissionEvent(Mission source, String type) {
		// Use EventObject constructor.
		super(source);
		
		this.type = type;
	}
	
	/**
	 * Gets the event type.
	 * @return event type.
	 */
	public String getType() {
		return type;
	}
}