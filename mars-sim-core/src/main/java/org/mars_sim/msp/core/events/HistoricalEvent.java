/**
 * Mars Simulation Project
 * HistoricalEvent.java
 * @version 3.2.0 2021-06-20
 * @author Barry Evans
 */
package org.mars_sim.msp.core.events;

import java.io.Serializable;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.time.MarsTime;

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

	/** Time event occurred. */
	private MarsTime timestamp;
	/** Source of event may be null. */
	private Object source;
	private String whatCause;
	private String whileDoing;
	private String who;
	private Unit container;
	private String homeTown;
	private String coordinates;

	/**
	 * Construct an event with the appropriate information. The time is not defined
	 * until the event is registered with the Event Manager.
	 * 
	 * @param category		{@link HistoricalEventCategory} Category of event
	 * @param type			{@link EventType} Type of event
	 * @param source		The source for this event
	 * @param whatCause		The cause for this event
	 * @param whileDoing	during or While doing what
	 * @param whoAffected	Who is being primarily affected by this event
	 * @param container		the building/vehicle where it occurs
	 * @see org.mars_sim.msp.core.events.HistoricalEventManager#registerNewEvent
	 */
	public HistoricalEvent(HistoricalEventCategory category, EventType type, Object source, String whatCause,
			String whileDoing, String whoAffected, Unit container) {
		this(category, type, source, whatCause, whileDoing, whoAffected, container,
				container.getAssociatedSettlement().getName(),
				container.getCoordinates());
	}

	/**
	 * Construct an event with the appropriate information. The time is not defined
	 * until the event is registered with the Event Manager.
	 * 
	 * @param category		{@link HistoricalEventCategory} Category of event
	 * @param type			{@link EventType} Type of event
	 * @param source		The source for this event
	 * @param whatCause		The cause for this event
	 * @param whileDoing	during or While doing what
	 * @param whoAffected	Who is being primarily affected by this event
	 * @param container		the building/vehicle where it occurs
	 * @param homeTown		the associated settlement where it belongs
	 * @param coordinates	the coordinates where it belongs
	 * @see org.mars_sim.msp.core.events.HistoricalEventManager#registerNewEvent
	 */
	public HistoricalEvent(HistoricalEventCategory category, EventType type, Object source, String whatCause,
			String whileDoing, String whoAffected, Unit container, String homeTown, Coordinates coordinates) {
		this.category = category;
		this.type = type;
		this.source = source;
		this.whatCause = whatCause;
		this.whileDoing = whileDoing;
		this.who = whoAffected;
		this.container = container;
		this.homeTown = homeTown;
		this.coordinates = coordinates.getFormattedString();
	}

	/**
	 * Set the timestamp for this event.
	 * 
	 * @param marsTime
	 */
	void setTimestamp(MarsTime marsTime) {
		this.timestamp = marsTime;
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
	 * Get the building/vehicle container.
	 * 
	 * @return he building/vehicle container string
	 */
	public Unit getContainer() {
		return container;
	}

	/**
	 * Get the coordinates.
	 * 
	 * @return the coordinates string
	 */
	public String getCoordinates() {
		return coordinates;
	}

	/**
	 * Get the associated settlement.
	 * 
	 * @return the associated settlement string
	 */
	public String getHomeTown() {
		return homeTown;
	}
	
	/**
	 * Get event time.
	 * 
	 * @return Time the event happened
	 */
	public MarsTime getTimestamp() {
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
