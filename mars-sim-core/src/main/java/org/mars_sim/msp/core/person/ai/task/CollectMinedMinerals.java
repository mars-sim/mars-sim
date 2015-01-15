/**
 * Mars Simulation Project
 * CollectMinedMinerals.java
 * @version 3.07 2014-09-22
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupport;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttribute;
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
    
    private static Logger logger = Logger.getLogger(CollectMinedMinerals.class.getName());
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.collectMinedMinerals"); //$NON-NLS-1$
    
    /** Task phases. */
    private static final TaskPhase COLLECT_MINERALS = new TaskPhase(Msg.getString(
            "Task.phase.collectMinerals")); //$NON-NLS-1$
    
    /** Rate of mineral collection (kg/millisol). */
    private static final double MINERAL_COLLECTION_RATE = 10D;

    // Data members
    private Rover rover; // Rover used.
    protected AmountResource mineralType; 

    /**
     * Constructor
     * @param person the person performing the task.
     * @param rover the rover used for the EVA operation.
     * @param mineralType the type of mineral to collect.
     */
    public CollectMinedMinerals(Person person, Rover rover, AmountResource mineralType) {

        // Use EVAOperation parent constructor.
        super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D);

        // Initialize data members.
        this.rover = rover;
        this.mineralType = mineralType;

        // Determine location for collection site.
        Point2D collectionSiteLoc = determineCollectionSiteLocation();
        setOutsideSiteLocation(collectionSiteLoc.getX(), collectionSiteLoc.getY());
        
        // Take bags for collecting mined minerals.
        if (!hasBags()) {
            takeBag();
            
            // If bags are not available, end task.
            if (!hasBags()) {
                logger.fine(person.getName() + " not able to find bag to collect mined minerals.");
                endTask();
            }
        }
        
        // Add task phases
        addPhase(COLLECT_MINERALS);
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
    
    @Override
    protected TaskPhase getOutsideSitePhase() {
        return COLLECT_MINERALS;
    }
    
    @Override
    protected double performMappedPhase(double time) {
        
        time = super.performMappedPhase(time);
        
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (COLLECT_MINERALS.equals(getPhase())) {
            return collectMineralsPhase(time);
        }
        else {
            return time;
        }
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

        // Check if site duration has ended or there is reason to cut the collect 
        // minerals phase short and return to the rover.
        if (shouldEndEVAOperation() || addTimeOnSite(time)) {
            setPhase(WALK_BACK_INSIDE);
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
		// 2015-01-15 Add addSupplyAmount()
        // not calling person.getInventory().addSupplyAmount(mineralType, mineralsCollected);
        mission.collectMineral(mineralType, mineralsCollected);
        if (((mineralsExcavated - mineralsCollected) <= 0D) || 
                (mineralsCollected >= remainingPersonCapacity)) {
            setPhase(WALK_BACK_INSIDE);
        }

        return 0D;
    }
    
    @Override
    public void endTask() {
        
        // Unload bag to rover's inventory.
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
        
        super.endTask();
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
            AmountResource oxygenResource = AmountResource.findAmountResource(LifeSupport.OXYGEN);
            carryMass += suit.getInventory().getAmountResourceRemainingCapacity(oxygenResource, false, false);
            AmountResource waterResource = AmountResource.findAmountResource(LifeSupport.WATER);
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
        int experienceAptitude = nManager.getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
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
    public void destroy() {
        super.destroy();

        rover = null;
        mineralType = null;
    }
}