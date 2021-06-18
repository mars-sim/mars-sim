/**
 * Mars Simulation Project
 * UnloadVehicleEVA.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
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
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
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
			if (BuildingManager.add2Garage((GroundVehicle)vehicle)) {
				// no need of doing EVA
	        	if (person.isOutside())
	        		setPhase(WALK_BACK_INSIDE);
	        	else
	        		endTask();
	        	return;
			}
			
			// Determine location for unloading.
			Point2D unloadingLoc = determineUnloadingLocation();
			setOutsideSiteLocation(unloadingLoc.getX(), unloadingLoc.getY());

			// Initialize task phase
			addPhase(UNLOADING);
//			setPhase(UNLOADING); 
			// NOTE: EVAOperation will set the phase. Do NOT do it here
			
			logger.log(person, Level.FINER, 0, "Going to unload "  + vehicle.getName() + ".");

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
		super("Unloading vehicle EVA", person, true, RandomUtil.getRandomDouble(10D) + 10D, null);

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
		Point2D unloadingLoc = determineUnloadingLocation();
		setOutsideSiteLocation(unloadingLoc.getX(), unloadingLoc.getY());
		
		settlement = CollectionUtils.findSettlement(person.getCoordinates());
		if (settlement == null) {
			endTask();
		}
				
		// Initialize phase
		addPhase(UNLOADING);
//		setPhase(UNLOADING); 
		// NOTE: EVAOperation will set the phase. Do NOT do it here
		
		logger.log(person, Level.FINER, 0, "Going to unload " + vehicle.getName() + ".");
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
        if (shouldEndEVAOperation() || addTimeOnSite(time)){
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
		
		if (!vehicle.isInSettlementVicinity() || BuildingManager.isInAGarage(vehicle)) {
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

		Inventory vehicleInv = vehicle.getInventory();
		
		Inventory settlementInv = settlement.getInventory();
		
		// Unload equipment.
		if (amountUnloading > 0D) {
			Iterator<Unit> k = vehicleInv.findAllUnitsOfClass(Equipment.class).iterator();
			while (k.hasNext() && (amountUnloading > 0D)) {
				Equipment equipment = (Equipment) k.next();

				// Unload inventories of equipment (if possible)
				unloadEquipmentInventory(equipment);

				equipment.transfer(vehicleInv, settlementInv);
//				vehicleInv.retrieveUnit(equipment);
//				settlementInv.storeUnit(equipment);
				
				amountUnloading -= equipment.getMass();
				logger.log(worker, Level.INFO, 10_000, "Unloaded " + equipment.getNickName()
					+ " from " + vehicle.getName() + ".");
			}
		}

		double totalAmount = 0;
		// Unload amount resources.
		Iterator<Integer> i = vehicleInv.getAllARStored(false).iterator();
		while (i.hasNext() && (amountUnloading > 0D)) {
			Integer resource = i.next();
			double amount = vehicleInv.getAmountResourceStored(resource, false);
			if (amount > amountUnloading) {
				amount = amountUnloading;
			}
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
		            settlement.getGoodsManager().updateGoodValue(GoodsUtil.getResourceGood(resource), false);	
				}
				
			} catch (Exception e) {
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
				if (!settlementInv.containsUnit(towedVehicle)) {
					settlementInv.storeUnit(towedVehicle);
					towedVehicle.findNewParkingLoc();
				}
			}
		}

		// Retrieve, examine and bury any dead bodies
		if (this instanceof Crewable) {
			Crewable crewable = (Crewable) this;
			for (Person p : crewable.getCrew()) {
				if (p.isDeclaredDead()) {
					
					logger.info(worker, "Was retrieving the dead body of " + p + " from " + vehicle.getName());
					
					p.transfer(vehicle, settlementInv);
					
					BuildingManager.addToMedicalBuilding(p, settlement.getIdentifier());			
					
					p.setAssociatedSettlement(settlement.getIdentifier());
//					p.getMind().getTaskManager().clearTask();
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
		List<Vehicle> result = new CopyOnWriteArrayList<Vehicle>();

		if (settlement != null) {
			Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				boolean needsUnloading = false;
				if (vehicle instanceof Rover && !vehicle.isReserved()) {
					int peopleOnboard = vehicle.getInventory().getNumContainedPeople();
					if (peopleOnboard == 0) {
						if (!BuildingManager.isInAGarage(vehicle)) {
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
						if (!BuildingManager.isInAGarage((GroundVehicle)vehicle)) {
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

		List<Mission> result = new CopyOnWriteArrayList<Mission>();

		Iterator<Mission> i = missionManager.getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				if (VehicleMission.DISEMBARKING.equals(mission.getPhase())) {
					VehicleMission vehicleMission = (VehicleMission) mission;
					if (vehicleMission.hasVehicle()) {
						Vehicle vehicle = vehicleMission.getVehicle();
						if (settlement == vehicle.getSettlement()) {
							int peopleOnboard = vehicle.getInventory().getNumContainedPeople();
							if (peopleOnboard == 0) {
								if (!isFullyUnloaded(vehicle)) {
									if (!BuildingManager.isInAGarage(vehicle)) {
										result.add(vehicleMission);
									}
								}
							}

							int robotsOnboard = vehicle.getInventory().getNumContainedRobots();
							if (robotsOnboard == 0) {
								if (!isFullyUnloaded(vehicle)) {
									if (!BuildingManager.isInAGarage(vehicle)) {
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
		List<Mission> unloadingMissions = getAllMissionsNeedingUnloading(worker.getSettlement());

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

	/**
	 * Determine location to unload the vehicle.
	 * 
	 * @return location.
	 */
	private Point2D determineUnloadingLocation() {

		Point2D.Double newLocation = null;
		boolean goodLocation = false;
		for (int x = 0; (x < 50) && !goodLocation; x++) {
			Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(vehicle, 1D);
			newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), boundedLocalPoint.getY(),
					vehicle);
			goodLocation = LocalAreaUtil.isLocationCollisionFree(newLocation.getX(), newLocation.getY(),
					worker.getCoordinates());
		}

		return newLocation;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return UNLOADING;
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
			if (amount < capacity) {
				amount = capacity;
			}
			try {
				eInv.retrieveAmountResource(resource, amount);
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
