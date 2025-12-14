/*
 * Mars Simulation Project
 * HistoricalEvent.java
 * @date 2025-10-16
 * @author Barry Evans
 */
package com.mars_sim.core.events;

import java.io.Serializable;

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
	private Object source;
	private String whatCause;
	private String whileDoing;
	private String who;
	private Entity entity;
	private Settlement homeTown;
	private Coordinates coordinates;

	/**
	 * Constructs an event with the appropriate information. The time is not defined
	 * until the event is registered with the Event Manager.
	 * 
	 * @param type			{@link HistoricalEventType} Type of event
	 * @param source		The source for this event
	 * @param whatCause		The cause for this event
	 * @param whileDoing	during or While doing what
	 * @param whoAffected	Who is being primarily affected by this event
	 * @param entity		the building/vehicle where it occurs
	 * @see com.mars_sim.core.events.HistoricalEventManager#registerNewEvent
	 */
	public HistoricalEvent(HistoricalEventType type, Object source, String whatCause,
			String whileDoing, String whoAffected, Entity entity, Settlement settlement) {
		this(type, source, whatCause, whileDoing, whoAffected, entity,
				settlement, settlement.getCoordinates());
	}

	/**
	 * Constructs an event with the appropriate information. The time is not defined
	 * until the event is registered with the Event Manager.
	 * 
	 * @param type			{@link HistoricalEventType} Type of event
	 * @param source		The source for this event
	 * @param whatCause		The cause for this event
	 * @param whileDoing	during or While doing what
	 * @param whoAffected	Who is being primarily affected by this event
	 * @param entity		where the event occurs
	 * @param settlement	the associated settlement where it belongs
	 * @param coordinates	the coordinates where it belongs
	 * @see com.mars_sim.core.events.HistoricalEventManager#registerNewEvent
	 */
	public HistoricalEvent(HistoricalEventType type, Object source, String whatCause,
			String whileDoing, String whoAffected, Entity entity, Settlement settlement, Coordinates coordinates) {
		this.type = type;
		this.source = source;
		this.whatCause = whatCause;
		this.whileDoing = whileDoing;
		this.who = whoAffected;
		this.entity = entity;
		this.homeTown = settlement;
		this.coordinates = coordinates;
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
	 * Gets the name of the offender or the person affected.
	 * 
	 * @return String the name.
	 */
	public String getWho() {
		return who;
	}

	/**
	 * Gets the building/vehicle entity.
	 * 
	 * @return he building/vehicle entity string
	 */
	public Entity getEntity() {
		return entity;
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
	public Object getSource() {
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
}
