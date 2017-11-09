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
     * @param person The person on the mission.
     * @param mission The mission with the event.
     * @param eventType The type of event.
     */
    public MissionHistoricalEvent(MissionMember member, Mission mission, String location, String reason, EventType eventType) {
        // Use HistoricalEvent constructor.
        super(HistoricalEventCategory.MISSION, 
        		eventType, 
        		member, 
        		location,
        		reason);
    }
}
