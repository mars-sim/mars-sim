/**
 * Mars Simulation Project
 * DigLocalIce.java
 * @version 3.1.0 2017-08-30
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

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The DigLocalIce class is a task for performing
 * collecting ice outside of a settlement.
 */
public class DigLocalIce
extends EVAOperation
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(DigLocalIce.class.getName());
	
    private static String sourceName = logger.getName();
    
	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase COLLECT_ICE = new TaskPhase(Msg.getString(
            "Task.phase.collectIce")); //$NON-NLS-1$

	/**  Collection rate of ice during EVA (kg/millisol). */
	private static final double COLLECTION_RATE = 20D;

	// Domain members
	/** Total ice collected in kg. */
	private double totalCollected;
	
	/** Airlock to be used for EVA. */
	private Airlock airlock;
	/** Bag for collecting ice. */
	private Bag bag;
	private Settlement settlement;

	private static int iceID = ResourceUtil.iceID;
	
	/**
	 * Constructor.
	 * @param person the person performing the task.
	 * @throws Exception if error constructing the task.
	 */
	public DigLocalIce(Person person) {
        // Use EVAOperation constructor.
        super(NAME, person, false, 10D);

        sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());

        settlement = person.getAssociatedSettlement();

        // Get an available airlock.
        airlock = getWalkableAvailableAirlock(person);
        if (airlock == null) {
            endTask();
        }

        // Determine digging location.
        Point2D.Double diggingLoc = determineDiggingLocation();
        setOutsideSiteLocation(diggingLoc.getX(), diggingLoc.getY());

        // Take bags for collecting ice.
        if (!hasBags()) {
            takeBag();

            // If bags are not available, end task.
            if (!hasBags()) {
                logger.fine(person.getName() + " not able to find bag to collect ice.");
                endTask();
            }
        }

        // Add task phases
        addPhase(COLLECT_ICE);

        logger.finest(person.getName() + " starting DigLocalIce task.");
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
     */
    private void takeBag() {
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

    @Override
    protected TaskPhase getOutsideSitePhase() {
        return COLLECT_ICE;
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);

        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (COLLECT_ICE.equals(getPhase())) {
            return collectIce(time);
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
        int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
        double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
        evaExperience += evaExperience * experienceAptitudeModifier;
        evaExperience *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience);

        // If phase is collect ice, add experience to areology skill.
        if (COLLECT_ICE.equals(getPhase())) {
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

    /**
     * Determine location for digging ice.
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

    @Override
    public void endTask() {

        // Unload bag to rover's inventory.
        if (bag != null) {
            double collectedAmount = bag.getInventory().getAmountResourceStored(iceID, false);
            double settlementCap = settlement.getInventory().getAmountResourceRemainingCapacity(
            		iceID, false, false);

            // Try to store ice in settlement.
            if (collectedAmount < settlementCap) {
                bag.getInventory().retrieveAmountResource(iceID, collectedAmount);
                settlement.getInventory().storeAmountResource(iceID, collectedAmount, false);
                settlement.getInventory().addAmountSupplyAmount(iceID, collectedAmount);
            }

            // Store bag.
            person.getInventory().retrieveUnit(bag);
            settlement.getInventory().storeUnit(bag);

            // Recalculate settlement good value for output item.
            GoodsManager goodsManager = settlement.getGoodsManager();
            goodsManager.updateGoodValue(GoodsUtil.getResourceGood(iceID), false);
        }

        super.endTask();
    }

    /**
     * Perform collect ice phase.
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     */
    private double collectIce(double time) {

        // Check for an accident during the EVA operation.
        checkForAccident(time);

        // Check for radiation exposure during the EVA operation.
        if (isRadiationDetected(time)){
            setPhase(WALK_BACK_INSIDE);
            return time;
        }
        // Check if there is reason to cut the collect
        // ice phase short and return.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        //AmountResource ice = AmountResource.findAmountResource("ice");
        double remainingPersonCapacity = person.getInventory().getAmountResourceRemainingCapacity(
        		iceID, true, false);

        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int strength = nManager.getAttribute(NaturalAttributeType.STRENGTH);
        int agility = nManager.getAttribute(NaturalAttributeType.AGILITY);
        int eva = person.getMind().getSkillManager().getSkillLevel(SkillType.EVA_OPERATIONS);
        
        double iceCollected = RandomUtil.getRandomDouble(.5) * time * COLLECTION_RATE * ((.5 * agility + strength) / 150D) * (eva + .1)/ 3D ;
        totalCollected += iceCollected;
        
        boolean finishedCollecting = false;
        if (iceCollected >= remainingPersonCapacity) {
            iceCollected = remainingPersonCapacity;
            finishedCollecting = true;
        }

        person.getInventory().storeAmountResource(iceID, iceCollected, true);

        if (finishedCollecting) {
            setPhase(WALK_BACK_INSIDE);

            LogConsolidated.log(logger, Level.INFO, 0, sourceName, 
	    		"[" + person.getLocationTag().getQuickLocation() +  "] " +
	    		person.getName() + " collected " + Math.round(totalCollected*100D)/100D 
	    		+ " kg of ice outside " + person.getAssociatedSettlement(), null);
 
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