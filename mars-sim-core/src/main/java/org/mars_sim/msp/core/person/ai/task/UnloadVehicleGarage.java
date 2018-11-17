/**
 * Mars Simulation Project
 * UnloadVehicleGarage.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Towing;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The UnloadVehicleGarage class is a task for unloading fuel and supplies from
 * a vehicle in a vehicle maintenance garage.
 */
public class UnloadVehicleGarage extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(UnloadVehicleGarage.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.unloadVehicleGarage"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase UNLOADING = new TaskPhase(Msg.getString("Task.phase.unloading")); //$NON-NLS-1$

	/**
	 * The amount of resources (kg) one person of average strength can unload per
	 * millisol.
	 */
	private static double UNLOAD_RATE = 20D;

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	/** The duration of the task (millisols). */
	private static final double DURATION = RandomUtil.getRandomDouble(40D) + 10D;

	// Data members
	/** The vehicle that needs to be unloaded. */
	private Vehicle vehicle;
	/** The settlement the person is unloading to. */
	private Settlement settlement;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task.
	 */
	public UnloadVehicleGarage(Person person) {
		// Use Task constructor.
		super(NAME, person, true, false, STRESS_MODIFIER, true, DURATION);

		settlement = person.getSettlement();

		VehicleMission mission = getMissionNeedingUnloading();
		if (mission != null) {
			vehicle = mission.getVehicle();
		} else {
			List<Vehicle> nonMissionVehicles = getNonMissionVehiclesNeedingUnloading(settlement);
			if (nonMissionVehicles.size() > 0) {
				vehicle = nonMissionVehicles.get(RandomUtil.getRandomInt(nonMissionVehicles.size() - 1));
			}
		}

		if (vehicle != null) {
			setDescription(Msg.getString("Task.description.unloadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$

			// If vehicle is in a garage, add person to garage.
			Building garageBuilding = BuildingManager.getBuilding(vehicle);
			if (garageBuilding != null) {
				// Walk to garage building.
				walkToActivitySpotInBuilding(garageBuilding, false);
			}

			// End task if vehicle or garage not available.
			if ((vehicle == null) || (garageBuilding == null)) {
				endTask();
			}

			// Initialize task phase
			addPhase(UNLOADING);
			setPhase(UNLOADING);
		} else
			endTask();
	}

	public UnloadVehicleGarage(Robot robot) {
		// Use Task constructor.
		super(NAME, robot, true, false, STRESS_MODIFIER, true, DURATION);

		settlement = robot.getSettlement();

		VehicleMission mission = getMissionNeedingUnloading();
		if (mission != null) {
			vehicle = mission.getVehicle();
		} else {
			List<Vehicle> nonMissionVehicles = getNonMissionVehiclesNeedingUnloading(settlement);
			if (nonMissionVehicles.size() > 0) {
				vehicle = nonMissionVehicles.get(RandomUtil.getRandomInt(nonMissionVehicles.size() - 1));
			}
		}

		if (vehicle != null) {
			setDescription(Msg.getString("Task.description.unloadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$

			// If vehicle is in a garage, add robot to garage.
			Building garageBuilding = BuildingManager.getBuilding(vehicle);
			if (garageBuilding != null) {
				// Walk to garage building.
				walkToActivitySpotInBuilding(garageBuilding, false);
			}

			// End task if vehicle or garage not available.
			if ((vehicle == null) || (garageBuilding == null)) {
				endTask();
			}

			// Initialize task phase
			addPhase(UNLOADING);
			setPhase(UNLOADING);
		} else
			endTask();
	}

	/**
	 * Constructor
	 * 
	 * @param person  the person to perform the task
	 * @param vehicle the vehicle to be unloaded
	 */
	public UnloadVehicleGarage(Person person, Vehicle vehicle) {
		// Use Task constructor.
		super("Unloading vehicle", person, true, false, STRESS_MODIFIER, true, DURATION);

		setDescription(Msg.getString("Task.description.unloadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$;
		this.vehicle = vehicle;

		settlement = person.getSettlement();

		// If vehicle is in a garage, add person to garage.
		Building garageBuilding = BuildingManager.getBuilding(vehicle);
		if (garageBuilding != null) {
			// Walk to garage building.
			walkToActivitySpotInBuilding(garageBuilding, false);
		}

		// Initialize phase
		addPhase(UNLOADING);
		setPhase(UNLOADING);

		logger.fine(person.getName() + " is unloading " + vehicle.getName());
	}

	public UnloadVehicleGarage(Robot robot, Vehicle vehicle) {
		// Use Task constructor.
		super("Unloading vehicle", robot, true, false, STRESS_MODIFIER, true, DURATION);

		setDescription(Msg.getString("Task.description.unloadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$;
		this.vehicle = vehicle;

		settlement = robot.getSettlement();

		// If vehicle is in a garage, add robot to garage.
		Building garageBuilding = BuildingManager.getBuilding(vehicle);
		if (garageBuilding != null) {
			// Walk to garage building.
			walkToActivitySpotInBuilding(garageBuilding, false);
		}

		// Initialize phase
		addPhase(UNLOADING);
		setPhase(UNLOADING);

		logger.fine(robot.getName() + " is unloading " + vehicle.getName());
	}

	@Override
	protected FunctionType getLivingFunction() {
		return FunctionType.GROUND_VEHICLE_MAINTENANCE;
	}

	@Override
	protected FunctionType getRoboticFunction() {
		return FunctionType.GROUND_VEHICLE_MAINTENANCE;
	}

	/**
	 * Gets a list of vehicles that need unloading and aren't reserved for a
	 * mission.
	 * 
	 * @param settlement the settlement the vehicle is at.
	 * @return list of vehicles.
	 */
	public static List<Vehicle> getNonMissionVehiclesNeedingUnloading(Settlement settlement) {
		List<Vehicle> result = new ArrayList<Vehicle>();

		if (settlement != null) {
			Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				boolean needsUnloading = false;
				if (!vehicle.isReserved()) {
					int peopleOnboard = CollectionUtils.getPerson(vehicle.getInventory().getContainedUnits()).size();
					if (peopleOnboard == 0) {
						if (BuildingManager.getBuilding(vehicle) != null) {
							if (vehicle.getInventory().getTotalInventoryMass(false) > 0D) {
								needsUnloading = true;
							}
							if (vehicle instanceof Towing) {
								if (((Towing) vehicle).getTowedVehicle() != null) {
									needsUnloading = true;
								}
							}
						}
					}

					int robotsOnboard = CollectionUtils.getRobot(vehicle.getInventory().getContainedUnits()).size();
					if (robotsOnboard == 0) {
						if (BuildingManager.getBuilding(vehicle) != null) {
							if (vehicle.getInventory().getTotalInventoryMass(false) > 0D) {
								needsUnloading = true;
							}
							if (vehicle instanceof Towing) {
								if (((Towing) vehicle).getTowedVehicle() != null) {
									needsUnloading = true;
								}
							}
						}
					}

				}
				if (needsUnloading) {
					result.add(vehicle);
				}
			}
		}

		return result;
	}

	/**
	 * Gets a list of all disembarking vehicle missions at a settlement.
	 * 
	 * @param settlement the settlement.
	 * @return list of vehicle missions.
	 */
	public static List<Mission> getAllMissionsNeedingUnloading(Settlement settlement) {

		List<Mission> result = new ArrayList<Mission>();

		MissionManager manager = Simulation.instance().getMissionManager();
		Iterator<Mission> i = manager.getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				if (VehicleMission.DISEMBARKING.equals(mission.getPhase())) {
					VehicleMission vehicleMission = (VehicleMission) mission;
					if (vehicleMission.hasVehicle()) {
						Vehicle vehicle = vehicleMission.getVehicle();
						if (settlement == vehicle.getSettlement()) {
							int peopleOnboard = CollectionUtils.getPerson(vehicle.getInventory().getContainedUnits())
									.size();
							if (peopleOnboard == 0) {
								if (!isFullyUnloaded(vehicle)) {
									if (BuildingManager.getBuilding(vehicle) != null) {
										result.add(vehicleMission);
									}
								}
							}

							int robotsOnboard = CollectionUtils.getRobot(vehicle.getInventory().getContainedUnits())
									.size();
							if (robotsOnboard == 0) {
								if (!isFullyUnloaded(vehicle)) {
									if (BuildingManager.getBuilding(vehicle) != null) {
										result.add(vehicleMission);
									}
								}
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets a random vehicle mission unloading at the settlement.
	 * 
	 * @return vehicle mission.
	 */
	private VehicleMission getMissionNeedingUnloading() {

		VehicleMission result = null;
		List<Mission> unloadingMissions = null;
		if (person != null)
			unloadingMissions = getAllMissionsNeedingUnloading(person.getSettlement());
		else if (robot != null)
			unloadingMissions = getAllMissionsNeedingUnloading(robot.getSettlement());

		if (unloadingMissions.size() > 0) {
			int index = RandomUtil.getRandomInt(unloadingMissions.size() - 1);
			result = (VehicleMission) unloadingMissions.get(index);
		}

		return result;
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
			throw new IllegalArgumentException("Task phase is null");
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
		int strength = 0;
		// Determine unload rate.
		if (person != null)
			strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);
		else if (robot != null)
			strength = robot.getRoboticAttributeManager().getAttribute(RoboticAttributeType.STRENGTH);

		double strengthModifier = .1D + (strength * .018D);
		double amountUnloading = UNLOAD_RATE * strengthModifier * time;

		Inventory vehicleInv = vehicle.getInventory();
		if (settlement == null) {
			endTask();
			return 0D;
		}

		Inventory settlementInv = settlement.getInventory();

		// Unload equipment.
		if (amountUnloading > 0D) {
			Iterator<Unit> k = vehicleInv.findAllUnitsOfClass(Equipment.class).iterator();
			while (k.hasNext() && (amountUnloading > 0D)) {
				Equipment equipment = (Equipment) k.next();

				// Unload inventories of equipment (if possible)
				unloadEquipmentInventory(equipment);

				vehicleInv.retrieveUnit(equipment);
				settlementInv.storeUnit(equipment);
				amountUnloading -= equipment.getMass();
			}
		}

		// Unload amount resources.
		Iterator<Integer> i = vehicleInv.getAllARStored(false).iterator();
		while (i.hasNext() && (amountUnloading > 0D)) {
			Integer resource = i.next();
			double amount = vehicleInv.getAmountResourceStored(resource, false);
			if (amount > amountUnloading)
				amount = amountUnloading;
			double capacity = settlementInv.getAmountResourceRemainingCapacity(resource, true, false);
			if (capacity < amount) {
				amount = capacity;
				amountUnloading = 0D;
			}
			try {
				vehicleInv.retrieveAmountResource(resource, amount);
				settlementInv.storeAmountResource(resource, amount, true);
			} catch (Exception e) {
			}
			amountUnloading -= amount;
		}

		// Unload item resources.
		if (amountUnloading > 0D) {
			Iterator<ItemResource> j = vehicleInv.getAllItemResourcesStored().iterator();
			while (j.hasNext() && (amountUnloading > 0D)) {
				ItemResource resource = j.next();
				int num = vehicleInv.getItemResourceNum(resource);
				if ((num * resource.getMassPerItem()) > amountUnloading) {
					num = (int) Math.round(amountUnloading / resource.getMassPerItem());
					if (num == 0) {
						num = 1;
					}
				}
				vehicleInv.retrieveItemResources(resource, num);
				settlementInv.storeItemResources(resource, num);
				amountUnloading -= (num * resource.getMassPerItem());
			}
		}

		// Unload towed vehicles.
		if (vehicle instanceof Towing) {
			Towing towingVehicle = (Towing) vehicle;
			Vehicle towedVehicle = towingVehicle.getTowedVehicle();
			if (towedVehicle != null) {
				towingVehicle.setTowedVehicle(null);
				towedVehicle.setTowingVehicle(null);
				if (!settlementInv.containsUnit(towedVehicle)) {
					settlementInv.storeUnit(towedVehicle);
					towedVehicle.determinedSettlementParkedLocationAndFacing();
				}
			}
		}

		// Retrieve, exam and bury any dead bodies
		if (this instanceof Crewable) {
			Crewable crewable = (Crewable) this;
			for (Person p : crewable.getCrew()) {
				if (p.isDeclaredDead()) {
					logger.info("Retrieving the dead body of " + p + " from " + vehicle.getName() + " parked inside "
							+ settlement);
					PhysicalCondition pc = p.getPhysicalCondition();
					pc.handleBody();
					// pc.retrieveBody();
					// pc.examBody(pc.getDeathDetails().getProblem());
					// p.buryBody();
				}
			}
		}

		if (isFullyUnloaded(vehicle)) {
			endTask();
		}

		return 0D;
	}

	/**
	 * Unload the inventory from a piece of equipment.
	 * 
	 * @param equipment the equipment.
	 */
	private void unloadEquipmentInventory(Equipment equipment) {
		Inventory eInv = equipment.getInventory();
		Inventory sInv = settlement.getInventory();

		// Unload amount resources.
		// Note: only unloading amount resources at the moment.
		Iterator<Integer> i = eInv.getAllARStored(false).iterator();
		while (i.hasNext()) {
			Integer resource = i.next();
			double amount = eInv.getAmountResourceStored(resource, false);
			double capacity = sInv.getAmountResourceRemainingCapacity(resource, true, false);
			if (amount < capacity)
				amount = capacity;
			try {
				eInv.retrieveAmountResource(resource, amount);
				sInv.storeAmountResource(resource, amount, true);
			} catch (Exception e) {
			}
		}
	}

	@Override
	protected void addExperience(double time) {
		// This task adds no experience.
	}

	/**
	 * Returns true if the vehicle is fully unloaded.
	 * 
	 * @param vehicle Vehicle to check.
	 * @return is vehicle fully unloaded?
	 */
	static public boolean isFullyUnloaded(Vehicle vehicle) {
		return (vehicle.getInventory().getTotalInventoryMass(false) == 0D);
	}

	@Override
	public int getEffectiveSkillLevel() {
		return 0;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(0);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();

		vehicle = null;
		settlement = null;
	}
}