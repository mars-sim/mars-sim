/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.72 2001-08-07
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

    /** Constructs a LoadVehicle object. 
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param vehicle the vehicle to be loaded
     */
    public LoadVehicle(Person person, VirtualMars mars, Vehicle vehicle) {
        super("Loading " + vehicle.getName(), person, mars);

     
        System.out.println(person.getName() + " is loading " + vehicle.getName()); 

        this.vehicle = vehicle;

        Settlement settlement = person.getSettlement();
        if (settlement == null) System.out.println(person.getName() + " settlement is null!");
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

        if (hasEnoughSupplies()) {
         
            // Load fuel
            double fuelAmount = vehicle.getFuelCapacity() - vehicle.getFuel();
            if (fuelAmount > unitsLoading) fuelAmount = unitsLoading;
            stores.removeFuel(fuelAmount);
            vehicle.addFuel(fuelAmount);
            unitsLoading -= fuelAmount;
            if (fuelAmount > 0D) System.out.println(person.getName() + " loading " + fuelAmount + " fuel into " + vehicle.getName());

            // Load oxygen 
            double oxygenAmount = vehicle.getOxygenCapacity() - vehicle.getOxygen();
            if (oxygenAmount > unitsLoading) oxygenAmount = unitsLoading;
            stores.removeOxygen(oxygenAmount);
            vehicle.addOxygen(oxygenAmount);
            unitsLoading -= oxygenAmount;
            if (oxygenAmount > 0D) System.out.println(person.getName() + " loading " + oxygenAmount + " oxygen into " + vehicle.getName());

            // Load water 
            double waterAmount = vehicle.getWaterCapacity() - vehicle.getWater();
            if (waterAmount > unitsLoading) waterAmount = unitsLoading;
            stores.removeWater(waterAmount);
            vehicle.addWater(waterAmount);
            unitsLoading -= waterAmount;
            if (waterAmount > 0D) System.out.println(person.getName() + " loading " + waterAmount + " water into " + vehicle.getName());

            // Load Food 
            double foodAmount = vehicle.getFoodCapacity() - vehicle.getFood();
            if (foodAmount > unitsLoading) foodAmount = unitsLoading;
            stores.removeFood(foodAmount);
            vehicle.addFood(foodAmount);
            unitsLoading -= foodAmount;
            if (foodAmount > 0D) System.out.println(person.getName() + " loading " + foodAmount + " food into " + vehicle.getName());
        }
        else {
            done = true;
            System.out.println(person.getName() + ": Not enough supplies at " + person.getSettlement().getName() + " to load " + vehicle.getName());
        }

        if (isFullyLoaded()) {
            done = true;
            System.out.println(person.getName() + ": " + vehicle.getName() + " is fully loaded.");
        }

        return 0;
    }

    /** Returns true if there are enough supplies in the settlements stores to supply vehicle.
     *  @return enough supplies?
     */
    public boolean hasEnoughSupplies() {
        boolean enoughSupplies = true;

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
     *  @return is vehicle fully loaded?
     */
    public boolean isFullyLoaded() {
        boolean result = true;
     
        if (vehicle.getFuel() != vehicle.getFuelCapacity()) result = false;
        if (vehicle.getOxygen() != vehicle.getOxygenCapacity()) result = false;
        if (vehicle.getWater() != vehicle.getWaterCapacity()) result = false;
        if (vehicle.getFood() != vehicle.getFoodCapacity()) result = false;

        return result;
    }
}
