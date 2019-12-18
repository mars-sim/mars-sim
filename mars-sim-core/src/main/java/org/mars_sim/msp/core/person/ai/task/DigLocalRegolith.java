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

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
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
	public static final double BASE_COLLECTION_RATE = 10D;

	public static final double SMALL_AMOUNT = 0.001;
			
	/** The resource id for a bag. */
//	private static final int BAG  = EquipmentType.convertName2ID("bag");
	
	// Domain members
//	private double duration = 40; // + RandomUtil.getRandomInt(10) - RandomUtil.getRandomInt(10);
	/** Total ice collected in kg. */
	private double totalCollected;

	/** Airlock to be used for EVA. */
	private Airlock airlock;
	/** Bag for collecting regolith. */
//	private Bag bag;
	private Settlement settlement;

	private double compositeRate;
	private double factor = .9;
    
	private boolean ended = false;
	
	private static int regolithID = ResourceUtil.regolithID;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 */
	public DigLocalRegolith(Person person) {
        // Use EVAOperation constructor.
        super(NAME, person, false, 50);
        
     	settlement = CollectionUtils.findSettlement(person.getCoordinates());
     	if (settlement == null) {
     		ended = true;
     		super.endTask();
        	return;
     	}
     	
        // Get an available airlock.
     	if (person.isInside()) {
	        airlock = getWalkableAvailableAirlock(person);
	        if (airlock == null) {
	        	ended = true;
	        	super.endTask();
	        	return;
	        }
     	}

        // Take bags for collecting regolith.
        if (!hasBags() && person.getInventory().findABag(false) == null) {
            takeBag();

            // If bags are not available, end task.
            if (!hasBags()) {
            	if (person.isOutside()){
                    setPhase(WALK_BACK_INSIDE);
                }
            	ended = true;
            	super.endTask();
            	return;
            }
        }

        if (!ended) {
            // Determine digging location.
            Point2D.Double diggingLoc = determineDiggingLocation();
            setOutsideSiteLocation(diggingLoc.getX(), diggingLoc.getY());

	        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
	        int strength = nManager.getAttribute(NaturalAttributeType.STRENGTH);
	        int agility = nManager.getAttribute(NaturalAttributeType.AGILITY);
	        int eva = person.getSkillManager().getSkillLevel(SkillType.EVA_OPERATIONS);
	        
	        factor = .9 * (1 - (agility + strength) / 200D);
	        compositeRate  = BASE_COLLECTION_RATE * ((.5 * agility + strength) / 150D) * (eva + .1);
	        
			// set the boolean to true so that it won't be done again today
	//		person.getPreference().setTaskDue(this, true);
			
	       	// Add task phases
        	addPhase(COLLECT_REGOLITH);
        	
//	        setPhase(WALK_TO_OUTSIDE_SITE);
        	
//	        logger.info(person.getName() + " was going to start digging for regolith.");
        }
    }

	   /**
     * Perform collect regolith phase.
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double collectRegolith(double time) {

    	if (getTimeCompleted() > getDuration()) {
    		if (person.isOutside())
    			setPhase(WALK_BACK_INSIDE);
            return time;
    	}
    	
        // Check for an accident during the EVA operation.
        checkForAccident(time);

        // Check for radiation exposure during the EVA operation.
        if (person.isOutside() && isRadiationDetected(time)){
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        // Check if there is reason to cut the collection phase short and return
        // to the airlock.
        if (person.isOutside() && shouldEndEVAOperation()) {
            setPhase(WALK_BACK_INSIDE);
            return time;
        }

        if (person.isInside()) {
            setPhase(WALK_TO_OUTSIDE_SITE);
            return time;
        }
        
        else {
        	Inventory pInv = person.getInventory();
	        Inventory bInv = pInv.findABag(false).getInventory();
	        
	        double collected = RandomUtil.getRandomDouble(2) * time * compositeRate;
	        
			// Modify collection rate by "Areology" skill.
			int areologySkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY); 
			if (areologySkill == 0) {
				collected /= 2D;
			}
			if (areologySkill >= 1) {
				collected = collected + .2 * collected * areologySkill;
			}
			
//			LogConsolidated.log(Level.INFO, 0, sourceName, 
//	        		"[" + person.getLocationTag().getLocale() +  "] " +
//	        		person.getName() + " just collected " + Math.round(collected*100D)/100D 
//	        		+ " kg regolith outside at " + person.getCoordinates().getFormattedString());
			
	        boolean finishedCollecting = false;
	        
	        double personRemainingCap = bInv.getAmountResourceRemainingCapacity(
	        		regolithID, false, false);
	        
	        double bagRemainingCap = bInv.getAmountResourceRemainingCapacity(
	        		regolithID, false, false);
	
	        if (personRemainingCap < SMALL_AMOUNT) {
//	        	logger.info(person + " case 1");
	            finishedCollecting = true;
	            collected = 0;
	        }
	        
	        else if (bagRemainingCap < SMALL_AMOUNT) {
//	        	logger.info(person + " case 2");
	            finishedCollecting = true;
	            collected = 0;
	        }
	        		
	        else if (//totalCollected + collected >= bInv.getGeneralCapacity()
	        		totalCollected + collected >= pInv.getGeneralCapacity()) {    
//	        	logger.info(person + " case 3 (" + bInv.getGeneralCapacity() + ", " + pInv.getGeneralCapacity());
	            finishedCollecting = true;
	            collected = bagRemainingCap;
	        }
	        
	        else if (collected > SMALL_AMOUNT 
	        		&& (collected >= bagRemainingCap || collected >= personRemainingCap)) {
//	        	logger.info(person + " case 4");
	        	finishedCollecting = true;
	        	collected = bagRemainingCap;    	
	        }    		

	        if (collected > 0) {
	        	totalCollected += collected;
	        	bInv.storeAmountResource(regolithID, collected, true);
	        }
	        
	        PhysicalCondition condition = person.getPhysicalCondition();
	        double stress = condition.getStress();
	        double fatigue = condition.getFatigue();
	        
	        // Add penalty to the fatigue
	        condition.setFatigue(fatigue + time * factor);
	        
	        // Add experience points
	        addExperience(time);
	        
	        if (finishedCollecting && totalCollected > 0) {
	            LogConsolidated.log(Level.INFO, 0, sourceName, 
	        		"[" + person.getLocationTag().getLocale() +  "] " +
	        		person.getName() + " collected a total of " + Math.round(totalCollected*100D)/100D 
	        		+ " kg regolith outside at " + person.getCoordinates().getFormattedString());
	            if (person.isOutside()) {
	            	setPhase(WALK_BACK_INSIDE);
	            }
	            else if (person.isInside()) {
	        		endTask();
	            }
	    	}
	        
	        if (fatigue > 1000 || stress > 50) {
	            LogConsolidated.log(Level.INFO, 0, sourceName, 
	        		"[" + person.getLocationTag().getLocale() +  "] " +
	        		person.getName() + " took a break from collecting regolith ("
	        		+ Math.round(totalCollected*100D)/100D + " kg collected) " 
	        		+ "; fatigue: " + Math.round(fatigue*10D)/10D 
	        		+ "; stress: " + Math.round(stress*100D)/100D + " %");
	            if (person.isOutside()) {
	            	setPhase(WALK_BACK_INSIDE);
	            }
	            else if (person.isInside()) {
	        		endTask();
	            }
	        }
        }
        
        return 0D;
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
//                bag = aBag;
            }
            else {
            	LogConsolidated.log(Level.WARNING, 10_000, sourceName,
    					"[" 
    					+ person.getLocationTag().getLocale()
    					+ "] "  + person.getName() 
    					+ " was strangely unable to carry an empty bag.");
            	ended = true;
            	super.endTask();
            }
        }
        else {
        	LogConsolidated.log(Level.WARNING, 10_000, sourceName,
					"[" 
					+ person.getLocationTag().getLocale()
					+ "] "  + person.getName() 
					+ " was unable to find an empty bag in the inventory.");
        	ended = true;
        	super.endTask();
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
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
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
    	ended = true;
    	Inventory pInv = person.getInventory();
    	Bag bag = pInv.findABag(false);
    	
    	// Unload bag to rover's inventory.
//        if (bag != null) {
//            Inventory bInv = bag.getInventory();
//            double reg0 = bInv.getAmountResourceStored(regolithID, false);
            double reg1 = pInv.getAmountResourceStored(regolithID, false);

          	Inventory sInv = settlement.getInventory();
          	
            double settlementCap = sInv.getAmountResourceRemainingCapacity(
                    regolithID, false, false);
            
            if (sInv != null) {
	            // Try to store regolith in settlement.
	            if (reg1 <= settlementCap) {
//	                bInv.retrieveAmountResource(regolithID, reg0);
	                pInv.retrieveAmountResource(regolithID, reg1);
	                // Store the ice
	                sInv.storeAmountResource(regolithID, reg1, false);
	                // Track supply
	                sInv.addAmountSupply(regolithID, reg1);
		            // Transfer the bag
		            bag.transfer(person, settlement);
					// Add to the daily output
					settlement.addOutput(regolithID, reg1, getTimeCompleted());
		            // Recalculate settlement good value for output item.
		            settlement.getGoodsManager().updateGoodValue(GoodsUtil.getResourceGood(regolithID), false);
	            }
            }
//        }

        super.endTask();
    }

 
    @Override
    public void destroy() {
        super.destroy();

        airlock = null;
//        bag = null;
        settlement = null;
    }
}