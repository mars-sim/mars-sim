/**
 * Mars Simulation Project
 * DigLocalRegolith.java
 * @version 3.1.0 2017-08-30
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The DigLocalRegolith class is a task for performing
 * collecting regolith outside a settlement.
 */
public class DigLocalRegolith
extends EVAOperation
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(DigLocalRegolith.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalRegolith"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase COLLECT_REGOLITH = new TaskPhase(Msg.getString(
            "Task.phase.collectRegolith")); //$NON-NLS-1$

	/** Collection rate of regolith during EVA (kg/millisol). */
	public static final double COLLECTION_RATE = 1D;

	/** The resource id for a bag. */
//	private static final int BAG  = EquipmentType.convertName2ID("bag");
	
	// Domain members
	/** Total ice collected in kg. */
	private double totalCollected;

	/** Airlock to be used for EVA. */
	private Airlock airlock;
	/** Bag for collecting regolith. */
	private Bag bag;
	private Settlement settlement;

	private static int regolithID = ResourceUtil.regolithID;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 */
	public DigLocalRegolith(Person person) {
        // Use EVAOperation constructor.
        super(NAME, person, false, 10D);

        settlement = person.getAssociatedSettlement();
        
        // Get an available airlock.
        airlock = getWalkableAvailableAirlock(person);
        if (airlock == null) {
            endTask();
        }

        // Determine digging location.
        Point2D.Double diggingLoc = determineDiggingLocation();
        setOutsideSiteLocation(diggingLoc.getX(), diggingLoc.getY());

        // Take bags for collecting regolith.
        if (!hasBags()) {
            takeBag();

            // If bags are not available, end task.
            if (!hasBags()) {
                endTask();
            }
        }

        // Add task phases
        addPhase(COLLECT_REGOLITH);

        logger.finest(person.getName() + " was going to start digging for regolith.");
    }

    /**
     * Checks if the person is carrying any bags.
     * @return true if carrying bags.
     */
    private boolean hasBags() {
        return person.getInventory().containsUnitClass(Bag.class);
    }

    /**
     * Takes an empty bag (preferably) from the rover.
     */
    private void takeBag() {
        Bag aBag = settlement.getInventory().findABag(true);
        if (aBag == null) {
        	// if no empty bag, take any bags
        	aBag = settlement.getInventory().findABag(false);
	    	// Add the equipment demand for a bag
//	    	settlement.getInventory().addEquipmentDemandTotalRequest(BAG, 1);
//	    	settlement.getInventory().addEquipmentDemand(BAG, 1);
        }
        if (aBag != null) {
            if (person.getInventory().canStoreUnit(aBag, false)) {
            	aBag.transfer(settlement, person);
//                settlement.getInventory().retrieveUnit(emptyBag);
//                person.getInventory().storeUnit(emptyBag);
                bag = aBag;
            }
            else {
            	LogConsolidated.log(Level.WARNING, 10_000, sourceName,
    					"[" 
    					+ person.getLocationTag().getLocale()
    					+ "] "  + person.getName() 
    					+ " was strangely unable to carry an empty bag.");
            }
        }
        else {
        	LogConsolidated.log(Level.WARNING, 10_000, sourceName,
					"[" 
					+ person.getLocationTag().getLocale()
					+ "] "  + person.getName() 
					+ " was unable to find an empty bag in the inventory.");
        }
    }

    @Override
    protected TaskPhase getOutsideSitePhase() {
        return COLLECT_REGOLITH;
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     */
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);

        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (COLLECT_REGOLITH.equals(getPhase())) {
            return collectRegolith(time);
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
        person.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);

        // If phase is collect regolith, add experience to areology skill.
        if (COLLECT_REGOLITH.equals(getPhase())) {
            // 1 base experience point per 10 millisols of collection time spent.
            // Experience points adjusted by person's "Experience Aptitude" attribute.
            double areologyExperience = time / 10D;
            areologyExperience += areologyExperience * experienceAptitudeModifier;
            person.getSkillManager().addExperience(SkillType.AREOLOGY, areologyExperience, time);
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
        SkillManager manager = person.getSkillManager();
        int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
        int areologySkill = manager.getEffectiveSkillLevel(SkillType.AREOLOGY);
        return (int) Math.round((double)(EVAOperationsSkill + areologySkill) / 2D);
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

    @Override
    public void endTask() {

        // Unload bag to rover's inventory.
        if (bag != null) {
//            AmountResource regolithID = ResourceUtil.regolithAR;
            double collectedAmount = bag.getInventory().getAmountResourceStored(regolithID, false);
            double settlementCap = settlement.getInventory().getAmountResourceRemainingCapacity(
                    regolithID, false, false);

            // Try to store regolith in settlement.
            if (collectedAmount < settlementCap) {
                bag.getInventory().retrieveAmountResource(regolithID, collectedAmount);
                settlement.getInventory().storeAmountResource(regolithID, collectedAmount, false);
                settlement.getInventory().addAmountSupply(regolithID, collectedAmount);
            }

            // transfer the bag
            bag.transfer(person, settlement);
			// Add to the daily output
			settlement.addOutput(regolithID, collectedAmount, getTimeCompleted());
            // Recalculate settlement good value for output item.
            settlement.getGoodsManager().updateGoodValue(GoodsUtil.getResourceGood(regolithID), false);
        }

        super.endTask();
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

        // 2015-05-29 Check for radiation exposure during the EVA operation.
        if (isRadiationDetected(time)){
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        // Check if there is reason to cut the collection phase short and return
        // to the airlock.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        //AmountResource regolith = AmountResource.findAmountResource("regolith");
        double remainingPersonCapacity = person.getInventory().getAmountResourceRemainingCapacity(
        		regolithID, true, false);

        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int strength = nManager.getAttribute(NaturalAttributeType.STRENGTH);
        int agility = nManager.getAttribute(NaturalAttributeType.AGILITY);
        int eva = person.getSkillManager().getSkillLevel(SkillType.EVA_OPERATIONS);
        
        double regolithCollected = .25 + RandomUtil.getRandomDouble(.25) 
        	* time * COLLECTION_RATE * ((.5 * agility + strength) / 150D) * (eva + .1)/ 5D ;
        totalCollected += regolithCollected;
        
        boolean finishedCollecting = false;
        if (regolithCollected >= remainingPersonCapacity) {
            regolithCollected = remainingPersonCapacity;
            finishedCollecting = true;
        }
        
        person.getInventory().storeAmountResource(regolithID, regolithCollected, true);
        
        // Add penalty to the fatigue
        double factor = 1 - (agility + strength) / 400D;
        double fatigue = person.getPhysicalCondition().getFatigue();
        person.getPhysicalCondition().setFatigue(fatigue + time * factor);
        
        if (finishedCollecting) {
            setPhase(WALK_BACK_INSIDE);

            LogConsolidated.log(Level.INFO, 0, sourceName, 
        		"[" + person.getLocationTag().getLocale() +  "] " +
        		person.getName() + " collected " + Math.round(totalCollected*100D)/100D 
        		+ " kg of regolith outside at " + person.getCoordinates().getFormattedString());
 
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