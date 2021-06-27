/**
 * Mars Simulation Project
 * MaintainGroundVehicleGarage.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The MaintainGroundVehicleGarage class is a task for performing preventive
 * maintenance on ground vehicles in a garage.
 */
public class MaintainGroundVehicleGarage extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MaintainGroundVehicleGarage.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainGroundVehicleGarage"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase MAINTAIN_VEHICLE = new TaskPhase(Msg.getString("Task.phase.maintainVehicle")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	// Data members
	/** The maintenance garage. */
	private VehicleMaintenance garage;
	/** Vehicle to be maintained. */
	private GroundVehicle vehicle;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public MaintainGroundVehicleGarage(Worker unit) {
		super(NAME, unit, true, false, STRESS_MODIFIER, SkillType.MECHANICS, 100D, 10D + RandomUtil.getRandomDouble(40D));

		if (unit instanceof Person) {
			Person lperson = (Person) unit;

			// Choose an available needy ground vehicle.
			vehicle = getNeedyGroundVehicle(lperson);
			if (vehicle != null) {
				vehicle.setReservedForMaintenance(true);
	            vehicle.addStatus(StatusType.MAINTENANCE);
			}
			else
				endTask();
		}

		else {
			Robot lrobot = (Robot) unit;

			// Choose an available needy ground vehicle.
			vehicle = getNeedyGroundVehicle(lrobot);
			if (vehicle != null) {
				vehicle.setReservedForMaintenance(true);
	            vehicle.addStatus(StatusType.MAINTENANCE);
			}
			else
				endTask();
		}

		// Determine the garage it's in.
		if (vehicle != null) {
			Building building = BuildingManager.getBuilding(vehicle);
			if (building != null) {
				try {
					garage = building.getVehicleMaintenance();
					// Walk to garage.
					walkToTaskSpecificActivitySpotInBuilding(building, FunctionType.GROUND_VEHICLE_MAINTENANCE, false);
				} catch (Exception e) {
					logger.severe(unit, "Problem walking to vehicle's garage activity spot", e);
				}
			} else {
				// If not in a garage, try to add it to a garage with empty space.
				Settlement settlement = worker.getSettlement();

				Iterator<Building> j = settlement.getBuildingManager()
						.getBuildings(FunctionType.GROUND_VEHICLE_MAINTENANCE).iterator();
				while (j.hasNext() && (garage == null)) {
					try {
						Building garageBuilding = j.next();
						VehicleMaintenance garageTemp = garageBuilding.getVehicleMaintenance();
						if (garageTemp.getCurrentVehicleNumber() < garageTemp.getVehicleCapacity()) {
							garage = garageTemp;
							garage.addVehicle(vehicle);

							// Walk to garage.
							walkToTaskSpecificActivitySpotInBuilding(garageBuilding, FunctionType.GROUND_VEHICLE_MAINTENANCE, false);
							break;
						}
					} catch (Exception e) {
						logger.severe(unit, "Problem walking to building activity spot", e);
					}
				}
			}
		}

		// End task if vehicle or garage not available.
		if ((vehicle == null) || (garage == null)) {
			endTask();
		}
		else {
			logger.log(worker, Level.FINER, 0, "Starting MaintainGroundVehicleGarage task on " + vehicle.getName());
		
			// Initialize phase
			addPhase(MAINTAIN_VEHICLE);
			setPhase(MAINTAIN_VEHICLE);
		}
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
		MalfunctionManager manager = vehicle.getMalfunctionManager();
		
		if (!vehicle.isInSettlementVicinity() || !BuildingManager.isInAGarage(vehicle)) {
        	endTask();
			return 0;
		}
		
		if (worker.getPerformanceRating() == 0D) {
			endTask();
		}

		// Check if maintenance has already been completed.
		if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) {
			endTask();
		}

		// If vehicle has malfunction, end task.
		if (manager.hasMalfunction()) {
			endTask();
		}

		if (isDone()) {
			endTask();
			return time;
		}

		// Add repair parts if necessary.
		Inventory inv = worker.getTopContainerUnit().getInventory();

		if (Maintenance.hasMaintenanceParts(inv, vehicle)) {
			Map<Integer, Integer> parts = new HashMap<>(manager.getMaintenanceParts());
			Iterator<Integer> j = parts.keySet().iterator();
			while (j.hasNext()) {
				Integer part = j.next();
				int number = parts.get(part);
				inv.retrieveItemResources(part, number);
				manager.maintainWithParts(part, number);
			}
		} else {
			vehicle.setReservedForMaintenance(false);
	        vehicle.removeStatus(StatusType.MAINTENANCE);
	        
			if (vehicle instanceof Crewable) {
				Crewable crewableVehicle = (Crewable) vehicle;
				if (crewableVehicle.getCrewNum() == 0 && crewableVehicle.getRobotCrewNum() == 0) {
					garage.removeVehicle(vehicle);
				}
			} else {
				garage.removeVehicle(vehicle);
			}
			endTask();
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
			// logger.info(person.getName() + " finished " + description);
			vehicle.setReservedForMaintenance(false);
			vehicle.removeStatus(StatusType.MAINTENANCE);
			if (vehicle instanceof Crewable) {
				Crewable crewableVehicle = (Crewable) vehicle;
				if (crewableVehicle.getCrewNum() == 0) {
					garage.removeVehicle(vehicle);
				}
			} else {
				garage.removeVehicle(vehicle);
			}
			endTask();
		}

		// Check if an accident happens during maintenance.
		checkForAccident(vehicle, 0.001D, time);

		return 0D;
	}

	/**
	 * Gets all ground vehicles requiring maintenance in a local garage.
	 * 
	 * @param person person checking.
	 * @return collection of ground vehicles available for maintenance.
	 */
	public static Collection<Vehicle> getAllVehicleCandidates(Person person) {
		Collection<Vehicle> result = new ConcurrentLinkedQueue<Vehicle>();

		if (person.isInSettlement()) {
			Iterator<Vehicle> vI = person.getSettlement().getParkedVehicles().iterator();
			while (vI.hasNext()) {
				Vehicle vehicle = vI.next();
				if (vehicle instanceof GroundVehicle && !vehicle.isReservedForMission()) {
					result.add(vehicle);
				}
			}
		}

		return result;
	}

	/**
	 * Gets all ground vehicles requiring maintenance in a local garage.
	 * 
	 * @param robot robot checking.
	 * @return collection of ground vehicles available for maintenance.
	 */
	public static Collection<Vehicle> getAllVehicleCandidates(Robot robot) {
		Collection<Vehicle> result = new ConcurrentLinkedQueue<Vehicle>();

		Settlement settlement = robot.getSettlement();
        if (settlement != null) {
            Iterator<Vehicle> vI = settlement.getParkedVehicles().iterator();
			while (vI.hasNext()) {
				Vehicle vehicle = vI.next();
				if (vehicle instanceof GroundVehicle && !vehicle.isReservedForMission()) {
					result.add(vehicle);
				}
			}
		}

		return result;
	}

	/**
	 * Gets a ground vehicle that requires maintenance in a local garage. Returns
	 * null if none available.
	 * 
	 * @param person person checking.
	 * @return ground vehicle
	 */
	private GroundVehicle getNeedyGroundVehicle(Person person) {

		GroundVehicle result = null;

		// Find all vehicles that can be maintained.
		Collection<Vehicle> availableVehicles = getAllVehicleCandidates(person);

		// Populate vehicles and probabilities.
		Map<Vehicle, Double> vehicleProb = new HashMap<Vehicle, Double>(availableVehicles.size());
		for (Vehicle vehicle : availableVehicles) {
			double prob = getProbabilityWeight(vehicle);
			if (prob > 0D) {
				vehicleProb.put(vehicle, prob);
			}
		}

		// Randomly determine needy vehicle.
		if (!vehicleProb.isEmpty()) {
			result = (GroundVehicle) RandomUtil.getWeightedRandomObject(vehicleProb);
		}

		if (result != null) {
            if (BuildingManager.isInAGarage(result)) {
            	result = null;
            }
            else {
                setDescription(Msg.getString("Task.description.maintainGroundVehicleGarage.detail",
                        result.getName())); //$NON-NLS-1$
            }
		}

		return result;
	}

	/**
	 * Gets a ground vehicle that requires maintenance in a local garage. Returns
	 * null if none available.
	 * 
	 * @param person person checking.
	 * @return ground vehicle
	 */
	private GroundVehicle getNeedyGroundVehicle(Robot robot) {

		GroundVehicle result = null;

		// Find all vehicles that can be maintained.
		Collection<Vehicle> availableVehicles = getAllVehicleCandidates(robot);

		// Populate vehicles and probabilities.
		Map<Vehicle, Double> vehicleProb = new HashMap<Vehicle, Double>(availableVehicles.size());
		for (Vehicle vehicle : availableVehicles) {		
//            if (BuildingManager.add2Garage((GroundVehicle)vehicle)) {
	            double prob = getProbabilityWeight(vehicle);
	            if (prob > 0D) {
	                vehicleProb.put(vehicle, prob);
	            }
//			}
		}

		// Randomly determine needy vehicle.
		if (!vehicleProb.isEmpty()) {
			result = (GroundVehicle) RandomUtil.getWeightedRandomObject(vehicleProb);
	        
            if (result != null) {
            	
	            if (BuildingManager.isInAGarage(result)) {
	            	result = null;
	            }
	            else {
	                setDescription(Msg.getString("Task.description.maintainGroundVehicleGarage.detail",
	                        result.getName())); //$NON-NLS-1$
	            }
	        }
		}

		return result;
	}

	/**
	 * Gets the probability weight for a vehicle.
	 * 
	 * @param vehicle the vehicle.
	 * @return the probability weight.
	 */
	private double getProbabilityWeight(Vehicle vehicle) {
		double result = 0D;
		MalfunctionManager manager = vehicle.getMalfunctionManager();
		boolean tethered = vehicle.isBeingTowed() || (vehicle.getTowingVehicle() != null);
		if (tethered)
			return 0;
		
		boolean hasMalfunction = manager.hasMalfunction();
		if (hasMalfunction)
			return 0;

		boolean hasParts = false;
		if (person != null) {
			hasParts = Maintenance.hasMaintenanceParts(person, vehicle);
		} else {
			hasParts = Maintenance.hasMaintenanceParts(robot, vehicle);
		}
		if (!hasParts)
			return 0;
		
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		boolean minTime = (effectiveTime >= 1000D);

		if (minTime) {
			result = effectiveTime;
		}
		return result;
	}
}
