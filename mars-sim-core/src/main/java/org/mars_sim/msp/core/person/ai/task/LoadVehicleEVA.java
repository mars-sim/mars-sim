/**
 * Mars Simulation Project
 * LoadVehicleAmountResource.java
 * @version 3.1.0 2017-03-22
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
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.robot.RoboticAttributeManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The LoadVehicleEVA class is a task for loading a vehicle with fuel and supplies
 * when the vehicle is outside.
 */
public class LoadVehicleEVA
extends EVAOperation
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(LoadVehicleEVA.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.loadVehicleEVA"); //$NON-NLS-1$

    /** Comparison to indicate a small but non-zero amount. */
    private static final double SMALL_AMOUNT_COMPARISON = .0000001D;

    /** Task phases. */
    private static final TaskPhase LOADING = new TaskPhase(Msg.getString(
            "Task.phase.loading")); //$NON-NLS-1$

    /** The amount of resources (kg) one person of average strength can load per millisol. */
    private static double LOAD_RATE = 20D;

    private static double WATER_NEED = 40D;
    
    private static double OXYGEN_NEED = 10D;
    
    // Data members
    /** The vehicle that needs to be loaded. */
    private Vehicle vehicle;
    /** The person's settlement. */
    private Settlement settlement;
    /** Resources required to load. */
    private Map<Integer, Number> requiredResources;
    /** Resources desired to load but not required. */
    private Map<Integer, Number> optionalResources;
    /** Equipment required to load. */
    private Map<Integer, Integer> requiredEquipment;
    /** Equipment desired to load but not required. */
    private Map<Integer, Integer> optionalEquipment;

    
	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;
//	private static int foodID = ResourceUtil.foodID;
//	private static int methaneID = ResourceUtil.methaneID;
	
    
    /**
     * Constructor.
     * @param person the person performing the task.
     */
    public LoadVehicleEVA(Person person) {
        // Use Task constructor
        super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D);

        List<Rover> roversNeedingEVASuits = getRoversNeedingEVASuits(person.getSettlement());
        if (roversNeedingEVASuits.size() > 0) {
            int roverIndex = RandomUtil.getRandomInt(roversNeedingEVASuits.size() - 1);
            vehicle = roversNeedingEVASuits.get(roverIndex);
            setDescription(Msg.getString("Task.description.loadVehicleEVA.detail",
                    vehicle.getName())); //$NON-NLS-1$
            requiredResources = new HashMap<Integer, Number>(2);
            requiredResources.put(waterID, WATER_NEED); 
            requiredResources.put(oxygenID, OXYGEN_NEED);
            optionalResources = new HashMap<Integer, Number>(0);
            requiredEquipment = new HashMap<>(1);
            requiredEquipment.put(EquipmentType.str2int(EVASuit.TYPE), 1);
            optionalEquipment = new HashMap<>(0);
            settlement = person.getSettlement();
        }

        VehicleMission mission = getMissionNeedingLoading();
        if ((vehicle == null) && (mission != null)) {
            vehicle = mission.getVehicle();
            setDescription(Msg.getString("Task.description.loadVehicleEVA.detail",
                    vehicle.getName())); //$NON-NLS-1$
            requiredResources = mission.getRequiredResourcesToLoad();
            optionalResources = mission.getOptionalResourcesToLoad();
            requiredEquipment = mission.getRequiredEquipmentToLoad();
            optionalEquipment = mission.getOptionalEquipmentToLoad();
            settlement = person.getSettlement();
        }

        if (vehicle != null) {
            // Determine location for loading.
            Point2D loadingLoc = determineLoadingLocation();
            setOutsideSiteLocation(loadingLoc.getX(), loadingLoc.getY());

            // Initialize task phase
            addPhase(LOADING);
        }
        else {
            endTask();
        }
    }
    public LoadVehicleEVA(Robot robot) {
        // Use Task constructor
        super(NAME, robot, true, RandomUtil.getRandomDouble(50D) + 10D);

        /*
        List<Rover> roversNeedingEVASuits = getRoversNeedingEVASuits(robot.getSettlement());
        if (roversNeedingEVASuits.size() > 0) {
            int roverIndex = RandomUtil.getRandomInt(roversNeedingEVASuits.size() - 1);
            vehicle = roversNeedingEVASuits.get(roverIndex);
            setDescription(Msg.getString("Task.description.loadVehicleEVA.detail",
                    vehicle.getName())); //$NON-NLS-1$
            requiredResources = new HashMap<Resource, Number>(2);
            requiredResources.put(AmountResource.findAmountResource(LifeSupportType.WATER), 40D);
            requiredResources.put(AmountResource.findAmountResource(LifeSupportType.OXYGEN), 10D);
            optionalResources = new HashMap<Resource, Number>(0);
            requiredEquipment = new HashMap<Class, Integer>(1);
            requiredEquipment.put(EVASuit.class, 1);
            optionalEquipment = new HashMap<Class, Integer>(0);
            settlement = robot.getSettlement();
        }

        VehicleMission mission = getMissionNeedingLoading();
        if ((vehicle == null) && (mission != null)) {
            vehicle = mission.getVehicle();
            setDescription(Msg.getString("Task.description.loadVehicleEVA.detail",
                    vehicle.getName())); //$NON-NLS-1$
            requiredResources = mission.getRequiredResourcesToLoad();
            optionalResources = mission.getOptionalResourcesToLoad();
            requiredEquipment = mission.getRequiredEquipmentToLoad();
            optionalEquipment = mission.getOptionalEquipmentToLoad();
            settlement = robot.getSettlement();
        }

        if (vehicle != null) {
            // Determine location for loading.
            Point2D loadingLoc = determineLoadingLocation();
            setOutsideSiteLocation(loadingLoc.getX(), loadingLoc.getY());

            // Initialize task phase
            addPhase(LOADING);
        }
        else {
            endTask();
        }
*/
    }

    /**
     * Constructor
     * @param person the person performing the task.
     * @param vehicle the vehicle to be loaded.
     * @param requiredResources a map of required resources to be loaded.
     * @param optionalResources a map of optional resources to be loaded.
     * @param requiredEquipment a map of required equipment to be loaded.
     * @param optionalEquipment a map of optional equipment to be loaded.
     */
    public LoadVehicleEVA(Person person, Vehicle vehicle, Map<Integer, Number> requiredResources,
            Map<Integer, Number> optionalResources, Map<Integer, Integer> requiredEquipment,
            Map<Integer, Integer> optionalEquipment) {
        // Use Task constructor.
        super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D);

        setDescription(Msg.getString("Task.description.loadVehicleEVA.detail",
                vehicle.getName())); //$NON-NLS-1$
        this.vehicle = vehicle;

        if (requiredResources != null) {
            this.requiredResources = new HashMap<Integer, Number>(requiredResources);
        }
        if (optionalResources != null) {
            this.optionalResources = new HashMap<Integer, Number>(optionalResources);
        }
        if (requiredEquipment != null) {
            this.requiredEquipment = new HashMap<>(requiredEquipment);
        }
        if (optionalEquipment != null) {
            this.optionalEquipment = new HashMap<>(optionalEquipment);
        }

        settlement = person.getSettlement();

        // Determine location for loading.
        Point2D loadingLoc = determineLoadingLocation();
        setOutsideSiteLocation(loadingLoc.getX(), loadingLoc.getY());

        // Initialize task phase
        addPhase(LOADING);
    }

    public LoadVehicleEVA(Robot robot, Vehicle vehicle, Map<Integer, Number> requiredResources,
            Map<Integer, Number> optionalResources, Map<Class<? extends Equipment>, Integer> requiredEquipment,
            Map<Class<? extends Equipment>, Integer> optionalEquipment) {
        // Use Task constructor.
        super(NAME, robot, true, RandomUtil.getRandomDouble(50D) + 10D);

/*
        setDescription(Msg.getString("Task.description.loadVehicleEVA.detail",
                vehicle.getName())); //$NON-NLS-1$
        this.vehicle = vehicle;

        if (requiredResources != null) {
            this.requiredResources = new HashMap<Resource, Number>(requiredResources);
        }
        if (optionalResources != null) {
            this.optionalResources = new HashMap<Resource, Number>(optionalResources);
        }
        if (requiredEquipment != null) {
            this.requiredEquipment = new HashMap<Class, Integer>(requiredEquipment);
        }
        if (optionalEquipment != null) {
            this.optionalEquipment = new HashMap<Class, Integer>(optionalEquipment);
        }

        settlement = robot.getSettlement();

        // Determine location for loading.
        Point2D loadingLoc = determineLoadingLocation();
        setOutsideSiteLocation(loadingLoc.getX(), loadingLoc.getY());

        // Initialize task phase
        addPhase(LOADING);

*/
    }

    /**
     * Gets a list of all embarking vehicle missions at a settlement with vehicle
     * currently in a garage.
     * @param settlement the settlement.
     * @return list of vehicle missions.
     */
    public static List<Mission> getAllMissionsNeedingLoading(Settlement settlement) {

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
     * Gets a list of rovers with crew who are missing EVA suits.
     * @param settlement the settlement.
     * @return list of rovers.
     */
    public static List<Rover> getRoversNeedingEVASuits(Settlement settlement) {

        List<Rover> result = new ArrayList<Rover>();

        Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            if (vehicle instanceof Rover) {
                Rover rover = (Rover) vehicle;
                if (!rover.isReservedForMission()) {
                    if (BuildingManager.getBuilding(rover) == null) {
                        Inventory roverInv = rover.getInventory();
                        int peopleOnboard = roverInv.findNumUnitsOfClass(Person.class);
                        if ((peopleOnboard > 0)) {
                            int numSuits = roverInv.findNumUnitsOfClass(EVASuit.class);
                            double water = roverInv.getAmountResourceStored(ResourceUtil.waterID, false);
                            double oxygen = roverInv.getARStored(ResourceUtil.oxygenID, false);
                            if ((numSuits == 0) || (water < WATER_NEED) || (oxygen < OXYGEN_NEED)) {
                                result.add(rover);
                            }
                        }

                        // robots need no suits, water, oxygen
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
        List<Mission> loadingMissions = null;
		if (person != null)
	       	loadingMissions = getAllMissionsNeedingLoading(person.getSettlement());
		else if (robot != null)
        	loadingMissions = getAllMissionsNeedingLoading(robot.getSettlement());

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

    /**
     * Determine location to load the vehicle.
     * @return location.
     */
    private Point2D determineLoadingLocation() {

        Point2D.Double newLocation = null;
        boolean goodLocation = false;
        for (int x = 0; (x < 50) && !goodLocation; x++) {
            Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(vehicle, 1D);
            newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(),
                    boundedLocalPoint.getY(), vehicle);
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
        return LOADING;
    }

    @Override
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);

        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (LOADING.equals(getPhase())) {
            return loadingPhase(time);
        }
        else {
            return time;
        }
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

        // Check for an accident during the EVA operation.
        checkForAccident(time);

        // 2015-05-29 Check for radiation exposure during the EVA operation.
        if (isRadiationDetected(time)){
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        // Check if site duration has ended or there is reason to cut the loading
        // phase short and return to the rover.
        if (shouldEndEVAOperation() || addTimeOnSite(time)) {
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        // Determine load rate.
        int strength =  0;
		if (person != null)
			strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.STRENGTH);
		else if (robot != null)
			strength = robot.getRoboticAttributeManager().getAttribute(RoboticAttributeType.STRENGTH);

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
        if (amountLoading > 0D) {
            amountLoading = loadEquipment(amountLoading);
        }

        // Load resources
        try {
            amountLoading = loadResources(amountLoading);
        }
        catch (Exception e) {
            logger.severe(e.getMessage());
        }

        // Put rover back into settlement.
        if (roverInSettlement) {
            sInv.storeUnit(vehicle);
        }

        if (isFullyLoaded(requiredResources, optionalResources, requiredEquipment,
                optionalEquipment, vehicle, settlement)) {
            setPhase(WALK_BACK_INSIDE);
        }

        return 0D;
    }

    /**
     * Loads the vehicle with required resources from the settlement.
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @return the remaining amount (kg) the person can load in this time period.
     */
    private double loadResources(double amountLoading) {

        // Load required resources.
        Iterator<Integer> iR = requiredResources.keySet().iterator();
        while (iR.hasNext() && (amountLoading > 0D)) {
            Integer resource = iR.next();
            if (resource < FIRST_ITEM_RESOURCE) {
                // Load amount resources
                amountLoading = loadAmountResource(amountLoading, resource, true);
            }
            else if (resource >= FIRST_ITEM_RESOURCE) {
                // Load item resources
                amountLoading = loadItemResource(amountLoading, resource, true);
            }
        }

        // Load optional resources.
        Iterator<Integer> iR2 = optionalResources.keySet().iterator();
        while (iR2.hasNext() && (amountLoading > 0D)) {
            Integer resource = iR2.next();
            if (resource < FIRST_ITEM_RESOURCE) {
                // Load amount resources
                amountLoading = loadAmountResource(amountLoading, resource, false);
            }
            else if (resource >= FIRST_ITEM_RESOURCE) {
                // Load item resources
                amountLoading = loadItemResource(amountLoading, resource, false);
            }
        }

        // Return remaining amount that can be loaded by person this time period.
        return amountLoading;
    }

    /**
     * Loads the vehicle with an amount resource from the settlement.
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @param resource the amount resource to be loaded.
     * @param required true if the amount resource is required to load, false if optional.
     * @return the remaining amount (kg) the person can load in this time period.
     */
    private double loadAmountResource(double amountLoading, Integer resource, boolean required) {

        Inventory vInv = vehicle.getInventory();
        Inventory sInv = settlement.getInventory();

        double amountNeededTotal = 0D;
        if (required) {
            amountNeededTotal = (Double) requiredResources.get(resource);
        }
        else {
            if (requiredResources.containsKey(resource)) {
                amountNeededTotal += (Double) requiredResources.get(resource);
            }
            amountNeededTotal += (Double) optionalResources.get(resource);
        }

        double amountAlreadyLoaded = vInv.getAmountResourceStored(resource, false);

        if (amountAlreadyLoaded < amountNeededTotal) {
            double amountNeeded = amountNeededTotal - amountAlreadyLoaded;
            boolean canLoad = true;
            String loadingError = "";

            // Check if enough resource in settlement inventory.
            double settlementStored = sInv.getAmountResourceStored(resource, false);
            sInv.addAmountDemandTotalRequest(resource);
            if (settlementStored < amountNeeded) {
                if (required) {
                    canLoad = false;
                    loadingError = "Not enough resource stored at settlement to load "
                            + "resource: " + resource + " needed: " + amountNeeded + ", stored: "
                            + settlementStored;
                }
                else {
                    amountNeeded = settlementStored;
                }
            }

            // Check remaining capacity in vehicle inventory.
            double remainingCapacity = vInv.getAmountResourceRemainingCapacity(resource, true, false);
            if (remainingCapacity < amountNeeded) {
                if (required) {
                    if ((amountNeeded - remainingCapacity) < .00001D) {
                        amountNeeded = remainingCapacity;
                    }
                    else {
                        canLoad = false;
                        loadingError = "Not enough capacity in vehicle for loading resource "
                                + resource + ": " + amountNeeded + ", remaining capacity: "
                                + remainingCapacity;
                    }
                }
                else {
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
                }
                catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                amountLoading -= resourceAmount;
            }
            else {
                endTask();
                throw new IllegalStateException(loadingError);
            }
        }
        else {
            if (required && optionalResources.containsKey(resource)) {
                amountNeededTotal += (Double) optionalResources.get(resource);
            }

            if (amountAlreadyLoaded > amountNeededTotal) {

                // In case vehicle wasn't fully unloaded first.
                double amountToRemove = amountAlreadyLoaded - amountNeededTotal;
                try {
                    vInv.retrieveAmountResource(resource, amountToRemove);
                    sInv.storeAmountResource(resource, amountToRemove, true);
       			    // 2015-01-15 Add addSupplyAmount()
                    //sInv.addAmountSupplyAmount(resource, amountToRemove);
                }
                catch (Exception e) {}
            }
        }

        //  Return remaining amount that can be loaded by person this time period.
        return amountLoading;
    }

    /**
     * Loads the vehicle with an item resource from the settlement.
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @param resource the item resource to be loaded.
     * @param required true if the item resource is required to load, false if optional.
     * @return the remaining amount (kg) the person can load in this time period.
     */
    private double loadItemResource(double amountLoading, Integer resource, boolean required) {

        Inventory vInv = vehicle.getInventory();
        Inventory sInv = settlement.getInventory();

        int numNeededTotal = 0;
        if (required) {
            numNeededTotal = (int) requiredResources.get(resource);
        }
        else {
            if (requiredResources.containsKey(resource)) {
                numNeededTotal += (int) requiredResources.get(resource);
            }
            numNeededTotal += (int) optionalResources.get(resource);
        }

        int numAlreadyLoaded = vInv.getItemResourceNum(resource);

        if (numAlreadyLoaded < numNeededTotal) {
            int numNeeded = numNeededTotal - numAlreadyLoaded;
            boolean canLoad = true;
            String loadingError = "";

            // Check if enough resource in settlement inventory.
            int settlementStored = sInv.getItemResourceNum(resource);
            if (settlementStored < numNeeded) {
                if (required) {
                    canLoad = false;
                    loadingError = "Not enough resource stored at settlement to load "
                            + "resource: " + resource + " needed: " + numNeeded + ", stored: "
                            + settlementStored;
                }
                else {
                    numNeeded = settlementStored;
                }
            }

            ItemResource ir = ItemResourceUtil.findItemResource(resource);
        
            // Check remaining capacity in vehicle inventory.
            double remainingMassCapacity = vInv.getRemainingGeneralCapacity(false);
            if (remainingMassCapacity < (numNeeded * ir.getMassPerItem())) {
                if (required) {
                    canLoad = false;
                    loadingError = "Not enough capacity in vehicle for loading resource "
                            + resource + ": " + numNeeded + ", remaining capacity: "
                            + remainingMassCapacity + " kg";
                }
                else {
                    numNeeded = (int) (remainingMassCapacity / ir.getMassPerItem());
                }
            }

            // Determine amount to load.
            int resourceNum = (int) (amountLoading / ir.getMassPerItem());
            if (resourceNum < 1) {
                resourceNum = 1;
            }
            if (resourceNum > numNeeded) {
                resourceNum = numNeeded;
            }

            if (canLoad) {

                // Load resource from settlement inventory to vehicle inventory.
                sInv.retrieveItemResources(resource, resourceNum);
                vInv.storeItemResources(resource, resourceNum);
                amountLoading -= (resourceNum * ir.getMassPerItem());
                if (amountLoading < 0D) amountLoading = 0D;
            }
            else {
                endTask();
                throw new IllegalStateException(loadingError);
            }
        }
        else {
            if (required && optionalResources.containsKey(resource)) {
                numNeededTotal += (Integer) optionalResources.get(resource);
            }

            if (numAlreadyLoaded > numNeededTotal) {

                // In case vehicle wasn't fully unloaded first.
                int numToRemove = numAlreadyLoaded - numNeededTotal;
                try {
                    vInv.retrieveItemResources(resource, numToRemove);
                    sInv.storeItemResources(resource, numToRemove);
                }
                catch (Exception e) {}
            }
        }

        // Return remaining amount that can be loaded by person this time period.
        return amountLoading;
    }

    /**
     * Loads the vehicle with required and optional equipment from the settlement.
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
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @return the remaining amount (kg) the person can load in this time period.
     */
    private double loadRequiredEquipment(double amountLoading) {

        Inventory vInv = vehicle.getInventory();
        Inventory sInv = settlement.getInventory();

        Iterator<Integer> iE = requiredEquipment.keySet().iterator();
        while (iE.hasNext() && (amountLoading > 0D)) {
            Integer equipmentType = iE.next();
            int numNeededTotal = (Integer) requiredEquipment.get(equipmentType);
            int numAlreadyLoaded = vInv.findNumUnitsOfClass(equipmentType);
            if (numAlreadyLoaded < numNeededTotal) {
                int numNeeded = numNeededTotal - numAlreadyLoaded;
                Collection<Unit> units = sInv.findAllUnitsOfClass(equipmentType);
                Object[] array  = units.toArray();

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
                                sInv.retrieveUnit(eq);
                                vInv.storeUnit(eq);
                                amountLoading -= eq.getMass();
                                if (amountLoading < 0D) {
                                    amountLoading = 0D;
                                }
                                loaded++;
                            }
                            else {
                                logger.warning(vehicle + " cannot store " + eq);
                                endTask();
                            }
                        }
                    }

                    array = null;
                }
                else {
                    endTask();
                }
            }
            else {

                if (optionalEquipment.containsKey(equipmentType)) {
                    numNeededTotal += (Integer) optionalEquipment.get(equipmentType);
                }

                if (numAlreadyLoaded > numNeededTotal) {

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
        }

        // Return remaining amount that can be loaded by person this time period.
        return amountLoading;
    }

    /**
     * Loads the vehicle with optional equipment from the settlement.
     * @param amountLoading the amount (kg) the person can load in this time period.
     * @return the remaining amount (kg) the person can load in this time period.
     */
    private double loadOptionalEquipment(double amountLoading) {

        Inventory vInv = vehicle.getInventory();
        Inventory sInv = settlement.getInventory();

        Iterator<Integer> iE = optionalEquipment.keySet().iterator();
        while (iE.hasNext() && (amountLoading > 0D)) {
        	Integer equipmentType = iE.next();
            int numNeededTotal = (Integer) optionalEquipment.get(equipmentType);
            if (requiredEquipment.containsKey(equipmentType)) {
                numNeededTotal += (Integer) requiredEquipment.get(equipmentType);
            }
            int numAlreadyLoaded = vInv.findNumUnitsOfClass(equipmentType);
            if (numAlreadyLoaded < numNeededTotal) {
                int numNeeded = numNeededTotal - numAlreadyLoaded;
                Collection<Unit> units = sInv.findAllUnitsOfClass(equipmentType);
                Object[] array  = units.toArray();

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
                            sInv.retrieveUnit(eq);
                            vInv.storeUnit(eq);
                            amountLoading -= eq.getMass();
                            if (amountLoading < 0D) {
                                amountLoading = 0D;
                            }
                            loaded++;
                        }
                        else {
                            logger.warning(vehicle + " cannot store " + eq);
                            endTask();
                        }
                    }
                }

                array = null;
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
    public static boolean hasEnoughSupplies(Settlement settlement, Vehicle vehicle, Map <Integer, Number> resources,
            Map<Integer, Integer> equipment, int vehicleCrewNum, double tripTime) {

    	 return LoadVehicleGarage.hasEnoughSupplies(settlement, vehicle,  resources,
    	    		 equipment, vehicleCrewNum, tripTime);
    }
    
//        // Check input parameters.
//        if (settlement == null) {
//            throw new IllegalArgumentException("settlement is null");
//        }
//
//        boolean enoughSupplies = true;
//        Inventory inv = settlement.getSettlementInventory();
//        Inventory vInv = vehicle.getSettlementInventory();
//
//        boolean roverInSettlement = false;
//        if (inv.containsUnit(vehicle)) {
//            roverInSettlement = true;
//            inv.retrieveUnit(vehicle);
//        }
//
//        // Check if there are enough resources at the settlement.
//        Iterator<Resource> iR = resources.keySet().iterator();
//        while (iR.hasNext()) {
//            Resource resource = iR.next();
//            if (resource instanceof AmountResource) {
//                double amountNeeded = (Double) resources.get(resource);
//                double remainingSettlementAmount = getRemainingSettlementAmount(settlement, vehicleCrewNum,
//                        (AmountResource) resource, tripTime);
//                double amountLoaded = vInv.getAmountResourceStored((AmountResource) resource, false);
//                double totalNeeded = amountNeeded + remainingSettlementAmount - amountLoaded;
//                if (inv.getAmountResourceStored((AmountResource) resource, false) < totalNeeded) {
//                    double stored = inv.getAmountResourceStored((AmountResource) resource, false);
//                    inv.addAmountDemandTotalRequest((AmountResource) resource);
//                    if (logger.isLoggable(Level.INFO)) {
//                        logger.info(resource.getName() + " needed: " + totalNeeded + " stored: " + stored);
//                    }
//                    enoughSupplies = false;
//                }
//            }
//            else if (resource instanceof ItemResource) {
//                int numNeeded = (Integer) resources.get(resource);
//                int remainingSettlementNum = getRemainingSettlementNum(settlement, vehicleCrewNum,
//                        (ItemResource) resource);
//                int numLoaded = vInv.getItemResourceNum((ItemResource) resource);
//                int totalNeeded = numNeeded + remainingSettlementNum - numLoaded;
//                if (inv.getItemResourceNum((ItemResource) resource) < totalNeeded) {
//                    int stored = inv.getItemResourceNum((ItemResource) resource);
//                    if (logger.isLoggable(Level.INFO)) {
//                        logger.info(resource.getName() + " needed: " + totalNeeded + " stored: " + stored);
//                    }
//                    enoughSupplies = false;
//                }
//            }
//            else {
//                throw new IllegalStateException("Unknown resource type: " + resource);
//            }
//        }
//
//        // Check if there is enough equipment at the settlement.
//        Iterator<Class> iE = equipment.keySet().iterator();
//        while (iE.hasNext()) {
//            Class equipmentType = iE.next();
//            int numNeeded = (Integer) equipment.get(equipmentType);
//            int remainingSettlementNum = getRemainingSettlementNum(settlement, vehicleCrewNum, equipmentType);
//            int numLoaded = vInv.findNumUnitsOfClass(equipmentType);
//            int totalNeeded = numNeeded + remainingSettlementNum - numLoaded;
//            if (inv.findNumEmptyUnitsOfClass(equipmentType, false) < totalNeeded) {
//                int stored = inv.findNumEmptyUnitsOfClass(equipmentType, false);
//                if (logger.isLoggable(Level.INFO)) {
//                    logger.info(equipmentType + " needed: " + totalNeeded + " stored: " + stored);
//                }
//                enoughSupplies = false;
//            }
//        }
//
//        if (roverInSettlement) {
//            inv.storeUnit(vehicle);
//        }
//
//        return enoughSupplies;
//    }

//    /**
//     * Gets the amount of an amount resource that should remain at the settlement.
//     * @param settlement the settlement
//     * @param vehicleCrewNum the number of crew leaving on the vehicle.
//     * @param resource the amount resource
//     * @param double tripTime the estimated trip time (millisols).
//     * @return remaining amount (kg)
//     */
//    private static double getRemainingSettlementAmount(Settlement settlement, int vehicleCrewNum,
//    		AmountResource resource, double tripTime) {
//
//    	return LoadVehicleGarage.getRemainingSettlementAmount(settlement, vehicleCrewNum,
//        		resource, tripTime);
//    }

//    /**
//     * Gets the number of an item resource that should remain at the settlement.
//     * @param settlement the settlement
//     * @param vehicleCrewNum the number of crew leaving on the vehicle.
//     * @param resource the item resource
//     * @return remaining number
//     */
//    private static int getRemainingSettlementNum(Settlement settlement, int vehicleCrewNum,
//            ItemResource resource) {
//        // No item resources required at settlement at this time.
//        return 0;
//    }
  
//    /**
//     * Gets the number of an equipment type that should remain at the settlement.
//     * @param settlement the settlement
//     * @param vehicleCrewNum the number of crew leaving on the vehicle.
//     * @param equipmentType the equipment type class.
//     * @return remaining number.
//     */
//    private static int getRemainingSettlementNum(Settlement settlement, int vehicleCrewNum,
//            Class<? extends Equipment> equipmentType) {
//        int remainingPeopleNum = settlement.getIndoorPeopleCount() - vehicleCrewNum;
//        // Leave one EVA suit for every four remaining people at settlement (min 1).
//        if (equipmentType == EVASuit.class) {
//            int minSuits = remainingPeopleNum / 4;
//            if (minSuits == 0) {
//                minSuits = 1;
//            }
//            return minSuits;
//        }
//        else return 0;
//    }

    /**
     * Checks if a vehicle has enough storage capacity for the supplies needed on the trip.
     * @param resources a map of the resources required.
     * @param equipment a map of the equipment types and numbers needed.
     * @param vehicle the vehicle to check.
     * @param settlement the settlement to disembark from.
     * @return true if vehicle can carry supplies.
     */
    public static boolean enoughCapacityForSupplies(Map<Integer, Number> resources,
            Map<Class<? extends Equipment>, Integer> equipment, Vehicle vehicle, Settlement settlement) {

        boolean sufficientCapacity = true;

        // Create vehicle inventory clone.
        Inventory inv = vehicle.getInventory().clone(null);

        try {
            // Add equipment clones.
            Iterator<Class<? extends Equipment>> i = equipment.keySet().iterator();
            while (i.hasNext()) {
                Class<? extends Equipment> equipmentType = i.next();
                int num = (Integer) equipment.get(equipmentType);
                Coordinates defaultLoc = new Coordinates(0D, 0D);
                for (int x = 0; x < num; x++) {
                    inv.storeUnit(EquipmentFactory.createEquipment(equipmentType, defaultLoc, false));
                }
            }

            // Add all resources.
            Iterator<Integer> j = resources.keySet().iterator();
            while (j.hasNext()) {
                Integer resource = j.next();
                if (resource < FIRST_ITEM_RESOURCE) {
                    double amount = (Double) (resources.get(resource));
                    inv.storeAmountResource(resource, amount, true);
       			 	// 2015-01-15 Add addSupplyAmount()
                    //inv.addSupplyAmount((AmountResource) resource, amount);
                }
                else {
                    int num = (Integer) (resources.get(resource));
                    inv.storeItemResources(resource, num);
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
     * @param requiredResources the resources that are required for the trip.
     * @param optionalResources the resources that are optional for the trip.
     * @param requiredEquipment the equipment that is required for the trip.
     * @param optionalEquipment the equipment that is optional for the trip.
     * @param vehicle the vehicle that is being checked.
     * @param settlement the settlement that the vehicle is being loaded from.
     * @return true if vehicle is fully loaded.
     */
    public static boolean isFullyLoaded(Map<Integer, Number> requiredResources,
            Map<Integer, Number> optionalResources, Map<Integer, Integer> requiredEquipment,
            Map<Integer, Integer> optionalEquipment, Vehicle vehicle, Settlement settlement) {

        boolean sufficientSupplies = true;

        // Check if there are enough resources in the vehicle.
        sufficientSupplies = isFullyLoadedWithResources(requiredResources, optionalResources,
                vehicle, settlement);

        // Check if there is enough equipment in the vehicle.
        if (sufficientSupplies) {
            sufficientSupplies = isFullyLoadedWithEquipment(requiredEquipment,
                    optionalEquipment, vehicle, settlement);
        }

        return sufficientSupplies;
    }

    /**
     * Checks if the vehicle is fully loaded with resources.
     * @param requiredResources the resources that are required for the trip.
     * @param optionalResources the resources that are optional for the trip.
     * @param vehicle the vehicle.
     * @param settlement the settlement that the vehicle is being loaded from.
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
            if (resource < FIRST_ITEM_RESOURCE) {
                double amount = (Double) (requiredResources.get(resource));
                double storedAmount = vInv.getAmountResourceStored(resource, false);
                if (storedAmount < (amount - SMALL_AMOUNT_COMPARISON)) {
                    sufficientSupplies = false;
                }
            }
            else if (resource >= FIRST_ITEM_RESOURCE) {
                int num = (Integer) (requiredResources.get(resource));
                if (vInv.getItemResourceNum(resource) < num) {
                    sufficientSupplies = false;
                }
            }
            else {
                throw new IllegalStateException("Unknown resource type: " + resource);
            }
        }

        // Check that optional resources are loaded or can't be loaded.
        Iterator<Integer> iR2 = optionalResources.keySet().iterator();
        while (iR2.hasNext() && sufficientSupplies) {
            Integer resource = iR2.next();
            if (resource < FIRST_ITEM_RESOURCE) {

                //AmountResource amountResource = (AmountResource) resource;
                double amount = (Double) (optionalResources.get(resource));
                if (requiredResources.containsKey(resource)) {
                    amount += (Double) (requiredResources.get(resource));
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
            }
            else if (resource >= FIRST_ITEM_RESOURCE) {

                ItemResource ir = ItemResourceUtil.findItemResource(resource);
                int num = (Integer) (optionalResources.get(resource));
                if (requiredResources.containsKey(resource)) {
                    num += (Integer) (requiredResources.get(resource));
                }

                int storedNum = vInv.getItemResourceNum(resource);
                if (storedNum < num) {
                    // Check if enough capacity in vehicle.
                    double vehicleCapacity = vInv.getRemainingGeneralCapacity(false);
                    boolean hasVehicleCapacity = (vehicleCapacity >= ((num - storedNum) * ir.getMassPerItem()));

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
            }
            else {
                throw new IllegalStateException("Unknown resource type: " + resource);
            }
        }

        return sufficientSupplies;
    }

    /**
     * Checks if the vehicle is fully loaded with resources.
     * @param requiredEquipment the equipment that is required for the trip.
     * @param optionalEquipment the equipment that is optional for the trip.
     * @param vehicle the vehicle.
     * @param settlement the settlement that the vehicle is being loaded from.
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
            if (vInv.findNumUnitsOfClass(equipmentType) < num) {
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

            int storedNum = vInv.findNumUnitsOfClass(equipmentType);
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

    @Override
    public int getEffectiveSkillLevel() {
    	SkillManager manager = null;
		if (person != null)
	   		manager = person.getMind().getSkillManager();
		else if (robot != null)
    		manager = robot.getBotMind().getSkillManager();

        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        return EVAOperationsSkill;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(2);
        results.add(SkillType.EVA_OPERATIONS);
        return results;
    }

    @Override
    protected void addExperience(double time) {

        // Add experience to "EVA Operations" skill.
        // (1 base experience point per 100 millisols of time spent)
        double evaExperience = time / 100D;
        NaturalAttributeManager nManager = null;
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        RoboticAttributeManager rManager = null;
        int experienceAptitude = 0;
        if (person != null) {
            nManager = person.getNaturalAttributeManager();
            experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
        }
        else if (robot != null) {
        	rManager = robot.getRoboticAttributeManager();
            experienceAptitude = rManager.getAttribute(RoboticAttributeType.EXPERIENCE_APTITUDE);
        }
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
		if (person != null)
	        person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);
		else if (robot != null)
			robot.getBotMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);
    }

    @Override
    public void destroy() {
        super.destroy();

        vehicle = null;
        settlement = null;

        if (requiredResources != null) {
            requiredResources.clear();
        }
        requiredResources = null;

        if (optionalResources != null) {
            optionalResources.clear();
        }
        optionalResources = null;

        if (requiredEquipment != null) {
            requiredEquipment.clear();
        }
        requiredEquipment = null;

        if (optionalEquipment != null) {
            optionalEquipment.clear();
        }
        optionalEquipment = null;
    }
}