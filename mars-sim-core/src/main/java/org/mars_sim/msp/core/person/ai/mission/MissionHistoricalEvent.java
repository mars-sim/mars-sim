/**
 * Mars Simulation Project
 * MissionHistoricalEvent.java
 * @version 3.07 2015-01-21

 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;

/**
 * This class represents the historical actions involving missions.
 */
public class MissionHistoricalEvent extends HistoricalEvent implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
	/**
     * Constructor 1.
     * @param person The person on the mission.
     * @param mission The mission with the event.
     * @param eventType The type of event.
     */
    public MissionHistoricalEvent(MissionMember member, Mission mission, String location, EventType eventType) {
        // Use HistoricalEvent constructor.
        super(HistoricalEventCategory.MISSION, 
        		eventType, 
        		member, 
        		location,
        		mission.getName());
    }
}
