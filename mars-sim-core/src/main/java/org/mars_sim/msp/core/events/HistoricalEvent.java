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
 * events. An event consists of a time stamp when it occurred, a description, an
 * optional Unit that is the source of the event and an optional Object that has
 * triggered the event.
 */
public abstract class HistoricalEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Category of event.
	 * 
	 * @see HistoricalEventManager
	 * @see HistoricalEventCategory
	 */
	private HistoricalEventCategory category;
	/** Type of historical events. */
	private EventType type;
//	/** TODO Long description of historical events should be internationalizable. */
//	private String description;
	/** Time event occurred. */
	private MarsClock timestamp;
	/** Source of event may be null. */
	private Object source;
	private String whileDoing;
	private String whatCause;
	private String who;
	private String location0;
	private String location1;
	private String associatedSettlement;

	/**
	 * Construct an event with the appropriate information. The time is not defined
	 * until the evnet is registered with the Event Manager.
	 * 
	 * @param category    {@link HistoricalEventCategory} Category of event.
	 * @param type        {@link EventType} Type of event.
	 * @param whatCause   The cause for this event
	 * @param whoAffected Who is being primarily affected by this event.
	 * @param location0   the building/vehicle where it occurs
	 * @param location1   the settlement/coordinate where it occurs
	 * @see org.mars_sim.msp.core.events.HistoricalEventManager#registerNewEvent
	 */
	public HistoricalEvent(HistoricalEventCategory category, EventType type, Object source, String whatCause,
			String whileDoing, String whoAffected, String location0, String location1, String associatedSettlement) {
		this.category = category;
		this.type = type;
		this.source = source;
		this.whatCause = whatCause;
		this.whileDoing = whileDoing;
		this.who = whoAffected;
		this.location0 = location0;
		this.location1 = location1;
		this.associatedSettlement = associatedSettlement;
	}

	/**
	 * Set the timestamp for this event.
	 * 
	 * @param timestamp
	 */
	void setTimestamp(MarsClock timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Get the cause.
	 * 
	 * @return String the cause.
	 */
	public String getWhatCause() {
		return whatCause;
	}

	/**
	 * Get the activity a person was engaging.
	 * 
	 * @return String the activity.
	 */
	public String getWhileDoing() {
		return whileDoing;
	}

	/**
	 * Get the name of the offender or the person affected.
	 * 
	 * @return String the name.
	 */
	public String getWho() {
		return who;
	}

	/**
	 * Get the building/vehicle.
	 * 
	 * @return String the building/vehicle.
	 */
	public String getLocation0() {
		return location0;
	}

	/**
	 * Get the settlement/coordinates.
	 * 
	 * @return String the settlement/coordinates
	 */
	public String getLocation1() {
		return location1;
	}

	/**
	 * Get the settlement/coordinates.
	 * 
	 * @return String the settlement/coordinates
	 */
	public String getAssociatedSettlement() {
		return associatedSettlement;
	}
	
	/**
	 * Get event time.
	 * 
	 * @return Time the event happened
	 */
	public MarsClock getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the type of event.
	 * 
	 * @return String representing the type.
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * Get event source.
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
		return category;
	}
}