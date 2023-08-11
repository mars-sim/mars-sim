/*
 * Mars Simulation Project
 * UnloadVehicleEVA.java
 * @date 2022-08-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Towing;
import org.mars_sim.msp.core.vehicle.Vehicle;

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
		super(NAME, person, true, RandomUtil.getRandomDouble(25D) + 10D, null);

		setDescription(Msg.getString("Task.description.unloadVehicleEVA.detail", vehicle.getName())); // $NON-NLS-1$
		this.vehicle = vehicle;

		if (person.isSuperUnFit()) {
			checkLocation();
        	return;
		}
		
		// Determine location for unloading.
		setOutsideLocation(vehicle);
		
		settlement = CollectionUtils.findSettlement(person.getCoordinates());
		if (settlement == null) {
			endTask();
		}
				
		// Initialize phase
		addPhase(UNLOADING);

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
		
		if (checkReadiness(time, true) > 0)
			return time;
		
		if (settlement == null || vehicle == null) {
			checkLocation();
			return time;
		}

		if (settlement.getBuildingManager().isInGarage(vehicle)) {
			checkLocation();
			return time;
		}
		
		// Determine unload rate.
		int strength = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);
		double strengthModifier = .1D + (strength * .018D);
		double amountUnloading = UNLOAD_RATE * strengthModifier * time / 4D;

		// Unload equipment.
		if (amountUnloading > 0D) {
			Set<Equipment> surplus = new HashSet<>();
			for(Equipment equipment : vehicle.getEquipmentSet()) {
				boolean doUnload = true;
				
				if (vehicle instanceof Crewable crew && equipment.getEquipmentType() == EquipmentType.EVA_SUIT) {
					int numSuit = vehicle.findNumContainersOfType(EquipmentType.EVA_SUIT);
					int numCrew = crew.getCrewNum();
					// Note: Ensure each crew member in the vehicle has an EVA suit to wear
					doUnload = (numSuit > numCrew);
				}

				// Add the equipemnt to the surplus
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
				
				logger.log(vehicle, Level.INFO, 10_000, "Surplus unloaded " + extra.getNickName());
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
		if (vehicle instanceof Towing) {
			Towing towingVehicle = (Towing) vehicle;
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
			if (totalAmount > 0) {
				logger.log(worker, Level.INFO, 10_000, 
						"Just unloaded a total of " + Math.round(totalAmount*100.0)/100.0 
						+ " kg of resources from " + vehicle.getName() + ".");
			}
			
			checkLocation();
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



	@Override
	protected TaskPhase getOutsideSitePhase() {
		return UNLOADING;
	}

	/**
	 * Returns true if the vehicle is fully unloaded.
	 * This has to ignore any EVA suits.
	 * 
	 * @param vehicle Vehicle to check.
	 * @return is vehicle fully unloaded?
	 */
	public static boolean isFullyUnloaded(Vehicle vehicle) {
		double total = vehicle.getStoredMass();
		for(Equipment e : vehicle.getEquipmentSet()) {
			if (e.getEquipmentType() == EquipmentType.EVA_SUIT) {
				total -= e.getMass();
			}
		}
		
		return total <= 0.001D;
	}
}
