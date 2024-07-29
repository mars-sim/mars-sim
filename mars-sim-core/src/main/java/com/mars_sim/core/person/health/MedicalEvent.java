/*
 * Mars Simulation Project
 * MedicalEvent.java
 * @date 2023-11-04
 * @author Scott Davis
 */
package com.mars_sim.core.person.health;

import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;

/**
 * This class represents the historical action of a medical problem occuring or
 * being resolved.  Death is also recorded with medical events.
 */
public class MedicalEvent extends HistoricalEvent {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private Person person;

	/**
	 * Constructor.
	 * 
	 * @param person the person with the medical problem.
	 * @param illness the medical problem.
	 * @param eventType the medical event type.
	 */
	public MedicalEvent(Person person, HealthProblem illness, EventType eventType) {
		// Call HistoricalEvent constructor.
		super(HistoricalEventCategory.MEDICAL,  
				eventType, 
				illness, 
				illness.getComplaint().getName(),
				person.getTaskDescription(),
				person.getName(),
				person
				);
		
		this.person = person;
	}
	
    public Person getPerson() {
    	return person;
    }
}
