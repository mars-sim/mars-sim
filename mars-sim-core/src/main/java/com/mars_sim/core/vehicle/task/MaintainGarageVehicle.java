/*
 * Mars Simulation Project
 * MaintainGarageVehicle.java
 * @date 2022-09-20
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle.task;

import java.util.logging.Level;

import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.VehicleMaintenance;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Flyer;
import com.mars_sim.core.vehicle.StatusType;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.tools.Msg;

/**
 * The MaintainGarageVehicle class is a task for performing preventive
 * maintenance on ground vehicles in a garage.
 */
public class MaintainGarageVehicle extends Task {
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MaintainGarageVehicle.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString(
			"Task.description.maintainGarageVehicle"); //$NON-NLS-1$

    private static final String DETAIL = Msg.getString(
    		"Task.description.maintainGarageVehicle.detail") + " "; //$NON-NLS-1$
    
	/** Task phases. */
	private static final TaskPhase MAINTAIN_VEHICLE = new TaskPhase(Msg.getString("Task.phase.maintainVehicle")); //$NON-NLS-1$

	private static final ExperienceImpact IMPACT = new ExperienceImpact(10D, 
			NaturalAttributeType.EXPERIENCE_APTITUDE, true, 0.1D, SkillType.MECHANICS);

	// Data members
	/** The maintenance garage. */
	private VehicleMaintenance garage;
	/** Vehicle to be maintained. */
	private Vehicle vehicle;

	/**
	 * Constructor.
	 * @param target
	 * 
	 * @param person the person to perform the task
	 */
	public MaintainGarageVehicle(Worker unit, Vehicle target) {
		super(NAME, unit,  false, IMPACT, 100D);

		// Choose an available needy ground vehicle.
		vehicle = target;
		if (vehicle.isReservedForMaintenance()) {
			clearTask(vehicle.getName() + " already reserved for Maintenance.");
			return;
		}

		vehicle.setReservedForMaintenance(true);
		vehicle.addSecondaryStatus(StatusType.MAINTENANCE);
        
		String des = DETAIL + vehicle.getName();
		setDescription(des);
		logger.info(person, 4_000, des + ".");
        
		// Determine the garage it's in.
		Building building = vehicle.getGarage();
		if (building != null) {
			garage = building.getVehicleMaintenance();
			// Walk to garage.
			walkToTaskSpecificActivitySpotInBuilding(building, FunctionType.VEHICLE_MAINTENANCE, false);
		}
		else {
			// If not in a garage, try to add it to a garage with empty space.
			Settlement settlement = worker.getSettlement();

			if (settlement == null) {
				clearTask(worker.getName() + " not in a settlement.");
				return;
			}
			
			vehicle.addToAGarage();
			
			building = settlement.getBuildingManager().addToGarageBuilding(vehicle);
				
			if (building != null) {
				garage = building.getVehicleMaintenance();
			}
		}

		// End task if vehicle or garage not available.
		if (garage == null) {
			clearTask("No available garage for " + vehicle.getName() + " maintenance.");
			return;
		}
		else {
			// Walk to garage.
			walkToTaskSpecificActivitySpotInBuilding(building, FunctionType.VEHICLE_MAINTENANCE, false);
		}

		logger.log(worker, Level.FINER, 0, "Starting maintainGarageVehicle task on " + vehicle.getName());
	
		// Initialize phase
		setPhase(MAINTAIN_VEHICLE);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (MAINTAIN_VEHICLE.equals(getPhase())) {
			return maintainVehiclePhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the maintain vehicle phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left after performing the phase.
	 */
	private double maintainVehiclePhase(double time) {
    	
		if (vehicle.getSettlement() == null || !vehicle.getSettlement().getBuildingManager().isInGarage(vehicle)) {
        	endTask();
			return time;
		}
		
		if (worker.getPerformanceRating() <= .1) {
			endTask();
			return time;
		}

		MalfunctionManager manager = vehicle.getMalfunctionManager();
		
		// Check if maintenance has already been completed.
		if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) {
			endTask();
			return time;
		}

		// If vehicle has malfunction, end task.
		if (manager.hasMalfunction()) {
			endTask();
			return time * .75;
		}

		if (isDone()) {
			endTask();
			return time;
		}
	
		// Note: if parts don't exist, it simply means that one can still do the 
		// inspection portion of the maintenance with no need of replacing any parts
		boolean partsPosted = manager.hasMaintenancePartsInStorage(vehicle.getAssociatedSettlement());
		
		if (partsPosted) {
			Unit containerUnit = vehicle.getAssociatedSettlement();

			int shortfall = manager.transferMaintenanceParts((EquipmentOwner) containerUnit);
			
			if (shortfall == -1) {
				logger.warning(worker, 30_000L, "No spare parts available for maintenance on " 
						+ vehicle + ".");
			}
		}

		// Determine effective work time based on "Mechanic" skill.
		double workTime = time;
		int mechanicSkill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);

		if (mechanicSkill == 0) {
			workTime /= 2;
		}
		if (mechanicSkill > 1) {
			workTime += workTime * (.2D * mechanicSkill);
		}

		// Add work to the maintenance
		manager.addInspectionMaintWorkTime(workTime);

		// Add experience points
		addExperience(time);

		// If maintenance is complete, task is done.
		if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) {
			logger.info(worker, "Completed maintenance on " + vehicle.getName() + ".");
			endTask();
		}

		// Check if an accident happens during maintenance.
		checkForAccident(vehicle, time, 0.007);

		return 0;
	}

	@Override
	protected void clearDown() {
		if (vehicle != null) {
			vehicle.clearDistanceLastMaintenance();
			
			vehicle.setReservedForMaintenance(false);
	        vehicle.removeSecondaryStatus(StatusType.MAINTENANCE);

			// Terminated early before the Garage was selected ?
			if (garage != null) {
				if (vehicle instanceof Crewable crewableVehicle) {
					boolean transCrew = (crewableVehicle.getCrewNum() > 0 || crewableVehicle.getRobotCrewNum() > 0);
					garage.removeVehicle(vehicle, transCrew);
				} else {
					if (vehicle.getVehicleType() == VehicleType.DELIVERY_DRONE) {
						garage.removeFlyer((Flyer)vehicle);
					}
					else
						garage.removeVehicle(vehicle, false);
				}
			}
		}
		super.clearDown();
	}
}
