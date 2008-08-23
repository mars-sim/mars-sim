/**
 * Mars Simulation Project
 * LivingAccommodations.java
 * @version 2.85 2008-08-20
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;

/**
 * The LivingAccommodations class is a building function for a living accommodations.
 */
public class LivingAccommodations extends Function implements Serializable {
        
    // Mass of water used per person per Sol for bathing, etc.
    public final static double WASH_WATER_USAGE_PERSON_SOL = 26D;
    
	public static final String NAME = "Living Accommodations";
    
 	private int beds;
 	private int sleepers;
 
 	/**
 	 * Constructor
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
	 */
	public LivingAccommodations(Building building) throws BuildingException {
		// Call Function constructor.
		super(NAME, building);
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		
		try {
			beds = config.getLivingAccommodationBeds(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("LivingAccommodations.constructor: " + e.getMessage());
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
        
        // Demand is one bed for every inhabitant. 
        double demand = settlement.getAllAssociatedPeople().size();
        
        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(NAME).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getName().equals(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                LivingAccommodations livingFunction = (LivingAccommodations) building.getFunction(NAME);
                supply += livingFunction.getBeds();
            }
        }
        
        double bedCapacityValue = demand / (supply + 1D);
        
        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
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
 	public void addSleeper() throws BuildingException {
 		sleepers++;
 		if (sleepers > beds) {
 			sleepers = beds;
 			throw new BuildingException("All beds are full.");
 		}
 	}
 	
 	/**
 	 * Removes a sleeper from a bed.
 	 * @throws BuildingException if no sleepers to remove.
 	 */
 	public void removeSleeper() throws BuildingException {
 		sleepers --;
 		if (sleepers < 0) {
 			sleepers = 0;
 			throw new BuildingException("Beds are empty.");
 		}
 	}
 
    /** 
     * Utilizes water for bathing, washing, etc based on population.
     * @param time amount of time passing (millisols)
     * @throws Exception if error in water usage.
     */
    public void waterUsage(double time) throws Exception {
    	
		Settlement settlement = getBuilding().getBuildingManager().getSettlement();
		double waterUsagePerPerson = (LivingAccommodations.WASH_WATER_USAGE_PERSON_SOL / 1000D) * time;
		double waterUsageSettlement = waterUsagePerPerson * settlement.getCurrentPopulationNum();
		double buildingProportionCap = (double) beds / (double) settlement.getPopulationCapacity();
		double waterUsageBuilding = waterUsageSettlement * buildingProportionCap;
        
		Inventory inv = getBuilding().getInventory();
		AmountResource water = AmountResource.findAmountResource("water");
		double waterUsed = waterUsageBuilding;
		double waterAvailable = inv.getAmountResourceStored(water);
		if (waterUsed > waterAvailable) waterUsed = waterAvailable;
		try {
			inv.retrieveAmountResource(water, waterUsed);
    	}
		catch (Exception e) {}
		
		AmountResource wasteWater = AmountResource.findAmountResource("waste water");
		double wasteWaterProduced = waterUsed;
		double wasteWaterCapacity = inv.getAmountResourceRemainingCapacity(wasteWater, false);
		if (wasteWaterProduced > wasteWaterCapacity) wasteWaterProduced = wasteWaterCapacity;
		try {
			inv.storeAmountResource(wasteWater, wasteWaterProduced, false);
		}
		catch (Exception e) {}
    }
    
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {
		try {
			waterUsage(time);
		}
		catch (Exception e) {
			throw new BuildingException("Error with LivingQuarters.waterUsage(): " + e.getMessage());
		}
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
	public double getPowerDownPowerRequired() {
		return 0D;
	}
}