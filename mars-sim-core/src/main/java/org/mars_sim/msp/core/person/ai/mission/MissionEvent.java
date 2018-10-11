/**
 * Mars Simulation Project
 * MissionEvent.java
 * @version 3.1.0 2018-10-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.util.EventObject;

/**
 * A mission change event.
 */
public class MissionEvent extends EventObject {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The event type. */
	private MissionEventType type;
	/** The event target object or null if none. */
	private Object target;

	/**
	 * Constructor
	 * 
	 * @param source         the object throwing the event.
	 * @param addMemberEvent the event type.
	 * @param target         the event target object (or null if none)
	 */
	public MissionEvent(Mission source, MissionEventType addMemberEvent, Object target) {
		// Use EventObject constructor.
		super(source);

		this.type = addMemberEvent;
		this.target = target;
	}

	/**
	 * Gets the event type.
	 * 
	 * @return event type.
	 */
	public MissionEventType getType() {
		return type;
	}

	/**
	 * Gets the event target object.
	 * 
	 * @return target object or null if none.
	 */
	public Object getTarget() {
		return target;
	}
}