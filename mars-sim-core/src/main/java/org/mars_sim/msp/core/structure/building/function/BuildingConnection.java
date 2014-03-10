/**
 * Mars Simulation Project
 * BuildingConnection.java
 * @version 3.06 2014-03-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;

public class BuildingConnection extends Function implements Serializable {

    public static final String NAME = "Building Connection";
    
    private static final double BASE_VALUE = 10D;
    
    public BuildingConnection(Building building) {
        // User Function constructor.
        super(NAME, building);
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
        return BASE_VALUE;
    }
    
    @Override
    public void timePassing(double time) {
        // TODO Do nothing.
    }

    @Override
    public double getFullPowerRequired() {
        return 0;
    }

    @Override
    public double getPowerDownPowerRequired() {
        return 0;
    }
    
    @Override
    public double getMaintenanceTime() {
        return 0D;
    }
}