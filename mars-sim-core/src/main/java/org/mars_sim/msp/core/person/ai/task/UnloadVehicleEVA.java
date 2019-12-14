/**
 * Mars Simulation Project
 * UnloadVehicleEVA.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.RoboticAttributeManager;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
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
	private static Logger logger = Logger.getLogger(UnloadVehicleEVA.class.getName());
	
	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
	
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
		super(NAME, person, true, 25);

		settlement = CollectionUtils.findSettlement(person.getCoordinates());
		if (settlement == null) {
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

			// Determine location for unloading.
			Point2D unloadingLoc = determineUnloadingLocation();
			setOutsideSiteLocation(unloadingLoc.getX(), unloadingLoc.getY());

			setDescription(Msg.getString("Task.description.unloadVehicleEVA.detail", vehicle.getName())); // $NON-NLS-1$

			// Initialize task phase
			addPhase(UNLOADING);
//			setPhase(UNLOADING); 
			// NOTE: EVAOperation will set the phase. Do NOT do it here
			
			LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
					+ " in " + person.getLocationTag().getImmediateLocation() + " was going to unload " + vehicle.getName() + ".", null);
		} else {
			endTask();
		}
	}

//	public UnloadVehicleEVA(Robot robot) {
//		// Use EVAOperation constructor.
//		super(NAME, robot, true, RandomUtil.getRandomDouble(50D) + 10D);
//
//	}

	/**
	 * Constructor
	 * 
	 * @param person  the person to perform the task
	 * @param vehicle the vehicle to be unloaded
	 */
	public UnloadVehicleEVA(Person person, Vehicle vehicle) {
		// Use EVAOperation constructor.
		super("Unloading vehicle EVA", person, true, RandomUtil.getRandomDouble(10D) + 10D);

		setDescription(Msg.getString("Task.description.unloadVehicleEVA.detail", vehicle.getName())); // $NON-NLS-1$
		this.vehicle = vehicle;

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
		
		LogConsolidated.log(logger, Level.FINER, 0, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
				+ " in " + person.getLocationTag().getImmediateLocation() + " was going to unload " + vehicle.getName() + ".", null);
		
//		logger.fine(person.getName() + " is unloading " + vehicle.getName());
	}

//	public UnloadVehicleEVA(Robot robot, Vehicle vehicle) {
//		// Use EVAOperation constructor.
//		super("Unloading vehicle EVA", robot, true, RandomUtil.getRandomDouble(50D) + 10D);
//
//	}

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
					int peopleOnboard = vehicle.getInventory().getNumContainedPeople();
					if (peopleOnboard == 0) {
						if (BuildingManager.getBuilding(vehicle) == null) {
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
						if (BuildingManager.getBuilding(vehicle) == null) {
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
									if (BuildingManager.getBuilding(vehicle) == null) {
										result.add(vehicleMission);
									}
								}
							}

							int robotsOnboard = vehicle.getInventory().getNumContainedRobots();
							if (robotsOnboard == 0) {
								if (!isFullyUnloaded(vehicle)) {
									if (BuildingManager.getBuilding(vehicle) == null) {
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
			if (person != null)
				goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(),
						person.getCoordinates());
			else if (robot != null)
				goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(),
						robot.getCoordinates());
		}

		return newLocation;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return UNLOADING;
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);

		if (getPhase() == null) {
//			logger.finer(person + " had no task phase. Ending the task of unloading vehicle with EVA.");
//			endTask();
//			return time;
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

		// Check for an accident during the EVA operation.
		checkForAccident(time);
		// Check for radiation exposure during the EVA operation.
		if (person.isOutside() && isRadiationDetected(time)) {
			setPhase(WALK_BACK_INSIDE);
			return time;
		}

		// Check if person should end EVA operation.
		if (person.isOutside() && (shouldEndEVAOperation() || addTimeOnSite(time))) {
			setPhase(WALK_BACK_INSIDE);
			return time;
		}

	
		// Determine unload rate.
		int strength = 0;
		if (person != null)
			strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);
		else if (robot != null)
			strength = robot.getRoboticAttributeManager().getAttribute(RoboticAttributeType.STRENGTH);
		double strengthModifier = .1D + (strength * .018D);
		double amountUnloading = UNLOAD_RATE * strengthModifier * time / 4D;

		Inventory vehicleInv = vehicle.getInventory();
		
		if (settlement == null) {
//			endTask();
			return time;
		}
		
		Inventory settlementInv = settlement.getInventory();
		
