/**
 * Mars Simulation Project
 * Equipment.java
 * @version 2.74 2002-04-27
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.equipment;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.malfunction.Malfunctionable;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonCollection;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.task.Maintenance;
import org.mars_sim.msp.simulation.person.ai.task.Repair;
import org.mars_sim.msp.simulation.person.ai.task.Task;

/** The Equipment class is an abstract class that represents  
 *  a useful piece of equipment, such as a EVA suite or a
 *  medpack.
 */
public abstract class Equipment extends Unit implements Malfunctionable {
   
    // Data members
    protected MalfunctionManager malfunctionManager; // The equipment's malfunction manager
	
    /** Constructs an Equipment object
     *  @param name the name of the unit
     *  @param location the unit's location
     *  @param mars the virtual Mars
     */
    Equipment(String name, Coordinates location, Mars mars) {
        super(name, location, mars);

	// Initialize malfunction manager.
	malfunctionManager = new MalfunctionManager(this, mars);
	malfunctionManager.addScopeString("Equipment");
    }

    /**
     * Gets the unit's malfunction manager.
     * @return malfunction manager
     */
    public MalfunctionManager getMalfunctionManager() {
        return malfunctionManager;
    }

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public PersonCollection getAffectedPeople() {
        PersonCollection people = new PersonCollection();

	// Check all people.
        PersonIterator i = mars.getUnitManager().getPeople().iterator(); 
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
