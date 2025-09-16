/*
 * Mars Simulation Project
 * UnloadVehicleGarage.java
 * @date 2025-07-21
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle.task;

import java.util.logging.Level;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Towing;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * The UnloadVehicleGarage class is a task for unloading fuel and supplies from
 * a vehicle in a vehicle maintenance garage.
 */
public class UnloadVehicleGarage extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(UnloadVehicleGarage.class.getName());
	
	private static final String NAME = Msg.getString("Task.description.unloadVehicleGarage");
	
	private static final String DETAIL = Msg.getString("Task.description.unloadVehicleGarage.detail");
	
	/** Task phases. */
	private static final TaskPhase UNLOADING = new TaskPhase(Msg.getString("Task.phase.unloading")); //$NON-NLS-1$

	private static final ExperienceImpact IMPACT = new ExperienceImpact(0.1D, NaturalAttributeType.EXPERIENCE_APTITUDE,
										PhysicalEffort.HIGH, 0.2D, SkillType.MECHANICS);
	
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
	 * Constructor.
	 *
	 * @param robot the robot to perform the task
	 * @param vehicle the vehicle to be unloaded
	 */
	public UnloadVehicleGarage(Worker worker, Vehicle vehicle) {
		// Use Task constructor.
		super(NAME, worker, false, IMPACT,
					RandomUtil.getRandomDouble(40D) + 10D);
		
		settlement = worker.getSettlement();

		if (worker.isOutside() || (settlement == null)) {
			endTask();
			return;
		}

		this.vehicle = vehicle;
		if (!vehicle.haveStatusType(StatusType.UNLOADING)) {
			clearTask(vehicle.getName() + " not ready for unloading.");
			return;
		}
		
		if (isFullyUnloaded(vehicle)) {
			vehicle.removeSecondaryStatus(StatusType.UNLOADING);
			clearTask(vehicle.getName() + " already unloaded. No need to execute UnloadVehicleGarage");
			return;
		}
		
		logger.log(worker, Level.FINER, 0, "Going to unload " + vehicle.getName() + ".");

		// Add the vehicle to a garage if possible
		Building garage = settlement.getBuildingManager().addToGarageBuilding(vehicle);

		// End task if vehicle or garage not available
		if (garage == null) {
			clearTask(vehicle.getName() + " no garage found.");
			return;
		}

		// Walk to garage
		walkToTaskSpecificActivitySpotInBuilding(garage, FunctionType.VEHICLE_MAINTENANCE, false);
		
		// Set the description
		setDescription(DETAIL + vehicle.getName());

		// Initialize phase
		setPhase(UNLOADING);
	}
	
	/**
	 * Gets the vehicle being unloaded.
	 *
	 * @return vehicle
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			logger.warning(worker, "Had no task phase. Ending the task of unloading vehicle garage.");
			endTask();
			return time;
		} else if (UNLOADING.equals(getPhase())) {
			return unloadingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Perform the unloading phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) after performing the phase.
	 */
	protected double unloadingPhase(double time) {
		if (!settlement.getBuildingManager().addToGarage(vehicle)) {
			logger.warning(vehicle, "Not in a garage");
        	endTask();
		}
		else {
			int strength = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);

			double strengthModifier = .1D + (strength * .018D);
			double amountUnloading = UNLOAD_RATE * strengthModifier * time;

			// Unload EVA Suits
			if (amountUnloading > 0) {
				amountUnloading = UnloadHelper.unloadEVASuits(vehicle, settlement, amountUnloading, 0);
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

			if (isFullyUnloaded(vehicle)) {
				vehicle.removeSecondaryStatus(StatusType.UNLOADING);

				// Remove the vehicle from garage when done
//				BuildingManager.removeFromGarage(vehicle);
				
				endTask();
			}
		}
		return 0D;
	}

	/**
	 * Returns true if the vehicle is fully unloaded.
	 *
	 * @param vehicle Vehicle to check.
	 * @return is vehicle fully unloaded?
	 */
	private static final boolean isFullyUnloaded(Vehicle vehicle) {
		return vehicle.isEmpty();
	}
}
