/*
 * Mars Simulation Project
 * UnloadVehicleEVA.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle.task;

import java.util.logging.Level;

import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Towing;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The UnloadVehicleEVA class is a task for unloading fuel and supplies from a
 * vehicle when the vehicle is outside.
 */
public class UnloadVehicleEVA extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	static SimLogger logger = SimLogger.getLogger(UnloadVehicleEVA.class.getName());
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.unloadVehicleEVA"); //$NON-NLS-1$
	
	/** Task phases. */
	private static final TaskPhase UNLOADING = 
			new TaskPhase(Msg.getString("Task.phase.unloading"), //$NON-NLS-1$
					createPhaseImpact(PhysicalEffort.HIGH)); 

	/**
	 * The amount of resources (kg) one person of average strength can unload per
	 * millisol.
	 */
	private static final double UNLOAD_RATE = 20D;

	// Data members
	/** The vehicle that needs to be unloaded. */
	private Vehicle vehicle;
	/** The settlement the person is unloading to. */
	private Settlement settlement;

	/**
	 * Constructor
	 * 
	 * @param person  the person to perform the task
	 * @param vehicle the vehicle to be unloaded
	 */
	public UnloadVehicleEVA(Person person, Vehicle vehicle) {
		// Use EVAOperation constructor.
		super(NAME, person, RandomUtil.getRandomDouble(25D) + 10D, UNLOADING);
		setMinimumSunlight(LightLevel.NONE);

		setDescription(Msg.getString("Task.description.unloadVehicleEVA.detail", vehicle.getName())); // $NON-NLS-1$
		this.vehicle = vehicle;

		if (person.isSuperUnfit()) {
			checkLocation("Super Unfit.");
        	return;
		}
		if (!vehicle.haveStatusType(StatusType.UNLOADING)) {
			checkLocation("Vehicle is not ready for Unloading");
        	return;
		}
		
		// Determine location for unloading.
		setOutsideLocation(vehicle);
		
		settlement = vehicle.getSettlement();
		if (!settlement.equals(person.getSettlement())) {
			endTask();
			return;
		}

		// Add the vehicle to a garage if possible
		Building garage = settlement.getBuildingManager().addToGarageBuilding(vehicle);
		if (garage != null) {
			endTask();
			return;
		}

		logger.log(person, Level.FINE, 20_000, "Going to unload "  + vehicle.getName() + ".");
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);
		if (!isDone()) {
			if (getPhase() == null) {
				throw new IllegalArgumentException("Task phase is null");
			}
			else if (UNLOADING.equals(getPhase())) {
				time = unloadingPhase(time);
			}
		}
		return time;
	}

	/**
	 * Perform the unloading phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) after performing the phase.
	 */
	protected double unloadingPhase(double time) {		
		double remainingTime = 0;
		
		if (checkReadiness(time) > 0)
			return time;

		// Check if the vehicle is in a garage
		if (settlement.getBuildingManager().isInGarage(vehicle)) {
			checkLocation("Vehicle in garage.");
			return time;
		}
		
		// Determine unload rate.
		int strength = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);
		double strengthModifier = .1D + (strength * .018D);
		double amountUnloading = UNLOAD_RATE * strengthModifier * time / 4D;

		// Unload EVA Suits
		if ((amountUnloading > 0) && (vehicle instanceof Crewable crew)) {
			amountUnloading = UnloadHelper.unloadEVASuits(vehicle, settlement, amountUnloading, crew.getCrewNum());
		}

		// Unload resources.
		if (amountUnloading > 0) {
			UnloadHelper.unloadInventory(vehicle, settlement, amountUnloading);
		}

		// Unload towed vehicles.
		if (vehicle instanceof Towing towingVehicle) {
			UnloadHelper.releaseTowedVehicle(towingVehicle, settlement);
		}

		// Retrieve, examine and bury any dead bodies
		if (vehicle instanceof Crewable crewable) {
			UnloadHelper.unloadDeceased(crewable, settlement);
		}
				
        // Add experience points
        addExperience(time);

		if (isFullyUnloaded(vehicle)) {
			vehicle.removeSecondaryStatus(StatusType.UNLOADING);
			checkLocation("Vehicle already fully unloaded.");
	        return remainingTime;
		}

		// Check for an accident during the EVA operation.
		checkForAccident(time);
        
		return remainingTime;
	}
	
	/**
	 * Gets the vehicle being unloaded.
	 * 
	 * @return vehicle
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}

	/**
	 * Returns true if the vehicle is fully unloaded.
	 * 
	 * Note: look at EVA suits and remove their mass.
	 * 
	 * @param vehicle Vehicle to check.
	 * @return is vehicle fully unloaded?
	 */
	public static boolean isFullyUnloaded(Vehicle vehicle) {
		double total = vehicle.getStoredMass();
		for(Equipment e : vehicle.getSuitSet()) {
			total -= e.getMass();
		}
		
		return total <= 0.001D;
	}
}
