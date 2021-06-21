/**
 * Mars Simulation Project
 * LoadVehicleGarage.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The LoadVehicleGarage class is a task for loading a vehicle with fuel and
 * supplies in a vehicle maintenance garage.
 */
public class LoadVehicleGarage extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(LoadVehicleGarage.class.getName());
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.loadVehicleGarage"); //$NON-NLS-1$

	/** Comparison to indicate a small but non-zero amount. */
	private static final double SMALL_AMOUNT_COMPARISON = .0000001D;

	/** Task phases. */
	private static final TaskPhase LOADING = new TaskPhase(Msg.getString("Task.phase.loading")); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	/**
	 * The amount of resources (kg) one person of average strength can load per
	 * millisol.
	 */
	private static double LOAD_RATE = 20D;
	
	/** The duration of the loading task (millisols). */
	private static double DURATION = RandomUtil.getRandomDouble(50D) + 10D;

	// Data members
	
	/** Resources required to load. */
	private Map<Integer, Number> requiredResources;
	/** Resources desired to load but not required. */
	private Map<Integer, Number> optionalResources;
	/** Equipment required to load. */
	private Map<Integer, Integer> requiredEquipment;
	/** Equipment desired to load but not required. */
	private Map<Integer, Integer> optionalEquipment;
	/** The vehicle that needs to be loaded. */
	private Vehicle vehicle;
	/** The person's settlement. */
	private Settlement settlement;

	public static AmountResource[] availableDesserts = PreparingDessert.getArrayOfDessertsAR();

	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
	private static int foodID = ResourceUtil.foodID;
