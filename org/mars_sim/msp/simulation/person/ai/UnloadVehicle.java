/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.74 2002-01-30
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

    // The amount of Oxygen that should be left
    private static double MINIMUMOXYGEN = 50D;

    // Data members
    private Vehicle vehicle;  // The vehicle that needs to be unloaded.
    private Settlement settlement; // The settlement the person is unloading to.

    /** Constructs a UnloadVehicle object.
     *
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @param vehicle the vehicle to be unloaded
     */
    public UnloadVehicle(Person person, VirtualMars mars, Vehicle vehicle) {
        super("Unloading " + vehicle.getName(), person, true, mars);

        this.vehicle = vehicle;

        settlement = person.getSettlement();

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
	double fuelAmount = vehicle.getInventory().getResourceMass(Inventory.FUEL);
        if (fuelAmount > amountUnloading) fuelAmount = amountUnloading;
	vehicle.getInventory().removeResource(Inventory.FUEL, fuelAmount);
	settlement.getInventory().addResource(Inventory.FUEL, fuelAmount);
        amountUnloading -= fuelAmount;

        // Unload oxygen. Always keep a minimum amount in the tank to allow
        // for slow people leaving vehicle
	double oxygenAmount = vehicle.getInventory().getResourceMass(Inventory.OXYGEN) - MINIMUMOXYGEN;
        if (oxygenAmount < 0D) oxygenAmount = 0D;
        if (oxygenAmount > amountUnloading) oxygenAmount = amountUnloading;
	vehicle.getInventory().removeResource(Inventory.OXYGEN, oxygenAmount);
	settlement.getInventory().addResource(Inventory.OXYGEN, oxygenAmount);
        amountUnloading -= oxygenAmount;

        // Unload water
	double waterAmount = vehicle.getInventory().getResourceMass(Inventory.WATER);
        if (waterAmount > amountUnloading) waterAmount = amountUnloading;
	vehicle.getInventory().removeResource(Inventory.WATER, waterAmount);
	settlement.getInventory().addResource(Inventory.WATER, waterAmount);
        amountUnloading -= waterAmount;

        // Unload Food
	double foodAmount = vehicle.getInventory().getResourceMass(Inventory.FOOD);
        if (foodAmount > amountUnloading) foodAmount = amountUnloading;
	vehicle.getInventory().removeResource(Inventory.FOOD, foodAmount);
	settlement.getInventory().addResource(Inventory.FOOD, foodAmount);
        amountUnloading -= foodAmount;

        // Unload Rock Samples 
	double rockAmount = vehicle.getInventory().getResourceMass(Inventory.ROCK_SAMPLES);
        if (rockAmount > amountUnloading) rockAmount = amountUnloading;
	vehicle.getInventory().removeResource(Inventory.ROCK_SAMPLES, rockAmount);
	settlement.getInventory().addResource(Inventory.ROCK_SAMPLES, rockAmount);
        amountUnloading -= rockAmount;

        if (isFullyUnloaded(vehicle)) done = true;

        return 0;
    }

    /** Returns true if the vehicle is fully unloaded.
     *
     * @param vehicle Vehicle to check.
     *  @return is vehicle fully unloaded?
     */
    static public boolean isFullyUnloaded(Vehicle vehicle) {
        boolean result = true;

        if (vehicle.getInventory().getResourceMass(Inventory.FUEL) != 0D) result = false;
        if (vehicle.getInventory().getResourceMass(Inventory.OXYGEN) > MINIMUMOXYGEN) result = false;
        if (vehicle.getInventory().getResourceMass(Inventory.WATER) != 0D) result = false;
        if (vehicle.getInventory().getResourceMass(Inventory.FOOD) != 0D) result = false;
        if (vehicle.getInventory().getResourceMass(Inventory.ROCK_SAMPLES) != 0D) result = false;

        return result;
    }
}
