/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.85 2008-09-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.equipment.Equipment;
import org.mars_sim.msp.simulation.equipment.EquipmentFactory;
import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PhysicalCondition;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManager;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.resource.Resource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/** 
 * The LoadVehicle class is a task for loading a vehicle with fuel and supplies.
 */
public class LoadVehicle extends Task implements Serializable {

	private static String CLASS_NAME = 
	    "org.mars_sim.msp.simulation.person.ai.task.LoadVehicle";
	private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Task phase
	private static final String LOADING = "Loading";
	
	private static final double STRESS_MODIFIER = .1D; // The stress modified per millisol.

    // The amount of resources (kg) one person of average strength can load per millisol.
    private static double LOAD_RATE = 20D;
    
    // The duration of the loading task (millisols).
    private static double DURATION = 100D;

    // Data members
    private Vehicle vehicle;  // The vehicle that needs to be loaded.
    private Settlement settlement; // The person's settlement.
    private Map<Resource, Number> resources; // Resources needed to load.
    private Map<Class, Integer> equipment; // Equipment needed to load.
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error creating task.
     */
    public LoadVehicle(Person person) throws Exception {
    	// Use Task constructor
    	super("Loading vehicle", person, true, false, STRESS_MODIFIER, true, DURATION);
    	
    	VehicleMission mission = getMissionNeedingLoading();
    	if (mission != null) {
    		vehicle = mission.getVehicle();
    		setDescription("Loading " + vehicle.getName());
    		resources = mission.getResourcesToLoad();
    		equipment = mission.getEquipmentToLoad();
    		settlement = person.getSettlement();
    		if (settlement == null) endTask();
    		
    		// Initialize task phase
            addPhase(LOADING);
            setPhase(LOADING);
    	}
    	else {
    		endTask();
    	}
    }
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @param vehicle the vehicle to be loaded.
     * @param resources a map of resources to be loaded. 
     * @param equipment a map of equipment to be loaded.
     * @throws Exception if error creating task.
     */
    public LoadVehicle(Person person, Vehicle vehicle, Map<Resource, Number> resources, 
    		Map<Class, Integer> equipment) throws Exception {
    	// Use Task constructor.
    	super("Loading vehicle", person, true, false, STRESS_MODIFIER, true, DURATION);
    	
    	setDescription("Loading " + vehicle.getName());
        this.vehicle = vehicle;
        
        if (resources != null) this.resources = new HashMap<Resource, Number>(resources);
        if (equipment != null) this.equipment = new HashMap<Class, Integer>(equipment);
        
        // tripProportion = tripDistance / vehicle.getRange();
        
        settlement = person.getSettlement();
        
        // Initialize task phase
        addPhase(LOADING);
        setPhase(LOADING);
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
        	
        	// Check all vehicle missions occuring at the settlement.
        	try {
        		List missions = getAllMissionsNeedingLoading(person.getSettlement());
        		result = 100D * missions.size();
        	}
        	catch (Exception e) {
        	    logger.log(Level.SEVERE, "Error finding loading missions.", e);
        	}
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier.
        Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(LoadVehicle.class);        
	
        return result;
    }
    
