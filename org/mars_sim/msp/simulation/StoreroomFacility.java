/**
 * Mars Simulation Project
 * StoreroomFacility.java
 * @version 2.73 2001-12-07
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

import java.io.Serializable;

/**
 * The StoreroomFacility class represents the collective storerooms in a settlement.
 * It defines the settlement's storage of food, oxygen, water and fuel.
 */
public class StoreroomFacility extends Facility implements Serializable {

    // Data members
    private double foodStores; // The settlement's stores of food.
    private double oxygenStores; // The settlement's stores of oxygen.
    private double waterStores; // The settlement's stores of water.
    private double fuelStores; // The settlement's stores of fuel (methane and other fuel).
    
    /** Constructor for random creation. 
     *  @param manager the storeroom's facility manager
     */
    StoreroomFacility(FacilityManager manager) {

        // Use Facility's constructor.
        super(manager, "Storerooms");

        // Initialize random amount for each resource from a quarter to half storage capacity.
        double foodCapacity = getFoodStorageCapacity();
        foodStores = (foodCapacity / 4D) + RandomUtil.getRandomDouble(foodCapacity / 4D);
        double oxygenCapacity = getOxygenStorageCapacity();
        oxygenStores = (oxygenCapacity / 4D) + RandomUtil.getRandomDouble(oxygenCapacity / 4D);
        double waterCapacity = getWaterStorageCapacity();
        waterStores = (waterCapacity / 4D) + RandomUtil.getRandomDouble(waterCapacity / 4D);
        double fuelCapacity = getFuelStorageCapacity();
        fuelStores = (fuelCapacity / 4D) + RandomUtil.getRandomDouble(fuelCapacity / 4D);
    }

    /** Constructor for set storage values (used later when facilities can be built or upgraded.) 
     *  @param manager the storeroom's facility manager
     *  @param food the initial food stores (kg)
     *  @param oxygen the initial oxygen stores (kg)
     *  @param water the initial water stores (kg)
     *  @param fuel the initial fuel stores (kg)
     */
    StoreroomFacility(FacilityManager manager, double food, double oxygen, double water, double fuel) {

        // Use Facility's constructor.
        super(manager, "Storerooms");

        // Set resources 
        addFood(food);
        addOxygen(oxygen);
        addWater(water);
        addFuel(fuel);
    }

    /** Returns the amount of food stored at the settlement. 
     *  @return the amount of food in storage (kg)
     */
    public double getFoodStores() {
        return foodStores;
    }

    /** Removes food from storage. 
     *  @param amount the amount of food requested from storage (kg)
     *  @return the amount of food actually received from storage (kg)
     */
    public double removeFood(double amount) {
        double result = amount;
        if (amount > foodStores) {
            result = foodStores;
            foodStores = 0D;
        }
        else foodStores -= amount;

        return result;
    }

    /** Adds food to storage. 
     *  @param amount the amount of food to be added (kg)
     */
    public void addFood(double amount) {
        if (amount > 0) {
            foodStores += amount;
            if (foodStores > getFoodStorageCapacity()) foodStores = getFoodStorageCapacity();
        }
    }

    /** Gets the food storage capacity of the settlement.
     *  @return the food storage capacity (kg)
     */
    public double getFoodStorageCapacity() {
        SimulationProperties properties = manager.getVirtualMars().getSimulationProperties();
        return properties.getSettlementFoodStorageCapacity();
    }

    /** Returns the amount of oxygen stored at the settlement. 
     *  @return the amount of oxygen in storage (in units)
     */
    public double getOxygenStores() {
        return oxygenStores;
    }

    /** Removes oxygen from storage. 
     *  @param amount the amount of oxygen requested from storage (in units)
     *  @return the amount of oxygen actually received from storage (in units)
     */
    public double removeOxygen(double amount) {
        double result = amount;
        if (amount > oxygenStores) {
            result = oxygenStores;
            oxygenStores = 0;
        }
        else oxygenStores -= amount;

        return result;
    }

    /** Adds oxygen to storage. 
     *  @param amount the amount of oxygen to be added (kg)
     */
    public void addOxygen(double amount) {
        if (amount > 0) {
            oxygenStores += amount;
            if (oxygenStores > getOxygenStorageCapacity()) oxygenStores = getOxygenStorageCapacity();
        }
    }

    /** Gets the oxygen storage capacity of the settlement.
     *  @return the oxygen storage capacity (kg)
     */
    public double getOxygenStorageCapacity() {
        SimulationProperties properties = manager.getVirtualMars().getSimulationProperties();
        return properties.getSettlementOxygenStorageCapacity();
    }

    /** Returns the amount of water stored at the settlement. 
     *  @return the  amount of water in storage (in units)
     */
    public double getWaterStores() {
        return waterStores;
    }

    /** Removes water from storage. 
     *  @param amount the amount of water requested from storage (in units)
     *  @return the amount of water actually received from storage (in units)
     */
    public double removeWater(double amount) {
        double result = amount;
        if (amount > waterStores) {
            result = waterStores;
            waterStores = 0;
        }
        else waterStores -= amount;

        return result;
    }

    /** Adds water to storage. 
     *  @param amount the amount of water to be added (kg)
     */
    public void addWater(double amount) {
        if (amount > 0) {
            waterStores += amount;
            if (waterStores > getWaterStorageCapacity()) waterStores = getWaterStorageCapacity();
        }
    }

    /** Gets the water storage capacity of the settlement.
     *  @return the water storage capacity (kg)
     */
    public double getWaterStorageCapacity() {
        SimulationProperties properties = manager.getVirtualMars().getSimulationProperties();
        return properties.getSettlementWaterStorageCapacity();
    }

    /** Returns the amount of fuel stored at the settlement. 
     *  @return the amount of fuel in storage (in units)
     */
    public double getFuelStores() {
        return fuelStores;
    }

    /** Removes fuel from storage. 
     *  @param amount the amount of fuel requested from storage (in units)
     *  @return the amount of fuel actually received from storage (in units)
     */
    public double removeFuel(double amount) {
        double result = amount;
        if (amount > fuelStores) {
            result = fuelStores;
            fuelStores = 0;
        }
        else fuelStores -= amount;

        return result;
    }

    /** Adds fuel to storage. 
     *  @param amount the amount of fuel to be added (kg)
     */
    public void addFuel(double amount) {
        if (amount > 0) {
            fuelStores += amount;
            if (fuelStores > getFuelStorageCapacity()) fuelStores = getFuelStorageCapacity();
        }
    }

    /** Gets the fuel storage capacity of the settlement.
     *  @return the fuel storage capacity (kg)
     */
    public double getFuelStorageCapacity() {
        SimulationProperties properties = manager.getVirtualMars().getSimulationProperties();
        return properties.getSettlementFuelStorageCapacity();
    }
}
