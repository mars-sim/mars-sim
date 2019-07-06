/**
 * Mars Simulation Project
 * MalfunctionEvent.java
 * @version 3.1.0 2017-09-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;

/**
 * This class represents the historical action of a Malfunction occurring or
 * being resolved.
 */
public class MalfunctionEvent extends HistoricalEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Create an event associated to a Malfunction.
	 * 
	 * @param type        {@link EventType} Type of event.
	 * @param whatCause   The cause for this event.
	 * @param whileDoing  the activity the person was engaging.
	 * @param whoAffected Who is being primarily affected by this event.
	 * @param location0   the building/vehicle where it occurs.
	 * @param location1   the settlement/coordinate where it occurs.
	 * @param associatedSettlement   the associated settlement.
	 */
	public MalfunctionEvent(EventType type, Malfunction malfunction, String whatCause, String whileDoing,
			String whoAffected, String location0, String location1, String associatedSettlement) {
//			Malfunctionable entity, Malfunction malfunction, EventType eventType, Object actor, String location, boolean fixed) {
		super(HistoricalEventCategory.MALFUNCTION, type, malfunction, whatCause, whileDoing, whoAffected, location0,
				location1, associatedSettlement);
	}
}