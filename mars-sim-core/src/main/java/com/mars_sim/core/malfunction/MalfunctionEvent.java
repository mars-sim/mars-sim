/*
 * Mars Simulation Project
 * MalfunctionEvent.java
 * @date 2023-11-04
 * @author Scott Davis
 */
package com.mars_sim.core.malfunction;

import com.mars_sim.core.Unit;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.person.EventType;

/**
 * This class represents the historical action of a Malfunction occurring or
 * being resolved.
 */
public class MalfunctionEvent extends HistoricalEvent {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Create an event associated to a Malfunction.
	 * 
	 * @param type        {@link EventType} Type of event.
	 * @param whileDoing  the activity the person was engaging.
	 * @param whoAffected Who is being primarily affected by this event.
	 * @param entity		the building/vehicle where it occurs
	 * @param homeTown		the associated settlement where it belongs
	 * @param coordinates	the coordinates where it belongs
	 */
	public MalfunctionEvent(EventType type, Malfunction malfunction, String whileDoing,
			String whoAffected, Unit entity) {
		super(HistoricalEventCategory.MALFUNCTION, type, malfunction, malfunction.getName(), whileDoing, whoAffected, entity,
				entity.getAssociatedSettlement());
	}
}
