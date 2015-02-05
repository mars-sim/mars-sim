/**
 * Mars Simulation Project
 * RoboticStation.java
 * @version 3.07 2015-01-21
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;

import java.io.Serializable;
import java.util.Iterator;

/**
 * The RoboticStation class is a building function for a Robotic Station.
 */
public class RoboticStation extends Function implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    public final static double POWER_USAGE_PER_ROBOT = 1D; // in kW

    private static final double SECONDS_IN_MILLISOL = 88.775244;
    
    private static final BuildingFunction FUNCTION = BuildingFunction.ROBOTIC_STATION;

    private int slots;
    private int sleepers;

    /**
     * Constructor
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public RoboticStation(Building building) {
        // Call Function constructor.
        super(FUNCTION, building);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        slots = config.getRoboticStation(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(config.getRoboticStationActivitySpots(building.getBuildingType()));
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static double getFunctionValue(String buildingName,
            boolean newBuilding, Settlement settlement) {

        // Demand is one stations for every robot
        double demand = settlement.getAllAssociatedRobots().size() * 1D;

        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding
                    && building.getBuildingType().equalsIgnoreCase(buildingName)
                    && !removedBuilding) {
                removedBuilding = true;
            } else {
                RoboticStation station = (RoboticStation) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += station.slots * wearModifier;
            }
        }

        double stationCapacityValue = demand / (supply + 1D);

        BuildingConfig config = SimulationConfig.instance()
                .getBuildingConfiguration();
        double stationCapacity = config.getRoboticStation(buildingName);

        return stationCapacity * stationCapacityValue;
    }

    /**
     * Gets the number of slots in the living accommodations.
     * @return number of slots.
     */
    public int getSlots() {
        return slots;
    }

    /**
     * Gets the number of robots sleeping in the stations.
     * @return number of robots
     */
    public int getSleepers() {
        return sleepers;
    }

    /**
     * Adds a sleeper to a station.
     * @throws BuildingException if stations are already in use.
     */
    public void addSleeper() {
        sleepers++;
        if (sleepers > slots) {
            sleepers = slots;
            throw new IllegalStateException("All slots are full.");
        }
    }

    /**
     * Removes a sleeper from a station.
     * @throws BuildingException if no sleepers to remove.
     */
    public void removeSleeper() {
        sleepers--;
        if (sleepers < 0) {
            sleepers = 0;
            throw new IllegalStateException("Slots are empty.");
        }
    }

    /**
     * Calculate the power for robots.
     * @param time amount of time passing (millisols)
     * @throws Exception if error in power usage.
     */
    public void powerUsage(double millisols) {

        Settlement settlement = getBuilding().getBuildingManager()
                .getSettlement();
        double energyPerRobot = POWER_USAGE_PER_ROBOT * millisols * SECONDS_IN_MILLISOL;
        double energyUsageSettlement = energyPerRobot * settlement.getCurrentNumOfRobots();
        double buildingProportionCap = (double) slots / (double) settlement.getRobotCapacity();
        double energyUsageBuilding = energyUsageSettlement * buildingProportionCap;

        /*
        Inventory inv = getBuilding().getInventory();
        AmountResource water = AmountResource.findAmountResource(org.mars_sim.msp.core.LifeSupport.WATER);
        double waterUsed = powerUsageBuilding;
        double waterAvailable = inv.getAmountResourceStored(water, false);
    	// 2015-01-09 Added addDemandTotalRequest()
        inv.addDemandTotalRequest(water);
        if (waterUsed > waterAvailable)
            waterUsed = waterAvailable;
        inv.retrieveAmountResource(water, waterUsed);        
    	// 2015-01-09 addDemandRealUsage()
       	inv.addDemandAmount(water, waterUsed);
        
        AmountResource wasteWater = AmountResource
                .findAmountResource("waste water");
        double wasteWaterProduced = waterUsed;
        double wasteWaterCapacity = inv.getAmountResourceRemainingCapacity(
                wasteWater, false, false);
        if (wasteWaterProduced > wasteWaterCapacity)
            wasteWaterProduced = wasteWaterCapacity;
        inv.storeAmountResource(wasteWater, wasteWaterProduced, false);
        // 2015-01-15 Add addSupplyAmount()
        inv.addSupplyAmount(wasteWater, wasteWaterProduced);
    */
    }

    /**
     * Time passing for the building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    public void timePassing(double time) {
    }

    /**
     * Gets the amount of power required when function is at full power.
     * @return power (kW)
     */
    public double getFullPowerRequired() {
    	//powerUsage(time);
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
        return slots * 7D;
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