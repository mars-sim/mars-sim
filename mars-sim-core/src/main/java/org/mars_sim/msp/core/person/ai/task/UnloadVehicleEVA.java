/**
 * Mars Simulation Project
 * UnloadVehicleEVA.java
 * @version 3.06 2014-01-29
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

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Towing;
import org.mars_sim.msp.core.vehicle.Vehicle;

/** 
 * The UnloadVehicleEVA class is a task for unloading fuel and supplies from a vehicle 
 * when the vehicle is outside.
 */
public class UnloadVehicleEVA extends EVAOperation implements Serializable {

    private static Logger logger = Logger.getLogger(UnloadVehicleEVA.class.getName());

    // Task phase
    private static final String WALK_TO_VEHICLE = "Walk to Vehicle";
    private static final String UNLOADING = "Unloading";
    private static final String WALK_TO_AIRLOCK = "Walk to Airlock";

    // The amount of resources (kg) one person of average strength can unload per millisol.
    private static double UNLOAD_RATE = 20D;

    // Data members
    private Vehicle vehicle;  // The vehicle that needs to be unloaded.
    private Settlement settlement; // The settlement the person is unloading to.
    private Airlock airlock; // Airlock to be used for EVA.
    private double unloadingXLoc;
    private double unloadingYLoc;
    private double enterAirlockXLoc;
    private double enterAirlockYLoc;

    /**
     * Constructor
     * @param person the person to perform the task.
     */
    public UnloadVehicleEVA(Person person) {
        // Use Task constructor.
        super("Unloading vehicle EVA", person);

        settlement = person.getSettlement();

        VehicleMission mission = getMissionNeedingUnloading();
        if (mission != null) {
            vehicle = mission.getVehicle();
        }
        else {
            List<Vehicle> nonMissionVehicles = getNonMissionVehiclesNeedingUnloading(settlement);
            if (nonMissionVehicles.size() > 0) {
                vehicle = nonMissionVehicles.get(RandomUtil.getRandomInt(nonMissionVehicles.size() - 1));
            }
        }

        if (vehicle != null) {

            // Determine location for unloading.
            Point2D unloadingLoc = determineUnloadingLocation();
            unloadingXLoc = unloadingLoc.getX();
            unloadingYLoc = unloadingLoc.getY();
            
            // Get an available airlock.
            airlock = getClosestWalkableAvailableAirlock(person, vehicle.getXLocation(), 
                    vehicle.getYLocation());
            if (airlock == null) {
                endTask();
            }
            else {
                // Determine location for reentering building airlock.
                Point2D enterAirlockLoc = determineAirlockEnteringLocation();
                enterAirlockXLoc = enterAirlockLoc.getX();
                enterAirlockYLoc = enterAirlockLoc.getY();
            }

            setDescription("Unloading " + vehicle.getName());

            // Initialize task phase
            addPhase(WALK_TO_VEHICLE);
            addPhase(UNLOADING);
            addPhase(WALK_TO_AIRLOCK);
        }
        else {
            endTask();
        }
    }

    /** 
     * Constructor
     * @param person the person to perform the task
     * @param vehicle the vehicle to be unloaded
     */
    public UnloadVehicleEVA(Person person, Vehicle vehicle) {
        // Use Task constructor.
        super("Unloading vehicle EVA", person);

        setDescription("Unloading " + vehicle.getName());
        this.vehicle = vehicle;

        // Determine location for unloading.
        Point2D unloadingLoc = determineUnloadingLocation();
        unloadingXLoc = unloadingLoc.getX();
        unloadingYLoc = unloadingLoc.getY();
        
        settlement = person.getSettlement();

        // Get an available airlock.
        airlock = getClosestWalkableAvailableAirlock(person, vehicle.getXLocation(), 
                vehicle.getYLocation());
        if (airlock == null) {
            endTask();
        }
        else {
            // Determine location for reentering building airlock.
            Point2D enterAirlockLoc = determineAirlockEnteringLocation();
            enterAirlockXLoc = enterAirlockLoc.getX();
            enterAirlockYLoc = enterAirlockLoc.getY();
        }

        // Initialize phase
        addPhase(WALK_TO_VEHICLE);
        addPhase(UNLOADING);
        addPhase(WALK_TO_AIRLOCK);

        // logger.info(person.getName() + " is unloading " + vehicle.getName());
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
                int numVehicles = 0;
                numVehicles += getAllMissionsNeedingUnloading(person.getSettlement()).size();
                numVehicles += getNonMissionVehiclesNeedingUnloading(person.getSettlement()).size();
                result = 50D * numVehicles;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }

        // Check if an airlock is available
        if (getWalkableAvailableAirlock(person) == null) {
            result = 0D;
        }

