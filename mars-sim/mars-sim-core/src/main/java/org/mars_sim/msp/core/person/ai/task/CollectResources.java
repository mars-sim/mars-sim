/**
 * Mars Simulation Project
 * CollectResources.java
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
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.vehicle.Rover;

/** 
 * The CollectResources class is a task for collecting resources at a site with an EVA from a rover.
 */
public class CollectResources
extends EVAOperation
implements Serializable {

    /** default serial id. */
	private static final long serialVersionUID = 1L;
	// TODO Task phases should be enums.
    private static final String WALK_TO_SITE = "Walk to Site";
    private static final String COLLECT_RESOURCES = "Collecting Resources";
    private static final String WALK_TO_ROVER = "Walk to Rover";

    // Data members
    /** Rover used. */
    protected Rover rover;
    /** Collection rate for resource (kg/millisol). */
    protected double collectionRate;
    /** Targeted amount of resource to collect at site. (kg) */
    protected double targettedAmount;
    /** Amount of resource already in rover cargo at start of task. (kg) */
    protected double startingCargo;
    /** The resource type. */
    protected AmountResource resourceType;
    /** The container type to use to collect resource. */
    protected Class containerType;
    private double collectionSiteXLoc;
    private double collectionSiteYLoc;
    private double enterAirlockXLoc;
    private double enterAirlockYLoc;

    /**
     * Constructor.
     * @param taskName The name of the task.
     * @param person The person performing the task.
     * @param rover The rover used in the task.
     * @param resourceType The resource type to collect.
     * @param collectionRate The rate (kg/millisol) of collection.
     * @param targettedAmount The amount (kg) desired to collect.
     * @param startingCargo The starting amount (kg) of resource in the rover cargo.
     * @param containerType the type of container to use to collect resource.
     * @throws Exception if error constructing this task.
     */
    public CollectResources(String taskName, Person person, Rover rover, AmountResource resourceType, 
            double collectionRate, double targettedAmount, double startingCargo, Class containerType) {

        // Use EVAOperation parent constructor.
        super(taskName, person);

        // Initialize data members.
        this.rover = rover;
        this.collectionRate = collectionRate;
        this.targettedAmount = targettedAmount;
        this.startingCargo = startingCargo;
        this.resourceType = resourceType;
        this.containerType = containerType;

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
        addPhase(COLLECT_RESOURCES);
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
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) {
            return exitRover(time);
        }
        else if (WALK_TO_SITE.equals(getPhase())) {
            return walkToCollectionSitePhase(time);
        }
        else if (COLLECT_RESOURCES.equals(getPhase())) {
            return collectResources(time);
        }
        else if (WALK_TO_ROVER.equals(getPhase())) {
            return walkToRoverAirlock(time);
        }
        else if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) {
            return enterRover(time);
        }
        else {
            return time;
        }
    }

    /**
     * Adds experience to the person's skills used in this task.
     * @param time the amount of time (ms) the person performed this task.
     */
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

        // If phase is collect resource, add experience to areology skill.
        if (COLLECT_RESOURCES.equals(getPhase())) {
            // 1 base experience point per 10 millisols of collection time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double areologyExperience = time / 10D;
            areologyExperience += areologyExperience * experienceAptitudeModifier;
            person.getMind().getSkillManager().addExperience(SkillType.AREOLOGY, areologyExperience);
        }
    }

    /**
     * Perform the exit rover phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error exiting rover.
     */
    private double exitRover(double time) {

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
            // Take container for collecting resource.
            if (!hasContainers()) {
                takeContainer();
            }

            if (hasContainers()) {
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
     * Checks if the person is carrying any containers.
     * @return true if carrying containers.
     */
    private boolean hasContainers() {
        return person.getInventory().containsUnitClass(containerType);
    }

    /**
     * Takes the least full container from the rover.
     * @throws Exception if error taking container.
     */
    private void takeContainer() {
        Unit container = findLeastFullContainer(rover.getInventory(), containerType, resourceType);
        if (container != null) {
            if (person.getInventory().canStoreUnit(container, false)) {
                rover.getInventory().retrieveUnit(container);
                person.getInventory().storeUnit(container);
            }
        }
    }

    /**
     * Gets the least full container in the rover.
     * @param inv the inventory to look in.
     * @param containerType the container class to look for.
     * @param resourceType the resource for capacity.
     * @return container.
     */
    private static Unit findLeastFullContainer(Inventory inv, Class containerType, 
            AmountResource resource) {
        Unit result = null;
        double mostCapacity = 0D;

        Iterator<Unit> i = inv.findAllUnitsOfClass(containerType).iterator();
        while (i.hasNext()) {
            Unit container = i.next();
            double remainingCapacity = container.getInventory().getAmountResourceRemainingCapacity(
                    resource, true, false);
            if (remainingCapacity > mostCapacity) {
                result = container;
                mostCapacity = remainingCapacity;
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
        
        // If not at resource collection site location, create walk outside subtask.
        if ((person.getXLocation() != collectionSiteXLoc) || (person.getYLocation() != collectionSiteYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    collectionSiteXLoc, collectionSiteYLoc, false);
            addSubTask(walkingTask);
        }
        else {
            setPhase(COLLECT_RESOURCES);
        }
        
        return time;
    }
    
    /**
     * Perform the walk to rover airlock phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToRoverAirlock(double time) {
        
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
     * Perform the collect resources phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error collecting resources.
     */
    private double collectResources(double time) {

        // Check for an accident during the EVA operation.
        checkForAccident(time);

        // Check if there is reason to cut the collection phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(EVAOperation.ENTER_AIRLOCK);
            return time;
        }

        double remainingPersonCapacity = person.getInventory().getAmountResourceRemainingCapacity(
                resourceType, true, false);
        double currentSamplesCollected = rover.getInventory().getAmountResourceStored(
                resourceType, false) - startingCargo; 
        double remainingSamplesNeeded = targettedAmount - currentSamplesCollected;
        double sampleLimit = remainingPersonCapacity;
        if (remainingSamplesNeeded < remainingPersonCapacity) {
            sampleLimit = remainingSamplesNeeded;
        }

        double samplesCollected = time * collectionRate;

        // Modify collection rate by "Areology" skill.
        int areologySkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
        if (areologySkill == 0) {
            samplesCollected /= 2D;
        }
        if (areologySkill > 1) {
            samplesCollected += samplesCollected * (.2D * areologySkill);
        }

        // Modify collection rate by polar region if ice collecting.
        if (resourceType.equals(AmountResource.findAmountResource("ice"))) {
            if (Simulation.instance().getMars().getSurfaceFeatures().inPolarRegion(person.getCoordinates())) {
                samplesCollected *= 3D;
            }
        }

        // Add experience points
        addExperience(time);

        // Collect resources.
        if (samplesCollected <= sampleLimit) {
            person.getInventory().storeAmountResource(resourceType, samplesCollected, true);
            return 0D;
        }
        else {
            if (sampleLimit >= 0D) {
                person.getInventory().storeAmountResource(resourceType, sampleLimit, true);
            }
            setPhase(WALK_TO_ROVER);
            return time - (sampleLimit / collectionRate);
        }
    }

    /**
     * Perform the enter rover phase of the task.
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     * @throws Exception if error entering rover.
     */
    private double enterRover(double time) {

        time = enterAirlock(time, rover.getAirlock());

        // Add experience points
        addExperience(time);

        if (enteredAirlock) {
            Inventory pInv = person.getInventory();

            if (pInv.containsUnitClass(containerType)) {
                // Load containers in rover.
                Iterator<Unit> i = pInv.findAllUnitsOfClass(containerType).iterator();
                while (i.hasNext()) {
                    Unit container = i.next();
                    pInv.retrieveUnit(container);
                    rover.getInventory().storeUnit(container);
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
     * Checks if a person can perform an CollectResources task.
     * @param person the person to perform the task
     * @param rover the rover the person will EVA from
     * @param containerType the container class to collect resources in.
     * @param resourceType the resource to collect.
     * @return true if person can perform the task.
     */
    public static boolean canCollectResources(Person person, Rover rover, Class containerType, 
            AmountResource resourceType) {

        // Check if person can exit the rover.
        boolean exitable = ExitAirlock.canExitAirlock(person, rover.getAirlock());

        SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();

        // Check if it is night time outside.
        boolean sunlight = surface.getSurfaceSunlight(rover.getCoordinates()) > 0;

        // Check if in dark polar region.
        boolean darkRegion = surface.inDarkPolarRegion(rover.getCoordinates());

        // Check if person's medical condition will not allow task.
        boolean medical = person.getPerformanceRating() < .5D;

        // Checks if available container with remaining capacity for resource.
        Unit container = findLeastFullContainer(rover.getInventory(), containerType, resourceType);
        boolean containerAvailable = (container != null);
        
        // Check if container and full EVA suit can be carried by person or is too heavy.
        double carryMass = 0D;
        if (container != null) {
            carryMass += container.getMass();
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

        return (exitable && (sunlight || darkRegion) && !medical && containerAvailable && canCarryEquipment);
    }

    /**
     * Gets the effective skill level a person has at this task.
     * @return effective skill level
     */
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        int areologySkill = manager.getEffectiveSkillLevel(SkillType.AREOLOGY);
        return (int) Math.round((double)(EVAOperationsSkill + areologySkill) / 2D); 
    }

    /**
     * Gets a list of the skills associated with this task.
     * May be empty list if no associated skills.
     * @return list of skills
     */
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(2);
        results.add(SkillType.EVA_OPERATIONS);
        results.add(SkillType.AREOLOGY);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        rover = null;
        resourceType = null;
        containerType = null;
    }
}