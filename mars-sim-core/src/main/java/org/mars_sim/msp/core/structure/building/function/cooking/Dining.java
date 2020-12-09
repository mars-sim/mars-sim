/**
 * Mars Simulation Project
 * Dining.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * The Dining class is a building function for dining.
 */
public class Dining
extends Function
implements Serializable {

    /** default serial id.*/
    private static final long serialVersionUID = 1L;

    // Data members
    private int capacity;
    
    /**
     * Constructor.
     * @param building the building this function is for.
     */
    public Dining(Building building) {
        // Use Function constructor.
        super(FunctionType.DINING, building);

        // Populate data members.
        capacity = buildingConfig.getDiningCapacity(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(buildingConfig.getDiningActivitySpots(building.getBuildingType()));
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

        // Settlements need enough dining capacity for all associated people.
        double demand = settlement.getNumCitizens();

        // Supply based on wear condition of buildings.
        double supply = 0D;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FunctionType.DINING).iterator();
        while (i.hasNext()) {
            Building diningBuilding = i.next();
            Dining dining = diningBuilding.getDining();
            double capacity = dining.getDiningCapacity();
            double wearFactor = ((diningBuilding.getMalfunctionManager().getWearCondition() / 100D) * .75D) + .25D;
            supply += capacity * wearFactor;
        }

        if (!newBuilding) {
            double capacity = buildingConfig.getDiningCapacity(buildingName);
            supply -= capacity;
            if (supply < 0D) supply = 0D;
        }

        return demand / (supply + 1D);
    }

    /**
     * Gets the dining capacity of the building.
     * @return capacity.
     */
    public int getDiningCapacity() {
        return capacity;
    }

    @Override
    public double getMaintenanceTime() {
        return capacity * 5D;
    }

}
