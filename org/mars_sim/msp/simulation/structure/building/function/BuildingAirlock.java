/**
 * Mars Simulation Project
 * VehicleAirlock.java
 * @version 2.75 2004-03-30
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.building.function;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;

/** 
 * The BuildingAirlock class represents an airlock for a building.
 */
public class BuildingAirlock extends Airlock {
    
    private Building building; // The building this airlock is for.
    
    /**
     * Constructor
     * 
     * @param building the building this airlock of for.
     * @param capacity number of people airlock can hold.
     * @throws IllegalArgumentException if building is not valid or if 
     * capacity is less than one.
     */
    public BuildingAirlock(Building building, int capacity) {
        // User Airlock constructor
        super(capacity);
        
        this.building = building;
        
        if (building == null) throw new IllegalArgumentException("building is null.");
    }
    
    /**
     * Enters a person into the airlock from either the inside or the outside.
     * Inner or outer door (respectively) must be open for person to enter.
     * @param person the person to enter the airlock
     * @param inside true if person is entering from inside
     *               false if person is entering from outside
     * @return true if person entered the airlock successfully
     */
    public boolean enterAirlock(Person person, boolean inside) {
        boolean result = super.enterAirlock(person, inside);
    
        if (result && inside) {
            try { 
            	LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
            	lifeSupport.addPerson(person);
            }
            catch (BuildingException e) {}
        }
        
        return result;
    }           
    
    /**
     * Causes a person within the airlock to exit either inside or outside.
     *
     * @param person the person to exit.
     * @throws Exception if person is not in the airlock.
     */
    protected void exitAirlock(Person person) throws Exception {
        Inventory inv = building.getInventory();
        
        if (inAirlock(person)) {
			LifeSupport lifeSupport = (LifeSupport) building.getFunction(LifeSupport.NAME);
        	
            if (pressurized) {
                lifeSupport.addPerson(person);
                inv.addUnit(person);
            }
            else {
                lifeSupport.removePerson(person);
                inv.dropUnitOutside(person);
            }
        }
        else throw new Exception(person.getName() + " not in airlock of " + getEntityName());
    }
    
    /**
     * Gets the name of the entity this airlock is attached to.
     *
     * @return name
     */
    public String getEntityName() {
        Settlement settlement = building.getBuildingManager().getSettlement();
        return settlement.getName() + ": " + building.getName();
    }
    
    /**
     * Gets the inventory of the entity this airlock is attached to.
     *
     * @return inventory
     */
    public Inventory getEntityInventory() {
        return building.getInventory();
    }
}