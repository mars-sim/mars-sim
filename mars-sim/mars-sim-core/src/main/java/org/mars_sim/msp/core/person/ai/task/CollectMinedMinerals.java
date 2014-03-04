/**
 * Mars Simulation Project
 * CollectMinedMinerals.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * Task for collecting minerals that have been mined at a site.
 */
public class CollectMinedMinerals
extends EVAOperation
implements Serializable {

    /** default serial id. */
	private static final long serialVersionUID = 1L;

	// TODO Task phases should be enums
    private static final String WALK_TO_SITE = "Walk to Site";
    private static final String COLLECT_MINERALS = "Collecting Minerals";
    private static final String WALK_TO_ROVER = "Walk to Rover";
    
    /** Rate of mineral collection (kg/millisol). */
    private static final double MINERAL_COLLECTION_RATE = 10D;

    // Data members
    private Rover rover; // Rover used.
    protected AmountResource mineralType; 
    private double collectionSiteXLoc;
    private double collectionSiteYLoc;
    private double enterAirlockXLoc;
    private double enterAirlockYLoc;

    /**
     * Constructor
     * @param person the person performing the task.
     * @param rover the rover used for the EVA operation.
     * @param mineralType the type of mineral to collect.
     * @throws Exception if error creating task.
     */
    public CollectMinedMinerals(Person person, Rover rover, AmountResource mineralType) {

        // Use EVAOperation parent constructor.
        super("Collect Minerals", person);

        // Initialize data members.
        this.rover = rover;
        this.mineralType = mineralType;

        // Determine location for collection site.
        Point2D collectionSiteLoc = determineCollectionSiteLocation();
        collectionSiteXLoc = collectionSiteLoc.getX();
        collectionSiteYLoc = collectionSiteLoc.getY();
        
        // Determine location for reentering rover airlock.
        Point2D enterAirlockLoc = determineRoverAirlockEnteringLocation();
        enterAirlockXLoc = enterAirlockLoc.getX();
        enterAirlockYLoc = enterAirlockLoc.getY();
        
        // Add task phases
        addPhase(WALK_TO_SITE);
        addPhase(COLLECT_MINERALS);
        addPhase(WALK_TO_ROVER);
    }
    
    /**
     * Determine location for the collection site.
     * @return site X and Y location outside rover.
     */
    private Point2D determineCollectionSiteLocation() {
        
        Point2D newLocation = null;
        boolean goodLocation = false;
        for (int x = 0; (x < 5) && !goodLocation; x++) {
            for (int y = 0; (y < 10) && !goodLocation; y++) {

                double distance = RandomUtil.getRandomDouble(50D) + (x * 100D) + 50D;
                double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
                double newXLoc = rover.getXLocation() - (distance * Math.sin(radianDirection));
                double newYLoc = rover.getYLocation() + (distance * Math.cos(radianDirection));
                Point2D boundedLocalPoint = new Point2D.Double(newXLoc, newYLoc);

                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                        boundedLocalPoint.getY(), rover);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                        person.getCoordinates());
            }
        }

        return newLocation;
    }
    
    /**
     * Determine location for returning to rover airlock.
     * @return X and Y location outside rover.
     */
    private Point2D determineRoverAirlockEnteringLocation() {
        
        Point2D vehicleLoc = LocalAreaUtil.getRandomExteriorLocation(rover, 1D);
        Point2D newLocation = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(), 
                vehicleLoc.getY(), rover);
        
        return newLocation;
    }

    /**
     * Perform the exit rover phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error exiting rover.
     */
    private double exitRoverPhase(double time) {

        try {
            time = exitAirlock(time, rover.getAirlock());

            // Add experience points
            addExperience(time);
        }
        catch (Exception e) {
            // Person unable to exit airlock.
            endTask();
        }

        if (exitedAirlock) {
            // Take bag for collecting mineral.
            if (!hasBags()) takeBag();

            if (hasBags()) {
                
                // Set task phase to walk to collecting site.
                setPhase(WALK_TO_SITE);
            }
            else {
                setPhase(ENTER_AIRLOCK);
            }
        }
        return time;
    }

    /**
     * Checks if the person is carrying any bags.
     * @return true if carrying bags.
     */
    private boolean hasBags() {
        return person.getInventory().containsUnitClass(Bag.class);
    }

    /**
     * Takes the most full bag from the rover.
     * @throws Exception if error taking bag.
     */
    private void takeBag() {
        Bag bag = findMostFullBag(rover.getInventory(), mineralType);
        if (bag != null) {
            if (person.getInventory().canStoreUnit(bag, false)) {
                rover.getInventory().retrieveUnit(bag);
                person.getInventory().storeUnit(bag);
            }
        }
    }

    /**
     * Gets the most but not completely full bag of the resource in the rover.
     * @param inv the inventory to look in.
     * @param resourceType the resource for capacity.
     * @return container.
     */
    private static Bag findMostFullBag(Inventory inv, AmountResource resource) {
        Bag result = null;
        double leastCapacity = Double.MAX_VALUE;

        Iterator<Unit> i = inv.findAllUnitsOfClass(Bag.class).iterator();
        while (i.hasNext()) {
            Bag bag = (Bag) i.next();
            double remainingCapacity = bag.getInventory().getAmountResourceRemainingCapacity(
                    resource, true, false);

            if ((remainingCapacity > 0D) && (remainingCapacity < leastCapacity)) {
                result = bag;
                leastCapacity = remainingCapacity;
            }
        }

        return result;
    }
    
    /**
     * Perform the walk to mining collection site phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToCollectionSitePhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // Check if there is reason to cut the EVA walk phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_ROVER);
            return time;
        }
        
        // If not at mining collection site location, create walk outside subtask.
        if ((person.getXLocation() != collectionSiteXLoc) || (person.getYLocation() != collectionSiteYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    collectionSiteXLoc, collectionSiteYLoc, false);
            addSubTask(walkingTask);
        }
        else {
            setPhase(COLLECT_MINERALS);
        }
        
        return time;
    }
    
    /**
     * Perform the walk to rover airlock phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToRoverAirlockPhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // If not at outside rover airlock location, create walk outside subtask.
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
     * Perform the collect minerals phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error collecting minerals.
     */
    private double collectMineralsPhase(double time) {

        // Check for an accident during the EVA operation.
        checkForAccident(time);

        // Check if there is reason to cut the collection phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_ROVER);
            return time;
        }

        Mining mission = (Mining) person.getMind().getMission();

        double mineralsExcavated = mission.getMineralExcavationAmount(mineralType);
        double remainingPersonCapacity = 
            person.getInventory().getAmountResourceRemainingCapacity(mineralType, true, false);

        double mineralsCollected = time * MINERAL_COLLECTION_RATE;

        // Modify collection rate by "Areology" skill.
        int areologySkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
        if (areologySkill == 0) mineralsCollected /= 2D;
        if (areologySkill > 1) mineralsCollected += mineralsCollected * (.2D * areologySkill);

        if (mineralsCollected > remainingPersonCapacity) mineralsCollected = remainingPersonCapacity;
        if (mineralsCollected > mineralsExcavated) mineralsCollected = mineralsExcavated;

        // Add experience points
        addExperience(time);

        // Collect minerals.
        person.getInventory().storeAmountResource(mineralType, mineralsCollected, true);
        mission.collectMineral(mineralType, mineralsCollected);
        if (((mineralsExcavated - mineralsCollected) <= 0D) || 
                (mineralsCollected >= remainingPersonCapacity)) setPhase(WALK_TO_ROVER);

        return 0D;
    }

    /**
     * Perform the enter rover phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error entering rover.
     */
    private double enterRoverPhase(double time) {

        time = enterAirlock(time, rover.getAirlock());

        // Add experience points
        addExperience(time);

        if (enteredAirlock) {
            Inventory pInv = person.getInventory();

            if (pInv.containsUnitClass(Bag.class)) {
                // Load bags in rover.
                Iterator<Unit> i = pInv.findAllUnitsOfClass(Bag.class).iterator();
                while (i.hasNext()) {
                    Bag bag = (Bag) i.next();
                    pInv.retrieveUnit(bag);
                    rover.getInventory().storeUnit(bag);
                }
            }
            else {
                endTask();
                return time;
            }
        }

        return 0D;
    }

    /**
     * Checks if a person can perform a CollectMinedMinerals task.
     * @param person the person to perform the task
     * @param rover the rover the person will EVA from
     * @param mineralType the resource to collect.
     * @return true if person can perform the task.
     */
    public static boolean canCollectMinerals(Person person, Rover rover, AmountResource mineralType) {

        // Check if person can exit the rover.
        boolean exitable = ExitAirlock.canExitAirlock(person, rover.getAirlock());

        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();

        // Check if it is night time outside.
        boolean sunlight = surface.getSurfaceSunlight(rover.getCoordinates()) > 0;

        // Check if in dark polar region.
        boolean darkRegion = surface.inDarkPolarRegion(rover.getCoordinates());

        // Check if person's medical condition will not allow task.
        boolean medical = person.getPerformanceRating() < .5D;

        // Checks if available bags with remaining capacity for resource.
        Bag bag = findMostFullBag(rover.getInventory(), mineralType);
        boolean bagAvailable = (bag != null);

        // Check if bag and full EVA suit can be carried by person or is too heavy.
        double carryMass = 0D;
        if (bag != null) {
            carryMass += bag.getMass();
        }
        EVASuit suit = (EVASuit) rover.getInventory().findUnitOfClass(EVASuit.class);
        if (suit != null) {
            carryMass += suit.getMass();
            AmountResource oxygenResource = AmountResource.findAmountResource("oxygen");
            carryMass += suit.getInventory().getAmountResourceRemainingCapacity(oxygenResource, false, false);
            AmountResource waterResource = AmountResource.findAmountResource("water");
            carryMass += suit.getInventory().getAmountResourceRemainingCapacity(waterResource, false, false);
        }
        double carryCapacity = person.getInventory().getGeneralCapacity();
        boolean canCarryEquipment = (carryCapacity >= carryMass);
        
        return (exitable && (sunlight || darkRegion) && !medical && bagAvailable && canCarryEquipment);
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
        person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);

        // If phase is collect minerals, add experience to areology skill.
        if (COLLECT_MINERALS.equals(getPhase())) {
            // 1 base experience point per 10 millisols of collection time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double areologyExperience = time / 10D;
            areologyExperience += areologyExperience * experienceAptitudeModifier;
            person.getMind().getSkillManager().addExperience(SkillType.AREOLOGY, areologyExperience);
        }
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(2);
        results.add(SkillType.EVA_OPERATIONS);
        results.add(SkillType.AREOLOGY);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        int areologySkill = manager.getEffectiveSkillLevel(SkillType.AREOLOGY);
        return (int) Math.round((double)(EVAOperationsSkill + areologySkill) / 2D); 
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) {
            return exitRoverPhase(time);
        }
        else if (WALK_TO_SITE.equals(getPhase())) {
            return walkToCollectionSitePhase(time);
        }
        else if (COLLECT_MINERALS.equals(getPhase())) {
            return collectMineralsPhase(time);
        }
        else if (WALK_TO_ROVER.equals(getPhase())) {
            return walkToRoverAirlockPhase(time);
        }
        else if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) {
            return enterRoverPhase(time);
        }
        else return time;
    }

    @Override
    public void destroy() {
        super.destroy();

        rover = null;
        mineralType = null;
    }
}