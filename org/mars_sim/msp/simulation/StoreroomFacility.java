/**
 * Mars Simulation Project
 * StoreroomFacility.java
 * @version 2.71 2000-11-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/**
 * The StoreroomFacility class represents the collective storerooms in a settlement.
 * It defines the settlement's storage of food, oxygen, water, fuel, parts and other
 * various materials.
 */
public class StoreroomFacility extends Facility {

    // Data members
    private double foodStores; // The settlement's stores of food.
    private double oxygenStores; // The settlement's stores of oxygen.
    private double waterStores; // The settlement's stores of water.
    private double fuelStores; // The settlement's stores of fuel (methane and other fuel).
    private double partsStores; // The settlement's stores of mechanical and electrical parts.

    // Constant data members
    private double MAX_UNITS_STORAGE = 1000D; /* The maximum number of units storeroom can hold
                                                 for any given resource type. */
    
    /** Constructor for random creation. 
     *  @param manager the storeroom's facility manager
     */
    StoreroomFacility(FacilityManager manager) {

        // Use Facility's constructor.
        super(manager, "Storerooms");

        // Initialize random capacity for each good from 10 to 100.
        foodStores = 10 + RandomUtil.getRandomInteger(90);
        oxygenStores = 10 + RandomUtil.getRandomInteger(90);
        waterStores = 10 + RandomUtil.getRandomInteger(90);
        fuelStores = 10 + RandomUtil.getRandomInteger(90);
        partsStores = 10 + RandomUtil.getRandomInteger(90);
    }

    /** Constructor for set storage values (used later when facilities can be built or upgraded.) 
     *  @param manager the storeroom's facility manager
     *  @param food the initial food stores (in units)
     *  @param oxygen the initial oxygen stores (in units)
     *  @param water the initial water stores (in units)
     *  @param fuel the initial fuel stores (in units)
     *  @param parts the initial parts stores (in units)
     */
    StoreroomFacility(FacilityManager manager, double food, double oxygen, double water, double fuel,
            double parts) {

        // Use Facility's constructor.
        super(manager, "Storerooms");

        // Initialize data members.
        foodStores = food;
        oxygenStores = oxygen;
        waterStores = water;
        fuelStores = fuel;
        partsStores = parts;
    }

    /** Returns the amount of food stored at the settlement. 
     *  @return the amount of food in storage (in units)
     */
    public double getFoodStores() {
        return foodStores;
    }

    /** Removes food from storage. 
     *  @param amount the amount of food requested from storage (in units)
     *  @return the amount of food actually received from storage (in units)
     */
    double removeFood(double amount) {
        double result = amount;
        if (amount > foodStores) {
            result = foodStores;
            foodStores = 0;
        }
        else foodStores -= amount;

        return result;
    }

    /** Adds food to storage. 
     * @param amount the amount of food to be added (in units)
     */
    void addFood(double amount) {
        foodStores += Math.abs(amount);
        if (foodStores > MAX_UNITS_STORAGE) foodStores = MAX_UNITS_STORAGE;
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
    double removeOxygen(double amount) {
        double result = amount;
        if (amount > oxygenStores) {
            result = oxygenStores;
            oxygenStores = 0;
        }
        else oxygenStores -= amount;

        return result;
    }

    /** Adds oxygen to storage. 
     *  @param amount the amount of oxygen to be added (in units)
     */
    void addOxygen(double amount) {
        oxygenStores += Math.abs(amount);
        if (oxygenStores > MAX_UNITS_STORAGE) oxygenStores = MAX_UNITS_STORAGE;
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
    double removeWater(double amount) {
        double result = amount;
        if (amount > waterStores) {
            result = waterStores;
            waterStores = 0;
        }
        else waterStores -= amount;

        return result;
    }

    /** Adds water to storage. 
     *  @param amount the amount of water to be added (in units)
     */
    void addWater(double amount) {
        waterStores += Math.abs(amount);
        if (waterStores > MAX_UNITS_STORAGE) waterStores = MAX_UNITS_STORAGE;
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
    double removeFuel(double amount) {
        double result = amount;
        if (amount > fuelStores) {
            result = fuelStores;
            fuelStores = 0;
        }
        else fuelStores -= amount;

        return result;
    }

    /** Adds fuel to storage. 
     *  @param amount the amount of fuel to be added (in units)
     */
    void addFuel(double amount) {
        fuelStores += Math.abs(amount);
        if (fuelStores > MAX_UNITS_STORAGE) fuelStores = MAX_UNITS_STORAGE;
    }

    /** Returns the amount of parts stored at the settlement. 
     *  @return the amount of parts in storage (in units)
     */
    public double getPartsStores() {
        return partsStores;
    }

    /** Removes parts from storage. 
     *  @param amount the amount of parts requested from storage (in units)
     *  @return the amount of parts actually received from storage (in units)
     */
    double removeParts(double amount) {
        double result = amount;
        if (amount > partsStores) {
            result = partsStores;
            partsStores = 0;
        }
        else partsStores -= amount;

        return result;
    }

    /** Adds parts to storage. 
     *  @param amount the amount of parts to be added (in units)
     */
    void addParts(double amount) {
        partsStores += Math.abs(amount);
        if (partsStores > MAX_UNITS_STORAGE) partsStores = MAX_UNITS_STORAGE;
    }
}
