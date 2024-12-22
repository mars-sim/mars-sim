/*
 * Mars Simulation Project
 * DigLocal.java
 * @date 2023-09-07
 * @author Scott Davis
 */

package com.mars_sim.core.structure.task;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.WalkOutside;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The DigLocal class is a task for collecting a resource outside a settlement.
 */
public abstract class DigLocal extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(DigLocal.class.getName());

	public static final int MAX_DROPOFF_DISTANCE = 10;
	public static final int MAX_DIGGING_DISTANCE = 100;
	
	public static final double SMALL_AMOUNT = 0.001;
	/** The loading speed of the resource at the storage bin [kg/millisols]. */
	public static final double LOADING_RATE = 10.0;

	public static final String WALK = "walk";
	
	static final TaskPhase DROP_OFF_RESOURCE = new TaskPhase(
												Msg.getString("Task.phase.dropOffResource")); //$NON-NLS-1$
	static final TaskPhase WALK_TO_BIN = new TaskPhase(
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
	protected DigLocal(String name, TaskPhase collectionPhase, int resourceID,
					EquipmentType containerType, Person person, int duration) {
        // Use EVAOperation constructor.
        super(name, person, duration, collectionPhase);

		setMinimumSunlight(LightLevel.NONE);

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
	    		abortEVA("No available walkable airlock for egress.");
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
	        	abortEVA("No digging location.");
	        	return;
	        }
        }

        if (dropOffLoc == null) {
        	dropOffLoc = determineBinLocation();
	        if (dropOffLoc == null) {
				abortEVA("No storage bin.");
	        	return;
	        }
        }

        setPhase(WALK_TO_OUTSIDE_SITE);
    }
	
	/**
	 * Where will any resources be dropped off on the surface
	 */
	LocalPosition getDropOffLocation() {
		return dropOffLoc;
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
	@Override
    protected double performMappedPhase(double time) {

        time = super.performMappedPhase(time);
		if (!isDone()) {
	        if (getPhase() == null) {
				logger.severe(worker, "Task phase is null.");
	        }
	        else if (collectionPhase.equals(getPhase())) {
	            time = collectResource(time);
	        }
			else if (WALK_TO_BIN.equals(getPhase())) {
				time = walkToBin();
			}
	        else if (DROP_OFF_RESOURCE.equals(getPhase())) {
	            time = dropOffResource(time);
	        }
		}
        return time;
    }

	/**
	 * Walks to a storage bin by adding a walking sub task. 
	 * 
	 * @return
	 */
    private double walkToBin() {
    	// Go to the drop off location
        if (person.isOutside()) {
        	
    		if (!dropOffLoc.isNear(person.getPosition())) { 	
        		// FUTURE: how to get the walk time and return the remaining time ?
    			
        		// Note that addSubTask() will internally check if the task is a duplicate

				boolean canAdd = addSubTask(new WalkOutside(person, person.getPosition(), dropOffLoc, false));
				if (!canAdd) {
					logger.log(person, Level.WARNING, 4_000,
							". Unable to add subtask WalkOutside.");
					// Note: may call below many times
					endTask();
				}
        	}
    		
        	else {
        		setPhase(DROP_OFF_RESOURCE);
        	}
        }
        
        else {
        	logger.severe(person, "Not outside. Unable to walk to the storage bin.");
            endTask();
        }

        return 0;
    }

    /**
     * Drops off resources from container.
     * 
     * @param time
     * @return
     */
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
            }
        }
        else if (!person.isOnDuty()) {
			// Duty has ended so abort digging
			abortEVA("End of work shift.");
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
     * Performs collect resource phase.
     *
     * @param time time (millisol) to perform phase.
     * @return time (millisol) remaining after performing phase.
     * @throws Exception
     */
    private double collectResource(double time) {
    	// Check container
        Container container = person.findContainer(containerType, false, resourceID);
		
		if (checkReadiness(time) > 0) {
			if (!((Equipment)container).isEmpty(false)) {
				// Has resources in container
				setPhase(WALK_TO_BIN);
			}
			else
				checkLocation("Found no resources.");
			return time;
		}
			
     	if (person.isInSettlement()) {
			abortEVA("Person still in settlement.");
     		return time;
     	}

        if (container == null) {
        	abortEVA("Found no " + containerType.getName() + " for " + resourceName + ".");
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
        	finishedCollecting = (excess > 0);
	     	collectionLimit += collected - excess;	     	
        }

        if (!finishedCollecting) {
        	double loadCap = person.getCarryingCapacity();
            if (collectionLimit >= loadCap) {
            	collectionLimit = loadCap;
    			finishedCollecting = true;
    		}
        }

		// Can not physically carry any more
		finishedCollecting = finishedCollecting || (person.getRemainingCarryingCapacity() <= 0);

        PhysicalCondition condition = person.getPhysicalCondition();
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
           
            setPhase(WALK_TO_BIN);
    	}

	    // Check for an accident during the EVA operation.
	    checkForAccident(time);

		
        return 0;
    }
    
	/**
	 * Unloads resources from the Container.
	 * 
	 * @param container
	 * @param amount
	 * @param effort
	 */
    private void unloadContainer(Container container, double amount, double effort) {
 
		// Retrieve this amount from the container
		container.retrieveAmountResource(resourceID, amount);

      	int newResourceID = 0;
      	
    	// Remap regoliths by allowing the possibility of misclassifying regolith types
		if (resourceID == ResourceUtil.regolithID) {
			int rand = RandomUtil.getRandomInt(10);
			
			// Reassign as the other 3 types of regoliths
			if (rand == 8) {			
				newResourceID = ResourceUtil.regolithBID;
			}
			else if (rand == 9) {						
				newResourceID = ResourceUtil.regolithCID;
			}
			else if (rand == 10) {					
				newResourceID = ResourceUtil.regolithDID;
			}
			else
				newResourceID = resourceID;
		}
		else if (resourceID == ResourceUtil.iceID) {
			newResourceID = resourceID;
		}
		
		// Add to the daily output
		settlement.addOutput(newResourceID, amount, effort);
		// Store the amount in the settlement
		settlement.storeAmountResource(newResourceID, amount);
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
                	abortEVA("Strangely unable to transfer an empty container for " + resourceName + ".");
                }
	        }
	        else {
	        	abortEVA("Unable to find an empty container in the inventory for " + resourceName + ".");
	        }
        }
        return container;
    }

	/**
	 * Finds a map of buildings having storage functions that can or cannot 
	 * hold resources being collected.
	 * 
	 * @param worker
	 * @param resourceID
	 * @return
	 */
	private static Map<Boolean, List<Building>> findStorageBuildings(Worker worker, int resourceID) {
		// Find any Storage function that can hold the resource being collected but
		// group by Buildings that are categorised as Storage
		return worker.getSettlement().getBuildingManager()
			.getBuildingSet(FunctionType.STORAGE).stream()
			.filter(b -> b.getStorage().getResourceStorageCapacity().containsKey(resourceID))
			.collect(Collectors.groupingBy(x -> (x.getCategory() == BuildingCategory.STORAGE)));
	}
	
	
    /**
     * Determines location for dropping off the resource.
     * 
     * @return a X and Y location of a storage bin
     */
    private LocalPosition determineBinLocation() {
		// Find any Storage function that can hold the resource being collected but
		// group by Buildings that are categorised as Storage
		Map<Boolean, List<Building>> binMap = findStorageBuildings(worker, resourceID);
		
		// Preference is Storage buildings
		List<Building> binList = binMap.computeIfAbsent(true, (v -> Collections.emptyList()));
		if (binList.isEmpty()) {
			binList = binMap.computeIfAbsent(false, (v -> Collections.emptyList()));
		}
        Building b = RandomUtil.getRandomElement(binList);
		if (b == null) {
			// If no proper bin is found, set the bin 
			// dropoff location next to the airlock 
			b = (Building)(airlock.getEntity());
		}

		LocalPosition p = LocalAreaUtil.getCollisionFreeRandomPosition(b, worker.getCoordinates(), MAX_DROPOFF_DISTANCE);
		if (p == null) {
			abortEVA("No suitable drop-off location near " + b + ".");
		}
		return p;
    }
    
    /**
     * Determines location for digging regolith.
     * 
     * @return digging X and Y location outside settlement.
     */
    private LocalPosition determineDiggingLocation() {
		if (airlock.getEntity() instanceof LocalBoundedObject boundedObject) {
			return LocalAreaUtil.getCollisionFreeRandomPosition(boundedObject,
																 person.getCoordinates(), MAX_DIGGING_DISTANCE);
		}

        return null;
    }

    /**
     * Closes out this task. If person is inside then transfer the resource from the container to the Settlement.
     */
    @Override
    protected void clearDown() {
		if (settlement == null) {
			return;
		}

		// This is the end of the Task so must return 
		Container container = person.findContainer(containerType, false, resourceID);
		if (container == null)
			return;

		// Transfer the container back to the settlement
		boolean success = container.transfer(settlement);
		if (!success)
			logger.warning(person, "Unable to transfer " + containerType.getName() + " back.");

		double amount = container.getAmountResourceStored(resourceID);
		if (amount > 0) {
			unloadContainer(container, amount, getTimeCompleted());
		}
		
		// Remove pressure suit and put on garment
		if (person.unwearPressureSuit(settlement)) {
			person.wearGarment(settlement);
		}
	
		// Assign thermal bottle
		person.assignThermalBottle();

		super.clearDown();
    }
    
	/**
	 * Is the person qualified for digging local ice or regolith ?
	 * 
	 * @return
	 */
	public static boolean canDigLocal(Person person) {
		// Check if person can exit the rover.
		if (getWalkableAvailableEgressAirlock(person) == null)
			return false;

		// Check if sunlight is insufficient
		if (EVAOperation.isGettingDark(person))
			return false;

		// Check if person's medical condition will not allow task.
		if (person.getPerformanceRating() < .2D)
			return false;

		return !person.isSuperUnfit();
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	@Override
	public void destroy() {
		worker = null;
		airlock = null;
		settlement = null;
		collectionPhase = null;
		diggingLoc = null;
		dropOffLoc = null;
		containerType = null;

		super.destroy();
	}
}
