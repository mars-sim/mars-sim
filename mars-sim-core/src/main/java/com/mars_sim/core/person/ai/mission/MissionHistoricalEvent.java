/**
 * Mars Simulation Project
 * MissionHistoricalEvent.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import com.mars_sim.core.Unit;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.person.EventType;
import com.mars_sim.mapdata.location.Coordinates;

/**
 * This class represents the historical actions involving missions.
 */
public class MissionHistoricalEvent extends HistoricalEvent {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor based on a container.
	 * 
	 * @param mission    the mission with the event.
	 * @param eventType  The type of event.
	 * @param whatCause  The cause for this event.
	 * @param whileDoing the activity the member was engaging.
	 * @param member     a member of this mission.
	 * @param container		the building/vehicle where it occurs
	 */
	public MissionHistoricalEvent(EventType eventType, Mission mission, String cause, String whileDoing, String member,
			Unit container) {
		// Use HistoricalEvent constructor.
		super(HistoricalEventCategory.MISSION, eventType, mission, cause, whileDoing, member, container);
	}

	
	/**
	 * Construct an event with the appropriate information. The time is not defined
	 * until the event is registered with the Event Manager.
	 * 
	 * @param type			{@link EventType} Type of event
	 * @param mission		The source for this event
	 * @param cause		The cause for this event
	 * @param whileDoing	during or While doing what
	 * @param member	Who is being primarily affected by this event
	 * @param container		the building/vehicle where it occurs
	 * @param homeTown		the associated settlement where it belongs
	 * @param coordinates	the coordinates where it belongs
	 */
    public MissionHistoricalEvent(EventType eventType, Mission mission, String cause, String whileDoing,
            String member, Unit container, String homeTown, Coordinates coordinates) {
		super(HistoricalEventCategory.MISSION, eventType, mission, cause, whileDoing, member, container,
					homeTown, coordinates);
    }
}
