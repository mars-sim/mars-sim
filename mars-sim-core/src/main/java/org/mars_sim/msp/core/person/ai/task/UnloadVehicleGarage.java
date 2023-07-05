/*
 * Mars Simulation Project
 * UnloadVehicleGarage.java
 * @date 2022-09-24
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.ResourceHolder;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Towing;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The UnloadVehicleGarage class is a task for unloading fuel and supplies from
 * a vehicle in a vehicle maintenance garage.
 */
public class UnloadVehicleGarage extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(UnloadVehicleGarage.class.getName());

	private static final int ICE_ID = ResourceUtil.iceID;
	private static final int REGOLITH_ID = ResourceUtil.regolithID;
	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int WATER_ID = ResourceUtil.waterID;
	private static final int FOOD_ID = ResourceUtil.foodID;
	private static final int METHANOL_ID = ResourceUtil.methanolID;

	/** Task name */
//	private static final String NAME = Msg.getString("Task.description.unloadVehicleGarage"); //$NON-NLS-1$
	
	/** Task phases. */
	private static final TaskPhase UNLOADING = new TaskPhase(Msg.getString("Task.phase.unloading")); //$NON-NLS-1$

	/**
	 * The amount of resources (kg) one person of average strength can unload per
	 * millisol.
	 */
	private static final double UNLOAD_RATE = 20D;

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	// Data members
	/** The vehicle that needs to be unloaded. */
	private Vehicle vehicle;
	/** The settlement the person is unloading to. */
	private Settlement settlement;

	/**
	 * Constructor
	 *
	 * @param robot the robot to perform the task
	 * @param vehicle the vehicle to be unloaded
	 */
	public UnloadVehicleGarage(Worker worker, Vehicle vehicle) {
		// Use Task constructor.
		super("Unloading vehicle", worker, true, false, STRESS_MODIFIER,
					RandomUtil.getRandomDouble(40D) + 10D);

		if (worker.isOutside()) {
			endTask();
		}

		this.vehicle = vehicle;

		settlement = worker.getSettlement();

		if (isFullyUnloaded(vehicle)) {
			clearTask(vehicle.getName() + " already unloaded");
			return;
		}
		logger.log(worker, Level.FINER, 0, "Going to unload " + vehicle.getName());

		// Add the vehicle to a garage if possible
		Building garage = settlement.getBuildingManager().addToGarageBuilding(vehicle);

		// End task if vehicle or garage not available
		if (garage == null) {
			clearTask(vehicle.getName() + " no garage found");
			return;
		}

		// Walk to garage
		walkToTaskSpecificActivitySpotInBuilding(garage, FunctionType.VEHICLE_MAINTENANCE, false);
		// Set the description
		setDescription(Msg.getString("Task.description.unloadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$

		// Initialize phase
		addPhase(UNLOADING);
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

		// Unload equipment.
		if (amountUnloading > 0D) {
			// Take own copy as the equipment list changes as we remove items. ??
			List<Equipment> held = new ArrayList<>(vehicle.getEquipmentSet());
			Iterator<Equipment> k = held.iterator();
			while (k.hasNext() && (amountUnloading > 0D)) {
				Equipment equipment = k.next();
				// Unload inventories of equipment (if possible)
				unloadEquipmentInventory(equipment, settlement);
				equipment.transfer(settlement);
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

				if (id != WATER_ID && id != METHANOL_ID
						&& id != FOOD_ID && id != OXYGEN_ID) {
					double laborTime = 0;
					if (id == ICE_ID || id == REGOLITH_ID)
						laborTime = CollectResources.LABOR_TIME;
					else
						laborTime = CollectMinedMinerals.LABOR_TIME;

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

					if (p.transfer(settlement)) {

						BuildingManager.addToMedicalBuilding(p, settlement);

						p.setAssociatedSettlement(settlement.getIdentifier());

						logger.info(worker, "successfully retrieved the dead body of " + p + " from " + vehicle.getName());
					}
					else {
						logger.warning(worker, "failed to retrieve the dead body of " + p + " from " + vehicle.getName());
					}
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
