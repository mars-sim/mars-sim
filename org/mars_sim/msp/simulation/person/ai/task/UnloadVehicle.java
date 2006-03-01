/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.78 2005-07-15
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.vehicle.Crewable;
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
        
        Inventory vehicleInv = vehicle.getInventory();
        Inventory settlementInv = settlement.getInventory();
        
        // Unload fuel
	    double fuelAmount = vehicleInv.getAmountResourceStored(vehicle.getFuelType());
        if (fuelAmount > amountUnloading) fuelAmount = amountUnloading;
        vehicleInv.retrieveAmountResource(vehicle.getFuelType(), fuelAmount);
        settlementInv.storeAmountResource(vehicle.getFuelType(), fuelAmount);
        amountUnloading -= fuelAmount;

        // Unload oxygen. 
        double oxygenAmount = vehicleInv.getAmountResourceStored(AmountResource.OXYGEN);
        if (oxygenAmount > amountUnloading) oxygenAmount = amountUnloading;
        vehicleInv.retrieveAmountResource(AmountResource.OXYGEN, oxygenAmount);
        settlementInv.storeAmountResource(AmountResource.OXYGEN, oxygenAmount);
        amountUnloading -= oxygenAmount;

        // Unload water
        double waterAmount = vehicleInv.getAmountResourceStored(AmountResource.WATER);
        if (waterAmount > amountUnloading) waterAmount = amountUnloading;
        vehicleInv.retrieveAmountResource(AmountResource.WATER, waterAmount);
        settlementInv.storeAmountResource(AmountResource.WATER, waterAmount);
        amountUnloading -= waterAmount;

        // Unload Food
        double foodAmount = vehicleInv.getAmountResourceStored(AmountResource.FOOD);
        if (foodAmount > amountUnloading) foodAmount = amountUnloading;
        vehicleInv.retrieveAmountResource(AmountResource.FOOD, foodAmount);
        settlementInv.storeAmountResource(AmountResource.FOOD, foodAmount);
        amountUnloading -= foodAmount;

        // Unload Rock Samples 
        double rockAmount = vehicleInv.getAmountResourceStored(AmountResource.ROCK_SAMPLES);
        if (rockAmount > amountUnloading) rockAmount = amountUnloading;
        vehicleInv.retrieveAmountResource(AmountResource.ROCK_SAMPLES, rockAmount);
        settlementInv.storeAmountResource(AmountResource.ROCK_SAMPLES, rockAmount);
        amountUnloading -= rockAmount;

		// Unload Ice 
		double iceAmount = vehicleInv.getAmountResourceStored(AmountResource.ICE);
		if (iceAmount > amountUnloading) iceAmount = amountUnloading;
		vehicleInv.retrieveAmountResource(AmountResource.ICE, iceAmount);
		settlementInv.storeAmountResource(AmountResource.ICE, iceAmount);
		amountUnloading -= iceAmount;

        // If crewable vehicle, unload EVA suits.
        if (vehicle instanceof Crewable) {
        	int numEvaSuits = vehicleInv.findNumUnitsOfClass(EVASuit.class);
        	for (int x = 0; x < numEvaSuits; x++) {
        		Unit suit = vehicleInv.findUnitOfClass(EVASuit.class);
        		if ((suit != null) && settlementInv.canStoreUnit(suit)) {
        			vehicleInv.retrieveUnit(suit);
        			settlementInv.storeUnit(suit);
        		}
        		else throw new Exception("Suit not found in vehicle or cannot be stored in settlement.");
        	}
        }
		
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

        Inventory vehicleInv = vehicle.getInventory();
        
        if (vehicleInv.getAmountResourceStored(vehicle.getFuelType()) != 0D) result = false;
        if (vehicleInv.getAmountResourceStored(AmountResource.OXYGEN) != 0D) result = false;
        if (vehicleInv.getAmountResourceStored(AmountResource.WATER) != 0D) result = false;
        if (vehicleInv.getAmountResourceStored(AmountResource.FOOD) != 0D) result = false;
        if (vehicleInv.getAmountResourceStored(AmountResource.ROCK_SAMPLES) != 0D) result = false;
        if (vehicleInv.getAmountResourceStored(AmountResource.ICE) != 0D) result = false;

        // If a crewable vehicle, check for remaining EVA suits.
        if (vehicle instanceof Crewable) {
        	if (vehicleInv.findNumUnitsOfClass(EVASuit.class) > 0) result = false;
        }
        
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