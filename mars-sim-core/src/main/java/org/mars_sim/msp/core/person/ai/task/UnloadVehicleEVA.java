/*
 * Mars Simulation Project
 * UnloadVehicleEVA.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Towing;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The UnloadVehicleEVA class is a task for unloading fuel and supplies from a
 * vehicle when the vehicle is outside.
 */
public class UnloadVehicleEVA extends EVAOperation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(UnloadVehicleEVA.class.getName());
	
	private static int iceID = ResourceUtil.iceID;
	private static int regolithID = ResourceUtil.regolithID;
	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	private static int foodID = ResourceUtil.foodID;
	private static int methaneID = ResourceUtil.methaneID;
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.unloadVehicleEVA"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase UNLOADING = new TaskPhase(Msg.getString("Task.phase.unloading")); //$NON-NLS-1$

	/**
	 * The amount of resources (kg) one person of average strength can unload per
	 * millisol.
	 */
	private static double UNLOAD_RATE = 20D;

	// Data members
	/** The vehicle that needs to be unloaded. */
	private Vehicle vehicle;
	/** The settlement the person is unloading to. */
	private Settlement settlement;

	/**
	 * Constructor
	 * 
	 * @param person the person to perform the task.
	 */
	public UnloadVehicleEVA(Person person) {
		// Use EVAOperation constructor.
		super(NAME, person, true, 25, null);

		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        	return;
		}
		
		settlement = CollectionUtils.findSettlement(person.getCoordinates());
		if (settlement == null) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}
		
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

			setDescription(Msg.getString("Task.description.unloadVehicleEVA.detail", vehicle.getName())); // $NON-NLS-1$

			// Add the rover to a garage if possible.
			if (settlement.getBuildingManager().addToGarage(vehicle)) {
				// no need of doing EVA
	        	if (person.isOutside())
	        		setPhase(WALK_BACK_INSIDE);
	        	else
	        		endTask();
	        	return;
			}
			
			// Determine location for unloading.
			setOutsideLocation(vehicle);

			// Initialize task phase
			addPhase(UNLOADING);

			// NOTE: EVAOperation will set the phase. Do NOT do it here
//			setPhase(UNLOADING); 
			logger.log(person, Level.FINE, 20_000, "Going to unload "  + vehicle.getName() + ".");

		} else {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}
	}

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

		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
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

		// NOTE: EVAOperation will set the phase. Do NOT do it here
//		setPhase(UNLOADING); 
		
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
		if (isDone()){
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        }
		
        // Check for radiation exposure during the EVA operation.
        if (isRadiationDetected(time)){
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        }
		
        // Check if there is a reason to cut short and return.
        if (addTimeOnSite(time)){
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
        }
	
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
		}

		if (settlement == null || vehicle == null) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return 0;
		}

		if (settlement.getBuildingManager().isInGarage(vehicle)) {
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return 0;
		}
		
		// Determine unload rate.
		int strength = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);
		double strengthModifier = .1D + (strength * .018D);
		double amountUnloading = UNLOAD_RATE * strengthModifier * time / 4D;

		// Unload equipment.
		if (amountUnloading > 0D) {
			Set<Equipment> originalEqm = new HashSet<>(vehicle.getEquipmentSet());
			Iterator<Equipment> k = originalEqm.iterator();
			while (k.hasNext() && (amountUnloading > 0D)) {	
				Equipment equipment = k.next();
				boolean doUnload = true;
				
				if (vehicle instanceof Crewable && equipment.getEquipmentType() == EquipmentType.EVA_SUIT) {
					int numSuit = vehicle.findNumContainersOfType(EquipmentType.EVA_SUIT);
					int numCrew = ((Crewable)vehicle).getCrewNum();
					// Note: Ensure each crew member in the vehicle has an EVA suit to wear
					doUnload = (numSuit > numCrew);
				}
				
				// Unload the equipment
				if (doUnload) {
					// Unload inventories of equipment (if possible)
					UnloadVehicleGarage.unloadEquipmentInventory(equipment, settlement);
					equipment.transfer(settlement);		
					amountUnloading -= equipment.getMass();
					
					logger.log(worker, Level.INFO, 10_000, "Unloaded " + equipment.getNickName()
						+ " from " + vehicle.getName() + ".");
				}
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

			
			vehicle.retrieveAmountResource(id, amount);
			settlement.storeAmountResource(id, amount);
			
			if (id != waterID && id != methaneID 
					&& id != foodID && id != oxygenID) {
				double laborTime = 0;
				if (id == iceID || id == regolithID)
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

		// Retrieve, examine and bury any dead bodies
		if (this instanceof Crewable) {
			Crewable crewable = (Crewable) this;
			for (Person p : crewable.getCrew()) {
				if (p.isDeclaredDead()) {
						
					if (p.transfer(settlement)) {
					
						BuildingManager.addToMedicalBuilding(p, settlement.getIdentifier());			
						
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
			
        	if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
	        return 0;
		}
		
        // Add experience points
        addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);
        
		return 0D;
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
				// Find unreserved vehicle parked at their home base. Visiting vehicles
				// should not unloaded outside a mission
				if (vehicle instanceof Rover && !vehicle.isReserved() &&
						(vehicle.getAssociatedSettlementID() == settlement.getIdentifier())) {
					int peopleOnboard = ((Crewable)vehicle).getCrewNum();
					if (peopleOnboard == 0) {
						if (!settlement.getBuildingManager().isInGarage(vehicle)) {
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

					int robotsOnboard = ((Crewable)vehicle).getRobotCrewNum();
					if (robotsOnboard == 0) {
						if (!settlement.getBuildingManager().isInGarage(vehicle)) {
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
		return UnloadVehicleGarage.getAllMissionsNeedingUnloading(settlement, false);
	}

	/**
	 * Gets a random vehicle mission unloading at the settlement.
	 * 
	 * @return vehicle mission.
	 */
	private VehicleMission getMissionNeedingUnloading() {

		VehicleMission result = null;
		List<Mission> unloadingMissions = UnloadVehicleGarage.getAllMissionsNeedingUnloading(worker.getSettlement(), false);

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
