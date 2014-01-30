/**
 * Mars Simulation Project
 * DigLocalRegolith.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * The DigLocalRegolith class is a task for performing
 * collecting regolith outside a settlement.
 */
public class DigLocalRegolith extends EVAOperation implements Serializable {

    private static Logger logger = Logger.getLogger(DigLocalRegolith.class.getName());
    
    // Phase name
    private static final String WALK_TO_SITE = "Walk to Dig Site";
    private static final String COLLECT_REGOLITH = "Collecting Regolith";
    private static final String WALK_TO_AIRLOCK = "Walk to Airlock";
    
    //  Collection rate of regolith during EVA (kg/millisol).
    private static final double COLLECTION_RATE = 20D;
    
    // Domain members
    private Airlock airlock; // Airlock to be used for EVA.
    private Bag bag; // Bag for collecting regolith.
    private Settlement settlement;
    private double diggingXLocation;
    private double diggingYLocation;
    private double enterAirlockXLoc;
    private double enterAirlockYLoc;
    
    /**
     * Constructor
     * @param person the person performing the task.
     * @throws Exception if error constructing the task.
     */
    public DigLocalRegolith(Person person) {
        // Use EVAOperation constructor.
        super("Digging local regolith", person);
        
        settlement = person.getSettlement();
        
        // Get an available airlock.
        airlock = getWalkableAvailableAirlock(person);
        if (airlock == null) {
            endTask();
        }
        
        // Determine digging location.
        Point2D.Double diggingLoc = determineDiggingLocation();
        diggingXLocation = diggingLoc.getX();
        diggingYLocation = diggingLoc.getY();
        
        // Determine location for reentering airlock.
        Point2D enterAirlockLoc = determineAirlockEnteringLocation();
        enterAirlockXLoc = enterAirlockLoc.getX();
        enterAirlockYLoc = enterAirlockLoc.getY();
        
        // Add task phases
        addPhase(WALK_TO_SITE);
        addPhase(COLLECT_REGOLITH);
        addPhase(WALK_TO_AIRLOCK);
        
        logger.finest(person.getName() + " starting DigLocalRegolith task.");
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
            Settlement settlement = person.getSettlement();
            Inventory inv = settlement.getInventory();
            
            try {
                // Factor the value of regolith at the settlement.
                GoodsManager manager = settlement.getGoodsManager();
                AmountResource regolithResource = AmountResource.findAmountResource("regolith");
                double value = manager.getGoodValuePerItem(GoodsUtil.getResourceGood(regolithResource));
                result = value;
                if (result > 100D) result = 100D;
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error checking good value of regolith.");
            }
            
            // Check at least one EVA suit at settlement.
            int numSuits = inv.findNumUnitsOfClass(EVASuit.class);
            if (numSuits == 0) result = 0D;
            
            // Check if at least one empty bag at settlement.
            int numEmptyBags = inv.findNumEmptyUnitsOfClass(Bag.class, false);
            if (numEmptyBags == 0) result = 0D;

            // Check if an airlock is available
            if (getWalkableAvailableAirlock(person) == null) {
                result = 0D;
            }

            // Check if it is night time.
            SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
            if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
                if (!surface.inDarkPolarRegion(person.getCoordinates()))
                    result = 0D;
            } 
            