//		if (person != null)
//			LogConsolidated.log(logger, Level.INFO, 0, sourceName, 
//				"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
//				+ " in " + person.getLocationTag().getImmediateLocation() + " proceeded to unload " + vehicle.getName() + ".", null);
//		else 
//			LogConsolidated.log(logger, Level.INFO, 0, sourceName, 
//					"[" + robot.getLocationTag().getLocale() + "] " + robot.getName() 
//					+ " in " + robot.getLocationTag().getImmediateLocation() + " proceeded to unload " + vehicle.getName() + ".", null);
		
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
				
				if (person != null)
					LogConsolidated.log(logger, Level.INFO, 3_000, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
//						+ person.getLocationTag().getImmediateLocation().toLowerCase() 
						+ " unloaded " + equipment.getNickName() + " from " + vehicle.getName() + ".", null);
				else
					LogConsolidated.log(logger, Level.INFO, 3_000, sourceName, 
						"[" + robot.getLocationTag().getLocale() + "] " + robot.getName() 
//						+ robot.getLocationTag().getImmediateLocation().toLowerCase() 
						+ " unloaded " + equipment.getNickName() + " from " + vehicle.getName() + ".", null);
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
			
			totalAmount += amount;
		}
		
		if (totalAmount > 0) {
			if (person != null)
				LogConsolidated.log(logger, Level.INFO, 3_000, sourceName, 
				"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
//				+ person.getLocationTag().getImmediateLocation() 
				+ " just unloaded a total of " + Math.round(totalAmount*100.0)/100.0 + " kg of resources from " + vehicle.getName() + ".", null);
			else
				LogConsolidated.log(logger, Level.INFO, 3_000, sourceName, 
				"[" + robot.getLocationTag().getLocale() + "] " + robot.getName() 
//				+ robot.getLocationTag().getImmediateLocation() 
				+ " just unloaded a total of " + Math.round(totalAmount*100.0)/100.0 + " kg of resources from " + vehicle.getName() + ".", null);
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
				if (person != null)
					LogConsolidated.log(logger, Level.INFO, 3_000, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
//						+ " in " + person.getLocationTag().getImmediateLocation() 
						+ " just unloaded a total of " + totalItems + " items from " + vehicle.getName() + ".", null);
				else
					LogConsolidated.log(logger, Level.INFO, 3_000, sourceName, 
						"[" + robot.getLocationTag().getLocale() + "] " + robot.getName() 
//						+ " in " + robot.getLocationTag().getImmediateLocation() 
						+ " just unloaded a total of " + totalItems + " items from " + vehicle.getName() + ".", null);
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
					
					LogConsolidated.log(logger, Level.INFO, 0, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
						+ " was retrieving the dead body of " + p + " from " + vehicle.getName() 
						+ " parked in the vicinity of "
						+ settlement, null);
					

					p.transfer(vehicle, settlementInv);
					
					BuildingManager.addToMedicalBuilding(p, settlement.getIdentifier());			
					
					p.setAssociatedSettlement(settlement.getIdentifier());
//					p.getMind().getTaskManager().clearTask();

				}
				
//				else {
//					LogConsolidated.log(logger, Level.FINER, 0, sourceName,
//							"[" + p.getLocationTag().getLocale() + "] " + p.getName() + " came home safety on rover "+ vehicle.getName() + ".", null);
//				
//					if (vehicle.getGarage() != null) {
//						// the rover is parked inside a garage
//						vehicle.getInventory().retrieveUnit(p);
//						settlement.getInventory().storeUnit(p);
//						BuildingManager.addPersonOrRobotToBuilding(p, vehicle.getGarage());
//						
////						p.getMind().getTaskManager().addTask(new Walk(p));
//						p.getMind().getTaskManager().getNewTask();
//					}
//					
//					else if (p != person) {
//						// the person is still inside the vehicle
//						// Clear any other task and 
////						p.getMind().getTaskManager().clearTask();
//					}
//				}
			}
		}

		if (isFullyUnloaded(vehicle)) {
			setPhase(WALK_BACK_INSIDE);
			
			if (person.isOutside()) {
				setPhase(WALK_BACK_INSIDE);	
			}
			else if (person.isInside()) {
	    		endTask();
	        }
			
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

	@Override
	public int getEffectiveSkillLevel() {
		if (person != null)
			return person.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
		else 
			return robot.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.EVA_OPERATIONS);
		return results;
	}

	@Override
	protected void addExperience(double time) {

		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;

		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = null;
		RoboticAttributeManager rManager = null;
		int experienceAptitude = 0;
		if (person != null) {
			nManager = person.getNaturalAttributeManager();
			experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		} else if (robot != null) {
			rManager = robot.getRoboticAttributeManager();
			experienceAptitude = rManager.getAttribute(RoboticAttributeType.EXPERIENCE_APTITUDE);
		}

		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		if (person != null)
			person.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);
		else if (robot != null)
			robot.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);
	}

	@Override
	public void destroy() {
		super.destroy();

		vehicle = null;
		settlement = null;
	}
}