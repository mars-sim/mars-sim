/**
 * Mars Simulation Project
 * Dining.java
 * @version 3.1.0 2017-10-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Function;

/**
 * The Dining class is a building function for dining.
 */
public class Dining
extends Function
implements Serializable {

    /** default serial id.*/
    private static final long serialVersionUID = 1L;

    private static final FunctionType FUNCTION = FunctionType.DINING;

    // Data members
    private int capacity;

    private static BuildingConfig config;
    
    /**
     * Constructor.
     * @param building the building this function is for.
     */
    public Dining(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);

        // Populate data members.
        config = SimulationConfig.instance().getBuildingConfiguration();
        capacity = config.getDiningCapacity(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(config.getDiningActivitySpots(building.getBuildingType()));
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
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building diningBuilding = i.next();
            Dining dining = diningBuilding.getDining();
            double capacity = dining.getDiningCapacity();
            double wearFactor = ((diningBuilding.getMalfunctionManager().getWearCondition() / 100D) * .75D) + .25D;
            supply += capacity * wearFactor;
        }

        if (!newBuilding) {
        	if (config == null)
        		config = SimulationConfig.instance().getBuildingConfiguration();
            double capacity = config.getDiningCapacity(buildingName);
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

    /**
     * Time passing for the building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    public void timePassing(double time) {}

    /**
     * Gets the amount of power required when function is at full power.
     * @return power (kW)
     */
    public double getFullPowerRequired() {
        return 0D;
    }

    /**
     * Gets the amount of power required when function is at power down level.
     * @return power (kW)
     */
    public double getPoweredDownPowerRequired() {
        return 0D;
    }

    @Override
    public double getMaintenanceTime() {
        return capacity * 5D;
    }

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
}