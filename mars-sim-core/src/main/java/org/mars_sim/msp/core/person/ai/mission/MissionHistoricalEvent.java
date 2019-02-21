/**
 * Mars Simulation Project
 * MissionHistoricalEvent.java
 * @version 3.1.0 2017-10-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;

/**
 * This class represents the historical actions involving missions.
 */
public class MissionHistoricalEvent extends HistoricalEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor 1.
	 * 
	 * @param mission    the mission with the event.
	 * @param eventType  The type of event.
	 * @param whatCause  The cause for this event.
	 * @param whileDoing the activity the member was engaging.
	 * @param member     a member of this mission.
	 * @param location0  the building/vehicle where it occurs.
	 * @param location1  the settlement/coordinate where it occurs.
	 */
	public MissionHistoricalEvent(EventType eventType, Mission mission, String cause, String whileDoing, String member,
			String location0, String location1, String associatedsettlement) {
		// Use HistoricalEvent constructor.
		super(HistoricalEventCategory.MISSION, eventType, mission, cause, whileDoing, member, location0, location1, associatedsettlement);
	}
}
