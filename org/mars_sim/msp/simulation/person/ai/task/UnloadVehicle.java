/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.78 2005-07-15
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/** 
 * The UnloadVehicle class is a task for unloading a fuel and supplies from a vehicle.
 */
public class UnloadVehicle extends Task implements Serializable {
	
	// Task phase
	private static final String UNLOADING = "Unloading";

    // The amount of resources (kg) one person of average strength can unload per millisol.
    private static double UNLOAD_RATE = 10D;
	private static final double STRESS_MODIFIER = .1D; // The stress modified per millisol.
	private static final double DURATION = 100D; // The duration of the task (millisols).

    // Data members
    private Vehicle vehicle;  // The vehicle that needs to be unloaded.
    private Settlement settlement; // The settlement the person is unloading to.

    /** 
     * Constructor
     * @param person the person to perform the task
     * @param vehicle the vehicle to be unloaded
     * @throws Exception if error constructing task.
     */
    public UnloadVehicle(Person person, Vehicle vehicle) throws Exception {
        super("Unloading vehicle", person, true, false, STRESS_MODIFIER, true, DURATION);

	    description = "Unloading " + vehicle.getName();
        this.vehicle = vehicle;

        settlement = person.getSettlement();
        
        // Initialize phase
        addPhase(UNLOADING);
        setPhase(UNLOADING);

        // System.out.println(person.getName() + " is unloading " + vehicle.getName());
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (UNLOADING.equals(getPhase())) return unloadingPhase(time);
    	else return time;
    }
    
    /**
     * Perform the unloading phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) after performing the phase.
     * @throws Exception if error in loading phase.
     */
    private double unloadingPhase(double time) throws Exception {
    	
        // Determine unload rate.
		int strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.STRENGTH);
		double strengthModifier = (double) strength / 50D;
        double amountUnloading = UNLOAD_RATE * strengthModifier * time;

        // If vehicle is not in a garage, unload rate is reduced.
        Building garage = BuildingManager.getBuilding(vehicle);
        if (garage == null) amountUnloading /= 4D;
        
        // Unload fuel
	    double fuelAmount = vehicle.getInventory().getResourceMass(vehicle.getFuelType());
        if (fuelAmount > amountUnloading) fuelAmount = amountUnloading;
	    vehicle.getInventory().removeResource(vehicle.getFuelType(), fuelAmount);
        settlement.getInventory().addResource(vehicle.getFuelType(), fuelAmount);
        amountUnloading -= fuelAmount;

        // Unload oxygen. 
        double oxygenAmount = vehicle.getInventory().getResourceMass(Resource.OXYGEN);
        if (oxygenAmount > amountUnloading) oxygenAmount = amountUnloading;
        vehicle.getInventory().removeResource(Resource.OXYGEN, oxygenAmount);
        settlement.getInventory().addResource(Resource.OXYGEN, oxygenAmount);
        amountUnloading -= oxygenAmount;

        // Unload water
        double waterAmount = vehicle.getInventory().getResourceMass(Resource.WATER);
        if (waterAmount > amountUnloading) waterAmount = amountUnloading;
        vehicle.getInventory().removeResource(Resource.WATER, waterAmount);
        settlement.getInventory().addResource(Resource.WATER, waterAmount);
        amountUnloading -= waterAmount;

        // Unload Food
        double foodAmount = vehicle.getInventory().getResourceMass(Resource.FOOD);
        if (foodAmount > amountUnloading) foodAmount = amountUnloading;
        vehicle.getInventory().removeResource(Resource.FOOD, foodAmount);
        settlement.getInventory().addResource(Resource.FOOD, foodAmount);
        amountUnloading -= foodAmount;

        // Unload Rock Samples 
        double rockAmount = vehicle.getInventory().getResourceMass(Resource.ROCK_SAMPLES);
        if (rockAmount > amountUnloading) rockAmount = amountUnloading;
        vehicle.getInventory().removeResource(Resource.ROCK_SAMPLES, rockAmount);
        settlement.getInventory().addResource(Resource.ROCK_SAMPLES, rockAmount);
        amountUnloading -= rockAmount;

		// Unload Ice 
		double iceAmount = vehicle.getInventory().getResourceMass(Resource.ICE);
		if (iceAmount > amountUnloading) iceAmount = amountUnloading;
		vehicle.getInventory().removeResource(Resource.ICE, iceAmount);
		settlement.getInventory().addResource(Resource.ICE, iceAmount);
		amountUnloading -= iceAmount;

        if (isFullyUnloaded(vehicle)) endTask();
        
        return 0D;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// This task adds no experience.
	}

    /** 
     * Returns true if the vehicle is fully unloaded.
     * @param vehicle Vehicle to check.
     * @return is vehicle fully unloaded?
     */
    static public boolean isFullyUnloaded(Vehicle vehicle) {
        boolean result = true;

        if (vehicle.getInventory().getResourceMass(vehicle.getFuelType()) != 0D) result = false;
        if (vehicle.getInventory().getResourceMass(Resource.OXYGEN) != 0D) result = false;
        if (vehicle.getInventory().getResourceMass(Resource.WATER) != 0D) result = false;
        if (vehicle.getInventory().getResourceMass(Resource.FOOD) != 0D) result = false;
        if (vehicle.getInventory().getResourceMass(Resource.ROCK_SAMPLES) != 0D) result = false;
        if (vehicle.getInventory().getResourceMass(Resource.ICE) != 0D) result = false;

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