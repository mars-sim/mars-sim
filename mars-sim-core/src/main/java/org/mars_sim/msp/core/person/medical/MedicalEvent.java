/**
 * Mars Simulation Project
 * MedicalEvent.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.medical;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;

/**
 * This class represents the historical action of a medical problem occuring or
 * being resolved.  Death is also recorded with medical events.
 */
public class MedicalEvent
extends HistoricalEvent {

	/**
	 * Constructor.
	 * @param person the person with the medical problem.
	 * @param illness the medical problem.
	 * @param eventType the medical event type.
	 */
	public MedicalEvent(Person person, HealthProblem illness, EventType eventType) {
		
		// Call HistoricalEvent constructor.
		super(HistoricalEventCategory.MEDICAL, eventType, person, 
			illness.getIllness().getType().getName());
	}
}
