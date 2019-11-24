/**
 * Mars Simulation Project
 * MaintainGroundVehicleGarage.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
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
	private static Logger logger = Logger.getLogger(MaintainGroundVehicleGarage.class.getName());

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

	private Person person;

	private Robot robot;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public MaintainGroundVehicleGarage(Unit unit) {
		super(NAME, unit, true, false, STRESS_MODIFIER, true, 10D + RandomUtil.getRandomDouble(40D));

		if (unit instanceof Person) {
			this.person = (Person) unit;

			// Choose an available needy ground vehicle.
			vehicle = getNeedyGroundVehicle(person);
			if (vehicle != null) {
				vehicle.setReservedForMaintenance(true);
	            vehicle.addStatus(StatusType.MAINTENANCE);
			}
			else
				endTask();
		}

		else {
			robot = (Robot) unit;

			// Choose an available needy ground vehicle.
			vehicle = getNeedyGroundVehicle(robot);
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
					walkToActivitySpotInBuilding(building, false);
				} catch (Exception e) {
					e.printStackTrace(System.err);
					logger.log(Level.SEVERE, "MaintainGroundVehicleGarage.constructor: " + e.getMessage(), e);
				}
			} else {
				// If not in a garage, try to add it to a garage with empty space.
				Settlement settlement = null;

				if (person != null)
					settlement = person.getSettlement();
				else
					settlement = robot.getSettlement();

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
							walkToActivitySpotInBuilding(garageBuilding, false);
						}
					} catch (Exception e) {
						e.printStackTrace(System.err);
						logger.log(Level.SEVERE, "MaintainGroundVehicleGarage.constructor: " + e.getMessage(), e);
					}
				}
			}
		}

		// End task if vehicle or garage not available.
		if ((vehicle == null) || (garage == null)) {
			endTask();
		}

		// Initialize phase
		addPhase(MAINTAIN_VEHICLE);
		setPhase(MAINTAIN_VEHICLE);

		if (person != null)
			logger.finest(person.getName() + " starting MaintainGroundVehicleGarage task.");
		else
			logger.finest(robot.getName() + " starting MaintainGroundVehicleGarage task.");
	}

	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.GROUND_VEHICLE_MAINTENANCE;
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

		if (person != null) {
			// If person is incapacitated, end task.
			if (person.getPerformanceRating() == 0D) {
				endTask();
			}
		} else {
			// If robot is disable, end task.
			if (robot.getPerformanceRating() == 0D) {
				endTask();
			}
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
			return time;
		}

		// Add repair parts if necessary.
		Inventory inv = null;
		if (person != null) {
			inv = person.getTopContainerUnit().getInventory();
		} else {
			inv = robot.getTopContainerUnit().getInventory();
		}

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
		int mechanicSkill = 0;

		if (person != null) {
			mechanicSkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
		} else {
			mechanicSkill = robot.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
		}

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
		checkForAccident(time);

		return 0D;
	}

	/**
	 * Adds experience to the person's skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience to "Mechanics" skill
		// (1 base experience point per 100 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		double newPoints = time / 100D;
		int experienceAptitude = 0;
		if (person != null) {
			experienceAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
			newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
			newPoints *= getTeachingExperienceModifier();
			person.getSkillManager().addExperience(SkillType.MECHANICS, newPoints, time);
		} else {
			experienceAptitude = robot.getRoboticAttributeManager().getAttribute(RoboticAttributeType.EXPERIENCE_APTITUDE);
			newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
			newPoints *= getTeachingExperienceModifier();
			robot.getSkillManager().addExperience(SkillType.MECHANICS, newPoints, time);
		}
	}

	/**
	 * Check for accident with entity during maintenance phase.
	 * 
	 * @param time the amount of time (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .001D;

		// Mechanic skill modification.
		int skill = 0;
		if (person != null) {
			skill = person.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
		} else {
			skill = robot.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
		}
		if (skill <= 3)
			chance *= (4 - skill);
		else
			chance /= (skill - 2);

		// Modify based on the vehicle's wear condition.
		chance *= vehicle.getMalfunctionManager().getWearConditionAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {

			if (person != null) {
				logger.info(person.getName() + " has an accident while performing maintenance on " + vehicle.getName()
						+ " inside a garage.");
				vehicle.getMalfunctionManager().createASeriesOfMalfunctions(vehicle.getName(), person);

			} else if (robot != null) {
				logger.info(robot.getName() + " has an accident while performing maintenance on " + vehicle.getName()
						+ " inside a garage.");
				vehicle.getMalfunctionManager().createASeriesOfMalfunctions(vehicle.getName(), robot);
			}

		}
	}

	/**
	 * Gets the vehicle the person is maintaining. Returns null if none.
	 * 
	 * @return entity
	 */
	public Malfunctionable getVehicle() {
		return vehicle;
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
				if ((vehicle instanceof GroundVehicle) && !vehicle.isReservedForMission()) {
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

		if (robot.isInSettlement()) {
			Iterator<Vehicle> vI = robot.getSettlement().getParkedVehicles().iterator();
			while (vI.hasNext()) {
				Vehicle vehicle = vI.next();
				if ((vehicle instanceof GroundVehicle) && !vehicle.isReservedForMission()) {
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
		// Iterator<Vehicle> i = availableVehicles.iterator();
		// while (i.hasNext()) {
		for (Vehicle vehicle : availableVehicles) {// = i.next();
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
			setDescription(Msg.getString("Task.description.maintainGroundVehicleGarage.detail", result.getName())); // $NON-NLS-1$
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
			setDescription(Msg.getString("Task.description.maintainGroundVehicleGarage.detail", result.getName())); // $NON-NLS-1$
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
		boolean hasMalfunction = manager.hasMalfunction();
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		boolean minTime = (effectiveTime >= 1000D);
		boolean enoughParts = false;
		if (person != null) {
			enoughParts = Maintenance.hasMaintenanceParts(person, vehicle);
		} else {
			enoughParts = Maintenance.hasMaintenanceParts(robot, vehicle);
		}
		if (!tethered && !hasMalfunction && minTime && enoughParts) {
			result = effectiveTime;
		}
		return result;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = null;
		if (person != null) {
			manager = person.getSkillManager();
		} else {
			manager = robot.getSkillManager();
		}

		return manager.getEffectiveSkillLevel(SkillType.MECHANICS);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.MECHANICS);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();

		garage = null;
		vehicle = null;
		person = null;
		robot = null;
	}
}