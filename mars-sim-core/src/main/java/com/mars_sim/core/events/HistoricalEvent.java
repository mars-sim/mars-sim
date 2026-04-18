/*
 * Mars Simulation Project
 * HistoricalEvent.java
 * @date 2025-10-16
 * @author Barry Evans
 */
package com.mars_sim.core.events;

import java.io.Serializable;
import java.util.Objects;

import com.mars_sim.core.Entity;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

/**
 * This class represents a time based event that has occurred in the simulation.
 * It is aimed at being subclassed to reflect the real simulation specific
 * events. An event consists of a time stamp when it occurred, a description, an
 * optional Unit that is the source of the event and an optional Object that has
 * triggered the event.
 */
public class HistoricalEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Type of historical events. */
	private HistoricalEventType type;

	/** Time event occurred. */
	private MarsTime timestamp;
	/** Source of event may be null. */
	private Entity source;
	private Settlement homeTown;
	private String whatCause;
	private String whileDoing;
	private Entity affected;
	private Coordinates coordinates;
	private boolean acknowledged = false;

	/**
	 * Constructs an event that relates directly to an Entity only. The time is not defined
	 * until the event is registered with the Event Manager.
	 * 
	 * @param type The type of event
	 * @param source The source entity of the event (can be null)
	 * @param whatCause A description of the cause of the event
	 * @param whileDoing A description of what the source was doing when the event occurred
	 * 
	 * @see com.mars_sim.core.events.HistoricalEventManager#registerNewEvent
	 */
	public HistoricalEvent(HistoricalEventType type, Entity source, Settlement homeTown, String whatCause,
			String whileDoing)  {
		this(type, source, homeTown,whatCause, whileDoing, null, null);
	}

	
	/**
	 * Constructs an event with the appropriate information. The time is not defined
	 * until the event is registered with the Event Manager.
	 * 
	 * @param type The type of event
	 * @param source The source entity of the event (can be null)
	 * @param whatCause A description of the cause of the event
	 * @param whileDoing A description of what the source was doing when the event occurred
	 * @param affected An entity that was affected by the event (can be null)
	 * 
	 * @see com.mars_sim.core.events.HistoricalEventManager#registerNewEvent
	 */
	public HistoricalEvent(HistoricalEventType type, Entity source, Settlement homeTown, String whatCause,
			String whileDoing, Entity affected)  {
		this(type, source, homeTown,whatCause, whileDoing, affected, null);
	}

	/**
	 * Constructs an event with the appropriate information. The time is not defined
	 * until the event is registered with the Event Manager.
	 * @param type The type of event
	 * @param source The source entity of the event
	 * @param homeTown The associated settlement of the event
	 * @param whatCause A description of the cause of the event
	 * @param whileDoing A description of what the source was doing when the event occurred
	 * @param affected An entity that was affected by the event (can be null)
	 * @param coordinates The coordinates where the event occurred (can be null)
	 */
	public HistoricalEvent(HistoricalEventType type, Entity source, Settlement homeTown, String whatCause,
			String whileDoing, Entity affected, Coordinates coordinates) {
		Objects.requireNonNull(type, "Event type cannot be null");
		Objects.requireNonNull(source, "Event source cannot be null");

		this.type = type;
		this.source = source;
		this.whatCause = whatCause;
		this.whileDoing = whileDoing;
		this.affected = affected;
		this.coordinates = coordinates;
		this.homeTown = homeTown;
	}

	/**
	 * Sets the timestamp for this event.
	 * 
	 * @param marsTime
	 */
	void setTimestamp(MarsTime marsTime) {
		this.timestamp = marsTime;
	}
	
	/**
	 * Gets the cause.
	 * 
	 * @return String the cause.
	 */
	public String getWhatCause() {
		return whatCause;
	}

	/**
	 * Gets the activity a person was engaging.
	 * 
	 * @return String the activity.
	 */
	public String getWhileDoing() {
		return whileDoing;
	}

	/**
	 * Gets the building/vehicle entity affected by the event.
	 * 
	 * @return the affected entity
	 */
	public Entity getAffected() {
		return affected;
	}

	/**
	 * Gets the coordinates.
	 * 
	 * @return the coordinates string
	 */
	public Coordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * Gets the associated settlement.
	 * 
	 * @return the associated settlement string
	 */
	public Settlement getHomeTown() {
		return homeTown;
	}
	
	/**
	 * Gets event time.
	 * 
	 * @return Time the event happened
	 */
	public MarsTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Gets the type of event.
	 * 
	 * @return String representing the type.
	 */
	public HistoricalEventType getType() {
		return type;
	}

	/**
	 * Gets event source.
	 * 
	 * @return source
	 */
	public Entity getSource() {
		return source;
	}
	
	/**
	 * Gets the category of the event.
	 * 
	 * @return {@link HistoricalEventCategory}
	 */
	public HistoricalEventCategory getCategory() {
		return type.getCategory();
	}

	/**
	 * Gets the acknowledged status of the event.
	 * 
	 * @return true if the event has been acknowledged, false otherwise
	 */
	public boolean isAcknowledged() {
		return acknowledged;
	}

	/**
	 * Sets the acknowledged status of the event.
	 * 
	 * @param acknowledged true to mark as acknowledged, false otherwise
	 */
	public void setAcknowledged(boolean acknowledged) {
		this.acknowledged = acknowledged;
	}

	/**
	 * Determines if this event is equivalent to another event. This is used to determine if an event is a repeat of recent events.
	 * @param newEvent The event to be compared with recent events.
	 * @return True if they are equivalent, false otherwise.
	 */
	public boolean isEquivalent(HistoricalEvent newEvent) {
		return (type == newEvent.getType()
				&& source.equals(newEvent.getSource())

				// Cause maybe be null so check for null before equals
				&& ((whatCause == null && newEvent.getWhatCause() == null)
					|| (whatCause != null && whatCause.equals(newEvent.getWhatCause()))));
	}
}
