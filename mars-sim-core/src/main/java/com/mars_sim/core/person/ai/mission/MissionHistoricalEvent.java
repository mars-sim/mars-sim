/*
 * Mars Simulation Project
 * MissionHistoricalEvent.java
 * @date 2025-10-16
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.mission;

import com.mars_sim.core.Unit;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.unit.AbstractMobileUnit;

/**
 * This class represents the historical actions involving missions.
 */
public class MissionHistoricalEvent extends HistoricalEvent {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor 1: based on a container.
	 * 
	 * @param mission    the mission with the event.
	 * @param eventType  The type of event.
	 * @param whatCause  The cause for this event.
	 * @param whileDoing the activity the member was engaging.
	 * @param member     the member of this mission.
	 * @param entity	 the building/vehicle where it occurs
	 */
	public MissionHistoricalEvent(EventType eventType, Mission mission, String cause, String whileDoing, String member,
			AbstractMobileUnit entity) {
		// Use HistoricalEvent constructor.
		super(HistoricalEventCategory.MISSION, eventType, mission, cause, whileDoing, member, entity,
					mission.getAssociatedSettlement(),
					entity.getCoordinates());
	}

	
	/**
	 * Constructs an event with the appropriate information. The time is not defined
	 * until the event is registered with the Event Manager.
	 * 
	 * @param type			{@link EventType} Type of event
	 * @param mission		The source for this event
	 * @param cause		The cause for this event
	 * @param whileDoing	during or While doing what
	 * @param member	Who is being primarily affected by this event
	 * @param entity		the building/vehicle where it occurs
	 * @param homeTown		the associated settlement where it belongs
	 * @param coordinates	the coordinates where it belongs
	 */
    public MissionHistoricalEvent(EventType eventType, Mission mission, String cause, String whileDoing,
            String member, Unit entity, Settlement homeTown, Coordinates coordinates) {
		super(HistoricalEventCategory.MISSION, eventType, mission, cause, whileDoing, member, entity,
					homeTown, coordinates);
    }
}
