/*
 * Mars Simulation Project
 * DigLocalIce.java
 * @date 2021-08-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
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
	private static SimLogger logger = SimLogger.getLogger(DigLocalIce.class.getName());
	
	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.digLocalIce"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase COLLECT_ICE = new TaskPhase(Msg.getString(
            "Task.phase.collectIce")); //$NON-NLS-1$

	public static final double SMALL_AMOUNT = 0.00001;
	
	private boolean ended = false;
	
	private double compositeRate;
	
    private double factor;
	/**  Collection rate of ice during EVA (kg/millisol). */
	private double collectionRate;
	/** Total ice collected in kg. */
	private double totalCollected;
	/** The resource name. */
	private String resourceString = "ice";
	/** Airlock to be used for EVA. */
	private Airlock airlock;
	/** The Settlement vicinity for collecting ice. */
	private Settlement settlement;

	private static int resourceID = ResourceUtil.iceID;
	
	/**
	 * Constructor.
	 * @param person the person performing the task.
	 * @throws Exception if error constructing the task.
	 */
	public DigLocalIce(Person person) {
        // Use EVAOperation constructor.
        super(NAME, person, false, 20, SkillType.AREOLOGY);
        
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
	      	return;
		}
				
     	settlement = CollectionUtils.findSettlement(person.getCoordinates());
     	if (settlement == null) {
     		ended = true;
        	endTask();
	      	return;
     	}
     	else
            collectionRate = settlement.getIceCollectionRate();
        
        // Get an available airlock.
     	if (person.isInside()) {
	        airlock = getWalkableAvailableAirlock(person);
	        if (airlock == null) {
	        	ended = true;
	        	endTask();
		      	return;
	        }
     	}

        // Take bags for collecting resource.
     	Bag aBag = transferBag();

        // If bags are not available, end task.
        if (aBag == null) {
        	logger.log(person, Level.INFO, 4_000, "No bags for " + resourceString + " are available."); 
        	if (person.isOutside()){
                setPhase(WALK_BACK_INSIDE);
            }
        	else {
            	ended = true;
            	endTask();
        	}
	      	return;
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
	        compositeRate = 10 * collectionRate * ((.5 * agility + strength) / 150D) * (eva + .1);
//	        logger.log(person, Level.INFO, 4_000, "compositeRate for ice: " + compositeRate);
	        
	        // Add task phases
	        addPhase(COLLECT_ICE);

	        setPhase(WALK_TO_OUTSIDE_SITE);
        	
//	        logger.log(person, Level.INFO, 4_000, "Started digging for ice.");
        }
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);
		if (!isDone()) {
	        if (getPhase() == null) {
				throw new IllegalArgumentException("Task phase is null");
	        }
	        else if (COLLECT_ICE.equals(getPhase())) {
	            time = collectIce(time);
	        }
		}
        return time;
    }
    
    /**
     * Perform collect ice phase.
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     */
    private double collectIce(double time) {
    	
		// Check for radiation exposure during the EVA operation.
		if (isDone() || isRadiationDetected(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}
		
        // Check if there is any EVA problems and if on-site time is over.
		if (shouldEndEVAOperation() || addTimeOnSite(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}
		
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}

        double collected = time * compositeRate;
        
		// Modify collection rate by "Areology" skill.
		int areologySkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY); 
		if (areologySkill >= 1) {
			collected = collected + .1 * collected * areologySkill;
		}
		else {
			collected /= 1.5D;
		}
		
        boolean finishedCollecting = false;
  
        Bag aBag = person.getInventory().findABag(false, resourceID);
        
        if (aBag == null) {
        	logger.log(person, Level.INFO, 4_000, "No bags for " + resourceString + " are available."); 
        	if (person.isOutside())
            	setPhase(WALK_BACK_INSIDE);
            else {
            	ended = true;
            	endTask();
            }
        	return 0;
        }
        
        double bagRemainingCap = aBag.getAmountResourceRemainingCapacity(
        		resourceID);
     
		if (collected >= bagRemainingCap) {
			collected = bagRemainingCap;
			finishedCollecting = true;
		}

        if (collected > SMALL_AMOUNT) {
        	aBag.storeAmountResource(resourceID, collected);
        }
        
        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double strengthMod = condition.getStrengthMod();
        
        // Add penalty to the fatigue
        condition.setFatigue(fatigue + time * factor * (1.1D - strengthMod));
        
        // Add experience points
        addExperience(time);
        
        if (finishedCollecting && totalCollected > 0 && collected > 0) {
        	totalCollected += collected;
        	
            logger.log(person, Level.INFO, 4_000, "Collected a total of " 
            	+ Math.round(totalCollected*100D)/100D 
            	+ " kg " + resourceString + ".");
         
            if (person.isOutside())
            	setPhase(WALK_BACK_INSIDE);
            else {
            	ended = true;
            	endTask();
         		return 0;
            }
    	}
        
     	if (person.isInSettlement()) {
//            logger.log(person, Level.INFO, 4_000, "Had already been back to the settlement."); 
        	ended = true;
        	endTask();
     		return 0;
     	}
     	
	    // Check for an accident during the EVA operation.
	    checkForAccident(time);
	    
        return 0D;
    }

    /**
     * Checks if the person is carrying any bags.
     * @return true if carrying bags.
     */
    private boolean hasBags() {
        return person.getInventory().hasABag(false, resourceID);
    }

    /**
     * Transfers an empty bag from a settlement to a person
     */
    private Bag transferBag() {
    	// Note: should take a bag before leaving the airlock
    	// Note: also consider dropping off the resource in a shed 
    	// or a shed outside of the workshop/landerhab for processing
        Bag aBag = settlement.getInventory().findABag(true, resourceID);
        if (aBag != null) {
            if (person.getInventory().canStoreUnit(aBag, false)) {
             	boolean successful = aBag.transfer(settlement, person);
            	if (successful) {
//            		logger.log(person, Level.INFO, 10_000, "Just obtained an empty bag for " + resourceString + ".");
            		return aBag;
            	}
            	else {
                	logger.log(person, Level.WARNING, 10_000, "Strangely unable to transfer an empty bag for " + resourceString + ".");
                	ended = true;
                	super.endTask();
                }
            }
            else {
            	logger.log(person, Level.WARNING, 10_000, "Strangely unable to carry an empty bag for " + resourceString + ".");
            	ended = true;
            	super.endTask();
            }
        }
        else {
        	logger.log(person, Level.WARNING, 10_000, "Unable to find an empty bag in the inventory for " + resourceString + ".");
        	ended = true;
        	super.endTask();
        }
        return null;
    }
    
    @Override
    protected TaskPhase getOutsideSitePhase() {
        return COLLECT_ICE;
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
                    goodLocation = LocalAreaUtil.isLocationCollisionFree(newLocation.getX(), newLocation.getY(),
                            person.getCoordinates());
                }
            }
        }

        return newLocation;
    }

    /**
     * Closes out this task. If person is inside then transfer the resource from the bag to the Settlement
     */
    @Override
    protected void clearDown() {
    	if (person.isOutside()) {
    		setPhase(WALK_BACK_INSIDE);
    	}
    	
    	else {
	    	Inventory pInv = person.getInventory();
	    	Bag bag = pInv.findABag(false, resourceID);
	    	if (bag == null)
	    		return;
	    	
            double amount = bag.getAmountResourceStored(resourceID);
//            logger.log(person, Level.INFO, 0,
//            			"Had " + Math.round(amount*10.0)/10.0 + " kg of "
//            			+ resourceString + ".");
            if (amount > 0) {
	        	Inventory sInv = settlement.getInventory();
	        	
	            double settlementCap = sInv.getAmountResourceRemainingCapacity(
	            		resourceID, true, false);

	            if (bag != null && sInv != null) {
		            if (amount > settlementCap) {
		            	amount = settlementCap;
		            	
		            	logger.log(person, Level.INFO, 0,
		            			resourceString + " storage full. Could only check in " 
		            			+ Math.round(amount*10.0)/10.0 + " kg.");
		            }
		            
		            else {
		            	logger.log(person, Level.INFO, 0, 
		            			"Checking in " + Math.round(amount*10.0)/10.0 + " kg " + resourceString + ".");	
		            }
//		            // Retrieve the resource from the person
//	            	pInv.retrieveAmountResource(resourceID, amount);
//	                // Store the resource in the settlement
//	                sInv.storeAmountResource(resourceID, amount, true);
	                // Track supply
	                sInv.addAmountSupply(resourceID, amount);
	                // Transfer the bag
	                bag.transfer(person, sInv);
					// Add to the daily output
					settlement.addOutput(resourceID, amount, getTimeCompleted());
		            // Recalculate settlement good value for output item.
		            settlement.getGoodsManager().determineGoodValueWithSupply(GoodsUtil.getResourceGood(resourceID), amount);
	            }
            }
    	}
    }
}
