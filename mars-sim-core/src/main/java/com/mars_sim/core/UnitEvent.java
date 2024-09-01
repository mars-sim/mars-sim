/*
 * Mars Simulation Project
 * UnitEvent.java
 * @date 2024-08-10
 * @author Scott Davis
 */
package com.mars_sim.core;

import java.util.EventObject;

/**
 * A unit change event.
 */
public class UnitEvent
extends EventObject {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The event type. */
	private final UnitEventType type;
	/** The event target object, if any. */
	private final Object target;

	/**
	 * Constructor.
	 * 
	 * @param source the object throwing the event.
	 * @param type the type of event.
	 */
	public UnitEvent(Unit source, UnitEventType type, Object target) {
		// Use EventObject constructor
		super(source);
		this.type = type;
		this.target = target;
	}

	/**
	 * Gets the type of event.
	 * @return event type
	 */
	public UnitEventType getType() {
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
	 * Override {@link Object#toString()} method.
	 */
	@Override
	public String toString() {
		return type.getName();
	}
}
