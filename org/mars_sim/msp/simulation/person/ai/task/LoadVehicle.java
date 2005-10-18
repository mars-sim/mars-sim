/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.78 2005-07-14
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/** 
 * The LoadVehicle class is a task for loading a vehicle with fuel and supplies.
 */
public class LoadVehicle extends Task implements Serializable {

	// Task phase
	private static final String LOADING = "Loading";
	
	private static final double STRESS_MODIFIER = .1D; // The stress modified per millisol.

    // The amount of resources (kg) one person of average strength can load per millisol.
    private static double LOAD_RATE = 10D;
    
    // The duration of the loading task (millisols).
    private static double DURATION = 100D;

    // Data members
    private Vehicle vehicle;  // The vehicle that needs to be loaded.
    private Settlement settlement; // The person's settlement.

    /** 
     * Constructor
     * @param person the person to perform the task
     * @param vehicle the vehicle to be loaded
     * @throws Exception if error constructing task.
     */
    public LoadVehicle(Person person, Vehicle vehicle) throws Exception {
        super("Loading vehicle", person, true, false, STRESS_MODIFIER, true, DURATION);

        description = "Loading " + vehicle.getName();
        this.vehicle = vehicle;

        settlement = person.getSettlement();
        
        // Initialize task phase
        addPhase(LOADING);
        setPhase(LOADING);
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (LOADING.equals(getPhase())) return loadingPhase(time);
    	else return time;
    }
    
    /**
     * Perform the loading phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) after performing the phase.
     * @throws Exception if error in loading phase.
     */
    private double loadingPhase(double time) throws Exception {
    	
        // Determine load rate.
        int strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.STRENGTH);
        double strengthModifier = (double) strength / 50D;
        double amountLoading = LOAD_RATE * strengthModifier * time;
        
        // If vehicle is not in a garage, load rate is reduced.
        Building garage = BuildingManager.getBuilding(vehicle);
        if (garage == null) amountLoading /= 4D;
           
        // If there are enough supplies at the settlement, load the vehicle.
        if (hasEnoughSupplies(settlement, vehicle)) {

            // Load fuel
	        double fuelAmount = vehicle.getInventory().getResourceRemainingCapacity(vehicle.getFuelType());
            if (fuelAmount > amountLoading) fuelAmount = amountLoading;
	        settlement.getInventory().removeResource(vehicle.getFuelType(), fuelAmount);
	        vehicle.getInventory().addResource(vehicle.getFuelType(), fuelAmount);
            amountLoading -= fuelAmount;

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
        
        return 0D;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// This task adds no experience.
	}

    /** Returns true if there are enough supplies in the settlements stores to supply vehicle.
     *  @param settlement the settlement the vehicle is at
     *  @param vehicle the vehicle to be checked
     *  @return enough supplies?
     */
    public static boolean hasEnoughSupplies(Settlement settlement, Vehicle vehicle) {
    	// Check input parameters.
    	if (settlement == null) throw new IllegalArgumentException("settlement is null");
    	if (vehicle == null) throw new IllegalArgumentException("vehicle is null");
    	
        boolean enoughSupplies = true;

        double neededFuel = vehicle.getInventory().getResourceRemainingCapacity(vehicle.getFuelType());
        double storedFuel = settlement.getInventory().getResourceMass(vehicle.getFuelType());
        if (neededFuel > storedFuel - 50D) enoughSupplies = false;

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

        if (i.getResourceRemainingCapacity(vehicle.getFuelType()) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Resource.OXYGEN) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Resource.WATER) > 0D) result = false;
        if (i.getResourceRemainingCapacity(Resource.FOOD) > 0D) result = false;

        return result;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		return 0;	
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List getAssociatedSkills() {
		List results = new ArrayList();
		return results;
	}
}