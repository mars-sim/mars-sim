/*
 * Mars Simulation Project
 * DigLocalRegolith.java
 * @date 2022-06-28
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.goods.GoodsUtil;
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
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The DigLocal class is a task for collecting a resource outside a settlement.
 */
public abstract class DigLocal
extends EVAOperation
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(DigLocal.class.getName());

	public static final double SMALL_AMOUNT = 0.00001;

	// Resource being collected
	private int resourceID;

	private double compositeRate;

	private double factor;

	/** Total resource collected in kg. */
	private double totalCollected;

	private String resourceName;
	
	/** Airlock to be used for EVA. */
	private Airlock airlock;

	private Settlement settlement;

	private TaskPhase collectionPhase;

	private LocalPosition diggingLoc;
	
	private EquipmentType containerType;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 */
	public DigLocal(String name, TaskPhase collectionPhase, int resourceID,
					EquipmentType containerType, Person person) {
        // Use EVAOperation constructor.
        super(name, person, false, 100, SkillType.AREOLOGY);

        this.containerType = containerType;
        this.resourceID = resourceID;
        this.resourceName = ResourceUtil.findAmountResourceName(resourceID);
        this.collectionPhase = collectionPhase;

        if (!person.isFit()) {
			checkLocation();
			return;
		}

        // To dig local a person must be in a Settlement
        if (!person.isInSettlement()) {
        	logger.warning(person, "Not in a settlement to start a DigLocal Task");
        	endTask();
        }

     	settlement = CollectionUtils.findSettlement(person.getCoordinates());
     	if (settlement == null)
     		endTask();

        // Get an available airlock in a settlement
     	if (person.isInSettlement()) {
	        airlock = getWalkableAvailableEgressAirlock(person);
	        if (airlock == null) {
	        	logger.log(person, Level.WARNING, 4_000, "No walkable airlock for egress.");
			    endTask();
	        }
     	}

        // Take bags for collecting resource.
        // If bags are not available, end task.
        if (transferContainer() == null) {
        	logger.log(person, Level.WARNING, 4_000, "No " + containerType.name()
        				+ " for " + resourceName + " are available.");
        	checkLocation();
        	return;
        }

        // Determine digging location.
        if (diggingLoc == null) {
        	diggingLoc = determineDiggingLocation();
	        if (diggingLoc != null) {
	        	setOutsideSiteLocation(diggingLoc);
//	           	logger.info(person, 4_000L, "Selected an outside digging site at " + diggingLoc + ".");
	        }
	        else {
	        	checkLocation();
	        	return;
	        }
        }

       	// Add task phases
    	addPhase(collectionPhase);

        setPhase(WALK_TO_OUTSIDE_SITE);
    }
	
	/**
	 * Gets the settlement where digging is taking place.
	 * 
	 * @return
	 */
	protected Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Sets the collection rate for the resource.
	 * 
	 * @param collectionRate
	 */
	protected void setCollectionRate(double collectionRate) {
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        int strength = nManager.getAttribute(NaturalAttributeType.STRENGTH);
        int agility = nManager.getAttribute(NaturalAttributeType.AGILITY);
        int eva = person.getSkillManager().getSkillLevel(SkillType.EVA_OPERATIONS);

        factor = .9 * (1 - (agility + strength) / 200D);
		compositeRate = collectionRate * ((.5 * agility + strength) / 150D) * (eva + .1);
	}

    /**
     * Performs the method mapped to the task's current phase.
     * 
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     */
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);
		if (!isDone()) {
	        if (getPhase() == null) {
	        	throw new IllegalArgumentException("Task phase is null");
	        }
	        else if (collectionPhase.equals(getPhase())) {
	            time = collectResource(time);
	        }
		}
        return time;
    }


	/**
     * Performs collect resource phase.
     *
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double collectResource(double time) {

		if (checkReadiness(time) > 0)
			return time;
		
     	if (person.isInSettlement()) {
     		endTask();
     		return 0;
     	}

     	// Get a container
        Container aBag = person.findContainer(containerType, false, resourceID);
        if (aBag == null) {
        	logger.log(person, Level.WARNING, 4_000, "Has no " + containerType.getName()
        			+ " for " + resourceName);
        	checkLocation();
        	return 0;
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

        if (collected > SMALL_AMOUNT) {
            double bagCap = aBag.getAmountResourceRemainingCapacity(resourceID);
            if (bagCap < collected) {
            	collected = bagCap;
    			finishedCollecting = true;
            }
        	aBag.storeAmountResource(resourceID, collected);
	     	totalCollected += collected;
        }

        if (!finishedCollecting) {
        	double loadCap = person.getCarryingCapacity();
            if (totalCollected >= loadCap) {
            	totalCollected = loadCap;
    			finishedCollecting = true;
    		}
        }

        if (!finishedCollecting) {
        	// Can not physically carry any more
	        finishedCollecting = (person.getRemainingCarryingCapacity() <= 0);
		}

        PhysicalCondition condition = person.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double strengthMod = condition.getStrengthMod();
        double skillMod = person.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);		
        		
        // Add penalty to the fatigue
        condition.setFatigue(fatigue + time * factor * (1.1D - strengthMod)/skillMod);

        // Add experience points
        addExperience(time);

        if (finishedCollecting) {
            logger.log(person, Level.FINE, 4_000, "Collected a total of "
            	+ Math.round(totalCollected*100D)/100D
        		+ " kg " + resourceName + ".");
            checkLocation();
         	return 0;
    	}

	    // Check for an accident during the EVA operation.
	    checkForAccident(time);

        return 0D;
    }

    /**
     * Transfers an empty bag from a settlement to a person.
     * 
     * @return a bag
     */
    private Container transferContainer() {
    	// Note: should take a bag before leaving the airlock
    	// Note: also consider dropping off the resource in a shed
    	// or a shed outside of the workshop/landerhab for processing
        Container aBag = person.findContainer(containerType, false, resourceID);
        if (aBag == null) {
        	// Doesn't have a Bag
        	aBag = settlement.findContainer(containerType, true, resourceID);
	        if (aBag != null) {
            	boolean successful = aBag.transfer(person);
            	if (!successful) {
            		aBag = null;
                	logger.log(person, Level.WARNING, 10_000, "Strangely unable to transfer an empty bag for " + resourceName + ".");
                	endTask();
                }
	        }
	        else {
	        	logger.log(person, Level.WARNING, 10_000, "Unable to find an empty bag in the inventory for " + resourceName + ".");
	        	endTask();
	        }
        }
        return aBag;
    }

    @Override
    protected TaskPhase getOutsideSitePhase() {
        return collectionPhase;
    }

    /**
     * Determines location for digging regolith.
     * 
     * @return digging X and Y location outside settlement.
     */
    private LocalPosition determineDiggingLocation() {
        boolean goodLocation = false;
        for (int x = 0; (x < 5) && !goodLocation; x++) {
            for (int y = 0; (y < 10) && !goodLocation; y++) {
                if (airlock.getEntity() instanceof LocalBoundedObject) {
                    LocalBoundedObject boundedObject = (LocalBoundedObject) airlock.getEntity();

                    double distance = RandomUtil.getRandomDouble(100D) + (x * 100D) + 50D;
                    double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
                    LocalPosition boundedLocalPoint = boundedObject.getPosition().getPosition(distance, radianDirection);

                    LocalPosition newLocation = LocalAreaUtil.getLocalRelativePosition(boundedLocalPoint, boundedObject);
                    goodLocation = LocalAreaUtil.isPositionCollisionFree(newLocation, person.getCoordinates());
                    
                    if (goodLocation)
                    	return newLocation;
                }
            }
        }

        return null;
    }

    /**
     * Closes out this task. If person is inside then transfer the resource from the bag to the Settlement.
     */
    @Override
    protected void clearDown() {
		if (person.isOutside())
            setPhase(WALK_BACK_INSIDE);
    	else {
	    	Container bag = person.findContainer(containerType, false, resourceID);
	    	if (bag == null)
	    		return;

            double amount = bag.getAmountResourceStored(resourceID);

            if (amount > 0) {

	            double settlementCap = settlement.getAmountResourceRemainingCapacity(resourceID);

	            if (amount > settlementCap) {
	            	amount = settlementCap;

	            	logger.warning(person,
	            			resourceName + " storage full. Could only check in "
	            			+ Math.round(amount*10.0)/10.0 + " kg.");
	            }

	            else {
	            	logger.fine(person,
	            			"Checking in " + Math.round(amount*10.0)/10.0 + " kg " + resourceName + ".");
	            }

                // Transfer the bag
                bag.transfer(settlement);
				// Add to the daily output
				settlement.addOutput(resourceID, amount, getTimeCompleted());
	            // Recalculate settlement good value for output item.
	            settlement.getGoodsManager().determineGoodValueWithSupply(GoodsUtil.getGood(resourceID), amount);
            }
    	}
    }
}
