/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;

/** The UnloadVehicle class is a task for unloading a fuel and supplies from a vehicle. 
 */
class UnloadVehicle extends Task implements Serializable {

    // The amount of resources (kg) one person can unload per millisol.
    private static double UNLOAD_RATE = 10D;

    // Data members
    private Vehicle vehicle;  // The vehicle that needs to be loaded.
    private StoreroomFacility stores;  // The settlement's stores.

    /** Constructs a UnloadVehicle object. 
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param vehicle the vehicle to be unloaded
     */
    public UnloadVehicle(Person person, VirtualMars mars, Vehicle vehicle) {
        super("Unloading " + vehicle.getName(), person, mars);

        this.vehicle = vehicle;

        Settlement settlement = person.getSettlement();
        FacilityManager facilities = settlement.getFacilityManager();
        stores = (StoreroomFacility) facilities.getFacility("Storerooms");

        // System.out.println(person.getName() + " is unloading " + vehicle.getName()); 
    }

    /** Performs this task for a given period of time 
     *  @param time amount of time to perform task (in millisols) 
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        double amountUnloading = UNLOAD_RATE * time;

        // Unload fuel
        double fuelAmount = vehicle.getFuel();
        if (fuelAmount > amountUnloading) fuelAmount = amountUnloading;
        stores.addFuel(fuelAmount);
        vehicle.consumeFuel(fuelAmount);
        amountUnloading -= fuelAmount;
        // if (fuelAmount > 0D) System.out.println(person.getName() + " unloading " + fuelAmount + " fuel from " + vehicle.getName());        

        // Unload oxygen 
        double oxygenAmount = vehicle.getOxygen();
        if (oxygenAmount > amountUnloading) oxygenAmount = amountUnloading;
        stores.addOxygen(oxygenAmount);
        vehicle.removeOxygen(oxygenAmount);
        amountUnloading -= oxygenAmount;
        // if (oxygenAmount > 0D) System.out.println(person.getName() + " unloading " + oxygenAmount + " oxygen from " + vehicle.getName());       
 
        // Unload water 
        double waterAmount = vehicle.getWater();
        if (waterAmount > amountUnloading) waterAmount = amountUnloading;
        stores.addWater(waterAmount);
        vehicle.removeWater(waterAmount);
        amountUnloading -= waterAmount;
        // if (waterAmount > 0D) System.out.println(person.getName() + " unloading " + waterAmount + " water from " + vehicle.getName());       

        // Unload Food 
        double foodAmount = vehicle.getFood();
        if (foodAmount > amountUnloading) foodAmount = amountUnloading;
        stores.addFood(foodAmount);
        vehicle.removeFood(foodAmount);
        amountUnloading -= foodAmount;
        // if (foodAmount > 0D) System.out.println(person.getName() + " unloading " + foodAmount + " food from " + vehicle.getName());       

        if (isFullyUnloaded()) done = true;

        return 0;
    }

    /** Returns true if the vehicle is fully unloaded.
     *  @return is vehicle fully unloaded?
     */
    public boolean isFullyUnloaded() {
        boolean result = true;
    
        if (vehicle.getFuel() != 0D) result = false;
        if (vehicle.getOxygen() != 0D) result = false;
        if (vehicle.getWater() != 0D) result = false;
        if (vehicle.getFood() != 0D) result = false;
 
        return result;
    }
}