        // Check if it is night time.
        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
        if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
            if (!surface.inDarkPolarRegion(person.getCoordinates())) {
                result = 0D;
            }
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
            result *= job.getStartTaskProbabilityModifier(UnloadVehicleEVA.class);        
        }

        return result;
    }

    /**
     * Gets a list of vehicles that need unloading and aren't reserved for a mission.
     * @param settlement the settlement the vehicle is at.
     * @return list of vehicles.
     */
    private static List<Vehicle> getNonMissionVehiclesNeedingUnloading(Settlement settlement) {
        List<Vehicle> result = new ArrayList<Vehicle>();

        if (settlement != null) {
            Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
            while (i.hasNext()) {
                Vehicle vehicle = i.next();
                boolean needsUnloading = false;
                if (!vehicle.isReserved()) {
                    int peopleOnboard = CollectionUtils.getPerson(
                            vehicle.getInventory().getContainedUnits()).size();
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
     * @param settlement the settlement.
     * @return list of vehicle missions.
     */
    private static List<Mission> getAllMissionsNeedingUnloading(Settlement settlement) {

        List<Mission> result = new ArrayList<Mission>();

        MissionManager manager = Simulation.instance().getMissionManager();
        Iterator<Mission> i = manager.getMissions().iterator();
        while (i.hasNext()) {
            Mission mission = (Mission) i.next();
            if (mission instanceof VehicleMission) {
                if (VehicleMission.DISEMBARKING.equals(mission.getPhase())) {
                    VehicleMission vehicleMission = (VehicleMission) mission;
                    if (vehicleMission.hasVehicle()) {
                        Vehicle vehicle = vehicleMission.getVehicle();
                        if (settlement == vehicle.getSettlement()) {
                            int peopleOnboard = CollectionUtils.getPerson(
                                    vehicle.getInventory().getContainedUnits()).size();
                            if (peopleOnboard == 0) {
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
     * @return vehicle mission.
     * @throws Exception if error finding vehicle mission.
     */
    private VehicleMission getMissionNeedingUnloading() {

        VehicleMission result = null;

        List<Mission> unloadingMissions = getAllMissionsNeedingUnloading(person.getSettlement());

        if (unloadingMissions.size() > 0) {
            int index = RandomUtil.getRandomInt(unloadingMissions.size() - 1);
            result = (VehicleMission) unloadingMissions.get(index);
        }

        return result;
    }

    /**
     * Gets the vehicle being unloaded.
     * @return vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    /**
     * Determine location to unload the vehicle.
     * @return location.
     */
    private Point2D determineUnloadingLocation() {
        
        Point2D.Double newLocation = null;
        boolean goodLocation = false;
        for (int x = 0; (x < 50) && !goodLocation; x++) {
            Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(vehicle, 1D);
            newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                    boundedLocalPoint.getY(), vehicle);
            goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                    person.getCoordinates());
        }
        
        return newLocation;
    }
    
    /**
     * Determine location outside building airlock.
     * @return location.
     */
    private Point2D determineAirlockEnteringLocation() {
        
        Point2D result = null;
        
        // Move the person to a random location outside the airlock entity.
        if (airlock.getEntity() instanceof LocalBoundedObject) {
            LocalBoundedObject entityBounds = (LocalBoundedObject) airlock.getEntity();
            Point2D.Double newLocation = null;
            boolean goodLocation = false;
            for (int x = 0; (x < 20) && !goodLocation; x++) {
                Point2D.Double boundedLocalPoint = LocalAreaUtil.getRandomExteriorLocation(entityBounds, 1D);
                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                        boundedLocalPoint.getY(), entityBounds);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                        person.getCoordinates());
            }
            
            result = newLocation;
        }
        
        return result;
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) {
            return exitEVA(time);
        }
        else if (WALK_TO_VEHICLE.equals(getPhase())) {
            return walkToVehiclePhase(time);
        }
        else if (UNLOADING.equals(getPhase())) {
            return unloadingPhase(time);
        }
        else if (WALK_TO_AIRLOCK.equals(getPhase())) {
            return walkToAirlockPhase(time);
        }
        else if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) {
            return enterEVA(time);
        }
        else {
            return time;
        }
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
            setPhase(WALK_TO_VEHICLE);
        }
        return time;
    }
    
    /**
     * Perform the walk to vehicle loading location phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToVehiclePhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // Check if there is reason to cut the EVA walk phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_AIRLOCK);
            return time;
        }
        
        // If not at vehicle unloading location, create walk outside subtask.
        if ((person.getXLocation() != unloadingXLoc) || (person.getYLocation() != unloadingYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    unloadingXLoc, unloadingYLoc, false);
            addSubTask(walkingTask);
        }
        else {
            setPhase(UNLOADING);
        }
        
        return time;
    }
    
    /**
     * Perform the walk to airlock phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToAirlockPhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // If not at outside airlock location, create walk outside subtask.
        if ((person.getXLocation() != enterAirlockXLoc) || (person.getYLocation() != enterAirlockYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    enterAirlockXLoc, enterAirlockYLoc, true);
            addSubTask(walkingTask);
        }
        else {
            setPhase(EVAOperation.ENTER_AIRLOCK);
        }
        
        return time;
    }

    /**
     * Perform the unloading phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) after performing the phase.
     * @throws Exception if error in loading phase.
     */
    protected double unloadingPhase(double time) {

        // Check for an accident during the EVA operation.
        checkForAccident(time);

        // Check if person should end EVA operation.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_AIRLOCK);
            return time;
        }

        // Determine unload rate.
        int strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.STRENGTH);
        double strengthModifier = .1D + (strength * .018D);
        double amountUnloading = UNLOAD_RATE * strengthModifier * time / 4D;

        Inventory vehicleInv = vehicle.getInventory();
        if (settlement == null) {
            endTask();
            return 0D;
        }
        Inventory settlementInv = settlement.getInventory();

        // Unload equipment.
        if (amountUnloading > 0D) {
            Iterator<Unit> k = vehicleInv.findAllUnitsOfClass(Equipment.class).iterator();
            while (k.hasNext() && (amountUnloading > 0D)) {
                Equipment equipment = (Equipment) k.next();

                // Unload inventories of equipment (if possible)
                unloadEquipmentInventory(equipment);

                vehicleInv.retrieveUnit(equipment);
                settlementInv.storeUnit(equipment);
                amountUnloading -= equipment.getMass();
            }
        }

        // Unload amount resources.
        Iterator<AmountResource> i = vehicleInv.getAllAmountResourcesStored(false).iterator();
        while (i.hasNext() && (amountUnloading > 0D)) {
            AmountResource resource = i.next();
            double amount = vehicleInv.getAmountResourceStored(resource, false);
            if (amount > amountUnloading) amount = amountUnloading;
            double capacity = settlementInv.getAmountResourceRemainingCapacity(resource, true, false);
            if (capacity < amount) {
                amount = capacity;
                amountUnloading = 0D;
            }
            try {
                vehicleInv.retrieveAmountResource(resource, amount);
                settlementInv.storeAmountResource(resource, amount, true);
            }
            catch (Exception e) {}
            amountUnloading -= amount;
        }

        // Unload item resources.
        if (amountUnloading > 0D) {
            Iterator<ItemResource> j = vehicleInv.getAllItemResourcesStored().iterator();
            while (j.hasNext() && (amountUnloading > 0D)) {
                ItemResource resource = j.next();
                int num = vehicleInv.getItemResourceNum(resource);
                if ((num * resource.getMassPerItem()) > amountUnloading) {
                    num = (int) Math.round(amountUnloading / resource.getMassPerItem());
                    if (num == 0) num = 1;
                }
                vehicleInv.retrieveItemResources(resource, num);
                settlementInv.storeItemResources(resource, num);
                amountUnloading -= (num * resource.getMassPerItem());
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

        if (isFullyUnloaded(vehicle)) {
            setPhase(WALK_TO_AIRLOCK);
        }

        return 0D;
    }

    /**
     * Unload the inventory from a piece of equipment.
     * @param equipment the equipment.
     */
    private void unloadEquipmentInventory(Equipment equipment) {
        Inventory eInv = equipment.getInventory();
        Inventory sInv = settlement.getInventory();

        // Unload amount resources.
        // Note: only unloading amount resources at the moment.
        Iterator<AmountResource> i = eInv.getAllAmountResourcesStored(false).iterator();
        while (i.hasNext()) {
            AmountResource resource = i.next();
            double amount = eInv.getAmountResourceStored(resource, false);
            double capacity = sInv.getAmountResourceRemainingCapacity(resource, true, false);
            if (amount < capacity) amount = capacity;
            try {
                eInv.retrieveAmountResource(resource, amount);
                sInv.storeAmountResource(resource, amount, true);
            }
            catch (Exception e) {}
        }
    }

    /** 
     * Returns true if the vehicle is fully unloaded.
     * @param vehicle Vehicle to check.
     * @return is vehicle fully unloaded?
     * @throws InventoryException if error checking vehicle.
     */
    static public boolean isFullyUnloaded(Vehicle vehicle) {
        return (vehicle.getInventory().getTotalInventoryMass(false) == 0D);
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

        if (enteredAirlock) {
            endTask();
        }
        
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
        airlock = null;
    }
}