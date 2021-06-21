/**
 * Mars Simulation Project
 * Communication.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

import java.io.Serializable;
import java.util.Iterator;

/**
 * The Communication class is a building function for communication.
 */
public class Communication
extends Function
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
  
    // Data members
    private int populationSupport;
    private int user;
    private int userCapacity;

    /**
     * Constructor.
     * @param building the building this function is for.
     */
    public Communication(Building building) {
        // Use Function constructor.
        super(FunctionType.COMMUNICATION, building);
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        // Settlements need one communication building.
        // Note: Might want to update this when we do more with simulating communication.
        double demand = 1D;

        // Supply based on wear condition of buildings.
        double supply = 0D;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.COMMUNICATION).iterator();
        while (i.hasNext()) {
            supply += (i.next().getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
        }

        if (!newBuilding) {
            supply -= 1D;
            if (supply < 0D) supply = 0D;
        }

        return demand / (supply + 1D);
    }


    /**
     * Gets the number of people this administration facility can support.
     * @return population that can be supported.
     */
    public int getPopulationSupport() {
        return populationSupport;
    }


    /**
     * Gets the number of people this comm facility can be used all at a time.
     * @return population that can be supported.
     */
    public int getUserCapacity() {
        return userCapacity;
    }


    /**
     * Gets the current number of people using the facility.
     * @return number of people.
     */
    public int getNumUser() {
        return user;
    }

    /**
     * Adds a person to the facility.
     * @throws BuildingException if person would exceed facility capacity.
     */
    public void addUser() {
        user++;
        if (user > userCapacity) {
            user = userCapacity;
            throw new IllegalStateException("The facility is full.");
        }
    }

    /**
     * Removes a person from the facility.
     * @throws BuildingException if nobody is using the facility.
     */
    public void removeUser() {
        user--;
        if (user < 0) {
            user = 0;
            throw new IllegalStateException("The facility is empty.");
        }
    }

    @Override
    public double getMaintenanceTime() {
        return populationSupport * 1D;
    }
}
