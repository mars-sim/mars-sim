/**
 * Mars Simulation Project
 * LoadVehicleEVA.java
 * @version 3.04 2013-02-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/** 
 * The LoadVehicleEVA class is a task for loading a vehicle with fuel and supplies 
 * when the vehicle is outside.
 */
public class LoadVehicleEVA extends EVAOperation implements Serializable {

    private static Logger logger = Logger.getLogger(LoadVehicleEVA.class.getName());
    
    // Comparison to indicate a small but non-zero amount.
    private static final double SMALL_AMOUNT_COMPARISON = .0000001D;
    
    // Task phase
    private static final String LOADING = "Loading";

    // The amount of resources (kg) one person of average strength can load per millisol.
    private static double LOAD_RATE = 20D;

    // Data members
    private Vehicle vehicle;  // The vehicle that needs to be loaded.
    private Settlement settlement; // The person's settlement.
    private Map<Resource, Number> resources; // Resources needed to load.
    private Map<Class, Integer> equipment; // Equipment needed to load.
    private Airlock airlock; // Airlock to be used for EVA.
    
    /**
     * Constructor
     * @param person the person performing the task.
     */
    public LoadVehicleEVA(Person person) {
        // Use Task constructor
        super("Loading vehicle EVA", person);
        
        // Get an available airlock.
        airlock = getAvailableAirlock(person);
        if (airlock == null) endTask();
        
        VehicleMission mission = getMissionNeedingLoading();
        if (mission != null) {
            vehicle = mission.getVehicle();
            setDescription("Loading " + vehicle.getName());
            resources = mission.getResourcesToLoad();
            equipment = mission.getEquipmentToLoad();
            settlement = person.getSettlement();
            if (settlement == null) endTask();
            
            // End task if vehicle not available.
            if (vehicle == null) endTask();    
            
            // Initialize task phase
            addPhase(LOADING);
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
     */
    public LoadVehicleEVA(Person person, Vehicle vehicle, Map<Resource, Number> resources, 
            Map<Class, Integer> equipment) {
        // Use Task constructor.
        super("Loading vehicle EVA", person);
        
        setDescription("Loading " + vehicle.getName());
        this.vehicle = vehicle;
        
        if (resources != null) this.resources = new HashMap<Resource, Number>(resources);
        if (equipment != null) this.equipment = new HashMap<Class, Integer>(equipment);
        
        settlement = person.getSettlement();
        
        // Get an available airlock.
        airlock = getAvailableAirlock(person);
        if (airlock == null) endTask();
        
        // Initialize task phase
        addPhase(LOADING);
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
            
            // Check all vehicle missions occurring at the settlement.
            try {
                List<Mission> missions = getAllMissionsNeedingLoading(person.getSettlement());
                result = 50D * missions.size();
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error finding loading missions.", e);
            }
        }

        // Check if an airlock is available
        if (getAvailableAirlock(person) == null) {
            result = 0D;
        }

        // Check if it is night time.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
            if (!surface.inDarkPolarRegion(person.getCoordinates()))
                result = 0D;
        } 
        
        // Crowded settlement modifier
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) {
                result *= 2D;
            }
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
        // Job modifier.
        Job job = person.getMind().getJob();
        if (job != null) {
            result *= job.getStartTaskProbabilityModifier(LoadVehicleEVA.class);        
        }
    
