/*
 * Mars Simulation Project
 * UnloadVehicleGarage.java
 * @date 2021-08-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Drone;
import org.mars_sim.msp.core.vehicle.Rover;
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
	private static SimLogger logger = SimLogger.getLogger(UnloadVehicleGarage.class.getName());

	private static int iceID = ResourceUtil.iceID;
	private static int regolithID = ResourceUtil.regolithID;
	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	private static int foodID = ResourceUtil.foodID;
	private static int methaneID = ResourceUtil.methaneID;
	
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
		super(NAME, person, true, false, STRESS_MODIFIER, DURATION);

		if (person.isOutside()) {
			endTask();
		}
		
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
			// Add the rover to a garage if possible.
			if (!settlement.getBuildingManager().addToGarage(vehicle)) {
				// Need to do EVA to unload
				endTask();
			}
			
			setDescription(Msg.getString("Task.description.unloadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$

			// If vehicle is in a garage, add person to garage.
			Building garageBuilding = BuildingManager.getBuilding(vehicle);
			if (garageBuilding != null) {
				// Walk to garage building.
				walkToTaskSpecificActivitySpotInBuilding(garageBuilding, FunctionType.GROUND_VEHICLE_MAINTENANCE, false);
			}

			// End task if vehicle or garage not available.
			if ((vehicle == null) || (garageBuilding == null)) {
				endTask();
			}

			// Initialize task phase
			addPhase(UNLOADING);
			setPhase(UNLOADING);

			logger.log(worker, Level.FINER, 0, "Going to unload " + vehicle.getName() + ".");
		} else
			endTask();
	}

	public UnloadVehicleGarage(Robot robot) {
		// Use Task constructor.
		super(NAME, robot, true, false, STRESS_MODIFIER, DURATION);

		if (robot.isOutside()) {
			endTask();
		}
		
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

		// Add the rover to a garage if possible
		if (vehicle != null && settlement.getBuildingManager().addToGarage(vehicle)) {
			// Walk to garage.
			Building garage = BuildingManager.getBuilding(vehicle);
			walkToTaskSpecificActivitySpotInBuilding(garage, FunctionType.GROUND_VEHICLE_MAINTENANCE, false);
		
			setDescription(Msg.getString("Task.description.unloadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$
			
			// Initialize task phase
			addPhase(UNLOADING);
			setPhase(UNLOADING);

			logger.log(robot, Level.FINE, 0, "Going to unload " + vehicle.getName() + ".");
		}
		else {
			endTask();
		}	
	}

	/**
	 * Constructor
	 * 
	 * @param person  the person to perform the task
	 * @param vehicle the vehicle to be unloaded
	 */
	public UnloadVehicleGarage(Person person, Vehicle vehicle) {
		// Use Task constructor.
		super("Unloading vehicle", person, true, false, STRESS_MODIFIER, DURATION);

		if (person.isOutside()) {
			endTask();
		}
		
		setDescription(Msg.getString("Task.description.unloadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$;
		this.vehicle = vehicle;

		settlement = person.getSettlement();

		// If vehicle is in a garage, add person to garage.
		Building garageBuilding = BuildingManager.getBuilding(vehicle);
		if (garageBuilding != null) {
			// Walk to garage building.
			walkToTaskSpecificActivitySpotInBuilding(garageBuilding, FunctionType.GROUND_VEHICLE_MAINTENANCE, false);
		}

		// Initialize phase
		addPhase(UNLOADING);
		setPhase(UNLOADING); 
	
		logger.log(person, Level.FINE, 0, "Going to unload " + vehicle.getName() + ".");
	}

	public UnloadVehicleGarage(Robot robot, Vehicle vehicle) {
		// Use Task constructor.
		super("Unloading vehicle", robot, true, false, STRESS_MODIFIER, DURATION);

		if (robot.isOutside()) {
			endTask();
		}
		
		setDescription(Msg.getString("Task.description.unloadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$;
		this.vehicle = vehicle;

		settlement = robot.getSettlement();

		// If vehicle is in a garage, add robot to garage.
		Building garageBuilding = BuildingManager.getBuilding(vehicle);
		if (garageBuilding != null) {
			// Walk to garage building.
			walkToTaskSpecificActivitySpotInBuilding(garageBuilding, FunctionType.GROUND_VEHICLE_MAINTENANCE, false);
		}

		// Initialize phase
		addPhase(UNLOADING);
		setPhase(UNLOADING); 
	
		logger.log(robot, Level.FINER, 0, "Going to unload " + vehicle.getName());
	}

	/**
	 * Gets a list of vehicles that need unloading and aren't reserved for a
	 * mission.
	 * 
	 * @param settlement the settlement the vehicle is at.
	 * @return list of vehicles.
	 */
	public static List<Vehicle> getNonMissionVehiclesNeedingUnloading(Settlement settlement) {
		List<Vehicle> result = new CopyOnWriteArrayList<Vehicle>();

		if (settlement != null) {
			Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				boolean needsUnloading = false;
				if (vehicle instanceof Rover && vehicle instanceof Drone && !vehicle.isReserved()
						&& (vehicle.getAssociatedSettlementID() == settlement.getIdentifier())) {
					int peopleOnboard = vehicle.getInventory().getNumContainedPeople();
					if (peopleOnboard == 0) {
						if (settlement.getBuildingManager().addToGarage(vehicle)) {
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

					int robotsOnboard = vehicle.getInventory().getNumContainedRobots();
					if (robotsOnboard == 0) {
						if (settlement.getBuildingManager().addToGarage(vehicle)) {
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
	public static List<Mission> getAllMissionsNeedingUnloading(Settlement settlement, boolean addToGarage) {

		List<Mission> result = new ArrayList<>();

		for(Mission mission : missionManager.getMissions()) {
			if (mission instanceof VehicleMission) {
				VehicleMission vehicleMission = (VehicleMission) mission;

				if (vehicleMission.isVehicleUnloadableHere(settlement)) {
					if (vehicleMission.hasVehicle()) {
						Vehicle vehicle = vehicleMission.getVehicle();					
						// If no one is in the Vehicle its a candidate
						if ((vehicle.getInventory().getNumContainedPeople() == 0)
								|| (vehicle.getInventory().getNumContainedRobots() == 0)) {
							
							// If looking for garaged vehicles; then add to garage otherwise
							// check vehicle is not in garage
							if (!isFullyUnloaded(vehicle)) {
								if ((addToGarage && settlement.getBuildingManager().addToGarage(vehicle))
										|| (!addToGarage && !settlement.getBuildingManager().isInGarage(vehicle))) {
									result.add(vehicleMission);
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
		List<Mission> unloadingMissions = getAllMissionsNeedingUnloading(worker.getSettlement(), true);

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
		int strength = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);

		double strengthModifier = .1D + (strength * .018D);
		double amountUnloading = UNLOAD_RATE * strengthModifier * time;

		Inventory vehicleInv = vehicle.getInventory();
		if (settlement == null) {
			endTask();
			return 0D;
		}

		if (!settlement.getBuildingManager().addToGarage(vehicle)) {
			logger.warning(vehicle, "Not in a garage");
        	endTask();
			return 0;
		}
				
		Inventory settlementInv = settlement.getInventory();

//		if (person != null)
//			LogConsolidated.log(Level.INFO, 0, sourceName,
//					"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " in "
//							+ person.getLocationTag().getImmediateLocation() + " proceeded to unload "
//							+ vehicle.getName() + ".",
//					null);
//		else
//			LogConsolidated.log(Level.INFO, 0, sourceName,
//					"[" + robot.getLocationTag().getLocale() + "] " + robot.getName() + " in "
//							+ robot.getLocationTag().getImmediateLocation() + " proceeded to unload "
//							+ vehicle.getName() + ".",
//					null);

		// Unload equipment.
		if (amountUnloading > 0D) {
			Iterator<Equipment> k = vehicleInv.findAllEquipment().iterator();
			while (k.hasNext() && (amountUnloading > 0D)) {
				Equipment equipment = k.next();
				// Unload inventories of equipment (if possible)
				unloadEquipmentInventory(equipment);
				equipment.transfer(vehicleInv, settlementInv);
				amountUnloading -= equipment.getMass();

				if (!vehicle.getName().contains("Mock")) {
					// Note: In maven test, the name of the vehicle is "Mock Vehicle" 
					// test if it's NOT under maven test
					logger.log(worker, Level.INFO, 10_000, "Unloaded "
										+ equipment.getNickName() + " from " + vehicle.getName() + ".");
				}
			}
		}

		double totalAmount = 0;
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
				
				if (resource != waterID && resource != methaneID 
						&& resource != foodID && resource != oxygenID) {
					double laborTime = 0;
					if (resource == iceID || resource == regolithID)
						laborTime = CollectResources.LABOR_TIME;
					else
						laborTime = CollectMinedMinerals.LABOR_TIME;
					
					settlementInv.addAmountSupply(resource, amount);
					// Add to the daily output
					settlement.addOutput(resource, amount, laborTime);
		            // Recalculate settlement good value for output item.
//		            settlement.getGoodsManager().updateGoodValue(GoodsUtil.getResourceGood(resource), false);	
				}
						
			} catch (Exception e) {
				logger.log(worker, Level.WARNING, 3_000,  "Could NOT unload the resources." + e);
			}
			amountUnloading -= amount;

			if (totalAmount > 0 && !vehicle.getName().contains("Mock")) {
				// Note: In maven test, the name of the vehicle is "Mock Vehicle" 
				// test if it's NOT under maven test

				logger.log(worker, Level.INFO, 10_000, "Just unloaded "
						+ Math.round(amount * 100.0) / 100.0 + " kg of resources from " 
						+ vehicle.getName() + ".");
			}
					
			totalAmount += amount;
		}

		int totalItems = 0;
		// Unload item resources.
		if (amountUnloading > 0D) {
			Iterator<Integer> j = vehicleInv.getAllItemResourcesStored().iterator();
			while (j.hasNext() && (amountUnloading > 0D)) {
				Integer resource = j.next();
				Part part= (Part)(ItemResourceUtil.findItemResource(resource));
				double mass = part.getMassPerItem();
				int num = vehicleInv.getItemResourceNum(resource);
				if ((num * mass) > amountUnloading) {
					num = (int) Math.round(amountUnloading / mass);
					if (num == 0) {
						num = 1;
					}
				}
				vehicleInv.retrieveItemResources(resource, num);
				settlementInv.storeItemResources(resource, num);
				amountUnloading -= (num * mass);
			}

			if (totalItems > 0) {
				logger.log(worker, Level.INFO, 10_000,"Just unloaded a total of "
									+ totalItems + " items from " + vehicle.getName() + ".");
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
					towedVehicle.findNewParkingLoc();
				}
			}
		}

		// Retrieve, exam and bury any dead bodies
		if (this instanceof Crewable) {
			Crewable crewable = (Crewable) this;
			for (Person p : crewable.getCrew()) {
				if (p.isDeclaredDead()) {
					
					logger.log(worker, Level.INFO, 0,"was retrieving the dead body of " + p + " from " + vehicle.getName() + ".");

					
					// Retrieve the dead person and place this person within a settlement	
					p.transfer(vehicle, settlementInv);
					
					BuildingManager.addToMedicalBuilding(p, settlement.getIdentifier());

					p.setAssociatedSettlement(settlement.getIdentifier());
					
				}
			}
		}

		if (isFullyUnloaded(vehicle)) {
			if (totalAmount > 0 && !vehicle.getName().contains("Mock")) {
				// Note: In maven test, the name of the vehicle is "Mock Vehicle" 
				// test if it's NOT under maven test

				logger.log(worker, Level.INFO, 10_000, "Unloaded a total of "
					+ Math.round(totalAmount * 100.0) / 100.0 + " kg of resources from " 
						+ vehicle.getName() + ".");
			}
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
		Inventory sInv = settlement.getInventory();

		// Note: only unloading amount resources at the moment.
		int resource = equipment.getResource();
		if (resource != -1) {
			double amount = equipment.getAmountResourceStored(resource);
			double capacity = sInv.getAmountResourceRemainingCapacity(resource, true, false);
			if (amount > capacity) {
				amount = capacity;
			}
			try {
				equipment.retrieveAmountResource(resource, amount);
				sInv.storeAmountResource(resource, amount, true);
			} catch (Exception e) {
			}
		}
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
}
