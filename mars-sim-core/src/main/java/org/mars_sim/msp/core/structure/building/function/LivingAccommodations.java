/**
 * Mars Simulation Project
 * LivingAccommodations.java
 * @version 3.07 2015-01-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;

import java.io.Serializable;
import java.util.Iterator;

/**
 * The LivingAccommodations class is a building function for a living accommodations.
 */
public class LivingAccommodations extends Function implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Mass of water used per person per Sol for bathing, etc. */
    public final static double WASH_WATER_USAGE_PERSON_SOL = 26D;

    private static final BuildingFunction FUNCTION = BuildingFunction.LIVING_ACCOMODATIONS;

    private int beds;
    private int sleepers;

    /**
     * Constructor
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public LivingAccommodations(Building building) {
        // Call Function constructor.
        super(FUNCTION, building);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        beds = config.getLivingAccommodationBeds(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(config.getLivingAccommodationsActivitySpots(building.getBuildingType()));
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

        // Demand is two beds for every inhabitant (with population expansion in mind).
        double demand = settlement.getAllAssociatedPeople().size() * 2D;

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
                LivingAccommodations livingFunction = (LivingAccommodations) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += livingFunction.beds * wearModifier;
            }
        }

        double bedCapacityValue = demand / (supply + 1D);

        BuildingConfig config = SimulationConfig.instance()
                .getBuildingConfiguration();
        double bedCapacity = config.getLivingAccommodationBeds(buildingName);

        return bedCapacity * bedCapacityValue;
    }

    /**
     * Gets the number of beds in the living accommodations.
     * @return number of beds.
     */
    public int getBeds() {
        return beds;
    }

    /**
     * Gets the number of people sleeping in the beds.
     * @return number of people
     */
    public int getSleepers() {
        return sleepers;
    }

    /**
     * Adds a sleeper to a bed.
     * @throws BuildingException if beds are already in use.
     */
    public void addSleeper() {
        sleepers++;
        if (sleepers > beds) {
            sleepers = beds;
            throw new IllegalStateException("All beds are full.");
        }
    }

    /**
     * Removes a sleeper from a bed.
     * @throws BuildingException if no sleepers to remove.
     */
    public void removeSleeper() {
        sleepers--;
        if (sleepers < 0) {
            sleepers = 0;
            throw new IllegalStateException("Beds are empty.");
        }
    }

    /**
     * Utilizes water for bathing, washing, etc based on population.
     * @param time amount of time passing (millisols)
     * @throws Exception if error in water usage.
     */
    public void waterUsage(double time) {

        Settlement settlement = getBuilding().getBuildingManager()
                .getSettlement();
        double waterUsagePerPerson = (LivingAccommodations.WASH_WATER_USAGE_PERSON_SOL / 1000D) * time;
        double waterUsageSettlement = waterUsagePerPerson * settlement.getCurrentPopulationNum();
        double buildingProportionCap = (double) beds / (double) settlement.getPopulationCapacity();
        double waterUsageBuilding = waterUsageSettlement * buildingProportionCap;

        Inventory inv = getBuilding().getInventory();
        AmountResource water = AmountResource.findAmountResource(org.mars_sim.msp.core.LifeSupport.WATER);
        double waterUsed = waterUsageBuilding;
        double waterAvailable = inv.getAmountResourceStored(water, false);
    	// 2015-01-09 Added addDemandTotalRequest()
        inv.addAmountDemandTotalRequest(water);
        if (waterUsed > waterAvailable)
            waterUsed = waterAvailable;
        inv.retrieveAmountResource(water, waterUsed);        
    	// 2015-01-09 addDemandRealUsage()
       	inv.addAmountDemand(water, waterUsed);
        
        AmountResource wasteWater = AmountResource
                .findAmountResource("waste water");
        double wasteWaterProduced = waterUsed;
        double wasteWaterCapacity = inv.getAmountResourceRemainingCapacity(
                wasteWater, false, false);
        if (wasteWaterProduced > wasteWaterCapacity)
            wasteWaterProduced = wasteWaterCapacity;
        inv.storeAmountResource(wasteWater, wasteWaterProduced, false);
        // 2015-01-15 Add addSupplyAmount()
        inv.addAmountSupplyAmount(wasteWater, wasteWaterProduced);
    }

    /**
     * Time passing for the building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    public void timePassing(double time) {
        waterUsage(time);
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
        return beds * 7D;
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