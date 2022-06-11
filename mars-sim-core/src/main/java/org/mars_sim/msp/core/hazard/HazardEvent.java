/*
 * Mars Simulation Project
 * HazardEvent.java
 * @date 2022-06-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.hazard;

import java.io.Serializable;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;

/**
 * This class represents the historical action of a hazard occurring or being
 * resolved.
 */
public class HazardEvent extends HistoricalEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Create an event associated to a hazard.
	 * 
	 * @param eventType		{@link EventType} The Type of hazard event.
	 * @param source		The source for this event.
	 * @param whatCause		The cause for this event.
	 * @param whileDoing	The activity the person was engaging.
	 * @param whoAffected	Who is being primarily affected by this event.
	 * @param location0		The building/vehicle where it occurs.
	 * @param location1		The settlement/coordinate where it occurs.
	 * @param associatedSettlement	The associated settlement.
	 */
	public HazardEvent(EventType eventType, Object source, String whatCause, String whileDoing, String whoAffected,
			String location0, String location1, String associatedSettlement) {
		super(HistoricalEventCategory.HAZARD, eventType, source, whatCause, whileDoing, whoAffected, location0, location1, associatedSettlement);
	}
}