            // Crowded settlement modifier
            if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) result *= 2D;

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();
            
            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) result *= job.getStartTaskProbabilityModifier(DigLocalRegolith.class);   
        }
    
        return result;
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
            return exitEVA(time);
        }
        else if (WALK_TO_SITE.equals(getPhase())) {
            return walkToDigSitePhase(time);
        }
        else if (COLLECT_REGOLITH.equals(getPhase())) {
            return collectRegolith(time);
        }
        else if (WALK_TO_AIRLOCK.equals(getPhase())) {
            return walkToAirlock(time);
        }
        else if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) {
            return enterEVA(time);
        }
        else {
            return time;
        }
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
        
        // If phase is collect regolith, add experience to areology skill.
        if (COLLECT_REGOLITH.equals(getPhase())) {
            // 1 base experience point per 10 millisols of collection time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double areologyExperience = time / 10D;
            areologyExperience += areologyExperience * experienceAptitudeModifier;
            person.getMind().getSkillManager().addExperience(Skill.AREOLOGY, areologyExperience);
        }
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(2);
        results.add(Skill.EVA_OPERATIONS);
        results.add(Skill.AREOLOGY);
        return results; 
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
        int areologySkill = manager.getEffectiveSkillLevel(Skill.AREOLOGY);
        return (int) Math.round((double)(EVAOperationsSkill + areologySkill) / 2D); 
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
            // Take bag.
            if (bag == null) {
                Bag emptyBag = null;
                Iterator<Unit> i = settlement.getInventory().findAllUnitsOfClass(Bag.class).iterator();
                while (i.hasNext() && (emptyBag == null)) {
                    Bag foundBag = (Bag) i.next();
                    if (foundBag.getInventory().isEmpty(false)) {
                        emptyBag = foundBag;
                    }
                }
                
                if (emptyBag != null) {
                    if (person.getInventory().canStoreUnit(emptyBag, false)) {
                        settlement.getInventory().retrieveUnit(emptyBag);
                        person.getInventory().storeUnit(emptyBag);
                        bag = emptyBag;
                    }
                    else {
                        logger.severe(person.getName() + " unable to carry empty bag");
                    }
                }
                else {
                    logger.severe("Unable to find empty bag in settlement inventory");
                }
            }
            
            if (bag != null) {
                setPhase(WALK_TO_SITE);
            }
            else {
                setPhase(ENTER_AIRLOCK);
            }
        }
        return time;
    }
    
    /**
     * Determine location for digging regolith.
     * @return digging X and Y location outside settlement.
     */
    private Point2D.Double determineDiggingLocation() {
        
        Point2D.Double newLocation = null;
        boolean goodLocation = false;
        for (int x = 0; (x < 5) && !goodLocation; x++) {
            for (int y = 0; (y < 10) && !goodLocation; y++) {
                if (airlock.getEntity() instanceof LocalBoundedObject) {
                    LocalBoundedObject boundedObject = (LocalBoundedObject) airlock.getEntity();
                    
                    double distance = RandomUtil.getRandomDouble(100D) + (x * 100D) + 50D;
                    double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
                    double newXLoc = boundedObject.getXLocation() - (distance * Math.sin(radianDirection));
                    double newYLoc = boundedObject.getYLocation() + (distance * Math.cos(radianDirection));
                    Point2D.Double boundedLocalPoint = new Point2D.Double(newXLoc, newYLoc);
                    
                    newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                            boundedLocalPoint.getY(), boundedObject);
                    goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                            person.getCoordinates());
                }
            }
        }
        
        return newLocation;
    }
    
    /**
     * Perform the walk to dig site phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToDigSitePhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // Check if there is reason to cut the EVA walk phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_AIRLOCK);
            return time;
        }
        
        // If not at construction site location, create walk outside subtask.
        if ((person.getXLocation() != diggingXLocation) || (person.getYLocation() != diggingYLocation)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    diggingXLocation, diggingYLocation, false);
            addSubTask(walkingTask);
        }
        else {
            setPhase(COLLECT_REGOLITH);
        }
        
        return time;
    }
    
    /**
     * Perform the walk to airlock phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToAirlock(double time) {
        
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
            if (bag != null) {
                AmountResource regolithResource = AmountResource.findAmountResource("regolith");
                double collectedAmount = bag.getInventory().getAmountResourceStored(regolithResource, false);
                double settlementCap = settlement.getInventory().getAmountResourceRemainingCapacity(
                        regolithResource, false, false);
                
                // Try to store regolith in settlement.
                if (collectedAmount < settlementCap) {
                    bag.getInventory().retrieveAmountResource(regolithResource, collectedAmount);
                    settlement.getInventory().storeAmountResource(regolithResource, collectedAmount, false);
                }

                // Store bag.
                person.getInventory().retrieveUnit(bag);
                settlement.getInventory().storeUnit(bag);
                
                // Recalculate settlement good value for output item.
                GoodsManager goodsManager = settlement.getGoodsManager();
                goodsManager.updateGoodValue(GoodsUtil.getResourceGood(regolithResource), false);
            }
            endTask();
        }
        return time;
    }
    
    /**
     * Perform collect regolith phase.
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double collectRegolith(double time) {
        
        // Check for an accident during the EVA operation.
        checkForAccident(time);
        
        // Check if there is reason to cut the collection phase short and return
        // to the airlock.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_AIRLOCK);
            return time;
        }
        
        AmountResource regolith = AmountResource.findAmountResource("regolith");
        double remainingPersonCapacity = person.getInventory().getAmountResourceRemainingCapacity(
                regolith, true, false);
        
        double regolithCollected = time * COLLECTION_RATE;
        boolean finishedCollecting = false;
        if (regolithCollected > remainingPersonCapacity) {
            regolithCollected = remainingPersonCapacity;
            finishedCollecting = true;
        }
        
        person.getInventory().storeAmountResource(regolith, regolithCollected, true);
        
        if (finishedCollecting) {
            setPhase(WALK_TO_AIRLOCK);
        }
        
        // Add experience points
        addExperience(time);
        
        return 0D;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        airlock = null;
        bag = null;
        settlement = null;
    }
}