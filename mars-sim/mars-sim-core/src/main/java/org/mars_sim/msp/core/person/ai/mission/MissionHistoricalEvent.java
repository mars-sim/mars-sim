/**
 * Mars Simulation Project
 * MissionHistoricalEvent.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;

/**
 * This class represents the historical actions involving missions.
 */
public class MissionHistoricalEvent
extends HistoricalEvent {

	/**
	 * Constructor.
	 * @param person The person on the mission.
	 * @param mission The mission with the event.
	 * @param eventType The type of event.
	 */
	public MissionHistoricalEvent(Person person, Mission mission, EventType eventType) {
		// Use HistoricalEvent constructor.
		super(HistoricalEventCategory.MISSION, eventType, person, mission.getName());
	}
}
