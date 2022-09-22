/*
 * Mars Simulation Project
 * LoadVehicleGarage.java
 * @date 2022-07-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.tool.RandomUtil;
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
	
	/** Simple Task name */
	public static final String SIMPLE_NAME = LoadVehicleGarage.class.getSimpleName();

	/** Task phases. */
	private static final TaskPhase LOADING = new TaskPhase(Msg.getString("Task.phase.loading")); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	// Data members
	/** The vehicle that needs to be loaded. */
	private Vehicle vehicle;
	/** The person's settlement. */
	private Settlement settlement;
	/** The vehicle mission instance. */
	private VehicleMission vehicleMission;

	private LoadingController loadController;
	
	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public LoadVehicleGarage(Person person) {
		// Use Task constructor
		super(NAME, person, true, false, STRESS_MODIFIER,
				RandomUtil.getRandomDouble(50D) + 10D);

		vehicleMission = getMissionNeedingLoading(person.getSettlement(), true);
		if (vehicleMission == null) {
			endTask();
			return;
		}
		
		initLoad(person);
	}
	
	/**
	 * Initialise the load task
	 * @param starter
	 */
	private void initLoad(Worker starter) {
		settlement = starter.getSettlement();
		if (settlement == null) {
			endTask();
			return;
		}

		vehicle = vehicleMission.getVehicle();
		if (vehicle == null) {
			endTask();
			return;
		}
		else {
			// Rover may already be in the Garage
			Building garage = vehicle.getGarage();
			
			if (garage == null) {
				// Add the rover to a garage if possible
				garage = settlement.getBuildingManager().addToGarageBuilding(vehicle);
			
				// End task if vehicle or garage not available
				if (garage == null) {
					endTask();
					return;
				}
			}
			
			// Walk to garage.
			walkToTaskSpecificActivitySpotInBuilding(garage, FunctionType.VEHICLE_MAINTENANCE, false);
		
			setDescription(Msg.getString("Task.description.loadVehicleGarage.detail", vehicle.getName())); // $NON-NLS-1$
			
			loadController = vehicleMission.getLoadingPlan();
			
			// Initialize task phase
			addPhase(LOADING);
			setPhase(LOADING);
		}
	}

	public LoadVehicleGarage(Robot robot) {
		// Use Task constructor
		super(NAME, robot, true, false, STRESS_MODIFIER,
				RandomUtil.getRandomDouble(50D) + 10D);

		vehicleMission = getMissionNeedingLoading(robot.getSettlement(), true);
		if (vehicleMission == null) {
			endTask();
			return;
		}
		
		initLoad(robot);
	}

	/**
	 * Constructor
	 * 
	 * @param person            the person performing the task.
	 * @param mission           the vehicle to be loaded.
	 */
	public LoadVehicleGarage(Person person, VehicleMission mission) {
		// Use Task constructor.
		super("Loading vehicle", person, true, false, STRESS_MODIFIER,
				RandomUtil.getRandomDouble(50D) + 10D);
		this.vehicleMission = mission;
		
		initLoad(person);
	}

	public LoadVehicleGarage(Robot robot, VehicleMission mission) {
		// Use Task constructor.
		super("Loading vehicle", robot, true, false, STRESS_MODIFIER,
				RandomUtil.getRandomDouble(50D) + 10D);
		this.vehicleMission = mission;
		
		initLoad(robot);
	}

	/**
	 * Gets a list of all embarking vehicle missions at a settlement with vehicle
	 * currently in a garage.
	 * 
	 * @param settlement the settlement.
	 * @param addToGarage Should the found vehicle be added to the garage
	 * @return list of vehicle missions.
	 * @throws Exception if error finding missions.
	 */
	public static List<Mission> getAllMissionsNeedingLoading(Settlement settlement, boolean addToGarage) {

		List<Mission> result = new ArrayList<>();
		for(Mission mission : missionManager.getMissions()) {
			if (mission instanceof VehicleMission) {
				VehicleMission vehicleMission = (VehicleMission) mission;
				LoadingController plan = vehicleMission.getLoadingPlan();

				// Must have a local Loading Plan that is not complete
				if ((plan != null) && plan.getSettlement().equals(settlement) && !plan.isCompleted()) {
					Vehicle vehicle = vehicleMission.getVehicle();
					if (!addToGarage || settlement.getBuildingManager().addToGarage(vehicle)) {
						result.add(vehicleMission);
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
	public static VehicleMission getMissionNeedingLoading(Settlement settlement, boolean addToGarage) {

		VehicleMission result = null;
		List<Mission> loadingMissions = getAllMissionsNeedingLoading(settlement, addToGarage);

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
            return 0;
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
	private double loadingPhase(double time) {
	
		// NOTE: if a person is not at a settlement or near its vicinity, 
		// then the settlement instance is set to null. 
    	boolean abortLoad = (settlement == null);
    	abortLoad |= (person != null && !person.getMind().hasActiveMission());
    	abortLoad |= !settlement.getBuildingManager().isInGarage(vehicle);
        
    	if (abortLoad) {
    		endTask();
			return time;
		}
		
        // Add experience points
        addExperience(time);
        
		if (loadController.load(worker, time)) {
			endTask();
		}
		
		return 0;
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

		boolean roverInSettlement = false;
		// Check defensively to make sure the vehicle is NOT parked in the settlement vicinity
		if (settlement.containsParkedVehicle(vehicle)) {
			roverInSettlement = true;
			// Ensure that this vehicle is inside a garage when running this LoadVehicleGarage task
			settlement.removeParkedVehicle(vehicle);
		}

		// Check if there are enough resources at the settlement.
		for (Entry<Integer, Number> required : resources.entrySet()) {
			int resource = required.getKey();
			if (resource < ResourceUtil.FIRST_ITEM_RESOURCE_ID) {

				double stored = settlement.getAmountResourceStored(resource);
				double needed = required.getValue().doubleValue();
				double settlementNeed = getSettlementNeed(settlement, vehicleCrewNum, resource, tripTime);
				double loaded = vehicle.getAmountResourceStored(resource);
				double totalNeeded = needed + settlementNeed - loaded;
					
				if (stored < totalNeeded) {
					if (logger.isLoggable(Level.INFO))
						logSettlementShortage(vehicle, ResourceUtil.findAmountResourceName(resource),
								loaded, needed, settlementNeed, stored);
					return false;
				}
			}

			else if (resource >= ResourceUtil.FIRST_ITEM_RESOURCE_ID) {
				int needed = required.getValue().intValue();
				int settlementNeed = getRemainingSettlementNum(settlement, vehicleCrewNum, resource);
				int numLoaded = vehicle.getItemResourceStored(resource);
				int totalNeeded = needed + settlementNeed - numLoaded;
				if (settlement.getItemResourceStored(resource) < totalNeeded) {
					int stored = settlement.getItemResourceStored(resource);
					if (logger.isLoggable(Level.INFO))
						logSettlementShortage(vehicle, ResourceUtil.findAmountResourceName(resource),
								numLoaded, needed, settlementNeed, stored);
					return false;
				}
			} else
				throw new IllegalStateException("Unknown resource type: " + resource);
		}

		// Check if there is enough equipment at the settlement.
		for(Entry<Integer, Integer> eRequired : equipment.entrySet()) {
			Integer equipmentType = eRequired.getKey();
			EquipmentType eType = EquipmentType.convertID2Type(equipmentType);
			int needed = eRequired.getValue();
			int settlementNeed = getRemainingSettlementNum(settlement, vehicleCrewNum, equipmentType);
			int numLoaded = vehicle.findNumEmptyContainersOfType(eType, false);
			int totalNeeded = needed + settlementNeed - numLoaded;
			int stored = settlement.findNumEmptyContainersOfType(eType, false);
			if (stored < totalNeeded) {	
				if (logger.isLoggable(Level.INFO)) {
					logSettlementShortage(vehicle, eType.toString(),
							numLoaded, totalNeeded, settlementNeed, stored);
				}
				return false;
			}
		}

		if (roverInSettlement)
			settlement.addParkedVehicle(vehicle);

		return true;
	}

	/**
	 * Logs the settlement shortage.
	 * 
	 * @param vehicle
	 * @param resource
	 * @param numLoaded
	 * @param needed
	 * @param settlementNeed
	 * @param stored
	 */
	private final static void logSettlementShortage(Vehicle vehicle, String resource,
									double numLoaded, double needed, double settlementNeed, double stored) {
		StringBuilder msg = new StringBuilder();
		msg.append("Not having enough ")
			.append(resource) 
			.append("; Loaded: ").append(numLoaded) 
			.append("; Needed: ").append(needed)
			.append("; Settlement's need: ").append(settlementNeed)
			.append("; Settlement's stored: ").append(stored);
		logger.info(vehicle, msg.toString());
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
		if (resource == ResourceUtil.oxygenID)
			amountPersonPerSol = personConfig.getNominalO2ConsumptionRate();
		else if (resource == ResourceUtil.waterID)
			amountPersonPerSol = personConfig.getWaterConsumptionRate();
		else if (resource == ResourceUtil.foodID)
			amountPersonPerSol = personConfig.getFoodConsumptionRate();
		else {
			// check if this resource is a dessert
			for (AmountResource dessert : PreparingDessert.getArrayOfDessertsAR()) {
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
}
