/**
 * Mars Simulation Project
 * MissionEvent.java
 * @version 2.80 2006-09-06
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.EventObject;

public class MissionEvent extends EventObject {
	
	// Event types
	public static final int NAME = 0;
	public static final int DESCRIPTION = 1;
	public static final int PHASE = 3;
	public static final int PHASE_DESCRIPTION = 4;
	
	// Data members
	private int type; // The event type.
	
	/**
	 * Constructor
	 * @param source the object throwing the event.
	 * @param type the event type.
	 */
	public MissionEvent(Object source, int type) {
		// Use EventObject constructor.
		super(source);
		
		this.type = type;
	}
	
	/**
	 * Gets the event type.
	 * @return event type.
	 */
	public int getType() {
		return type;
	}
}