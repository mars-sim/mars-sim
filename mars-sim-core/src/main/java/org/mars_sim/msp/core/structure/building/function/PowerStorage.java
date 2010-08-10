/**
 * Mars Simulation Project
 * PowerGeneration.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.PowerGrid;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The PowerStorage class is a building function for storing power.
 */
public class PowerStorage extends Function implements Serializable {

    // Building function name.
    public static final String NAME = "Power Storage";
    
    // Data members.
    private double powerStorageCapacity;
    private double powerStored;
    
    /**
     * Constructor
     * @param building the building with the function.
     * @throws BuildingException if error parsing configuration.
     */
    public PowerStorage(Building building) throws BuildingException {
        // Call Function constructor.
        super(NAME, building);
        
        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        try {
            powerStorageCapacity = config.getPowerStorageCapacity(building.getName());
        }
        catch (Exception e) {
            throw new BuildingException("PowerStorage.constructor: " + e.getMessage());
        }
    }
    
    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static final double getFunctionValue(String buildingName, boolean newBuilding, 
            Settlement settlement) throws Exception {
        
        PowerGrid grid = settlement.getPowerGrid();
        
        double hrInSol = MarsClock.convertMillisolsToSeconds(1000D) / 60D / 60D;
        double demand = grid.getRequiredPower() * hrInSol;
        
        double supply = 0D;
        Iterator<Building> iStore = settlement.getBuildingManager().getBuildings(PowerStorage.NAME).iterator();
        while (iStore.hasNext()) {
            Building building = iStore.next();
            PowerStorage store = (PowerStorage) building.getFunction(PowerStorage.NAME);
            double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
            supply += store.getPowerStorageCapacity() * wearModifier;
        }
        
        double existingPowerStorageValue = demand / (supply + 1D);
        
        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        double powerStorage = config.getPowerStorageCapacity(buildingName);
        
        double value = powerStorage * existingPowerStorageValue / hrInSol;
        if (value > 10D) value = 10D;
        
        return value;
    }
    
    /**
     * Gets the building's power storage capacity.
     * @return capacity (kW hr).
     */
    public double getPowerStorageCapacity() {
        return powerStorageCapacity;
    }
    
    /**
     * Gets the building's stored power.
     * @return power (kW hr).
     */
    public double getPowerStored() {
        return powerStored;
    }
    
    /**
     * Sets the power stored in the building.
     * @param powerStored the stored power (kW hr).
     */
    public void setPowerStored(double powerStored) {
        if (powerStored > powerStorageCapacity) powerStored = powerStorageCapacity;
        else if (powerStored < 0D) powerStored = 0D;
        this.powerStored = powerStored;
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
    public void timePassing(double time) throws BuildingException {
        // Do nothing.
    }
}