/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.73 2001-10-07
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.task;

import org.mars_sim.msp.simulation.*;

/** The LoadVehicle class is a task for loading a vehicle with fuel and supplies. 
 */
class LoadVehicle extends Task {

    // Data members
    private Vehicle vehicle;  // The vehicle that needs to be loaded.
    private StoreroomFacility stores;  // The settlement's stores.
    private Settlement settlement; // The person's settlement.

    /** Constructs a LoadVehicle object. 
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param vehicle the vehicle to be loaded
     */
    public LoadVehicle(Person person, VirtualMars mars, Vehicle vehicle) {
        super("Loading " + vehicle.getName(), person, mars);

        this.vehicle = vehicle;

        settlement = person.getSettlement();
        FacilityManager facilities = settlement.getFacilityManager();
        stores = (StoreroomFacility) facilities.getFacility("Storerooms");
    }

    /** Performs this task for a given period of time 
     *  @param time amount of time to perform task (in millisols) 
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        double unitsLoading = time;

        if (hasEnoughSupplies(settlement, vehicle)) {
         
            // Load fuel
            double fuelAmount = vehicle.getFuelCapacity() - vehicle.getFuel();
            if (fuelAmount > unitsLoading) fuelAmount = unitsLoading;
            stores.removeFuel(fuelAmount);
            vehicle.addFuel(fuelAmount);
            unitsLoading -= fuelAmount;

            // Load oxygen 
            double oxygenAmount = vehicle.getOxygenCapacity() - vehicle.getOxygen();
            if (oxygenAmount > unitsLoading) oxygenAmount = unitsLoading;
            stores.removeOxygen(oxygenAmount);
            vehicle.addOxygen(oxygenAmount);
            unitsLoading -= oxygenAmount;

            // Load water 
            double waterAmount = vehicle.getWaterCapacity() - vehicle.getWater();
            if (waterAmount > unitsLoading) waterAmount = unitsLoading;
            stores.removeWater(waterAmount);
            vehicle.addWater(waterAmount);
            unitsLoading -= waterAmount;

            // Load Food 
            double foodAmount = vehicle.getFoodCapacity() - vehicle.getFood();
            if (foodAmount > unitsLoading) foodAmount = unitsLoading;
            stores.removeFood(foodAmount);
            vehicle.addFood(foodAmount);
            unitsLoading -= foodAmount;
        }
        else done = true;

        if (isFullyLoaded(vehicle)) done = true;

        return 0;
    }

    /** Returns true if there are enough supplies in the settlements stores to supply vehicle.
     *  @param settlement the settlement the vehicle is at
     *  @param vehicle the vehicle to be checked
     *  @return enough supplies?
     */
    public static boolean hasEnoughSupplies(Settlement settlement, Vehicle vehicle) {
        boolean enoughSupplies = true;

        FacilityManager facilities = settlement.getFacilityManager();
        StoreroomFacility stores = (StoreroomFacility) facilities.getFacility("Storerooms");

        double neededFuel = vehicle.getFuelCapacity() - vehicle.getFuel();
        if (neededFuel > stores.getFuelStores() - 50D) enoughSupplies = false;
        
        double neededOxygen = vehicle.getOxygenCapacity() - vehicle.getOxygen();
        if (neededOxygen > stores.getOxygenStores() - 50D) enoughSupplies = false;

        double neededWater = vehicle.getWaterCapacity() - vehicle.getWater();
        if (neededWater > stores.getWaterStores() - 50D) enoughSupplies = false;
 
        double neededFood = vehicle.getFoodCapacity() - vehicle.getFood();
        if (neededFood > stores.getFoodStores() - 50D) enoughSupplies = false;

        return enoughSupplies;
    }

    /** Returns true if the vehicle is fully loaded with supplies.
     *  @param vehicle to be checked
     *  @return is vehicle fully loaded?
     */
    public static boolean isFullyLoaded(Vehicle vehicle) {
        boolean result = true;
     
        if (vehicle.getFuel() != vehicle.getFuelCapacity()) result = false;
        if (vehicle.getOxygen() != vehicle.getOxygenCapacity()) result = false;
        if (vehicle.getWater() != vehicle.getWaterCapacity()) result = false;
        if (vehicle.getFood() != vehicle.getFoodCapacity()) result = false;

        return result;
    }
}