        return result;
    }
    
    /**
     * Gets a list of all embarking vehicle missions at a settlement with vehicle 
     * currently in a garage.
     * @param settlement the settlement.
     * @return list of vehicle missions.
     */
    private static List<Mission> getAllMissionsNeedingLoading(Settlement settlement) {
        
        List<Mission> result = new ArrayList<Mission>();
        
        MissionManager manager = Simulation.instance().getMissionManager();
        Iterator<Mission> i = manager.getMissions().iterator();
        while (i.hasNext()) {
            Mission mission = (Mission) i.next();
            if (mission instanceof VehicleMission) {
                if (VehicleMission.EMBARKING.equals(mission.getPhase())) {
                    VehicleMission vehicleMission = (VehicleMission) mission;
                    if (vehicleMission.hasVehicle()) {
                        Vehicle vehicle = vehicleMission.getVehicle();
                        if (settlement == vehicle.getSettlement()) {
                            if (!vehicleMission.isVehicleLoaded()) {
                                if (BuildingManager.getBuilding(vehicle) == null) {
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
     * @return vehicle mission.
     */
    private VehicleMission getMissionNeedingLoading() {
        
        VehicleMission result = null;
        
        List<Mission> loadingMissions = getAllMissionsNeedingLoading(person.getSettlement());
        
        if (loadingMissions.size() > 0) {
            int index = RandomUtil.getRandomInt(loadingMissions.size() - 1);
            result = (VehicleMission) loadingMissions.get(index);
        }
        
        return result;
    }
    
    /**
     * Gets the vehicle being loaded.
     * @return vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
        if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitEVA(time);
        if (LOADING.equals(getPhase())) return loadingPhase(time);
        if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterEVA(time);
        else return time;
    }
    
    /**
     * Perform the exit airlock phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error exiting the airlock.
     */
    private double exitEVA(double time) {
        
        try {
            time = exitAirlock(time, airlock);
        
            // Add experience points
            addExperience(time);
        }
        catch (Exception e) {
            // Person unable to exit airlock.
            endTask();
        }
        
        if (exitedAirlock) {
            setPhase(LOADING);
            
            // Move person outside next vehicle.
            moveToLoadingLocation();
        }
        return time;
    }
    
    /**
     * Move person outside next to vehicle.
     */
    private void moveToLoadingLocation() {
        Point2D.Double newLocation = null;
        boolean goodLocation = false;
        for (int x = 0; (x < 20) && !goodLocation; x++) {
            Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(vehicle, 1D);
            newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                    boundedLocalPoint.getY(), vehicle);
            goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                    person.getCoordinates());
        }

        person.setXLocation(newLocation.getX());
        person.setYLocation(newLocation.getY());
    }
    
    /**
     * Perform the loading phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) after performing the phase.
     */
    double loadingPhase(double time) {
        
        if (settlement == null) {
            endTask();
            return 0D;
        }
        
        // Determine load rate.
        int strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.STRENGTH);
        double strengthModifier = .1D + (strength * .018D);
        double amountLoading = LOAD_RATE * strengthModifier * time / 4D;
        
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
        try {
            amountLoading = loadResources(amountLoading);
        }
        catch (Exception e) {
            logger.severe(e.getMessage());
        }

        // Put rover back into settlement.
        if (roverInSettlement) sInv.storeUnit(vehicle);
        
        if (isFullyLoaded(resources, equipment, vehicle)) {
            setPhase(ENTER_AIRLOCK);
        }
        
        return 0D;
    }
    
    /**
     * Loads the vehicle with required resources from the settlement.
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @return the remaining amount (kg) the person can load in this time period.
     */
    private double loadResources(double amountLoading) {
        
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
     */
    private double loadAmountResource(double amountLoading, AmountResource resource) {
        
        Inventory vInv = vehicle.getInventory();
        Inventory sInv = settlement.getInventory();
        
        double amountNeededTotal = (Double) resources.get(resource);
        double amountAlreadyLoaded = vInv.getAmountResourceStored(resource, false);
        if (amountAlreadyLoaded < amountNeededTotal) {
            double amountNeeded = amountNeededTotal - amountAlreadyLoaded;
            if (sInv.getAmountResourceStored(resource, false) >= amountNeeded) {
                double remainingCapacity = vInv.getAmountResourceRemainingCapacity(resource, true, false);
                if (remainingCapacity < amountNeeded) {
                    // Deal with miniscule errors.
                    if ((amountNeeded - remainingCapacity) < .00001D) amountNeeded = remainingCapacity;
                    else {
                        endTask();
                        throw new IllegalStateException("Not enough capacity in vehicle for loading resource " + 
                                resource + ": " + amountNeeded + ", remaining capacity: " + remainingCapacity);
                    }
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
        
        //  Return remaining amount that can be loaded by person this time period.
        return amountLoading;
    }
    
    /**
     * Loads the vehicle with an item resource from the settlement.
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @param resource the item resource to be loaded.
     * @return the remaining amount (kg) the person can load in this time period.
     */
    private double loadItemResource(double amountLoading, ItemResource resource) {
        
        Inventory vInv = vehicle.getInventory();
        Inventory sInv = settlement.getInventory();
        
        int numNeededTotal = (Integer) resources.get(resource);
        ItemResource itemResource = resource;
        int numAlreadyLoaded = vInv.getItemResourceNum(itemResource);
        if (numAlreadyLoaded < numNeededTotal) {
            int numNeeded = numNeededTotal - numAlreadyLoaded;
            if ((sInv.getItemResourceNum(itemResource) >= numNeeded) && 
                    (vInv.getRemainingGeneralCapacity(false) >= (numNeeded * itemResource.getMassPerItem()))) {
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
        
        // Return remaining amount that can be loaded by person this time period.
        return amountLoading;
    }
    
    /**
     * Loads the vehicle with required equipment from the settlement.
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @return the remaining amount (kg) the person can load in this time period.
     */
    private double loadEquipment(double amountLoading) {
        
        Inventory vInv = vehicle.getInventory();
        Inventory sInv = settlement.getInventory();
        
        Iterator<Class> iE = equipment.keySet().iterator();
        while (iE.hasNext() && (amountLoading > 0D)) {
            Class equipmentType = iE.next();
            int numNeededTotal = (Integer) equipment.get(equipmentType);
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
                        if (eInv != null) isEmpty = eq.getInventory().isEmpty(false);
                        
                        if (isEmpty) {
                            if (vInv.canStoreUnit(eq, false)) {
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
                Collection<Unit> units = vInv.findAllUnitsOfClass(equipmentType);
                Object[] array = units.toArray();
                
                for (int x = 0; x < numToRemove; x++) {
                    Equipment eq = (Equipment) array[x];
                    vInv.retrieveUnit(eq);
                    sInv.storeUnit(eq);
                }
                
                array = null;
            }
        }
        
        // Return remaining amount that can be loaded by person this time period.
        return amountLoading;
    }

    /** 
     * Checks if there are enough supplies in the settlement's stores to supply trip.
     * @param settlement the settlement the vehicle is at.
     * @param resources a map of resources required for the trip.
     * @param equipment a map of equipment required for the trip.
     * @param vehicleCrewNum the number of people in the vehicle crew.
     * @param tripTime the estimated time for the trip (millisols).
     * @return true if enough supplies
     */
    public static boolean hasEnoughSupplies(Settlement settlement, Vehicle vehicle, Map <Resource, Number> resources, 
            Map<Class, Integer> equipment, int vehicleCrewNum, double tripTime) {
        
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
                double amountNeeded = (Double) resources.get(resource);
                double remainingSettlementAmount = getRemainingSettlementAmount(settlement, vehicleCrewNum, 
                        (AmountResource) resource, tripTime);
                double amountLoaded = vInv.getAmountResourceStored((AmountResource) resource, false);
                double totalNeeded = amountNeeded + remainingSettlementAmount - amountLoaded;
                if (inv.getAmountResourceStored((AmountResource) resource, false) < totalNeeded) {
                    double stored = inv.getAmountResourceStored((AmountResource) resource, false);
                    if (logger.isLoggable(Level.INFO)) 
                        logger.info(resource.getName() + " needed: " + totalNeeded + " stored: " + stored);
                    enoughSupplies = false;
                }
            }
            else if (resource instanceof ItemResource) {
                int numNeeded = (Integer) resources.get(resource);
                int remainingSettlementNum = getRemainingSettlementNum(settlement, vehicleCrewNum, 
                        (ItemResource) resource);
                int numLoaded = vInv.getItemResourceNum((ItemResource) resource);
                int totalNeeded = numNeeded + remainingSettlementNum - numLoaded;
                if (inv.getItemResourceNum((ItemResource) resource) < totalNeeded) {
                    int stored = inv.getItemResourceNum((ItemResource) resource);
                    if (logger.isLoggable(Level.INFO)) 
                        logger.info(resource.getName() + " needed: " + totalNeeded + " stored: " + stored);
                    enoughSupplies = false;
                }
            }
            else throw new IllegalStateException("Unknown resource type: " + resource);
        }
        
        // Check if there is enough equipment at the settlement.
        Iterator<Class> iE = equipment.keySet().iterator();
        while (iE.hasNext()) {
            Class equipmentType = iE.next();
            int numNeeded = (Integer) equipment.get(equipmentType);
            int remainingSettlementNum = getRemainingSettlementNum(settlement, vehicleCrewNum, equipmentType);
            int numLoaded = vInv.findNumUnitsOfClass(equipmentType);
            int totalNeeded = numNeeded + remainingSettlementNum - numLoaded;
            if (inv.findNumEmptyUnitsOfClass(equipmentType, false) < totalNeeded) {
                int stored = inv.findNumEmptyUnitsOfClass(equipmentType, false);
                if (logger.isLoggable(Level.INFO)) 
                    logger.info(equipmentType + " needed: " + totalNeeded + " stored: " + stored);
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
     */
    private static double getRemainingSettlementAmount(Settlement settlement, int vehicleCrewNum,
            AmountResource resource, double tripTime) {
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
     */
    private static int getRemainingSettlementNum(Settlement settlement, int vehicleCrewNum,
            ItemResource resource) {
        // No item resources required at settlement at this time.
        return 0;
    }
    
    /**
     * Gets the number of an equipment type that should remain at the settlement.
     * @param settlement the settlement
     * @param vehicleCrewNum the number of crew leaving on the vehicle.
     * @param equipmentType the equipment type class.
     * @return remaining number.
     */
    private static int getRemainingSettlementNum(Settlement settlement, int vehicleCrewNum, 
            Class equipmentType) {
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
     */
    public static boolean enoughCapacityForSupplies(Map<Resource, Number> resources, 
            Map<Class, Integer> equipment, Vehicle vehicle, Settlement settlement) {
        
        boolean sufficientCapacity = true;
        
        // Create vehicle inventory clone.
        Inventory inv = vehicle.getInventory().clone(null);
  
        try {
            // Add equipment clones.
            Iterator<Class> i = equipment.keySet().iterator();
            while (i.hasNext()) {
                Class equipmentType = i.next();
                int num = (Integer) equipment.get(equipmentType);
                Coordinates defaultLoc = new Coordinates(0D, 0D);
                for (int x = 0; x < num; x++) 
                    inv.storeUnit(EquipmentFactory.getEquipment(equipmentType, defaultLoc, false));
            }
            
            // Add all resources.
            Iterator<Resource> j = resources.keySet().iterator();
            while (j.hasNext()) {
                Resource resource = j.next();
                if (resource instanceof AmountResource) {
                    double amount = (Double) resources.get(resource);
                    inv.storeAmountResource((AmountResource) resource, amount, true);
                }
                else {
                    int num = (Integer) resources.get(resource);
                    inv.storeItemResources((ItemResource) resource, num);
                }
            }
        }
        catch (Exception e) {
            logger.info(e.getMessage());
            sufficientCapacity = false;
        }
        
        return sufficientCapacity;
    }

    /** 
     * Checks if the vehicle is fully loaded with supplies.
     * @return true if vehicle is fully loaded.
     */
    public static boolean isFullyLoaded(Map<Resource, Number> resources, Map<Class, Integer> equipment,
            Vehicle vehicle) {
        
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
     */
    private static boolean isFullyLoadedWithResources(Map<Resource, Number> resources, 
            Vehicle vehicle) {
        
        if (vehicle == null) throw new IllegalArgumentException("vehicle is null");
        
        boolean sufficientSupplies = true;
        Inventory inv = vehicle.getInventory();

        Iterator<Resource> iR = resources.keySet().iterator();
        while (iR.hasNext() && sufficientSupplies) {
            Resource resource = iR.next();
            if (resource instanceof AmountResource) {
                double amount = (Double) resources.get(resource);
                double storedAmount = inv.getAmountResourceStored((AmountResource) resource, false);
                if (storedAmount < (amount - SMALL_AMOUNT_COMPARISON)) {
                    sufficientSupplies = false;
                }
            }
            else if (resource instanceof ItemResource) {
                int num = (Integer) resources.get(resource);
                if (inv.getItemResourceNum((ItemResource) resource) < num) sufficientSupplies = false;
            }
            else throw new IllegalStateException("Unknown resource type: " + resource);
        }
        
        return sufficientSupplies;
    }
    
    /**
     * Checks if the vehicle is fully loaded with resources.
     * @param equipment the equipment map.
     * @param vehicle the vehicle.
     * @return true if vehicle is full loaded.
     */
    private static boolean isFullyLoadedWithEquipment(Map<Class, Integer> equipment, Vehicle vehicle) {
        
        if (vehicle == null) throw new IllegalArgumentException("vehicle is null");
        
        boolean sufficientSupplies = true;
        Inventory inv = vehicle.getInventory();
        
        Iterator<Class> iE = equipment.keySet().iterator();
        while (iE.hasNext() && sufficientSupplies) {
            Class equipmentType = iE.next();
            int num = (Integer) equipment.get(equipmentType);
            if (inv.findNumUnitsOfClass(equipmentType) < num) sufficientSupplies = false;
        }
        
        return sufficientSupplies;
    }
    
    /**
     * Perform the enter airlock phase of the task.
     * @param time amount of time to perform the phase
     * @return time remaining after performing the phase
     * @throws Exception if error entering airlock.
     */
    private double enterEVA(double time) {
        time = enterAirlock(time, airlock);
        
        // Add experience points
        addExperience(time);
        
        if (enteredAirlock) endTask();
        return time;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
        return EVAOperationsSkill; 
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(2);
        results.add(Skill.EVA_OPERATIONS);
        return results;
    }

    @Override
    protected void addExperience(double time) {

        // Add experience to "EVA Operations" skill.
        // (1 base experience point per 100 millisols of time spent)
        double evaExperience = time / 100D;
        
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(Skill.EVA_OPERATIONS, evaExperience);
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        vehicle = null;
        settlement = null;
        if (resources != null) resources.clear();
        resources = null;
        if (equipment != null) equipment.clear();
        equipment = null;
        airlock = null;
    }
}