/**
 * Mars Simulation Project
 * RoboticStations.java
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
 * The LivingAccommodations class is a building function for a Robotic Station.
 */
public class RoboticStations extends Function implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    public final static double POWER_USAGE_PER_ROBOT_PER_SOL = 1D;

    private static final BuildingFunction FUNCTION = BuildingFunction.ROBOTIC_STATION;

    private int stations;
    private int sleepers;

    /**
     * Constructor
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public RoboticStations(Building building) {
        // Call Function constructor.
        super(FUNCTION, building);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        stations = config.getRoboticStations(building.getBuildingType());

        // Load activity spots
        //loadActivitySpots(config.getLivingAccommodationsActivitySpots(building.getBuildingType()));
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

        // Demand is two stations for every inhabitant (with population expansion in mind).
        double demand = settlement.getAllAssociatedRobots().size() * 2D;

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
                RoboticStations livingFunction = (RoboticStations) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += livingFunction.stations * wearModifier;
            }
        }

        double stationCapacityValue = demand / (supply + 1D);

        BuildingConfig config = SimulationConfig.instance()
                .getBuildingConfiguration();
        double stationCapacity = config.getRoboticStations(buildingName);

        return stationCapacity * stationCapacityValue;
    }

    /**
     * Gets the number of stations in the living accommodations.
     * @return number of stations.
     */
    public int getStations() {
        return stations;
    }

    /**
     * Gets the number of people sleeping in the stations.
     * @return number of people
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
        if (sleepers > stations) {
            sleepers = stations;
            throw new IllegalStateException("All stations are full.");
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
            throw new IllegalStateException("Stations are empty.");
        }
    }

    /**
     * Calculate the power for robots.
     * @param time amount of time passing (millisols)
     * @throws Exception if error in power usage.
     */
    public void powerUsage(double time) {

        Settlement settlement = getBuilding().getBuildingManager()
                .getSettlement();
        double powerUsagePerRobot = (RoboticStations.POWER_USAGE_PER_ROBOT_PER_SOL / 1000D) * time;
        double powerUsageSettlement = powerUsagePerRobot * settlement.getCurrentNumOfRobots();
        double buildingProportionCap = (double) stations / (double) settlement.getRobotCapacity();
        double powerUsageBuilding = powerUsageSettlement * buildingProportionCap;

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
        powerUsage(time);
    }

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
        return stations * 7D;
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