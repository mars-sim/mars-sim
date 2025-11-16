/*
 * Mars Simulation Project
 * EntityEvent.java
 * @date 2025-11-16
 * @author Scott Davis
 */
package com.mars_sim.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * An entity change event.
 */
public class EntityEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The event source. */
	private final Entity source;
	/** The event type. */
	private final String type;
	/** The event target object, if any. */
	private final Object target;

	/**
	 * Constructor.
	 * 
	 * @param source the entity throwing the event.
	 * @param type the type of event.
	 * @param target the target object of the event.
	 */
	public EntityEvent(Entity source, String type, Object target) {
		Objects.requireNonNull(source, "Source entity cannot be null.");
		Objects.requireNonNull(type, "Event type cannot be null.");
		
		this.source = source;
		this.type = type;
		this.target = target;
	}

	/**
	 * Constructor.
	 * 
	 * @param source the entity throwing the event.
	 * @param type the type of event.
	 */
	public EntityEvent(Entity source, String type) {
		this(source, type, null);
	}

	/**
	 * Gets the source entity of the event.
	 * @return source entity
	 */
	public Entity getSource() {
		return source;
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
	 * Override {@link Object#toString()} method.
	 */
	@Override
	public String toString() {
		return type;
	}
}
