/*
 * Mars Simulation Project
 * UnloadVehicleEVA.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.vehicle.task;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.CollectMinedMinerals;
import com.mars_sim.core.person.ai.task.CollectResources;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.UnloadVehicleGarage;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Towing;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The UnloadVehicleEVA class is a task for unloading fuel and supplies from a
 * vehicle when the vehicle is outside.
 */
public class UnloadVehicleEVA extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(UnloadVehicleEVA.class.getName());
	
	private static final int ICE_ID = ResourceUtil.iceID;
	private static final int REGOLITH_ID = ResourceUtil.regolithID;
	private static final int OXYGEN_ID = ResourceUtil.oxygenID;
	private static final int WATER_ID = ResourceUtil.waterID;
	private static final int FOOD_ID = ResourceUtil.foodID;
	private static final int METHANOL_ID = ResourceUtil.methanolID;
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.unloadVehicleEVA"); //$NON-NLS-1$
	
	/** Task phases. */
	private static final TaskPhase UNLOADING = new TaskPhase(Msg.getString("Task.phase.unloading")); //$NON-NLS-1$

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

		if (person.isSuperUnFit()) {
			checkLocation("Super unfit.");
        	return;
		}
		
		// Determine location for unloading.
		setOutsideLocation(vehicle);
		
		if (unitManager == null)
			unitManager = Simulation.instance().getUnitManager();
		
		settlement = unitManager.findSettlement(person.getCoordinates());
		if (settlement == null) {
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
		
		if (settlement == null) {
			checkLocation("Settlement is null.");
			return time;
		}

		if (vehicle == null) {
			checkLocation("Vehicle is null.");
			return time;
		}
		
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

		// Unload equipment.
		if (amountUnloading > 0D) {
			Set<Equipment> surplus = new UnitSet<>();
			for(Equipment equipment : vehicle.getSuitSet()) {
				boolean doUnload = true;
				
				if (vehicle instanceof Crewable crew) {
					int numSuit = vehicle.findNumEVASuits();
					int numCrew = crew.getCrewNum();
					// Note: Ensure each crew member in the vehicle has an EVA suit to wear
					doUnload = (numSuit > numCrew);
				}

				// Add the equipment to the surplus
				if (doUnload) {
					surplus.add(equipment);
					amountUnloading -= equipment.getMass();
					if (amountUnloading < 0) {
						break;
					}
				}
			}

			// Unload the surplus
			for(Equipment extra : surplus) {
				// Unload inventories of equipment (if possible)
				UnloadVehicleGarage.unloadEquipmentInventory(extra, settlement);
				extra.transfer(settlement);		
				
				logger.info(vehicle, 10_000, "Surplus unloaded " + extra.getName());
			}
		}

		double totalAmount = 0;
		// Unload amount resources.
		Iterator<Integer> i = vehicle.getAmountResourceIDs().iterator();
		while (i.hasNext() && (amountUnloading > 0D)) {
			int id = i.next();
			double amount = vehicle.getAmountResourceStored(id);
			if (amount > amountUnloading) {
				amount = amountUnloading;
			}
			double capacity = settlement.getAmountResourceRemainingCapacity(id);
			if (capacity < amount) {
				amount = capacity;
				amountUnloading = 0D;
			}
			
			// Transfer the amount resource from vehicle to settlement
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
			}

			amountUnloading -= amount;
			
			if (totalAmount > 0) {
					logger.log(worker, Level.INFO, 10_000, "Just unloaded " 
							+ Math.round(amount*100.0)/100.0 + " kg of resources from " + vehicle.getName() + ".");
			}
			
			totalAmount += amount;
		}
		
		int totalItems = 0;
		// Unload item resources.
		if (amountUnloading > 0D) {
			Iterator<Integer> j = vehicle.getItemResourceIDs().iterator();
			while (j.hasNext() && (amountUnloading > 0D)) {
				int id = j.next();
				Part part = ItemResourceUtil.findItemResource(id);
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
				
				totalItems += num;
			}
			
			if (totalItems > 0) {
				logger.log(worker, Level.INFO, 10_000, "Just unloaded a total of "
						+ totalItems + " items from " + vehicle.getName() + ".");
			}
			
			person.getPhysicalCondition().stressMuscle(time/2);
		}

		// Unload towed vehicles.
		if (vehicle instanceof Towing towingVehicle) {
			Vehicle towedVehicle = towingVehicle.getTowedVehicle();
			if (towedVehicle != null) {
				towingVehicle.setTowedVehicle(null);
				towedVehicle.setTowingVehicle(null);
				if (!settlement.containsParkedVehicle(towedVehicle)) {
					settlement.addParkedVehicle(towedVehicle);
					towedVehicle.findNewParkingLoc();
					
					person.getPhysicalCondition().stressMuscle(time/2);
				}
			}
		}

		// Retrieve, examine and bury any dead bodies
		if (this instanceof Crewable crewable) {
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
			if (totalAmount > 0) {
				logger.log(worker, Level.INFO, 10_000, 
						"Just unloaded a total of " + Math.round(totalAmount*100.0)/100.0 
						+ " kg of resources from " + vehicle.getName() + ".");
			}
			
			checkLocation("Vehicle already fully unloaded.");
	        return remainingTime;
		}
		
        // Add experience points
        addExperience(time);

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
