/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.75 2003-04-27
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.simulation.vehicle.*;
import java.io.Serializable;

/** 
 * The LoadVehicle class is a task for loading a vehicle with fuel and supplies.
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
    public LoadVehicle(Person person, Mars mars, Vehicle vehicle) {
        super("Loading vehicle", person, true, mars);

        description = "Loading " + vehicle.getName();
        this.vehicle = vehicle;

        settlement = person.getSettlement();
    }

    /** Performs this task for a given period of time
     *  @param time amount of time to perform task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();
	
        // Determine load rate.
        double amountLoading = LOAD_RATE * time;
        
        // If vehicle is not in a garage, load rate is reduced.
        VehicleMaintenance garage = BuildingManager.getBuilding(vehicle);
        if (garage == null) amountLoading /= 4D;
           

        if (hasEnoughSupplies(settlement, vehicle)) {

            // Load methane
	        double methaneAmount = vehicle.getInventory().getResourceRemainingCapacity(Resource.METHANE);
            if (methaneAmount > amountLoading) methaneAmount = amountLoading;
	        settlement.getInventory().removeResource(Resource.METHANE, methaneAmount);
	        vehicle.getInventory().addResource(Resource.METHANE, methaneAmount);
            amountLoading -= methaneAmount;

            // Load oxygen
	        double oxygenAmount = vehicle.getInventory().getResourceRemainingCapacity(Resource.OXYGEN);
            if (oxygenAmount > amountLoading) oxygenAmount = amountLoading;
	        settlement.getInventory().removeResource(Resource.OXYGEN, oxygenAmount);
	        vehicle.getInventory().addResource(Resource.OXYGEN, oxygenAmount);
            amountLoading -= oxygenAmount;
	    
            // Load water
	        double waterAmount = vehicle.getInventory().getResourceRemainingCapacity(Resource.WATER);
            if (waterAmount > amountLoading) waterAmount = amountLoading;
	        settlement.getInventory().removeResource(Resource.WATER, waterAmount);
	        vehicle.getInventory().addResource(Resource.WATER, waterAmount);
            amountLoading -= waterAmount;

            // Load Food
            double foodAmount = vehicle.getInventory().getResourceRemainingCapacity(Resource.FOOD);
            if (foodAmount > amountLoading) foodAmount = amountLoading;
	        settlement.getInventory().removeResource(Resource.FOOD, foodAmount);
	        vehicle.getInventory().addResource(Resource.FOOD, foodAmount);
            amountLoading -= foodAmount;
        }
        else endTask();

        if (isFullyLoaded(vehicle)) endTask();

        return 0;
    }

    /** Returns true if there are enough supplies in the settlements stores to supply vehicle.
     *  @param settlement the settlement the vehicle is at
     *  @param vehicle the vehicle to be checked
     *  @return enough supplies?
     */
    public static boolean hasEnoughSupplies(Settlement settlement, Vehicle vehicle) {
        boolean enoughSupplies = true;

        double neededMethane = vehicle.getInventory().getResourceRemainingCapacity(Resource.METHANE);
        double storedMethane = settlement.getInventory().getResourceMass(Resource.METHANE);
        if (neededMethane > storedMethane - 50D) enoughSupplies = false;

        double neededOxygen = vehicle.getInventory().getResourceRemainingCapacity(Resource.OXYGEN);
        double storedOxygen = settlement.getInventory().getResourceMass(Resource.OXYGEN);
        if (neededOxygen > storedOxygen - 50D) enoughSupplies = false;

        double neededWater = vehicle.getInventory().getResourceRemainingCapacity(Resource.WATER);
        double storedWater = settlement.getInventory().getResourceMass(Resource.WATER);
        if (neededWater > storedWater - 50D) enoughSupplies = false;
	
        double neededFood = vehicle.getInventory().getResourceRemainingCapacity(Resource.FOOD);
        double storedFood = settlement.getInventory().getResourceMass(Resource.FOOD);
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

        if (i.getResourceRemainingCapacity(Resource.METHANE) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Resource.OXYGEN) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Resource.WATER) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Resource.FOOD) > 0D) result = false;

        return result;
    }
}
