/**
 * Mars Simulation Project
 * HistoricalEvent.java
 * @version 3.1.0 2017-10-05
 * @author Barry Evans
 */
package org.mars_sim.msp.core.events;

import java.io.Serializable;

import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * This class represents a time based event that has occurred in the simulation.
 * It is aimed at being subclassed to reflect the real simulation specific
 * events.
 * An event consists of a time stamp when it occurred, a description, an
 * optional Unit that is the source of the event and an optional Object that has
 * triggered the event.
 */
public abstract class HistoricalEvent implements Serializable {
	
    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	/**
	 * Category of event.
	 * @see HistoricalEventManager
	 * @see HistoricalEventCategory
	 */
	private HistoricalEventCategory category;
	/** Type of historical events. */
	private EventType type;
	/** TODO Long description of historical events should be internationalizable. */
	private String description;
	/** Time event occurred. */
	private MarsClock timestamp;
	/** Source of event may be null. */
	private Object source;
	/** Actor or witness of the event. */
	private Object actor;
	/** Location occurred. */	
	private String location;

	/**
	 * Construct an event with the appropriate information. The time is not
	 * defined until the evnet is registered with the Event Manager.
	 * @param category {@link HistoricalEventCategory} Category of event.
	 * @param type {@link EventType} Type of event.
	 * @param source The object that has produced the event, if this is null
	 * then it is a global simulation event. It could be a Unit or a Building.
	 * @param description Long description of event.
	 * @see org.mars_sim.msp.core.events.HistoricalEventManager#registerNewEvent(HistoricalEvent)
	 */
	public HistoricalEvent(HistoricalEventCategory category, EventType type, Object source, Object actor, String location, String description) {
		this.category = category;
		this.type = type;
		this.source = source;
		this.actor = actor;
		this.location = location;
		this.description = description;
		// need count++ next time: System.out.println("HistoricalEvent.java constructor");
	}

	/**
	 * Set the timestamp for this event.
	 * @see org.mars_sim.msp.core.events.HistoricalEventManager#registerNewEvent(HistoricalEvent)
	 */
	void setTimestamp(MarsClock timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Get description of the event observed.
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get the Unit source.
	 * @return Object as the source of event.
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * Get the actor or witness.
	 * @return Object as the actor of event.
	 */
	public Object getActor() {
		return actor;
	}
	
	/**
	 * Get the location.
	 * @return Object as the location.
	 */
	public String getLocation() {
		return location;
	}
	
	/**
	 * Get event time.
	 * @return Time the event happened
	 */
	public MarsClock getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the type of event.
	 * @return String representing the type.
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * Gets the category of the event.
	 * @return {@link HistoricalEventCategory}
	 */
	public HistoricalEventCategory getCategory() {
		return category;
	}
}