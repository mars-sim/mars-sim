/*
 * Mars Simulation Project
 * DigLocalRegolith.java
 * @date 2021-10-21
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.AirlockType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The DigLocal class is a task for performing
 * collecting a resource outside a settlement.
 */
public abstract class DigLocal
extends EVAOperation
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(DigLocal.class.getName());

	public static final double SMALL_AMOUNT = 0.00001;

	private double compositeRate;

	private double factor;

	/** Total resource collected in kg. */
	private double totalCollected;

	/** Airlock to be used for EVA. */
	private Airlock airlock;

	private Settlement settlement;

	private TaskPhase collectionPhase;

	// Resource being collected
	private int resourceID;
	private String resourceName;

	private EquipmentType containerType;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 */
	public DigLocal(String name, TaskPhase collectionPhase, int resourceID,
					EquipmentType containerType, Person person) {
        // Use EVAOperation constructor.
        super(name, person, false, 20, SkillType.AREOLOGY);

        this.containerType = containerType;
        this.resourceID = resourceID;
        this.resourceName = ResourceUtil.findAmountResourceName(resourceID);
        this.collectionPhase = collectionPhase;

        if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
	      	return;
		}

        // To dig local a person must be in a Settlement
        if (!person.isInSettlement()) {
        	logger.warning(person, "Is not in a settlement to start a DigLocal Task");
        	endTask();
        	return;
        }

     	settlement = CollectionUtils.findSettlement(person.getCoordinates());
     	if (settlement == null) {
     		endTask();
     		return;
     	}

        // Get an available airlock in a settlement
     	if (person.isInside()) {
	        airlock = getWalkableAvailableAirlock(person);
	        if (airlock == null) {
	        	endTask();
	     		return;
	        }
	        else if (airlock.getAirlockType() == AirlockType.BUILDING_AIRLOCK && !airlock.addReservation(person.getIdentifier())) {
			    endTask();
			    return;
	        }
     	}

        // Take bags for collecting resource.
     	Container	aBag = transferContainer();

        // If bags are not available, end task.
        if (aBag == null) {
        	logger.log(person, Level.WARNING, 4_000, "No " + containerType.name()
        				+ " for " + resourceName + " are available.");

        	if (person.isOutside()){
                setPhase(WALK_BACK_INSIDE);
            }
        	else {
            	endTask();
        	}
	      	return;
        }


        // Determine digging location.
        Point2D.Double diggingLoc = determineDiggingLocation();
        setOutsideSiteLocation(diggingLoc.getX(), diggingLoc.getY());

		// set the boolean to true so that it won't be done again today
//		person.getPreference().setTaskDue(this, true);

     // Determine storage bin location.
//            Point2D.Double binLoc = determineBinLocation();
//            setBinLocation(binLoc.getX(), binLoc.getY());

       	// Add task phases
    	addPhase(collectionPhase);
//        	addPhase(WALK_TO_BIN);
//        	addPhase(DROP_OFF_RESOURCE);
        setPhase(WALK_TO_OUTSIDE_SITE);
    }

	/**
	 * Get the settlement where digging is taking place
	 * @return
	 */
	protected Settlement getSettlement() {
		return settlement;
	}

	/**
	 * This set the colleciton trate for the resource.
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
     * Perform collect resource phase.
     *
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double collectResource(double time) {

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

     	if (person.isInSettlement()) {
     		endTask();
     		return 0;
     	}

     	// Get a container
        Container aBag = person.findContainer(containerType, false, resourceID);
        if (aBag == null) {
        	logger.log(person, Level.WARNING, 4_000, "Has no " + containerType.getName()
        			+ " for " + resourceName);
        	if (person.isOutside())
            	setPhase(WALK_BACK_INSIDE);
            else {
            	endTask();
            }
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

        // Add penalty to the fatigue
        condition.setFatigue(fatigue + time * factor * (1.1D - strengthMod));

        // Add experience points
        addExperience(time);

        if (finishedCollecting) {

            logger.log(person, Level.FINE, 4_000, "Collected a total of "
            	+ Math.round(totalCollected*100D)/100D
        		+ " kg " + resourceName + ".");

            if (person.isOutside())
            	setPhase(WALK_BACK_INSIDE);
            else {
            	endTask();
         		return 0;
            }
    	}

	    // Check for an accident during the EVA operation.
	    checkForAccident(time);

        return 0D;
    }

    /**
     * Transfers an empty bag from a settlement to a person
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
                    goodLocation = LocalAreaUtil.isLocationCollisionFree(newLocation.getX(), newLocation.getY(),
                            person.getCoordinates());
                }
            }
        }

        return newLocation;
    }

    public Building findBin() {
    	return null;
    }

    /**
     * Determine storage bin location for dropping off regolith.
     * @return storage bin X and Y location outside settlement.
     */
    private Point2D.Double determineBinLocation() {

    	Building bin = findBin();
    	double x = bin.getXLocation();
    	double y = bin.getYLocation();
    	int facing = (int)bin.getFacing();

    	if (facing == 0) {
			x -= 3;
		} else if (facing == 90) {
			y -= 3;
		} else if (facing == 180) {
			x += 3;
		} else if (facing == 270) {
			y += 3;
		} else {
			x -= 3;
		}

        return new Point2D.Double(x, y);
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
	            settlement.getGoodsManager().determineGoodValueWithSupply(GoodsUtil.getResourceGood(resourceID), amount);
            }
    	}
    }
}
