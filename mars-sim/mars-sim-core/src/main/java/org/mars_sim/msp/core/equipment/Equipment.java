/**
 * Mars Simulation Project
 * Equipment.java
 * @version 2.79 2006-01-18
 * @author Scott Davis
 */
 
package org.mars_sim.msp.core.equipment;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.Task;

/** 
 * The Equipment class is an abstract class that represents  
 * a useful piece of equipment, such as a EVA suite or a
 * medpack.
 */
public abstract class Equipment extends Unit {
	
    /** Constructs an Equipment object
     *  @param name the name of the unit
     *  @param location the unit's location
     */
    Equipment(String name, Coordinates location) {
        super(name, location);
    }

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public Collection<Person> getAffectedPeople() {
        Collection<Person> people = new ConcurrentLinkedQueue<Person>();

		// Check all people.
        Iterator<Person> i = Simulation.instance().getUnitManager().getPeople().iterator(); 
        while (i.hasNext()) {
	    	Person person = i.next();
	    	Task task = person.getMind().getTaskManager().getTask();

		    // Add all people maintaining this equipment.
		    if (task instanceof Maintenance) {
	    	    if (((Maintenance) task).getEntity() == this) {
				    if (!people.contains(person)) people.add(person);
				}
	    	}
	    
	    	// Add all people repairing this equipment.
	    	if (task instanceof Repair) {
	        	if (((Repair) task).getEntity() == this) {
	            	if (!people.contains(person)) people.add(person);
				}
		    }
		}	
	
		return people;
    }
}