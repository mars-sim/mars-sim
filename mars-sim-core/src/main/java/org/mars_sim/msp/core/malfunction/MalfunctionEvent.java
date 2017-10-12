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
public class MalfunctionEvent
extends HistoricalEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Create an event associated to a Malfunction.
	 * @param entity Malfunctionable entity with problem.
	 * @param malfunction Problem that has occurred.
	 * @param location the place where the malfunction took place.
	 * @param fixed Is the malfunction resolved.
	 */
	public MalfunctionEvent(Malfunctionable entity, Malfunction malfunction, String location, boolean fixed) {
		super(
			HistoricalEventCategory.MALFUNCTION,
			(fixed ? EventType.MALFUNCTION_FIXED : EventType.MALFUNCTION_UNFIXED), 
			entity,
			location,
			malfunction.getName() //+ (fixed? " Fixed" : " Occurred")
		);
	}
}