//	private static int methaneID = ResourceUtil.methaneID;
	
	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public LoadVehicleGarage(Person person) {
		// Use Task constructor
		super(NAME, person, true, false, STRESS_MODIFIER, DURATION);

		VehicleMission mission = getMissionNeedingLoading();
		if (mission == null) {
			endTask();
			return;
		}
		
		settlement = person.getSettlement();
		if (settlement == null) {
			endTask();
			return;
		}

		if (!isDone()) {
			vehicle = mission.getVehicle();
			// Add the rover to a garage if possible
			if (vehicle != null && BuildingManager.add2Garage(vehicle)) {
				// Walk to garage.
				walkToTaskSpecificActivitySpotInBuilding(BuildingManager.getBuilding(vehicle),
														 FunctionType.GROUND_VEHICLE_MAINTENANCE, false);
			
				setDescription(Msg.getString("Task.description.loadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$
				requiredResources = mission.getRequiredResourcesToLoad();
				optionalResources = mission.getOptionalResourcesToLoad();
				requiredEquipment = mission.getRequiredEquipmentToLoad();
				optionalEquipment = mission.getOptionalEquipmentToLoad();
				
				// Initialize task phase
				addPhase(LOADING);
				setPhase(LOADING);
			}
			else {
				endTask();
				return;
			}

		}
	}

	public LoadVehicleGarage(Robot robot) {
		// Use Task constructor
		super(NAME, robot, true, false, STRESS_MODIFIER, DURATION);

		VehicleMission mission = getMissionNeedingLoading();
		if (mission != null) {
			vehicle = mission.getVehicle();
			setDescription(Msg.getString("Task.description.loadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$
			requiredResources = mission.getRequiredResourcesToLoad();
			// TODO: add extra food/dessert as optionalResources
			optionalResources = mission.getOptionalResourcesToLoad();
			requiredEquipment = mission.getRequiredEquipmentToLoad();
			optionalEquipment = mission.getOptionalEquipmentToLoad();
			
//			if (requiredResources.containsKey(1))
//				logger.info("2. food : " + Math.round((double)requiredResources.get(1)*100.0)/100.0);
			
			settlement = robot.getSettlement();
			if (settlement == null) {
				endTask();
				return;
			}

			// If vehicle is in a garage, add robot to garage.
			Building garageBuilding = BuildingManager.getBuilding(vehicle);
			if (garageBuilding != null) {
				// Walk to garage.
				walkToTaskSpecificActivitySpotInBuilding(garageBuilding, FunctionType.GROUND_VEHICLE_MAINTENANCE,
														 false);
			}

			// End task if vehicle or garage not available.
			if ((vehicle == null) || (garageBuilding == null)) {
				endTask();
				return;
			}

			// Initialize task phase
			addPhase(LOADING);
			setPhase(LOADING);
		} 
		
		else {
			endTask();
			return;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param person            the person performing the task.
	 * @param vehicle           the vehicle to be loaded.
	 * @param requiredResources a map of required resources to be loaded.
	 * @param optionalResources a map of optional resources to be loaded.
	 * @param requiredEquipment a map of required equipment to be loaded.
	 * @param optionalEquipment a map of optional equipment to be loaded.
	 */
	public LoadVehicleGarage(Person person, Vehicle vehicle, Map<Integer, Number> requiredResources,
			Map<Integer, Number> optionalResources, Map<Integer, Integer> requiredEquipment,
			Map<Integer, Integer> optionalEquipment) {
		// Use Task constructor.
		super("Loading vehicle", person, true, false, STRESS_MODIFIER, DURATION);

		setDescription(Msg.getString("Task.description.loadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$
		this.vehicle = vehicle;
		
		if (requiredResources != null) {
			this.requiredResources = new ConcurrentHashMap<Integer, Number>(requiredResources);
		}
		if (optionalResources != null) {
			this.optionalResources = new ConcurrentHashMap<Integer, Number>(optionalResources);
		}
		if (requiredEquipment != null) {
			this.requiredEquipment = new ConcurrentHashMap<>(requiredEquipment);
		}
		if (optionalEquipment != null) {
			this.optionalEquipment = new ConcurrentHashMap<>(optionalEquipment);
		}

//		if (requiredResources.containsKey(1))
//			logger.info("3. food : " + Math.round((double)requiredResources.get(1)*100.0)/100.0);
		
		settlement = person.getSettlement();
		if (settlement == null) {
			endTask();
			return;
		}
		
//    	if (!BuildingManager.isRoverInAGarage(vehicle))
//    		endTask();
//    	else 
//    		// Walk to garage.
//    		walkToActivitySpotInBuilding(BuildingManager.getBuilding(vehicle), false);
    	
		// If vehicle is in a garage, add person to garage.
		Building garage = BuildingManager.getBuilding(vehicle);
		if (garage != null) {
			// Walk to garage.
			walkToTaskSpecificActivitySpotInBuilding(garage, FunctionType.GROUND_VEHICLE_MAINTENANCE, false);
		}
		else {
			endTask();
			return;
		}

		if (!isDone()) {
			// Initialize task phase
			addPhase(LOADING);
			setPhase(LOADING);
		}
	}

	public LoadVehicleGarage(Robot robot, Vehicle vehicle, Map<Integer, Number> requiredResources,
			Map<Integer, Number> optionalResources, Map<Integer, Integer> requiredEquipment,
			Map<Integer, Integer> optionalEquipment) {
		// Use Task constructor.
		super("Loading vehicle", robot, true, false, STRESS_MODIFIER, DURATION);

		setDescription(Msg.getString("Task.description.loadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$
		this.vehicle = vehicle;
		
		if (requiredResources != null) {
			this.requiredResources = new ConcurrentHashMap<Integer, Number>(requiredResources);
		}
		if (optionalResources != null) {
			this.optionalResources = new ConcurrentHashMap<Integer, Number>(optionalResources);
		}
		if (requiredEquipment != null) {
			this.requiredEquipment = new ConcurrentHashMap<>(requiredEquipment);
		}
		if (optionalEquipment != null) {
			this.optionalEquipment = new ConcurrentHashMap<>(optionalEquipment);
		}

//		if (requiredResources.containsKey(1))
//			logger.info("4. food : " + Math.round((double)requiredResources.get(1)*100.0)/100.0);
		
		settlement = robot.getSettlement();

		// If vehicle is in a garage, add robot to garage.
		Building garage = BuildingManager.getBuilding(vehicle);
		if (garage != null) {
			// Walk to garage.
			walkToTaskSpecificActivitySpotInBuilding(garage, FunctionType.GROUND_VEHICLE_MAINTENANCE, false);
		}

		// Initialize task phase
		addPhase(LOADING);
		setPhase(LOADING);
	}

	/**
	 * Gets a list of all embarking vehicle missions at a settlement with vehicle
	 * currently in a garage.
	 * 
	 * @param settlement the settlement.
	 * @return list of vehicle missions.
	 * @throws Exception if error finding missions.
	 */
	public static List<Mission> getAllMissionsNeedingLoading(Settlement settlement) {

		List<Mission> result = new CopyOnWriteArrayList<Mission>();

		Iterator<Mission> i = missionManager.getMissions().iterator();
		while (i.hasNext()) {
			Mission mission = i.next();
			if (mission instanceof VehicleMission) {
				if (VehicleMission.EMBARKING.equals(mission.getPhase())) {
					VehicleMission vehicleMission = (VehicleMission) mission;
					if (vehicleMission.hasVehicle()) {
						Vehicle vehicle = vehicleMission.getVehicle();
						if (settlement == vehicle.getSettlement()) {
							if (!vehicleMission.isVehicleLoaded()) {
								if (BuildingManager.isInAGarage(vehicle)) {
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
	 * Gets a random vehicle mission loading at the settlement.
	 * 
	 * @return vehicle mission.
	 * @throws Exception if error finding vehicle mission.
	 */
	private VehicleMission getMissionNeedingLoading() {

		VehicleMission result = null;
		List<Mission> loadingMissions = getAllMissionsNeedingLoading(worker.getSettlement());

		if (!loadingMissions.isEmpty()) {
			int index = RandomUtil.getRandomInt(loadingMissions.size() - 1);
			result = (VehicleMission) loadingMissions.get(index);
		}

		return result;
	}

	/**
	 * Gets the vehicle being loaded.
	 * 
	 * @return vehicle
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}

	/**
	 * Performs the method mapped to the task's current phase.
	 * 
	 * @param time the amount of time (millisol) the phase is to be performed.
	 * @return the remaining time (millisol) after the phase has been performed.
	 */
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
        	logger.severe(worker, "phase is null.");
            return 0;//throw new IllegalArgumentException("Task phase is null");
		} else if (LOADING.equals(getPhase())) {
			return loadingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Perform the loading phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) after performing the phase.
	 */
	double loadingPhase(double time) {
	
		// NOTE: if a person is not at a settlement or near its vicinity, 
		// then the settlement instance is set to null. 
    	if (settlement == null) {
    		endTask();
    		return 0;
    	}
    	
    	if (person != null && !person.getMind().hasActiveMission()) {
    		endTask();
    		return 0;
    	}
    	
		if (!vehicle.isInSettlementVicinity() || !BuildingManager.isInAGarage(vehicle)) {
        	endTask();
			return 0;
		}
		
		if (!isDone()) {
			int strength = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);
			double strengthModifier = .1D + (strength * .018D);
			double amountLoading = LOAD_RATE * strengthModifier * time / 12D;
	
			// Temporarily remove rover from settlement so that inventory doesn't get mixed
			// in.
			Inventory sInv = settlement.getInventory();
			boolean roverInSettlement = false;
			if (sInv.containsUnit(vehicle)) {
				roverInSettlement = true;
				sInv.retrieveUnit(vehicle);
			}
			else { // if the rover is no longer in the settlement, end the task
				endTask();
				return 0;
			}
			
			// Load equipment
			if (amountLoading > 0D) {
				amountLoading = loadEquipment(amountLoading);
			}
			
			// Load resources
			try {
				if (amountLoading > 0D) {
					amountLoading = loadResources(amountLoading);
				}
			} catch (Exception e) {
				logger.severe(vehicle, "Error in loadResources()" + e.getMessage());
			}
	
			// Put rover back into settlement.
			if (roverInSettlement) {
				sInv.storeUnit(vehicle);
			}
	
			if (isFullyLoaded(requiredResources, optionalResources, requiredEquipment, optionalEquipment, vehicle,
					settlement)) {
				endTask();
				return 0;
			}
		}
		
		return 0;
	}

	/**
	 * Loads the vehicle with required resources from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @return the remaining amount (kg) the person can load in this time period.
	 * @throws Exception if problem loading resources.
	 */
	private double loadResources(double amountLoading) {

		// Load required resources.
		Iterator<Integer> iR = requiredResources.keySet().iterator();
		while (iR.hasNext() && (amountLoading > 0D)) {
			Integer resource = iR.next();
			if (resource < FIRST_ITEM_RESOURCE_ID) {
				// Load amount resources
				amountLoading = loadAmountResource(amountLoading, resource, true);
			} else if (resource >= FIRST_ITEM_RESOURCE_ID) {
				// Load item resources
				amountLoading = loadItemResource(amountLoading, resource, true);
			}
		}

		// Load optional resources.
		Iterator<Integer> iR2 = optionalResources.keySet().iterator();
		while (iR2.hasNext() && (amountLoading > 0D)) {
			Integer resource = iR2.next();
			if (resource < FIRST_ITEM_RESOURCE_ID) {
				// Load amount resources
				amountLoading = loadAmountResource(amountLoading, resource, false);
			} else if (resource >= FIRST_ITEM_RESOURCE_ID) {
				// Load item resources
				amountLoading = loadItemResource(amountLoading, resource, false);
			}
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * Loads the vehicle with an amount resource from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param resource      the amount resource to be loaded.
	 * @param required      true if the amount resource is required to load, false
	 *                      if optional.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadAmountResource(double amountLoading, Integer resource, boolean required) {
		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();
		
		double amountNeededTotal = 0D;
		if (required) {
			amountNeededTotal = (Double) requiredResources.get(resource);
		} else {
			if (requiredResources.containsKey(resource)) {
				amountNeededTotal += (Double) requiredResources.get(resource);
			}
			amountNeededTotal += (Double) optionalResources.get(resource);
		}

		double amountAlreadyLoaded = vInv.getAmountResourceStored(resource, false);
//		if (resource == 1)
//			LogConsolidated.log(Level.INFO, 0, sourceName, //"LoadVehicleGarage's loadAmountResource - " + 
//			System.out.println("1. " + 
//				vehicle.getName() + " - "
//				+ ResourceUtil.findAmountResourceName(resource) 
//				+ " needed: " + Math.round(amountNeededTotal*100.0)/100.0
//				+ " already loaded: " + Math.round(amountAlreadyLoaded*100.0)/100.0);
		
		
		if (amountAlreadyLoaded < amountNeededTotal) {
			double amountNeeded = amountNeededTotal - amountAlreadyLoaded;
			boolean canLoad = true;
			String loadingErrorMsg = "";

			// Check if enough resource in settlement inventory.
			double settlementStored = sInv.getAmountResourceStored(resource, false);
			// add tracking demand
			sInv.addAmountDemandTotalRequest(resource, amountNeeded);
		
			if (settlementStored < amountNeeded) {
				if (required) {
					canLoad = false;
					loadingErrorMsg = " did NOT have enough resource stored at settlement to load " + "resource: " 
							+ ResourceUtil.findAmountResourceName(resource)
							+ " needed: " + Math.round(amountNeeded * 100D) / 100D + ", stored: "
							+ Math.round(settlementStored * 100D) / 100D;
				} else {
					amountNeeded = settlementStored;
				}
			}

			// Check remaining capacity in vehicle inventory.
			double remainingCapacity = vInv.getAmountResourceRemainingCapacity(resource, true, false);
			if (remainingCapacity < amountNeeded) {
				if (required) {
					if ((amountNeeded - remainingCapacity) < SMALL_AMOUNT_COMPARISON) {
						amountNeeded = remainingCapacity;
					} else {
						canLoad = false;
						loadingErrorMsg = " did NOT have enough capacity in vehicle for loading resource " 
								+ ResourceUtil.findAmountResourceName(resource) + ": "
								+ Math.round(amountNeeded * 100D) / 100D + ", remaining capacity: "
								+ Math.round(remainingCapacity * 100D) / 100D;
					}
				} else {
					amountNeeded = remainingCapacity;
				}
			}

			// Determine amount to load.
			double resourceAmount = amountNeeded;
			if (amountNeeded > amountLoading) {
				resourceAmount = amountLoading;
			}

			if (canLoad) {

				// Load resource from settlement inventory to vehicle inventory.
				try {
					sInv.retrieveAmountResource(resource, resourceAmount);
					vInv.storeAmountResource(resource, resourceAmount, true);
					
//					if (resource == 1)
						//LogConsolidated.log(Level.INFO, 0, sourceName, //"LoadVehicleGarage's loadAmountResource - " 
//						System.out.println("2" + ". " + 
//						vehicle.getName() + " - "
//							+ ResourceUtil.findAmountResourceName(resource) 
//							+ " needed: " + amountNeededTotal
//							+ " loading: " + resourceAmount);
					
					sInv.addAmountDemand(resource, resourceAmount);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
				amountLoading -= resourceAmount;
			}
			
			else {
    			logger.warning(vehicle, loadingErrorMsg);
				endTask();
				// throw new IllegalStateException(loadingError);

			}
		}
		
		else { // if (amountAlreadyLoaded >= amountNeededTotal) {
			if (required && optionalResources.containsKey(resource)) {
				amountNeededTotal += (Double) optionalResources.get(resource);
			}

			if (amountAlreadyLoaded > 0 && amountNeededTotal > 0 
					&& amountAlreadyLoaded > amountNeededTotal) {

				// In case vehicle wasn't fully unloaded first.
				double amountToRemove = amountAlreadyLoaded - amountNeededTotal;
				
				if (amountToRemove > SMALL_AMOUNT_COMPARISON) {
					try {
						vInv.retrieveAmountResource(resource, amountToRemove);
						sInv.storeAmountResource(resource, amountToRemove, true);
						
//						if (resource == 1)
//							LogConsolidated.log(Level.INFO, 0, sourceName, //"LoadVehicleGarage's loadAmountResource - " + 
//									System.out.println("4. " + 
//									vehicle.getName() + " - "
//									+ ResourceUtil.findAmountResourceName(resource) 
//									+ " needed: " + amountNeededTotal
//									+ " loaded: " + amountAlreadyLoaded
//									+ " returning: " + amountToRemove);
					
					} catch (Exception e) {
						logger.warning(vehicle, "Was trying to return the excessive " + ResourceUtil.findAmountResourceName(resource));
					}
				}
			}
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * Loads the vehicle with an item resource from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @param resource      the item resource to be loaded.
	 * @param required      true if the item resource is required to load, false if
	 *                      optional.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadItemResource(double amountLoading, Integer id, boolean required) {

		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();

		int numNeededTotal = 0;
		if (required) {
			numNeededTotal = (int) requiredResources.get(id);
		} else {
			if (requiredResources.containsKey(id)) {
				numNeededTotal += (int) requiredResources.get(id);
			}
			numNeededTotal += (int) optionalResources.get(id);
		}

		int numAlreadyLoaded = vInv.getItemResourceNum(id);

		Part p = ItemResourceUtil.findItemResource(id);

		if (numAlreadyLoaded < numNeededTotal) {
			int numNeeded = numNeededTotal - numAlreadyLoaded;
			boolean canLoad = true;
			String loadingError = "";

			// Check if enough resource in settlement inventory.
			int settlementStored = sInv.getItemResourceNum(id);
			// Add tracking demand
			sInv.addItemDemand(id, numNeeded);
			sInv.addItemDemandTotalRequest(id, numNeeded);
			
			if (settlementStored < numNeeded) {
				if (required) {
					canLoad = false;
					loadingError = " did NOT have enough " + p + " stored at settlement to load up.  Needed: "
							+ numNeeded + ".  Stored: " + settlementStored;
				} else {
					numNeeded = settlementStored;
				}
			}

			// Check remaining capacity in vehicle inventory.
			double remainingMassCapacity = vInv.getRemainingGeneralCapacity(false);
			if (remainingMassCapacity < (numNeeded * p.getMassPerItem())) {
				if (required) {
					canLoad = false;
					loadingError = " did NOT enough capacity in vehicle for loading " + numNeeded + " " + p  
							+ ", remaining capacity: " + remainingMassCapacity + " kg";
				} else {
					numNeeded = (int) (remainingMassCapacity / p.getMassPerItem());
				}
			}

			// Determine amount to load.
			int resourceNum = (int) (amountLoading / p.getMassPerItem());
			if (resourceNum < 1) {
				resourceNum = 1;
			}
			if (resourceNum > numNeeded) {
				resourceNum = numNeeded;
			}

			if (canLoad) {

				// Load resource from settlement inventory to vehicle inventory.
				sInv.retrieveItemResources(id, resourceNum);
				vInv.storeItemResources(id, resourceNum);
				amountLoading -= (resourceNum * p.getMassPerItem());
				if (amountLoading < 0D)
					amountLoading = 0D;
			} else {
    			logger.warning(vehicle, loadingError);
				endTask();
//				throw new IllegalStateException(loadingError);
			}
		} else {
			if (required && optionalResources.containsKey(id)) {
				numNeededTotal += (int) optionalResources.get(id);
			}

			if (numAlreadyLoaded > numNeededTotal) {

				// In case vehicle wasn't fully unloaded first.
				int numToRemove = numAlreadyLoaded - numNeededTotal;
				try {
					vInv.retrieveItemResources(id, numToRemove);
					sInv.storeItemResources(id, numToRemove);
				} catch (Exception e) {
				}
			}
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * Loads the vehicle with required and optional equipment from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadEquipment(double amountLoading) {

		// Load required equipment.
		amountLoading = loadRequiredEquipment(amountLoading);

		// Load optional equipment.
		amountLoading = loadOptionalEquipment(amountLoading);

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * Loads the vehicle with required equipment from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadRequiredEquipment(double amountLoading) {

		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();

		Iterator<Integer> iE = requiredEquipment.keySet().iterator();
		while (iE.hasNext() && (amountLoading > 0D)) {
			Integer equipmentType = iE.next();
			int numNeededTotal = requiredEquipment.get(equipmentType);
			int numAlreadyLoaded = vInv.findNumEquipment(equipmentType);
			if (numAlreadyLoaded < numNeededTotal) {
				int numNeeded = numNeededTotal - numAlreadyLoaded;
				Collection<Unit> units = sInv.findAllUnitsOfClass(equipmentType);
				Object[] array = units.toArray();

				if (units.size() >= numNeeded) {
					int loaded = 0;
					for (int x = 0; (x < units.size()) && (loaded < numNeeded) && (amountLoading > 0D); x++) {
						Equipment eq = (Equipment) array[x];

						boolean isEmpty = true;
						Inventory eInv = eq.getInventory();
						if (eInv != null) {
							isEmpty = eq.getInventory().isEmpty(false);
						}

						if (isEmpty) {
							if (vInv.canStoreUnit(eq, false)) {
								// Put this equipment into a vehicle
								eq.transfer(sInv, vInv);
//								sInv.retrieveUnit(eq);
//								vInv.storeUnit(eq);
								amountLoading -= eq.getMass();
								if (amountLoading < 0D) {
									amountLoading = 0D;
								}
								loaded++;
							} else {
                    			logger.warning(vehicle,"Cannot store " + eq);
								endTask();
							}
						}
					}

					array = null;
				} else {
					endTask();
				}
			} else {

				if (optionalEquipment.containsKey(equipmentType)) {
					numNeededTotal += optionalEquipment.get(equipmentType);
				}

				if (numAlreadyLoaded > numNeededTotal) {

					// In case vehicle wasn't fully unloaded first.
					int numToRemove = numAlreadyLoaded - numNeededTotal;
					Collection<Unit> units = vInv.findAllUnitsOfClass(equipmentType);
					Object[] array = units.toArray();

					for (int x = 0; x < numToRemove; x++) {
						Equipment eq = (Equipment) array[x];
						// Put this equipment into the settlement
						eq.transfer(vInv, sInv);
//						vInv.retrieveUnit(eq);
//						sInv.storeUnit(eq);
					}

					array = null;
				}
			}
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}

	/**
	 * Loads the vehicle with optional equipment from the settlement.
	 * 
	 * @param amountLoading the amount (kg) the person can load in this time period.
	 * @return the remaining amount (kg) the person can load in this time period.
	 */
	private double loadOptionalEquipment(double amountLoading) {

		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();

		Iterator<Integer> iE = optionalEquipment.keySet().iterator();
		while (iE.hasNext() && (amountLoading > 0D)) {
			Integer equipmentType = iE.next();
			int numNeededTotal = optionalEquipment.get(equipmentType);
			if (requiredEquipment.containsKey(equipmentType)) {
				numNeededTotal += requiredEquipment.get(equipmentType);
			}
			int numAlreadyLoaded = vInv.findNumEquipment(equipmentType);
			if (numAlreadyLoaded < numNeededTotal) {
				int numNeeded = numNeededTotal - numAlreadyLoaded;
				Collection<Unit> units = sInv.findAllUnitsOfClass(equipmentType);
				Object[] array = units.toArray();

				if (units.size() < numNeeded) {
					numNeeded = units.size();
				}

				int loaded = 0;
				for (int x = 0; (x < units.size()) && (loaded < numNeeded) && (amountLoading > 0D); x++) {
					Equipment eq = (Equipment) array[x];

					boolean isEmpty = true;
					Inventory eInv = eq.getInventory();
					if (eInv != null) {
						isEmpty = eq.getInventory().isEmpty(false);
					}

					if (isEmpty) {
						if (vInv.canStoreUnit(eq, false)) {
							// Put this equipment into a vehicle
							eq.transfer(sInv, vInv);
//							sInv.retrieveUnit(eq);
//							vInv.storeUnit(eq);
							amountLoading -= eq.getMass();
							if (amountLoading < 0D) {
								amountLoading = 0D;
							}
							loaded++;
						} else {
                			logger.warning(vehicle, "Cannot store " + eq);
							endTask();
						}
					}
				}

				array = null;
			} else if (numAlreadyLoaded > numNeededTotal) {

				// In case vehicle wasn't fully unloaded first.
				int numToRemove = numAlreadyLoaded - numNeededTotal;
				Collection<Unit> units = vInv.findAllUnitsOfClass(equipmentType);
				Object[] array = units.toArray();

				for (int x = 0; x < numToRemove; x++) {
					Equipment eq = (Equipment) array[x];
					// Put this equipment into the settlement
					eq.transfer(vInv, sInv);
//					vInv.retrieveUnit(eq);
//					sInv.storeUnit(eq);
				}

				array = null;
			}
		}

		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
	}


	/**
	 * Checks if there are enough supplies in the settlement's stores to supply
	 * trip.
	 * 
	 * @param settlement     the settlement the vehicle is at.
	 * @param resources      a map of resources required for the trip.
	 * @param equipment      a map of equipment required for the trip.
	 * @param vehicleCrewNum the number of people in the vehicle crew.
	 * @param tripTime       the estimated time for the trip (millisols).
	 * @return true if enough supplies
	 * @throws Exception if error checking supplies.
	 */
	public static boolean hasEnoughSupplies(Settlement settlement, Vehicle vehicle,
			Map<Integer, Number> resources, Map<Integer, Integer> equipment, int vehicleCrewNum, double tripTime) {

		// Check input parameters.
		if (settlement == null)
			throw new IllegalArgumentException("settlement is null");

		boolean enoughSupplies = true;
		Inventory inv = settlement.getInventory();
		Inventory vInv = vehicle.getInventory();

		boolean roverInSettlement = false;
		if (inv.containsUnit(vehicle)) {
			roverInSettlement = true;
			inv.retrieveUnit(vehicle);
		}

		// Check if there are enough resources at the settlement.

		for (Integer resource : resources.keySet()) {

			if (resource < FIRST_ITEM_RESOURCE_ID) {
				// Added all desserts to the matching test
//				boolean isDessert = false;
//				double amountDessertLoaded = 0;
//				double stored = inv.getAmountResourceStored(resource, false);
//				double settlementNeed = getSettlementNeed(settlement, vehicleCrewNum, resource, tripTime);
//				double needed = (Double) resources.get(resource);
//				String dessertName = "";
				
				// Put together a list of available dessert
//				for (AmountResource dessert : availableDesserts) {
//					dessertName = dessert.getName();
//					if (ResourceUtil.findAmountResource(resource).getName().equals(dessertName)) {
//						// Add the amount of all six desserts together
//						amountDessertLoaded = vInv.getAmountResourceStored(resource, false);
////						stored += inv.getAmountResourceStored(resource, false);
////						settlementNeed += getSettlementNeed(settlement, vehicleCrewNum, resource, tripTime);
//						isDessert = true;
//					}
//				}
//
//				if (isDessert) {
//					double totalNeeded = needed + settlementNeed - amountDessertLoaded;
//
//					if (stored < totalNeeded) {
//						if (logger.isLoggable(Level.INFO))
//							LogConsolidated.log(Level.INFO, 5000, sourceName,
//									" Not enough "
//									+ Conversion.capitalize(dessertName) 
//									+ ". Mission need: " + Math.round(needed * 100.0) / 100.0  
//									+ " ; " + settlement + " need: " + Math.round(settlementNeed* 100.0) / 100.0
//									+ " ; Stored: " + Math.round(stored* 100.0) / 100.0);
//						inv.addAmountDemandTotalRequest(resource, totalNeeded);
//						enoughSupplies = false;
//					}
//				}

//				else { // this resource is not a dessert
					double stored = inv.getAmountResourceStored(resource, false);
					double needed = (Double) resources.get(resource);
					double settlementNeed = getSettlementNeed(settlement, vehicleCrewNum, resource, tripTime);
					double loaded = vInv.getAmountResourceStored(resource, false);
					double totalNeeded = needed + settlementNeed - loaded;
//					if (Conversion.capitalize(ResourceUtil.findAmountResourceName(resource)).equalsIgnoreCase("food"))
//						System.out.println(settlement + "'s food supply : "
//								+ " Mission need: " + Math.round(needed * 100.0) / 100.0  
//								+ " ; " + settlement + " need: " + Math.round(settlementNeed* 100.0) / 100.0
//								+ " ; Stored: " + Math.round(stored* 100.0) / 100.0);
								
					if (stored < totalNeeded) {
						if (logger.isLoggable(Level.INFO))
							logger.log(vehicle, Level.INFO, 5000, " Not enough "
									+ ResourceUtil.findAmountResourceName(resource) 
									+ "; Loaded : " + Math.round(loaded * 100.0) / 100.0 
									+ "; Mission need: " + Math.round(needed * 100.0) / 100.0  
									+ "; " + settlement + " need: " + Math.round(settlementNeed* 100.0) / 100.0
									+ "; " + settlement + " stored: " + Math.round(stored* 100.0) / 100.0);
						inv.addAmountDemandTotalRequest(resource, totalNeeded);
						enoughSupplies = false;
						return false;
					}
//				}
			}

			else if (resource >= FIRST_ITEM_RESOURCE_ID) {
				int needed = (Integer) resources.get(resource);
				int settlementNeed = getRemainingSettlementNum(settlement, vehicleCrewNum, resource);
				int numLoaded = vInv.getItemResourceNum(resource);
				int totalNeeded = needed + settlementNeed - numLoaded;
				if (inv.getItemResourceNum(resource) < totalNeeded) {
					int stored = inv.getItemResourceNum(resource);
					if (logger.isLoggable(Level.INFO))
						logger.log(vehicle, Level.INFO, 0, " Not enough "
								+ Conversion.capitalize(ResourceUtil.findAmountResourceName(resource)) 
								+ "; Loaded : " + numLoaded
								+ "; Mission need : " + needed   
								+ "; " + settlement + " need : " + settlementNeed
								+ "; " + settlement + " stored : " + stored);
					inv.addItemDemandTotalRequest(resource, totalNeeded);
					enoughSupplies = false;
					return false;
				}
			} else
				throw new IllegalStateException("Unknown resource type: " + resource);
		}

		// Check if there is enough equipment at the settlement.
		Iterator<Integer> iE = equipment.keySet().iterator();
		while (iE.hasNext()) {
			Integer equipmentType = iE.next();			
			String name = Conversion.capitalize(EquipmentType.convertID2Enum(equipmentType).toString());
//			Class<?> c = EquipmentFactory.getEquipmentClass(equipmentID);
			int needed = equipment.get(equipmentType);
			int settlementNeed = getRemainingSettlementNum(settlement, vehicleCrewNum, equipmentType);
			int numLoaded = vInv.findNumEquipment(equipmentType);
			int totalNeeded = needed + settlementNeed - numLoaded;
			int stored = inv.findNumEmptyUnitsOfClass(equipmentType, false);
			if (stored < totalNeeded) {	
				if (logger.isLoggable(Level.INFO))
					logger.log(vehicle, Level.INFO, 0, "Not enough "
							+ name 
							+ "; Mission need: " + needed 
							+ "; Loaded : " + numLoaded 
							+ "; " + settlement + " need: " + settlementNeed
							+ "; " + settlement + " stored : " + stored);
				enoughSupplies = false;
				return false;
			}
		}

		if (roverInSettlement)
			inv.storeUnit(vehicle);

		return enoughSupplies;
	}

	/**
	 * Gets the amount of an amount resource that should remain at the settlement.
	 * 
	 * @param settlement     the settlement
	 * @param vehicleCrewNum the number of crew leaving on the vehicle.
	 * @param resource       the amount resource
	 * @param                double tripTime the estimated trip time (millisols).
	 * @return remaining amount (kg)
	 * @throws Exception if error getting the remaining amount.
	 */
	static double getSettlementNeed(Settlement settlement, int vehicleCrewNum, Integer resource, double tripTime) {
		int remainingPeopleNum = settlement.getIndoorPeopleCount() - vehicleCrewNum;
		double amountPersonPerSol = 0D;
		double tripTimeSols = tripTime / 1000D;

		if (personConfig == null) 
			personConfig = SimulationConfig.instance().getPersonConfig();
		
		// Only life support resources are required at settlement at this time.
		if (resource == oxygenID)
			amountPersonPerSol = personConfig.getNominalO2ConsumptionRate();
		else if (resource == waterID)
			amountPersonPerSol = personConfig.getWaterConsumptionRate();
		else if (resource == foodID)
			amountPersonPerSol = personConfig.getFoodConsumptionRate();
		else {
			// check if this resource is a dessert
			for (AmountResource dessert : availableDesserts) {
				if (ResourceUtil.findAmountResource(resource).getName().equals(dessert.getName())) {
					amountPersonPerSol = PreparingDessert.getDessertMassPerServing();
					break;
				}
			}
		}

		return remainingPeopleNum * (amountPersonPerSol * tripTimeSols);
	}

	/**
	 * Gets the number of an item resource that should remain at the settlement.
	 * 
	 * @param settlement     the settlement
	 * @param vehicleCrewNum the number of crew leaving on the vehicle.
	 * @param resource       the item resource
	 * @return remaining number
	 * @throws Exception if error getting the remaining number.
	 */
	private static int getRemainingSettlementNum(Settlement settlement, int vehicleCrewNum, Integer resource) {
		// No item resources required at settlement at this time.
		return 1;
	}

//    /**
//     * Gets the number of an equipment type that should remain at the settlement.
//     * @param settlement the settlement
//     * @param vehicleCrewNum the number of crew leaving on the vehicle.
//     * @param equipmentType the equipment type class.
//     * @return remaining number.
//     * @throws Exception if error getting the remaining number.
//     */
//    private static int getRemainingSettlementNum(Settlement settlement, int vehicleCrewNum,
//            Class<? extends Equipment> equipmentType) {
//    	int remainingPeopleNum = settlement.getIndoorPeopleCount() - vehicleCrewNum;
//    	// Leave one EVA suit for every four remaining people at settlement (min 1).
//    	if (equipmentType == EVASuit.class) {
//    		int minSuits = remainingPeopleNum / 4;
//    		if (minSuits == 0) {
//    		    minSuits = 1;
//    		}
//    		return minSuits;
//    	}
//    	else {
//    	    return 0;
//    	}
//    }

	/**
	 * Checks if a vehicle has enough storage capacity for the supplies needed on
	 * the trip.
	 * 
	 * @param resources  a map of the resources required.
	 * @param equipment  a map of the equipment types and numbers needed.
	 * @param vehicle    the vehicle to check.
	 * @param settlement the settlement to disembark from.
	 * @return true if vehicle can carry supplies.
	 * @throws Exception if error
	 */
	public static boolean enoughCapacityForSupplies(Map<Integer, Number> resources, Map<Integer, Integer> equipment,
			Vehicle vehicle, Settlement settlement) {

		boolean sufficientCapacity = true;

		// Create vehicle inventory clone.
		Inventory inv = vehicle.getInventory().clone(null);

		try {
			// Add equipment clones.
			Iterator<Integer> i = equipment.keySet().iterator();
			while (i.hasNext()) {
				Integer id = i.next();
				int num = equipment.get(id);
//				Coordinates defaultLoc = new Coordinates(0D, 0D);
				for (int x = 0; x < num; x++)
					inv.storeUnit(EquipmentFactory.createEquipment(id, settlement.getCoordinates(), false));
			}

			// Add all resources.
			Iterator<Integer> j = resources.keySet().iterator();
			while (j.hasNext()) {
				Integer resource = j.next();

				if (resource < FIRST_ITEM_RESOURCE_ID) {
					double amount = (Double) (resources.get(resource));
//					System.out.println("LoadVehicleGarage : " + ResourceUtil.findAmountResourceName(resource) 
//						+ " amount : " + amount);
					inv.storeAmountResource(resource, amount, true);
				} else {
					int num = (Integer) (resources.get(resource));
					inv.storeItemResources(resource, num);
				}
			}
		} catch (Exception e) {
			logger.warning(vehicle, "NOT enough capacity in rover to store needed resources for a proposed mission. " + e.getMessage());
			sufficientCapacity = false;
		}

		return sufficientCapacity;
	}

	/**
	 * Checks if the vehicle is fully loaded with supplies.
	 * 
	 * @param requiredResources the resources that are required for the trip.
	 * @param optionalResources the resources that are optional for the trip.
	 * @param requiredEquipment the equipment that is required for the trip.
	 * @param optionalEquipment the equipment that is optional for the trip.
	 * @param vehicle           the vehicle that is being checked.
	 * @param settlement        the settlement that the vehicle is being loaded
	 *                          from.
	 * @return true if vehicle is fully loaded.
	 */
	public static boolean isFullyLoaded(Map<Integer, Number> requiredResources, Map<Integer, Number> optionalResources,
			Map<Integer, Integer> requiredEquipment, Map<Integer, Integer> optionalEquipment, Vehicle vehicle,
			Settlement settlement) {

		boolean sufficientSupplies = true;

		// Check if there are enough resources in the vehicle.
		sufficientSupplies = isFullyLoadedWithResources(requiredResources, optionalResources, vehicle, settlement);

		// Check if there is enough equipment in the vehicle.
		if (sufficientSupplies)
			sufficientSupplies = isFullyLoadedWithEquipment(requiredEquipment, optionalEquipment, vehicle, settlement);

		return sufficientSupplies;
	}

	/**
	 * Checks if the vehicle is fully loaded with resources.
	 * 
	 * @param requiredResources the resources that are required for the trip.
	 * @param optionalResources the resources that are optional for the trip.
	 * @param vehicle           the vehicle.
	 * @param settlement        the settlement that the vehicle is being loaded
	 *                          from.
	 * @return true if vehicle is loaded.
	 */
	private static boolean isFullyLoadedWithResources(Map<Integer, Number> requiredResources,
			Map<Integer, Number> optionalResources, Vehicle vehicle, Settlement settlement) {

		if (vehicle == null) {
			throw new IllegalArgumentException("vehicle is null");
		}

		boolean sufficientSupplies = true;
		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();

		// Check that required resources are loaded first.
		Iterator<Integer> iR = requiredResources.keySet().iterator();
		while (iR.hasNext() && sufficientSupplies) {
			Integer resource = iR.next();
			if (resource < FIRST_ITEM_RESOURCE_ID) {
				double amount = (double) requiredResources.get(resource);
				double storedAmount = vInv.getAmountResourceStored(resource, false);
				if (storedAmount < (amount - SMALL_AMOUNT_COMPARISON)) {
					sufficientSupplies = false;
				}
			} else if (resource >= FIRST_ITEM_RESOURCE_ID) {
				int num = (Integer) (requiredResources.get(resource));
				if (vInv.getItemResourceNum(resource) < num) {
					sufficientSupplies = false;
				}
			} else {
				throw new IllegalStateException("Unknown resource type: " + resource);
			}
		}

		// Check that optional resources are loaded or can't be loaded.
		Iterator<Integer> iR2 = optionalResources.keySet().iterator();
		while (iR2.hasNext() && sufficientSupplies) {
			Integer resource = iR2.next();
			if (resource < FIRST_ITEM_RESOURCE_ID) {

				// AmountResource amountResource = (AmountResource) resource;
				double amount = (Double) optionalResources.get(resource);
				if (requiredResources.containsKey(resource)) {
					amount += (Double) requiredResources.get(resource);
				}

				double storedAmount = vInv.getAmountResourceStored(resource, false);
				if (storedAmount < (amount - SMALL_AMOUNT_COMPARISON)) {
					// Check if enough capacity in vehicle.
					double vehicleCapacity = vInv.getAmountResourceRemainingCapacity(resource, true, false);
					boolean hasVehicleCapacity = (vehicleCapacity >= (amount - storedAmount));

					// Check if enough stored in settlement.
					double storedSettlement = sInv.getAmountResourceStored(resource, false);
					if (settlement.getParkedVehicles().contains(vehicle)) {
						storedSettlement -= storedAmount;
					}
					boolean hasStoredSettlement = (storedSettlement >= (amount - storedAmount));

					if (hasVehicleCapacity && hasStoredSettlement) {
						sufficientSupplies = false;
					}
				}
			} else if (resource >= FIRST_ITEM_RESOURCE_ID) {

				// ItemResource itemResource = (ItemResource) resource;
				int num = (Integer) (optionalResources.get(resource));
				if (requiredResources.containsKey(resource)) {
					num += (Integer) (requiredResources.get(resource));
				}

				int storedNum = vInv.getItemResourceNum(resource);
				if (storedNum < num) {
					// Check if enough capacity in vehicle.
					double vehicleCapacity = vInv.getRemainingGeneralCapacity(false);
					boolean hasVehicleCapacity = (vehicleCapacity >= ((num - storedNum)
							* ItemResourceUtil.findItemResource(resource).getMassPerItem()));

					// Check if enough stored in settlement.
					int storedSettlement = sInv.getItemResourceNum(resource);
					if (settlement.getParkedVehicles().contains(vehicle)) {
						storedSettlement -= storedNum;
					}
					boolean hasStoredSettlement = (storedSettlement >= (num - storedNum));

					if (hasVehicleCapacity && hasStoredSettlement) {
						sufficientSupplies = false;
					}
				}
			} else {
				throw new IllegalStateException("Unknown resource type: " + resource);
			}
		}

		return sufficientSupplies;
	}

	/**
	 * Checks if the vehicle is fully loaded with resources.
	 * 
	 * @param requiredEquipment the equipment that is required for the trip.
	 * @param optionalEquipment the equipment that is optional for the trip.
	 * @param vehicle           the vehicle.
	 * @param settlement        the settlement that the vehicle is being loaded
	 *                          from.
	 * @return true if vehicle is full loaded.
	 */
	private static boolean isFullyLoadedWithEquipment(Map<Integer, Integer> requiredEquipment,
			Map<Integer, Integer> optionalEquipment, Vehicle vehicle, Settlement settlement) {

		if (vehicle == null) {
			throw new IllegalArgumentException("vehicle is null");
		}

		boolean sufficientSupplies = true;
		Inventory vInv = vehicle.getInventory();
		Inventory sInv = settlement.getInventory();

		// Check that required equipment is loaded first.
		Iterator<Integer> iE = requiredEquipment.keySet().iterator();
		while (iE.hasNext() && sufficientSupplies) {
			Integer equipmentType = iE.next();
			int num = requiredEquipment.get(equipmentType);
			if (vInv.findNumEquipment(equipmentType) < num) {
				sufficientSupplies = false;
			}
		}

		// Check that optional equipment is loaded or can't be loaded.
		Iterator<Integer> iE2 = optionalEquipment.keySet().iterator();
		while (iE2.hasNext() && sufficientSupplies) {
			Integer equipmentType = iE2.next();
			int num = optionalEquipment.get(equipmentType);
			if (requiredEquipment.containsKey(equipmentType)) {
				num += requiredEquipment.get(equipmentType);
			}

			int storedNum = vInv.findNumEquipment(equipmentType);
			if (storedNum < num) {

				// Check if enough stored in settlement.
				int storedSettlement = sInv.findNumEmptyUnitsOfClass(equipmentType, false);
				if (settlement.getParkedVehicles().contains(vehicle)) {
					storedSettlement -= storedNum;
				}
				boolean hasStoredSettlement = (storedSettlement >= (num - storedNum));

				if (hasStoredSettlement) {
					sufficientSupplies = false;
				}
			}
		}

		return sufficientSupplies;
	}
}
