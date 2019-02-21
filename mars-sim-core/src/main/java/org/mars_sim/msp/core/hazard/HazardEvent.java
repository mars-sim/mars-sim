/**
 * Mars Simulation Project
 * HazardEvent.java
 * @version 3.1.0 2018-06-21
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

//	/** Type of historical events. */
//	private EventType type;
//	/** TODO Long description of historical events should be internationalizable. */
//	private String description;
//	/** Source of event. May be null. */
//	private Object source;
//	/** Actor or witness of the event. */
//	private Object actor;
//	/** Location occurred. */	
//	private String location;

	/**
	 * Create an event associated to a hazard.
	 * 
	 * @param type        {@link EventType} Type of hazard event.
	 * @param whatCause   The cause for this event.
	 * @param whileDoing  the activity the person was engaging.
	 * @param whoAffected Who is being primarily affected by this event.
	 * @param location0   the building/vehicle where it occurs.
	 * @param location1   the settlement/coordinate where it occurs.
	 */
	public HazardEvent(EventType type, Object source, String whatCause, String whileDoing, String whoAffected,
			String location0, String location1, String associatedSettlement) {
		super(HistoricalEventCategory.HAZARD, type, source, whatCause, whileDoing, whoAffected, location0, location1, associatedSettlement);
	}
}