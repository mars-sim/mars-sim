/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.74 2002-01-30
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import java.io.Serializable;

/** The LoadVehicle class is a task for loading a vehicle with fuel and supplies.
 */
class LoadVehicle extends Task implements Serializable {

    // The amount of resources (kg) one person can load per millisol.
    private static double LOAD_RATE = 10D;

    // Data members
    private Vehicle vehicle;  // The vehicle that needs to be loaded.
    private Settlement settlement; // The person's settlement.

    /** Constructs a LoadVehicle object.
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param vehicle the vehicle to be loaded
     */
    public LoadVehicle(Person person, VirtualMars mars, Vehicle vehicle) {
        super("Loading " + vehicle.getName(), person, true, mars);

        this.vehicle = vehicle;

        settlement = person.getSettlement();
    }

    /** Performs this task for a given period of time
     *  @param time amount of time to perform task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        double amountLoading = LOAD_RATE * time;

        if (hasEnoughSupplies(settlement, vehicle)) {

            // Load fuel
	    double fuelAmount = vehicle.getInventory().getResourceRemainingCapacity(Inventory.FUEL);
            if (fuelAmount > amountLoading) fuelAmount = amountLoading;
	    settlement.getInventory().removeResource(Inventory.FUEL, fuelAmount);
	    vehicle.getInventory().addResource(Inventory.FUEL, fuelAmount);
            amountLoading -= fuelAmount;

            // Load oxygen
	    double oxygenAmount = vehicle.getInventory().getResourceRemainingCapacity(Inventory.OXYGEN);
            if (oxygenAmount > amountLoading) oxygenAmount = amountLoading;
	    settlement.getInventory().removeResource(Inventory.OXYGEN, oxygenAmount);
	    vehicle.getInventory().addResource(Inventory.OXYGEN, oxygenAmount);
            amountLoading -= oxygenAmount;
	    
            // Load water
	    double waterAmount = vehicle.getInventory().getResourceRemainingCapacity(Inventory.WATER);
            if (waterAmount > amountLoading) waterAmount = amountLoading;
	    settlement.getInventory().removeResource(Inventory.WATER, waterAmount);
	    vehicle.getInventory().addResource(Inventory.WATER, waterAmount);
            amountLoading -= waterAmount;

            // Load Food
	    double foodAmount = vehicle.getInventory().getResourceRemainingCapacity(Inventory.FOOD);
            if (foodAmount > amountLoading) foodAmount = amountLoading;
	    settlement.getInventory().removeResource(Inventory.FOOD, foodAmount);
	    vehicle.getInventory().addResource(Inventory.FOOD, foodAmount);
            amountLoading -= foodAmount;
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

	double neededFuel = vehicle.getInventory().getResourceRemainingCapacity(Inventory.FUEL);
	double storedFuel = settlement.getInventory().getResourceMass(Inventory.FUEL);
        if (neededFuel > storedFuel - 50D) enoughSupplies = false;

	double neededOxygen = vehicle.getInventory().getResourceRemainingCapacity(Inventory.OXYGEN);
	double storedOxygen = settlement.getInventory().getResourceMass(Inventory.OXYGEN);
        if (neededOxygen > storedOxygen - 50D) enoughSupplies = false;

	double neededWater = vehicle.getInventory().getResourceRemainingCapacity(Inventory.WATER);
	double storedWater = settlement.getInventory().getResourceMass(Inventory.WATER);
        if (neededWater > storedWater - 50D) enoughSupplies = false;
	
	double neededFood = vehicle.getInventory().getResourceRemainingCapacity(Inventory.FOOD);
	double storedFood = settlement.getInventory().getResourceMass(Inventory.FOOD);
        if (neededFood > storedFood - 50D) enoughSupplies = false;

        return enoughSupplies;
    }

    /** Returns true if the vehicle is fully loaded with supplies.
     *  @param vehicle to be checked
     *  @return is vehicle fully loaded?
     */
    public static boolean isFullyLoaded(Vehicle vehicle) {
        boolean result = true;

        Inventory i = vehicle.getInventory();

        if (i.getResourceRemainingCapacity(Inventory.FUEL) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Inventory.OXYGEN) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Inventory.WATER) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Inventory.FOOD) > 0D) result = false;

        return result;
    }
}
