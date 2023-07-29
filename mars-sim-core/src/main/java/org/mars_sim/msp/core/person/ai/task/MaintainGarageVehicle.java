/*
 * Mars Simulation Project
 * MaintainGarageVehicle.java
 * @date 2022-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.Iterator;
import java.util.logging.Level;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Flyer;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * The MaintainGarageVehicle class is a task for performing preventive
 * maintenance on ground vehicles in a garage.
 */
public class MaintainGarageVehicle extends Task {
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MaintainGarageVehicle.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainGarageVehicle"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase MAINTAIN_VEHICLE = new TaskPhase(Msg.getString("Task.phase.maintainVehicle")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

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
		super(NAME, unit, true, false, STRESS_MODIFIER, SkillType.MECHANICS, 100D);

		// Choose an available needy ground vehicle.
		vehicle = target;
		if (vehicle.isReservedForMaintenance()) {
			clearTask(vehicle.getName() + " already reserved for Maintenance.");
			return;
		}

		vehicle.setReservedForMaintenance(true);
		vehicle.addSecondaryStatus(StatusType.MAINTENANCE);
        setDescription(Msg.getString("Task.description.maintainGarageVehicle.detail", vehicle.getName()));

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
			
			Iterator<Building> j = settlement.getBuildingManager()
					.getBuildings(FunctionType.VEHICLE_MAINTENANCE).iterator();
			while (j.hasNext() && (garage == null)) {
				Building garageBuilding = j.next();
				VehicleMaintenance garageTemp = garageBuilding.getVehicleMaintenance();
					
				if (vehicle.getVehicleType() == VehicleType.DELIVERY_DRONE) {
					if (garageTemp.getFlyerCapacity() > 0) {
						garage = garageTemp;
						garage.addFlyer((Flyer)vehicle);

						// Walk to garage.
						walkToTaskSpecificActivitySpotInBuilding(garageBuilding, FunctionType.VEHICLE_MAINTENANCE, false);
						break;
					}							
				}
				else {
					if (garageTemp.getAvailableCapacity() > 0) {
						garage = garageTemp;
						garage.addVehicle(vehicle);

						// Walk to garage.
						walkToTaskSpecificActivitySpotInBuilding(garageBuilding, FunctionType.VEHICLE_MAINTENANCE, false);
						break;
					}
				}
			}
		}

		// End task if vehicle or garage not available.
		if (garage == null) {
			clearTask(vehicle.getName() + " Can not find available garage for maintenance");
		}

		logger.log(worker, Level.FINER, 0, "Starting maintainGarageVehicle task on " + vehicle.getName());
	
		// Initialize phase
		addPhase(MAINTAIN_VEHICLE);
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

		Settlement settlement = worker.getSettlement();
		
		int shortfall = manager.transferMaintenanceParts(settlement);
		if (shortfall == -1) {
			clearTask("No spare parts for maintenance");
			return time;
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
		manager.addMaintenanceWorkTime(workTime);

		// Add experience points
		addExperience(time);

		// If maintenance is complete, task is done.
		if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) {
			logger.info(worker, "Completed maintenance on " + vehicle.getName() + ".");
			endTask();
		}

		// Check if an accident happens during maintenance.
		checkForAccident(vehicle, time, 0.001);

		return 0;
	}

	@Override
	protected void clearDown() {
		if (vehicle != null) {
			vehicle.clearDistanceLastMaintenance();
			
			vehicle.setReservedForMaintenance(false);
	        vehicle.removeSecondaryStatus(StatusType.MAINTENANCE);

			// Terminated early befor ethe Garage was selected ?
			if (garage != null) {
				if (vehicle instanceof Crewable) {
					Crewable crewableVehicle = (Crewable) vehicle;
					if (crewableVehicle.getCrewNum() == 0 && crewableVehicle.getRobotCrewNum() == 0) {
						garage.removeVehicle(vehicle, false);
					}
					else
						garage.removeVehicle(vehicle, true);
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
