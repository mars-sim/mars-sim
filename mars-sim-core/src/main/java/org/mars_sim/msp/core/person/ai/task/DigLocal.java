/*
 * Mars Simulation Project
 * DigLocal.java
 * @date 2022-07-26
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
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
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The DigLocal class is a task for collecting a resource outside a settlement.
 */
public abstract class DigLocal extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(DigLocal.class.getName());
	/** The extended amount of millisols for dropping off resources. */
	public static final int EXTENDED_TIME = 5;
	
	public static final double SMALL_AMOUNT = 0.00001;
	/** The loading speed of the resource at the storage bin [kg/millisols]. */
	public static final double LOADING_RATE = 10.0;

	private static final TaskPhase DROP_OFF_RESOURCE = new TaskPhase(
												Msg.getString("Task.phase.dropOffResource")); //$NON-NLS-1$
	private static final TaskPhase WALK_TO_BIN = new TaskPhase(
												Msg.getString("Task.phase.walkToBin")); //$NON-NLS-1$

	// Resource being collected
	private int resourceID;

	private double compositeRate;

	private double factor;
	/** The amount of resource that can be collected by this person per trip [in kg]. */
	private double collectionLimit;
	
	private String resourceName;
	
	/** Airlock to be used for EVA. */
	private Airlock airlock;

	private Settlement settlement;

	private TaskPhase collectionPhase;

	private LocalPosition diggingLoc;
	
	private LocalPosition dropOffLoc;
	
	private EquipmentType containerType;

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public DigLocal(String name, TaskPhase collectionPhase, int resourceID,
					EquipmentType containerType, Person person, int duration) {
        // Use EVAOperation constructor.
        super(name, person, false, duration, SkillType.AREOLOGY);

        addAdditionSkill(SkillType.PROSPECTING);
        
        this.containerType = containerType;
        this.resourceID = resourceID;
        this.resourceName = ResourceUtil.findAmountResourceName(resourceID);
        this.collectionPhase = collectionPhase;

        // To dig local a person must be in a Settlement
        if (!person.isInSettlement()) {
        	abortEVA("Not in a settlement to start a DigLocal Task.");
			return;
        }

		settlement = person.getSettlement();
     		
        // Get an available airlock in a settlement
     	if (person.isInSettlement()) {
	        airlock = getWalkableAvailableEgressAirlock(person);
	        if (airlock == null) {
	    		abortEVA("No walkable airlock for egress.");
				return;
	        }
     	}

        // Take container for collecting resource.
        // If container are not available, end task.
        if (collectContainer() == null) {
        	abortEVA("No " + containerType.name() + " for " + resourceName + " are available.");
        	return;
        }

        // Determine digging location.
        if (diggingLoc == null) {
        	diggingLoc = determineDiggingLocation();
	        if (diggingLoc != null) {
	        	setOutsideSiteLocation(diggingLoc);
	        }
	        else {
	        	abortEVA("No digging location");
	        	return;
	        }
        }

        if (dropOffLoc == null) {
        	dropOffLoc = determineBinLocation();
	        if (dropOffLoc == null) {
				abortEVA("No storage bin");
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
			else if (WALK_TO_BIN.equals(getPhase())) {
				time = walkToBin(time);
			}
		}
        return time;
    }

	/**
	 * Walks to a storage bin by adding a walking sub task. 
	 * 
	 * @param time
	 * @return
	 */
    private double walkToBin(double time) {
    	// Go to the drop off location
        if (person.isOutside()) {
			if (dropOffLoc == null) {
				logger.severe(person, "No location for storage bin.");
				endTask();
			}
        	else if (!person.getPosition().equals(dropOffLoc)) {
        		addSubTask(new WalkOutside(person, person.getPosition(),
        			dropOffLoc, true));
        	}
        	else {
        		setPhase(DROP_OFF_RESOURCE);
        	}
        }
        else {
        	logger.severe(person, "Not outside. Can't walk to the storage bin.");
            endTask();
        }

        return time * .9;
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
			abortEVA("Person in settlement");
     		return time;
     	}

     	// Get a container
        Container container = person.findContainer(containerType, false, resourceID);
        if (container == null) {
        	abortEVA("Has no " + containerType.getName() + " for " + resourceName);
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
        	double excess = container.storeAmountResource(resourceID, collected);
        	if (excess > 0) {
    			finishedCollecting = true;
            }
	     	collectionLimit += collected - excess;
	     	
	     	person.getPhysicalCondition().stressMuscle(time);
        }

        if (!finishedCollecting) {
        	double loadCap = person.getCarryingCapacity();
            if (collectionLimit >= loadCap) {
            	collectionLimit = loadCap;
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

	private double dropOffResource(double time) {
    	double remainingTime = time;
    	
    	Container container = person.findContainer(containerType, false, resourceID);
    	if (container == null)
    		return 0D;
	   	
    	double amount = container.getAmountResourceStored(resourceID);	
    	
        if (amount > 0) {
        	 	
           	double portion = LOADING_RATE * time;
           	         	
           	if (portion > amount) {
           		portion = amount;
           	} 	
           	
            if (portion > 0) {
	        	double loadingTime = portion / LOADING_RATE;
	        	
	        	remainingTime = remainingTime - loadingTime;
	        	
				// Transfer the resource out of the Container
				unloadContainer(container, portion, time);
	            
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
	 * Unload resources from the Container
	 */
    private void unloadContainer(Container container, double amount, double effort) {
		// Retrieve this amount from the container
		container.retrieveAmountResource(resourceID, amount);
		// Add to the daily output
		settlement.addOutput(resourceID, amount, effort);
		// Store the amount in the settlement
		settlement.storeAmountResource(resourceID, amount);
	}

	/**
     * Transfers an empty container from a settlement to a person.
     * 
     * @return a container
     */
    private Container collectContainer() {
    	// Note: should take a container before leaving the airlock
    	// Note: also consider dropping off the resource in a shed
    	// or a shed outside of the workshop/landerhab for processing
        Container container = person.findContainer(containerType, false, resourceID);
        if (container == null) {
        	// Doesn't have a container
        	container = settlement.findContainer(containerType, true, resourceID);
	        if (container != null) {
            	boolean successful = container.transfer(person);
            	if (!successful) {
            		container = null;
                	abortEVA("Strangely unable to transfer an empty container for " + resourceName);
                }
	        }
	        else {
	        	abortEVA("Unable to find an empty container in the inventory for " + resourceName);
	        }
        }
        return container;
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
		// Find any Storage function that can hold the resource being collected but
		// group by Buildings that are categorised as Storage
		Map<Boolean, List<Building>> binMap = BuildingManager
				.findStorageBuildings(worker, resourceID, 
				BuildingCategory.STORAGE);
		
		// Preference is Storage buildings
		List<Building> binList = binMap.get(true);
		if (binList.isEmpty()) {
			binList = binMap.get(false);
			if (binList.isEmpty()) {
				return null;
			}
		}
        int rand = RandomUtil.getRandomInt(binList.size() - 1);
        Building b = binList.get(rand); 

        // Set the bin drop off location (next to the bin)    	
		LocalPosition p = LocalAreaUtil.getCollisionFreeRandomPosition(b, worker.getCoordinates(), 1D);
		if (p == null) {
			abortEVA("Can not find a suitable drop-off location near " + b);
		}
		return p;
    }
    
    /**
     * Determines location for digging regolith.
     * 
     * @return digging X and Y location outside settlement.
     */
    private LocalPosition determineDiggingLocation() {
		if (airlock.getEntity() instanceof LocalBoundedObject) {
			LocalBoundedObject boundedObject = (LocalBoundedObject) airlock.getEntity();
			return  LocalAreaUtil.getCollisionFreeRandomPosition(boundedObject,
																 person.getCoordinates(), 100D);
		}

        return null;
    }

    /**
     * Closes out this task. If person is inside then transfer the resource from the container to the Settlement.
     */
    @Override
    protected void clearDown() {
		if (person.isOutside()) {
			// THis has no effect as Task is closing down
            setPhase(WALK_BACK_INSIDE);
		}

		// This is the end of the Task so must return 
		Container container = person.findContainer(containerType, false, resourceID);
		if (container == null)
			return;

		// Transfer the container back to the settlement
		container.transfer(settlement);

		double amount = container.getAmountResourceStored(resourceID);
		if (amount > 0) {
			unloadContainer(container, amount, getTimeCompleted());
		}
    }
}
