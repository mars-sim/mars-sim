/*
 * Mars Simulation Project
 * DigLocal.java
 * @date 2022-07-26
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingCategory;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
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
	/** The extended amount of millisols for dropping off resources. */
	public static final int EXTENDED_TIME = 5;
	
	public static final double SMALL_AMOUNT = 0.00001;
	/** The loading speed of the resource at the storage bin [kg/millisols]. */
	public static final double LOADING_RATE = 10.0;

	// Resource being collected
	private int resourceID;

	private double compositeRate;

	private double factor;
	/** The amount of resource that can be collected by this person per trip [in kg]. */
	private double collectionLimit;
	/** The amount of resource that collected in one task session [in kg]. */
	private double collectionTotal;
	
	private String resourceName;
	
	/** Airlock to be used for EVA. */
	private Airlock airlock;

	private Settlement settlement;

	private TaskPhase collectionPhase;

	private LocalPosition diggingLoc;
	
	private LocalPosition binLoc;
	
	private LocalPosition dropOffLoc;
	
	private List<Building> binList;
	
	private EquipmentType containerType;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public DigLocal(String name, TaskPhase collectionPhase, int resourceID,
					EquipmentType containerType, Person person) {
        // Use EVAOperation constructor.
        super(name, person, false, 100, SkillType.AREOLOGY);

        addAdditionSkill(SkillType.PROSPECTING);
        
        this.containerType = containerType;
        this.resourceID = resourceID;
        this.resourceName = ResourceUtil.findAmountResourceName(resourceID);
        this.collectionPhase = collectionPhase;

        // To dig local a person must be in a Settlement
        if (!person.isInSettlement()) {
        	logger.warning(person, "Not in a settlement to start a DigLocal Task.");
    		checkLocation();
			return;
        }

     	settlement = CollectionUtils.findSettlement(person.getCoordinates());
     	if (settlement == null) {
    		checkLocation();
			return;
     	}
     		
        // Get an available airlock in a settlement
     	if (person.isInSettlement()) {
	        airlock = getWalkableAvailableEgressAirlock(person);
	        if (airlock == null) {
	        	logger.log(person, Level.WARNING, 4_000, "No walkable airlock for egress.");
	    		checkLocation();
				return;
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

        if (binLoc == null) {
        	binLoc = determineBinLocation();
	        if (diggingLoc != null) {
//	           	logger.info(person, 4_000L, "Selected the drop-off bin at " + diggingLoc + ".");
	        }
	        else {
	        	return;
	        }
        }
        
       	// Add task phases
    	addPhase(collectionPhase);
    	addPhase(DROP_OFF_RESOURCE);

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
	        else if (DROP_OFF_RESOURCE.equals(getPhase())) {
	            time = dropOffResource(time);
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
   	
		if (checkReadiness(time, false) > 0) {
			checkLocation();
			return time;
		}
			
     	if (person.isInSettlement()) {
			checkLocation();
     		return time;
     	}

     	// Get a container
        Container aBag = person.findContainer(containerType, false, resourceID);
        if (aBag == null) {
        	logger.log(person, Level.WARNING, 4_000, "Has no " + containerType.getName()
        			+ " for " + resourceName);
        	checkLocation();
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

        if (collected > SMALL_AMOUNT) {
        	double excess = aBag.storeAmountResource(resourceID, collected);
        	if (excess > 0) {
    			finishedCollecting = true;
            }
	     	collectionLimit += collected - excess;
	     	collectionTotal += collectionLimit;
	     	
	     	person.getPhysicalCondition().stressMuscle(time);
        }

        if (!finishedCollecting) {
        	double loadCap = person.getCarryingCapacity();
            if (collectionLimit >= loadCap) {
            	collectionLimit = loadCap;
            	collectionTotal += collectionLimit;
    			finishedCollecting = true;
    		}
        }

        if (!finishedCollecting) {
        	// Can not physically carry any more
	        finishedCollecting = (person.getRemainingCarryingCapacity() <= 0);
		}

        PhysicalCondition condition = person.getPhysicalCondition();
//        double fatigue = condition.getFatigue();
        double strengthMod = condition.getStrengthMod();
        double skillMod = 1.0 + person.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);		
        		
        // Add penalty to the fatigue
        condition.increaseFatigue(time * factor * (1.1D - strengthMod)/skillMod);

        // Account for hormone regulation, musculosketetal impact and record exercise time
        condition.workout(time);
		
        // Add experience points
        addExperience(time);

        if (finishedCollecting) {
            logger.log(person, Level.FINE, 4_000, "Collected a total of "
            	+ Math.round(collectionLimit*100D)/100D
        		+ " kg " + resourceName + ".");        
           
         	if (person.getPosition().equals(dropOffLoc)) {
    			setPhase(DROP_OFF_RESOURCE);
    		}
         	
         	else if (person.getPosition().equals(diggingLoc)) {  		 	
    	    	// If not at the bin location, go to there first
        		setPhase(WALK_TO_BIN);
    		}
    	}

	    // Check for an accident during the EVA operation.
	    checkForAccident(time);
	    
		if (isDone() || getTimeLeft() - time < 0) {
			// Need to extend the duration so as to drop off the resources
			setDuration(getDuration() + EXTENDED_TIME);
		}
		
        return 0;
    }

    @Override
	public double dropOffResource(double time) {
    	double remainingTime = time;
    	
    	Container bag = person.findContainer(containerType, false, resourceID);
    	if (bag == null)
    		return time;
	   	
    	double bagAmount = bag.getAmountResourceStored(resourceID);	
    	
        if (bagAmount > 0) {
        	 	
           	double portion = LOADING_RATE * time;
           	         	
           	if (portion > bagAmount) {
           		portion = bagAmount;
           	} 	
           	
           	// Check if the bin runs out of storage space for that resource
//          Building bin = getBinWithMostSpace(portion);
        	
//            double settlementCap = settlement.getAmountResourceRemainingCapacity(resourceID);

//            if (portion > settlementCap) {
//            	portion = settlementCap;
//
//            	logger.info(person, 20_000L,
//    	            	resourceName + " storage full in " + settlement + ". Could only check in "
//    	            	+ Math.round(portion*10.0)/10.0 + " kg.");
//            }
//
//            else {
//            	logger.info(person, 4_000,
//            			"Checking in " + Math.round(portion*10.0)/10.0 + " kg " + resourceName + " at storage bin.");
//            }
            
            if (portion > 0) {
	        	double loadingTime = portion / LOADING_RATE;
	        	
	        	remainingTime = remainingTime - loadingTime;
	        	
	        	// Retrieve this amount from the bag
	        	bag.retrieveAmountResource(resourceID, portion);
				// Add to the daily output
				settlement.addOutput(resourceID, portion, time);
				// Store the amount in the settlement
				settlement.storeAmountResource(resourceID, portion);
	            
	        	if (isDone() || getTimeLeft() - time < 0) {
	    			// Need to extend the duration so as to drop off the resources
	    			setDuration(getDuration() + EXTENDED_TIME);
	
	    			return remainingTime;
	    		}
            }
        }
        else {
        	// Reset this holder
        	collectionLimit = 0;
        	// Go back to the digging site
        	setPhase(WALK_TO_OUTSIDE_SITE);
        }
        
    	return remainingTime;
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
        			checkLocation();
                }
	        }
	        else {
	        	logger.log(person, Level.WARNING, 10_000, "Unable to find an empty bag in the inventory for " + resourceName + ".");
				checkLocation();
	        }
        }
        return aBag;
    }

    @Override
    protected TaskPhase getOutsideSitePhase() {
        return collectionPhase;
    }
    
    /**
     * Determines location for dropping off the resource.
     * 
     * @return a X and Y location of a storage bin
     */
    private LocalPosition determineBinLocation() {
    	LocalPosition p = null;
        if (binList == null) {
        	binList = worker.getSettlement().getBuildingManager()
        		.getBuildings(FunctionType.STORAGE).stream()
        		.filter(b -> b.getCategory() != BuildingCategory.HALLWAY
				&& !b.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION)
				&& b.getName().toLowerCase().contains(resourceName))
				.collect(Collectors.toList());
        }
	
        int size = binList.size();
        
        if (size == 0)
        	return p;
        
        if (size == 1) {
        	Building b = binList.get(0); 
            // Set the bin drop off location (next to the bin)
        	setBinDropOffLocation(b);
        	return b.getPosition();
        }
        
        int rand = RandomUtil.getRandomInt(size - 1);
        Building b = binList.get(rand); 
        // Set the bin drop off location (next to the bin)
    	setBinDropOffLocation(b);
    	
    	return b.getPosition();
    }
    
    /** 
     * Gets a storage bin that has needed storage space.
     * Return amount of storage space available.
     * 
     * @param amount
     * @return amount available
     */
    private Building getBinWithMostSpace(double amount) {
    	Building bestBin = null;
    	double bestSpace = 0;
    	
        for (Building b: binList) {
        	Map<Integer, Double> map = b.getStorage().getResourceStorageCapacity() ;
        	if (map.containsKey(resourceID)) {
        		double value = map.get(resourceID);
        		if (value > bestSpace) {
        			bestSpace = value;
        			bestBin = b;
        		}
        	}
        }
        	
        return bestBin;
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

            	settlement = person.getSettlement();
            	
//	            double settlementCap = settlement.getAmountResourceRemainingCapacity(resourceID);
//
//	            if (amount > settlementCap) {
//	            	amount = settlementCap;
//
//	            	logger.warning(person,
//	            			resourceName + " storage full. Could only check in "
//	            			+ Math.round(amount*10.0)/10.0 + " kg.");
//	            }

//	            else {
//	            	logger.fine(person,
//	            			"Checking in " + Math.round(amount*10.0)/10.0 + " kg " + resourceName + ".");
//	            }
	                	
		    	// Transfer the bag back to the settlement
		    	bag.transfer(settlement);
//				// Add to the daily output
				settlement.addOutput(resourceID, amount, getTimeCompleted());
//				// Store the amount in the settlement
				settlement.storeAmountResource(resourceID, amount);
//				
//            	logger.info(person,
//            			"Checking in " + Math.round(collectionTotal*10.0)/10.0 
//            			+ " kg " + resourceName + ".");
            }
    	}
    }
}
