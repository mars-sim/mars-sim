/**
 * Mars Simulation Project
 * InhabitableBuilding.java
 * @version 2.75 2003-05-30
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building;

import java.util.*;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.person.*;

/**
 * The InhabitableBuilding class is an abstract class representing a 
 * building capable of inhabitation.
 */
public abstract class InhabitableBuilding extends Building {
   
    // Power required to sustain life support for one occupant.
    private static final double LIFE_SUPPORT_OCCUPANT_POWER = 2D;
   
    protected int occupantCapacity;
    protected PersonCollection occupants;
   
    /**
     * Constructor
     *
     * @param name the building's name.
     * @param manager the building's building manager.
     * @param populationCapacity the number of people that can occupy 
     *        the building at one time.
     */
    public InhabitableBuilding(String name, BuildingManager manager, int occupantCapacity) {
        // Use Building constructor.
        super(name, manager);
        
        this.occupantCapacity = occupantCapacity;
        occupants = new PersonCollection();
    }
    
    /**
     * Gets the building's occupant capacity.
     * to do: If the number of occupants exceeds the occupant capacity,
     * occupant stress levels will increase.
     *
     * @return occupant capacity
     */
    public int getOccupantCapacity() {
        return occupantCapacity;
    }
    
    /**
     * Gets the current occupant number of the building.
     *
     * @return occupant number
     */
    public int getOccupantNumber() {
        return occupants.size();
    }
    
    /**
     * Gets the available occupancy room.
     *
     * @return occupancy room
     */
    public int getAvailableOccupancy() {
        int available = getOccupantCapacity() - getOccupantNumber();
        if (available > 0) return available;
        else return 0;
    }
    
    /**
     * Checks if the building contains a particular person.
     *
     * @return true if person is in building.
     */
    public boolean containsPerson(Person person) {
        if (occupants.contains(person)) return true;
        else return false;
    }
    
    /**
     * Gets a collection of occupants in the building.
     *
     * @return collection of occupants
     */
    public PersonCollection getOccupants() {
        return new PersonCollection(occupants);
    }
    
    /**
     * Adds a person to the building.
     * Note: building occupant capacity can be exceeded but stress levels
     * in the building will increase.
     *
     * @param person new person to add to building.
     * @throws BuildingException if person is already building occupant.
     */
    public void addPerson(Person person) throws BuildingException {
        if (!occupants.contains(person)) {
            // Remove person from any other inhabitable building in the settlement.
            Iterator i = getBuildingManager().getBuildings(InhabitableBuilding.class).iterator();
            while (i.hasNext()) {
                InhabitableBuilding building = (InhabitableBuilding) i.next();
                if (building.containsPerson(person)) building.removePerson(person);
            }

            // Add person to this building.            
            occupants.add(person);
        }
        else throw new BuildingException("Person already occupying building.");
    }
    
    /**
     * Removes a person from the building.
     *
     * @param person occupant to remove from building.
     * @throws BuildingException if person is not building occupant.
     */
    public void removePerson(Person person) throws BuildingException {
        if (occupants.contains(person)) occupants.remove(person);
        else throw new BuildingException("Person does not occupy building.");
    }
    
    /**
     * Gets the base-line power required for just life support.
     * @return power in kW.
     */
    public double getLifeSupportPowerRequired() {
        return getOccupantCapacity() * LIFE_SUPPORT_OCCUPANT_POWER;
    }   
    
    /**
     * Gets the power the building requires for power-down mode.
     * @return power in kW.
     */
    public double getPoweredDownPowerRequired() {
        return getLifeSupportPowerRequired();
    }   
    
    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public PersonCollection getAffectedPeople() {
        PersonCollection affectedPeople = super.getAffectedPeople();
        
        PersonIterator i = occupants.iterator();
        while (i.hasNext()) {
            Person occupant = i.next();
            if (!affectedPeople.contains(occupant)) affectedPeople.add(occupant);
        }
        
        return affectedPeople;
    }
    
    /**
     * Time passing for building.
     * Child building should override this method for things
     * that happen over time for the building.
     *
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) {
        
        // Use Building.timePassing()
        super.timePassing(time);
        
        // Make sure all occupants are actually in settlement.
        // If not, remove them as occupants.
        Inventory inv = getBuildingManager().getSettlement().getInventory();
        PersonIterator i = occupants.iterator();
        while (i.hasNext()) {
            if (!inv.containsUnit(i.next())) i.remove();
        }
    }   
}
