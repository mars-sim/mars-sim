/**
 * Mars Simulation Project
 * MissionEvent.java
 * @version 2.75 2004-01-15
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.mission;

import org.mars_sim.msp.simulation.events.*;
import org.mars_sim.msp.simulation.person.Person;

/**
 * This class represents the historical actions involving missions.
 */
public class MissionEvent extends HistoricalEvent {
	
	// Mission event types.
	public static final String START = "Mission Started";
	public static final String JOINING = "Mission Joined";
	public static final String FINISH = "Mission Finished";
	public static final String DEVELOPMENT = "Mission Development";

	/**
	 * Constructor
	 * @param person The person on the mission.
	 * @param mission The mission with the event.
	 * @param eventType The type of event.
	 */
	public MissionEvent(Person person, Mission mission, String eventType) {
		
		// Use HistoricalEvent constructor.
		super(HistoricalEventManager.MISSION, eventType, person, mission.getName());
	}
}
