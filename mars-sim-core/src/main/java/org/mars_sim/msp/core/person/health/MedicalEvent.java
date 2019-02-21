/**
 * Mars Simulation Project
 * MedicalEvent.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.health;

import java.io.Serializable;

import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;

/**
 * This class represents the historical action of a medical problem occuring or
 * being resolved.  Death is also recorded with medical events.
 */
public class MedicalEvent
extends HistoricalEvent implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private Person person;

	/**
	 * Constructor.
	 * @param person the person with the medical problem.
	 * @param illness the medical problem.
	 * @param eventType the medical event type.
	 */
	public MedicalEvent(Person person, HealthProblem illness, EventType eventType) {
		// Call HistoricalEvent constructor.
		super(HistoricalEventCategory.MEDICAL,  
				eventType, 
				illness, 
				illness.getIllness().getType().getName(),
				person.getTaskDescription(),
				person.getName(),
				person.getLocationTag().getImmediateLocation(),
				person.getLocationTag().getLocale(),
				person.getAssociatedSettlement().getName()
				);
		
		this.person = person;
	}
	
    public Person getPerson() {
    	return person;
    }
    
    

}