    /**
     * Gets a list of all embarking vehicle missions at a settlement.
     * @param settlement the settlement.
     * @return list of vehicle missions.
     * @throws Exception if error finding missions.
     */
    private static List<Mission> getAllMissionsNeedingLoading(Settlement settlement) throws Exception {
    	
    	List<Mission> result = new ArrayList<Mission>();
    	
    	MissionManager manager = Simulation.instance().getMissionManager();
    	Iterator i = manager.getMissions().iterator();
    	while (i.hasNext()) {
    		Mission mission = (Mission) i.next();
    		if (mission instanceof VehicleMission) {
    			if (VehicleMission.EMBARKING.equals(mission.getPhase())) {
    				VehicleMission vehicleMission = (VehicleMission) mission;
    				if (vehicleMission.hasVehicle()) {
    					Vehicle vehicle = vehicleMission.getVehicle();
    					if (settlement == vehicle.getSettlement()) {
    						if (!vehicleMission.isVehicleLoaded()) result.add(vehicleMission);
    					}
    				}
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Gets a random vehicle mission loading at the settlement.
     * @return vehicle mission.
     * @throws Exception if error finding vehicle mission.
     */
    private VehicleMission getMissionNeedingLoading() throws Exception {
    	
    	VehicleMission result = null;
    	
    	List loadingMissions = getAllMissionsNeedingLoading(person.getSettlement());
    	
    	if (loadingMissions.size() > 0) {
    		int index = RandomUtil.getRandomInt(loadingMissions.size() - 1);
    		result = (VehicleMission) loadingMissions.get(index);
    	}
    	
    	return result;
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (LOADING.equals(getPhase())) return loadingPhase(time);
    	else return time;
    }
    
    /**
     * Perform the loading phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) after performing the phase.
     * @throws Exception if error in loading phase.
     */
    double loadingPhase(double time) throws Exception {
    	
    	if (settlement == null) {
    		endTask();
    		return 0D;
    	}
    	
        // Determine load rate.
        int strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.STRENGTH);
        double strengthModifier = (double) strength / 50D;
        double amountLoading = LOAD_RATE * strengthModifier * time;
        
        // If vehicle is not in a garage, load rate is reduced.
        Building garage = BuildingManager.getBuilding(vehicle);
        if (garage == null) amountLoading /= 4D;
        
        // Temporarily remove rover from settlement so that inventory doesn't get mixed in.
        Inventory sInv = settlement.getInventory();
        boolean roverInSettlement = false;
        if (sInv.containsUnit(vehicle)) {
            roverInSettlement = true;
            sInv.retrieveUnit(vehicle);
        }
        
        // Load equipment
        if (amountLoading > 0D) amountLoading = loadEquipment(amountLoading);
        
        // Load resources
        amountLoading = loadResources(amountLoading);

        // Put rover back into settlement.
        if (roverInSettlement) sInv.storeUnit(vehicle);
        
        if (isFullyLoaded(resources, equipment, vehicle)) endTask();
        
        return 0D;
    }
    
    /**
     * Loads the vehicle with required resources from the settlement.
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @return the remaining amount (kg) the person can load in this time period.
     * @throws Exception if problem loading resources.
     */
    private double loadResources(double amountLoading) throws Exception {
        
        Iterator<Resource> iR = resources.keySet().iterator();
        while (iR.hasNext() && (amountLoading > 0D)) {
        	Resource resource = iR.next();
        	if (resource instanceof AmountResource) {
        		// Load amount resources
        		amountLoading = loadAmountResource(amountLoading, (AmountResource) resource);
        	}
        	else if (resource instanceof ItemResource) {
        		// Load item resources
        		amountLoading = loadItemResource(amountLoading, (ItemResource) resource);
        	}
        }
        
        // Return remaining amount that can be loaded by person this time period.
        return amountLoading;
    }
    
    /**
     * Loads the vehicle with an amount resource from the settlement.
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @param resource the amount resource to be loaded.
     * @return the remaining amount (kg) the person can load in this time period.
     * @throws Exception if problem loading resource.
     */
    private double loadAmountResource(double amountLoading, AmountResource resource) throws Exception {
    	
    	Inventory vInv = vehicle.getInventory();
        Inventory sInv = settlement.getInventory();
        /*
        boolean roverInSettlement = false;
        if (sInv.containsUnit(vehicle)) {
        	roverInSettlement = true;
        	sInv.retrieveUnit(vehicle);
        }
        */
    	double amountNeededTotal = ((Double) resources.get(resource)).doubleValue();
		double amountAlreadyLoaded = vInv.getAmountResourceStored(resource);
		if (amountAlreadyLoaded < amountNeededTotal) {
			double amountNeeded = amountNeededTotal - amountAlreadyLoaded;
			if (sInv.getAmountResourceStored(resource) >= amountNeeded) {
				double remainingCapacity = vInv.getAmountResourceRemainingCapacity(resource, true);
				if (remainingCapacity < amountNeeded) {
                    endTask();
                    throw new Exception("Not enough capacity in vehicle for loading resource " + resource + 
                            ": " + amountNeeded + ", remaining capacity: " + remainingCapacity);
                }
				double resourceAmount = amountNeeded;
				if (amountNeeded > amountLoading) resourceAmount = amountLoading;
				try {
					sInv.retrieveAmountResource(resource, resourceAmount);
					vInv.storeAmountResource(resource, resourceAmount, true);
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
				}
				amountLoading -= resourceAmount;
			}
			else endTask();
		}
		else if (amountAlreadyLoaded > amountNeededTotal) {
			// In case vehicle wasn't fully unloaded first.
			double amountToRemove = amountAlreadyLoaded - amountNeededTotal;
			try {
				vInv.retrieveAmountResource(resource, amountToRemove);
				sInv.storeAmountResource(resource, amountToRemove, true);
			}
			catch (Exception e) {}
		}
		
		// if (roverInSettlement) sInv.storeUnit(vehicle);
		
		//  Return remaining amount that can be loaded by person this time period.
		return amountLoading;
    }
    
    /**
     * Loads the vehicle with an item resource from the settlement.
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @param resource the item resource to be loaded.
     * @return the remaining amount (kg) the person can load in this time period.
     * @throws Exception if problem loading resource.
     */
    private double loadItemResource(double amountLoading, ItemResource resource) throws Exception {
    	
    	Inventory vInv = vehicle.getInventory();
        Inventory sInv = settlement.getInventory();
        
        /*
        boolean roverInSettlement = false;
        if (sInv.containsUnit(vehicle)) {
        	roverInSettlement = true;
        	sInv.retrieveUnit(vehicle);
        }
        */
        int numNeededTotal = ((Integer) resources.get(resource)).intValue();
		ItemResource itemResource = (ItemResource) resource;
		int numAlreadyLoaded = vInv.getItemResourceNum(itemResource);
		if (numAlreadyLoaded < numNeededTotal) {
			int numNeeded = numNeededTotal - numAlreadyLoaded;
			if ((sInv.getItemResourceNum(itemResource) >= numNeeded) && 
					(vInv.getRemainingGeneralCapacity() >= (numNeeded * itemResource.getMassPerItem()))) {
				int resourceNum = (int) (amountLoading / itemResource.getMassPerItem());
				if (resourceNum < 1) resourceNum = 1;
				if (resourceNum > numNeeded) resourceNum = numNeeded;
				sInv.retrieveItemResources(itemResource, resourceNum);
				vInv.storeItemResources(itemResource, resourceNum);
				amountLoading -= (resourceNum * itemResource.getMassPerItem());
				if (amountLoading < 0D) amountLoading = 0D;
			}
			else endTask();
		}
		else if (numAlreadyLoaded > numNeededTotal) {
			// In case vehicle wasn't fully unloaded first.
			int numToRemove = numAlreadyLoaded - numNeededTotal;
			try {
				vInv.retrieveItemResources(resource, numToRemove);
				sInv.storeItemResources(resource, numToRemove);
			}
			catch (Exception e) {}
		}
		
		// if (roverInSettlement) sInv.storeUnit(vehicle);
		
		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
    }
    
    /**
     * Loads the vehicle with required equipment from the settlement.
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @return the remaining amount (kg) the person can load in this time period.
     * @throws Exception if problem loading equipment.
     */
    private double loadEquipment(double amountLoading) throws Exception {
    	
    	Inventory vInv = vehicle.getInventory();
        Inventory sInv = settlement.getInventory();
        /*
        boolean roverInSettlement = false;
        if (sInv.containsUnit(vehicle)) {
        	roverInSettlement = true;
        	sInv.retrieveUnit(vehicle);
        }
        */
        Iterator<Class> iE = equipment.keySet().iterator();
        while (iE.hasNext() && (amountLoading > 0D)) {
        	Class equipmentType = iE.next();
        	int numNeededTotal = ((Integer) equipment.get(equipmentType)).intValue();
        	int numAlreadyLoaded = vInv.findNumUnitsOfClass(equipmentType);
        	if (numAlreadyLoaded < numNeededTotal) {
        		int numNeeded = numNeededTotal - numAlreadyLoaded;
        		Collection units = sInv.findAllUnitsOfClass(equipmentType);
        		Object[] array  = units.toArray();
        		
        		if (units.size() >= numNeeded) {
        			int loaded = 0;
        			for (int x = 0; (x < units.size()) && (loaded < numNeeded) && (amountLoading > 0D); x++) {
        				Equipment eq = (Equipment) array[x];
        				
        				boolean isEmpty = true;
        				Inventory eInv = eq.getInventory();
        				if (eInv != null) isEmpty = eq.getInventory().isEmpty();
        				
        				if (isEmpty) {
        					if (vInv.canStoreUnit(eq)) {
        						sInv.retrieveUnit(eq);
        						vInv.storeUnit(eq);
        						amountLoading -= eq.getMass();
        						if (amountLoading < 0D) amountLoading = 0D;
        						loaded++;
        					}
        					else endTask();
        				}
        			}
        			
        			array = null;
        		}
        		else {
        		    endTask();
        		}
        	}
    		else if (numAlreadyLoaded > numNeededTotal) {
    			// In case vehicle wasn't fully unloaded first.
    			int numToRemove = numAlreadyLoaded - numNeededTotal;
    			Collection units = vInv.findAllUnitsOfClass(equipmentType);
    			Object[] array = units.toArray();
    			
    			for (int x = 0; x < numToRemove; x++) {
    				Equipment eq = (Equipment) array[x];
    				vInv.retrieveUnit(eq);
    				sInv.storeUnit(eq);
    			}
    			
    			array = null;
    		}
        }
        
        // if (roverInSettlement) sInv.storeUnit(vehicle);
        
		// Return remaining amount that can be loaded by person this time period.
		return amountLoading;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// This task adds no experience.
	}

    /** 
     * Checks if there are enough supplies in the settlement's stores to supply trip.
     * @param settlement the settlement the vehicle is at.
     * @param resources a map of resources required for the trip.
     * @param equipment a map of equipment required for the trip.
     * @param vehicleCrewNum the number of people in the vehicle crew.
     * @param tripTime the estimated time for the trip (millisols).
     * @return true if enough supplies
     * @throws Exception if error checking supplies.
     */
    public static boolean hasEnoughSupplies(Settlement settlement, Vehicle vehicle, Map <Resource, Number> resources, 
    		Map<Class, Integer> equipment, int vehicleCrewNum, double tripTime) throws Exception {
    	
    	// Check input parameters.
    	if (settlement == null) throw new IllegalArgumentException("settlement is null");
    	
        boolean enoughSupplies = true;
        Inventory inv = settlement.getInventory();
        Inventory vInv = vehicle.getInventory();
        
        boolean roverInSettlement = false;
        if (inv.containsUnit(vehicle)) {
        	roverInSettlement = true;
        	inv.retrieveUnit(vehicle);
        }
        
        // Check if there are enough resources at the settlement.
        Iterator<Resource> iR = resources.keySet().iterator();
        while (iR.hasNext()) {
        	Resource resource = iR.next();
        	if (resource instanceof AmountResource) {
        		double amountNeeded = ((Double) resources.get(resource)).doubleValue();
        		double remainingSettlementAmount = getRemainingSettlementAmount(settlement, vehicleCrewNum, (AmountResource) resource, tripTime);
        		double amountLoaded = vInv.getAmountResourceStored((AmountResource) resource);
        		double totalNeeded = amountNeeded + remainingSettlementAmount - amountLoaded;
        		if (inv.getAmountResourceStored((AmountResource) resource) < totalNeeded) {
        			double stored = inv.getAmountResourceStored((AmountResource) resource);
        			if (logger.isLoggable(Level.FINEST)) 
        				logger.finest(resource.getName() + " needed: " + totalNeeded + " stored: " + stored);
        			enoughSupplies = false;
        		}
        	}
        	else if (resource instanceof ItemResource) {
        		int numNeeded = ((Integer) resources.get(resource)).intValue();
        		int remainingSettlementNum = getRemainingSettlementNum(settlement, vehicleCrewNum, (ItemResource) resource);
        		int numLoaded = vInv.getItemResourceNum((ItemResource) resource);
        		int totalNeeded = numNeeded + remainingSettlementNum - numLoaded;
        		if (inv.getItemResourceNum((ItemResource) resource) < totalNeeded) {
        			int stored = inv.getItemResourceNum((ItemResource) resource);
        			if (logger.isLoggable(Level.FINEST)) 
        				logger.finest(resource.getName() + " needed: " + totalNeeded + " stored: " + stored);
        			enoughSupplies = false;
        		}
        	}
        	else throw new Exception("Unknown resource type: " + resource);
        }
        
        // Check if there is enough equipment at the settlement.
        Iterator<Class> iE = equipment.keySet().iterator();
        while (iE.hasNext()) {
        	Class equipmentType = iE.next();
        	int numNeeded = ((Integer) equipment.get(equipmentType)).intValue();
        	int remainingSettlementNum = getRemainingSettlementNum(settlement, vehicleCrewNum, equipmentType);
        	int numLoaded = vInv.findNumUnitsOfClass(equipmentType);
    		int totalNeeded = numNeeded + remainingSettlementNum - numLoaded;
        	if (inv.findNumEmptyUnitsOfClass(equipmentType) < totalNeeded) {
        		int stored = inv.findNumEmptyUnitsOfClass(equipmentType);
    			if (logger.isLoggable(Level.FINEST)) 
    				logger.finest(equipmentType + " needed: " + totalNeeded + " stored: " + stored);
        		enoughSupplies = false;
        	}
        }
        
        if (roverInSettlement) inv.storeUnit(vehicle);

        return enoughSupplies;
    }
    
    /**
     * Gets the amount of an amount resource that should remain at the settlement.
     * @param settlement the settlement
     * @param vehicleCrewNum the number of crew leaving on the vehicle.
     * @param resource the amount resource
     * @param double tripTime the estimated trip time (millisols).
     * @return remaining amount (kg)
     * @throws Exception if error getting the remaining amount.
     */
    private static final double getRemainingSettlementAmount(Settlement settlement, int vehicleCrewNum, 
    		AmountResource resource, double tripTime) throws Exception {
    	int remainingPeopleNum = settlement.getCurrentPopulationNum() - vehicleCrewNum;
    	double amountPersonPerSol = 0D;
    	double tripTimeSols = tripTime / 1000D;
    	
    	// Only life support resources are required at settlement at this time.
    	AmountResource oxygen = AmountResource.findAmountResource("oxygen");
    	AmountResource water = AmountResource.findAmountResource("water");
    	AmountResource food = AmountResource.findAmountResource("food");
    	if (resource.equals(oxygen)) amountPersonPerSol = PhysicalCondition.getOxygenConsumptionRate();
    	else if (resource.equals(water)) amountPersonPerSol = PhysicalCondition.getWaterConsumptionRate();
    	else if (resource.equals(food)) amountPersonPerSol = PhysicalCondition.getFoodConsumptionRate();
    	
    	return remainingPeopleNum * (amountPersonPerSol * tripTimeSols);
    }
    
    /**
     * Gets the number of an item resource that should remain at the settlement.
     * @param settlement the settlement
     * @param vehicleCrewNum the number of crew leaving on the vehicle.
     * @param resource the item resource
     * @return remaining number
     * @throws Exception if error getting the remaining number.
     */
    private static final int getRemainingSettlementNum(Settlement settlement, int vehicleCrewNum, 
    		ItemResource resource) throws Exception {
    	// No item resources required at settlement at this time.
    	return 0;
    }
    
    /**
     * Gets the number of an equipment type that should remain at the settlement.
     * @param settlement the settlement
     * @param vehicleCrewNum the number of crew leaving on the vehicle.
     * @param equipmentType the equipment type class.
     * @return remaining number.
     * @throws Exception if error getting the remaining number.
     */
    private static final int getRemainingSettlementNum(Settlement settlement, int vehicleCrewNum, Class equipmentType) 
    		throws Exception {
    	int remainingPeopleNum = settlement.getCurrentPopulationNum() - vehicleCrewNum;
    	// Leave one EVA suit for every four remaining people at settlement (min 1).
    	if (equipmentType == EVASuit.class) {
    		int minSuits = remainingPeopleNum / 4;
    		if (minSuits == 0) minSuits = 1;
    		return minSuits;
    	}
    	else return 0;
    }
    
    /**
     * Checks if a vehicle has enough storage capacity for the supplies needed on the trip.
     * @param resources a map of the resources required.
     * @param equipment a map of the equipment types and numbers needed.
     * @param vehicle the vehicle to check.
     * @param settlement the settlement to disembark from.
     * @return true if vehicle can carry supplies.
     * @throws Exception if error
     */
    public static boolean enoughCapacityForSupplies(Map<Resource, Number> resources, Map<Class, Integer> equipment, 
    		Vehicle vehicle, Settlement settlement) throws Exception {
    	
    	boolean sufficientCapacity = true;
    	
    	// Create vehicle inventory clone.
    	Inventory inv = vehicle.getInventory().clone(null);
  
    	try {
    		// Add equipment clones.
    		Iterator<Class> i = equipment.keySet().iterator();
    		while (i.hasNext()) {
    			Class equipmentType = i.next();
    			int num = ((Integer) equipment.get(equipmentType)).intValue();
    			Coordinates defaultLoc = new Coordinates(0D, 0D);
    			for (int x = 0; x < num; x++) 
    				inv.storeUnit(EquipmentFactory.getEquipment(equipmentType, defaultLoc, false));
    		}
    		
    		// Add all resources.
    		Iterator j = resources.keySet().iterator();
    		while (j.hasNext()) {
    			Resource resource = (Resource) j.next();
    			if (resource instanceof AmountResource) {
    				double amount = ((Double) resources.get(resource)).doubleValue();
    				inv.storeAmountResource((AmountResource) resource, amount, true);
    			}
    			else {
    				int num = ((Integer) resources.get(resource)).intValue();
    				inv.storeItemResources((ItemResource) resource, num);
    			}
    		}
    	}
    	catch (InventoryException e) {
    		sufficientCapacity = false;
    	}
    	
    	return sufficientCapacity;
    }

    /** 
     * Checks if the vehicle is fully loaded with supplies.
     * @return true if vehicle is fully loaded.
     * @throws Exception if error checking supplies.
     */
    public static final boolean isFullyLoaded(Map<Resource, Number> resources, Map<Class, Integer> equipment, 
    		Vehicle vehicle) throws Exception {
    	
    	boolean sufficientSupplies = true;

        // Check if there are enough resources in the vehicle.
        sufficientSupplies = isFullyLoadedWithResources(resources, vehicle);
        
        // Check if there is enough equipment in the vehicle.
        if (sufficientSupplies) sufficientSupplies = isFullyLoadedWithEquipment(equipment, vehicle);

        return sufficientSupplies;
    }
    
    /**
     * Checks if the vehicle is fully loaded with resources.
     * @param resources the resource map.
     * @param vehicle the vehicle.
     * @return true if vehicle is loaded.
     * @throws Exception if error checking vehicle.
     */
    private static final boolean isFullyLoadedWithResources(Map<Resource, Number> resources, Vehicle vehicle) 
    		throws Exception {
    	
    	if (vehicle == null) throw new IllegalArgumentException("vehicle is null");
    	
    	boolean sufficientSupplies = true;
        Inventory inv = vehicle.getInventory();

        Iterator<Resource> iR = resources.keySet().iterator();
        while (iR.hasNext() && sufficientSupplies) {
        	Resource resource = iR.next();
        	if (resource instanceof AmountResource) {
        		double amount = ((Double) resources.get(resource)).doubleValue();
        		if (inv.getAmountResourceStored((AmountResource) resource) < amount) sufficientSupplies = false;
        	}
        	else if (resource instanceof ItemResource) {
        		int num = ((Integer) resources.get(resource)).intValue();
        		if (inv.getItemResourceNum((ItemResource) resource) < num) sufficientSupplies = false;
        	}
        	else throw new Exception("Unknown resource type: " + resource);
        }
        
        return sufficientSupplies;
    }
    
    /**
     * Checks if the vehicle is fully loaded with resources.
     * @param equipment the equipment map.
     * @param vehicle the vehicle.
     * @return true if vehicle is full loaded.
     * @throws Exception if error checking vehicle.
     */
    private static final boolean isFullyLoadedWithEquipment(Map<Class, Integer> equipment, Vehicle vehicle) 
    		throws Exception {
    	
    	if (vehicle == null) throw new IllegalArgumentException("vehicle is null");
    	
    	boolean sufficientSupplies = true;
        Inventory inv = vehicle.getInventory();
        
        Iterator<Class> iE = equipment.keySet().iterator();
        while (iE.hasNext() && sufficientSupplies) {
        	Class equipmentType = iE.next();
        	int num = ((Integer) equipment.get(equipmentType)).intValue();
        	if (inv.findNumUnitsOfClass(equipmentType) < num) sufficientSupplies = false;
        }
        
        return sufficientSupplies;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		return 0;	
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		return new ArrayList<String>(0);
	}
}