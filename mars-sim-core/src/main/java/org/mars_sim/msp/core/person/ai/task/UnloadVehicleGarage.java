/*
 * Mars Simulation Project
 * UnloadVehicleGarage.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.data.ResourceHolder;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
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

		if (settlement != null) {
			init(robot);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param person the robot to perform the task.
	 */
	public UnloadVehicleGarage(Robot robot) {
		// Use Task constructor.
		super(NAME, robot, true, false, STRESS_MODIFIER, DURATION);

		if (robot.isOutside()) {
			endTask();
		}
		
		settlement = robot.getSettlement();

		if (settlement != null) {
			init(robot);
		}
	}

	/**
	 * Initialise the task
	 * @param starter
	 */
	private void init(Worker starter) {
		
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

			initLoad(starter);

			logger.log(starter, Level.FINE, 0, "Going to unload " + vehicle.getName() + ".");
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
	
		this.vehicle = vehicle;

		settlement = person.getSettlement();

		if (vehicle != null && settlement != null) {
			initLoad(person);
		}

		logger.log(person, Level.FINE, 0, "Going to unload " + vehicle.getName() + ".");
	}

	/**
	 * Constructor
	 * 
	 * @param robot the robot to perform the task
	 * @param vehicle the vehicle to be unloaded
	 */
	public UnloadVehicleGarage(Robot robot, Vehicle vehicle) {
		// Use Task constructor.
		super("Unloading vehicle", robot, true, false, STRESS_MODIFIER, DURATION);

		if (robot.isOutside()) {
			endTask();
		}
		
		this.vehicle = vehicle;

		settlement = robot.getSettlement();

		if (vehicle != null && settlement != null) {
			initLoad(robot);
		}
	
		logger.log(robot, Level.FINER, 0, "Going to unload " + vehicle.getName());
	}

	/**
	 * Initialise the load task
	 * @param starter
	 */
	private void initLoad(Worker starter) {
//		Building garageBuilding = BuildingManager.getBuilding(vehicle);
//		if (garageBuilding != null) {
//		// If vehicle is in a garage, add walk there to the garage.
//			walkToTaskSpecificActivitySpotInBuilding(garageBuilding, FunctionType.GROUND_VEHICLE_MAINTENANCE, false);
//		}

		// Add the vehicle to a garage if possible
		Building garage = settlement.getBuildingManager().addToGarageBuilding(vehicle);

//		System.out.println("garage is " + garage);
		
		// End task if vehicle or garage not available
		if (garage == null) {
			endTask();
			return;
		}
		
		// Walk to garage
		walkToTaskSpecificActivitySpotInBuilding(garage, FunctionType.GROUND_VEHICLE_MAINTENANCE, false);
		// Set the description
		setDescription(Msg.getString("Task.description.unloadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$
		
		// Initialize phase
		addPhase(UNLOADING);
		setPhase(UNLOADING); 
	}
	
	/**
	 * Gets a list of vehicles that need unloading and aren't reserved for a
	 * mission.
	 * 
	 * @param settlement the settlement the vehicle is at.
	 * @return list of vehicles.
	 */
	public static List<Vehicle> getNonMissionVehiclesNeedingUnloading(Settlement settlement) {
		List<Vehicle> result = new ArrayList<>();

		if (settlement != null) {
			Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				boolean needsUnloading = false;
				if (vehicle instanceof Rover && !vehicle.isReserved()
						&& (vehicle.getAssociatedSettlementID() == settlement.getIdentifier())) {
					int peopleOnboard = ((Crewable)vehicle).getCrewNum();
					if (peopleOnboard == 0) {
						if (vehicle.getStoredMass() > 0D) {
							needsUnloading = true;
						}
						if (vehicle instanceof Towing) {
							if (((Towing) vehicle).getTowedVehicle() != null) {
								needsUnloading = true;
							}
						}
					}

					int robotsOnboard = ((Crewable)vehicle).getRobotCrewNum();
					if (robotsOnboard == 0) {
						if (vehicle.getStoredMass() > 0D) {
							needsUnloading = true;
						}
						if (vehicle instanceof Towing) {
							if (((Towing) vehicle).getTowedVehicle() != null) {
								needsUnloading = true;
							}
						}
					}

				}
				if (needsUnloading && settlement.getBuildingManager().addToGarage(vehicle)) {
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
						if (vehicle instanceof Crewable) {
							Crewable c = (Crewable)vehicle;
							if (c.getCrewNum() > 0 || c.getRobotCrewNum() > 0) {
								// Has occupants so skip it
								continue;
							}
						}
						
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

		if (settlement == null) {
			endTask();
			return 0D;
		}

		if (!settlement.getBuildingManager().addToGarage(vehicle)) {
			logger.warning(vehicle, "Not in a garage");
        	endTask();
			return 0;
		}
		
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
			// Take own copy as the equipment list changes as we remove items. ??
			List<Equipment> held = new ArrayList<>(vehicle.getEquipmentList());
			Iterator<Equipment> k = held.iterator();
			while (k.hasNext() && (amountUnloading > 0D)) {
				Equipment equipment = k.next();
				// Unload inventories of equipment (if possible)
				unloadEquipmentInventory(equipment, settlement);
				equipment.transfer(vehicle, settlement);
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
		Iterator<Integer> i = vehicle.getAmountResourceIDs().iterator();
		while (i.hasNext() && (amountUnloading > 0D)) {
			int id = i.next();;
			double amount = vehicle.getAmountResourceStored(id);
			if (amount > amountUnloading)
				amount = amountUnloading;
			double capacity = settlement.getAmountResourceRemainingCapacity(id);
			if (capacity < amount) {
				amount = capacity;
				amountUnloading = 0D;
			}
			try {
				vehicle.retrieveAmountResource(id, amount);
				settlement.storeAmountResource(id, amount);
				
				if (id != waterID && id != methaneID 
						&& id != foodID && id != oxygenID) {
					double laborTime = 0;
					if (id == iceID || id == regolithID)
						laborTime = CollectResources.LABOR_TIME;
					else
						laborTime = CollectMinedMinerals.LABOR_TIME;
					
//					settlementInv.addAmountSupply(id, amount);
					// Add to the daily output
					settlement.addOutput(id, amount, laborTime);
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
			Iterator<Integer> j = vehicle.getItemResourceIDs().iterator();
			while (j.hasNext() && (amountUnloading > 0D)) {
				int id = j.next();
				Part part = (Part)(ItemResourceUtil.findItemResource(id));
				double mass = part.getMassPerItem();
				int num = vehicle.getItemResourceStored(id);
				if ((num * mass) > amountUnloading) {
					num = (int) Math.round(amountUnloading / mass);
					if (num == 0) {
						num = 1;
					}
				}
				vehicle.retrieveItemResource(id, num);
				settlement.storeItemResource(id, num);
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
				if (!settlement.containsParkedVehicle(towedVehicle)) {
					settlement.addParkedVehicle(towedVehicle);
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
					p.transfer(p, settlement);
					
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
	public static void unloadEquipmentInventory(Equipment equipment, Settlement settlement) {
	
		// Note: only unloading amount resources at the moment.
		if (equipment instanceof ResourceHolder) {
			ResourceHolder rh = (ResourceHolder) equipment;
			for(int resource : rh.getAmountResourceIDs()) {
				double amount = rh.getAmountResourceStored(resource);
				double capacity = settlement.getAmountResourceRemainingCapacity(resource);
				if (amount > capacity) {
					amount = capacity;
				}
				try {
					rh.retrieveAmountResource(resource, amount);
					settlement.storeAmountResource(resource, amount);
				} catch (Exception e) {
				}
			}
		}
	}


	/**
	 * Returns true if the vehicle is fully unloaded.
	 * 
	 * @param vehicle Vehicle to check.
	 * @return is vehicle fully unloaded?
	 */
	static private boolean isFullyUnloaded(Vehicle vehicle) {
		return (vehicle.getStoredMass() == 0D);
	}
}
