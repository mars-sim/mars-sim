/**
 * Mars Simulation Project
 * Facility.java
 * @version 2.74 2002-04-27
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.*;
import org.mars_sim.msp.simulation.malfunction.*;
import java.io.Serializable;

/** The Facility class is an abstract class that is the parent to all
 *  settlement facilities and has data members and methods common to
 *  all facilities.
 */
public abstract class Facility implements Malfunctionable, Serializable {

    // Data members
    String name;                           // Name of the facility.
    FacilityManager manager;               // The Settlement's FacilityManager.
    MalfunctionManager malfunctionManager; // The facility's malfunction manager.

    /** Constructs a Facility object 
     *  @param manager manager of the facility
     *  @name name of the facility
     */
    public Facility(FacilityManager manager, String name) {
        // Initialize data members
        this.manager = manager;
        this.name = name;

	malfunctionManager = new MalfunctionManager(this, manager.getSettlement().getMars());
	malfunctionManager.addScopeString("Facility");
    }

    /** Returns the name of the facility. 
     *  @return name of the facility
     */
    public String getName() {
        return name;
    }

    /** Returns this facility's manager
     *  @return facility manager
     */
    public FacilityManager getFacilityManager() {
        return manager;
    }

    /** 
     * Gets the malfunction manager.
     * @return malfunction manager
     */
    public MalfunctionManager getMalfunctionManager() {
        return malfunctionManager;
    }
    
    /** Called every clock pulse for time events in facilities.
     *  Override in children to use this. 
     *  @param time the amount of time passing (in millisols) 
     */
    void timePassing(double time) {}

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public PersonCollection getAffectedPeople() {
        PersonCollection people = new PersonCollection();

	// Check all people.
	Mars mars = getFacilityManager().getSettlement().getMars();
	PersonIterator i = mars.getUnitManager().getPeople().iterator();
        while (i.hasNext()) {
            Person person = i.next();
            Task task = person.getMind().getTaskManager().getTask();

            // Add all people maintaining this facility. 
            if (task instanceof MaintainSettlement) {
                if (((MaintainSettlement) task).getEntity() == this) {
                    if (!people.contains(person)) people.add(person);
                }
            }

            // Add all people repairing this facility.
            /*
            if (task instanceof RepairSettlement) {
                if (((RepairSettlement) task).getEntity() == this) {
                    if (!people.contains(person) people.add(person);
                }
            }
            */
        }

        return people;
    }
}
