/**
 * Mars Simulation Project
 * BuildingAirlock.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building.function;

import java.awt.geom.Point2D;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/** 
 * The BuildingAirlock class represents an airlock for a building.
 */
public class BuildingAirlock extends Airlock {

    private static Logger logger = Logger.getLogger(BuildingAirlock.class.getName());

    // Data members.
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

        // Check if person is entering airlock from inside.
        if (result && inside) {
            // Add person to the building.
            BuildingManager.addPersonToBuildingRandomLocation(person, building);
        }

        return result;
    }         

    /**
     * Causes a person within the airlock to exit either inside or outside.
     *
     * @param person the person to exit.
     * @throws Exception if person is not in the airlock.
     */
    protected void exitAirlock(Person person) {
        Inventory inv = building.getInventory();

        if (inAirlock(person)) {

            if (PRESSURIZED.equals(getState())) {
                // Exit person to inside building.
                BuildingManager.addPersonToBuildingRandomLocation(person, building);
                inv.storeUnit(person);
            }
            else if (DEPRESSURIZED.equals(getState())) {
                // Exit person to outside building.
                BuildingManager.removePersonFromBuilding(person, building);
                inv.retrieveUnit(person);
                
                // Move person to a random exterior location to the building.
                Point2D.Double buildingLoc = LocalAreaUtil.getRandomExteriorLocation(building, 1D);
                Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
                        buildingLoc.getY(), building);
                person.setXLocation(settlementLoc.getX());
                person.setYLocation(settlementLoc.getY());
            }
            else {
                logger.severe("Building airlock in incorrect state for exiting: " + getState());
            }
        }
        else throw new IllegalStateException(person.getName() + " not in airlock of " + getEntityName());
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
    
    @Override
    public Object getEntity() {
        return building;
    }
}