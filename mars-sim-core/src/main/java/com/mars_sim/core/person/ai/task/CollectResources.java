/*
 * Mars Simulation Project
 * CollectResources.java
 * @date 2024-07-14
 * @author Scott Davis
 */

package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.ContainerUtil;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.EVASuitUtil;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.CollectResourcesMission;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * The CollectResources class is a task for collecting resources at a site with
 * an EVA from a rover.
 */
public class CollectResources extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CollectResources.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.collectResources"); //$NON-NLS-1$
	
	/** Simple Task name */
	static final String SIMPLE_NAME = CollectResources.class.getSimpleName();
	
	/** The average labor time it takes to find the resource. */
	public static final double LABOR_TIME = 200D;

	/** Task phases. */
	private static final TaskPhase COLLECT_RESOURCES = 
			new TaskPhase(Msg.getString("Task.phase.collectResources"), //$NON-NLS-1$
					createPhaseImpact(PhysicalEffort.HIGH, SkillType.AREOLOGY, SkillType.PROSPECTING));
    public static final LightLevel LIGHT_LEVEL = LightLevel.NONE;

	// Data members
	/** Collection rate for resource (kg/millisol). */
	protected double collectionRate;
	/** Targeted amount of resource to collect at site. (kg) */
	protected double targettedAmount;
	/** Amount of resource already in rover cargo at start of task. (kg) */
	protected double startingCargo;
	/** The composite rate of collection. */
	private double compositeRate;

	/** The resource type. */
	protected Integer resourceType;
	
	/** The container type to use to collect resource. */
	protected EquipmentType containerType;
	/** Rover used. */
	protected Rover rover;
	
	private CollectResourcesMission mission;
	
	/**
	 * Constructor.
	 *
	 * @param person          The person performing the task.
	 * @param rover           The rover used in the task.
	 * @param resourceType    The resource type to collect.
	 * @param collectionRate  The rate (kg/millisol) of collection.
	 * @param targettedAmount The amount (kg) desired to collect.
	 * @param startingCargo   The starting amount (kg) of resource in the rover
	 *                        cargo.
	 * @param containerType   the type of container to use to collect resource.
	 */
	public CollectResources(Person person, Rover rover, Integer resourceType, double collectionRate,
			double targettedAmount, double startingCargo, EquipmentType containerType, CollectResourcesMission mission) {

		// Use EVAOperation parent constructor.
		super(NAME, person,
					LABOR_TIME + RandomUtil.getRandomDouble(-10D, 10D), COLLECT_RESOURCES);

		setMinimumSunlight(LIGHT_LEVEL);
		
		if (person.isSuperUnfit()) {
			checkLocation("Super Unfit.");
	      	return;
		}

		// Initialize data members.
		this.rover = rover;
		this.collectionRate = collectionRate;
		this.targettedAmount = targettedAmount;
		this.startingCargo = startingCargo;
		this.resourceType = resourceType;
		this.containerType = containerType;
		this.mission = mission;

		// Determine location for collection site.
		setRandomOutsideLocation(rover);

		// Take container for collecting resource.
		int num = numContainers();
		if (num == 0) {
			boolean hasIt = takeContainer();

			// If container is not available, end task.
			if (!hasIt) {
				logger.warning(person, 5000,
						"Unable to find a " + containerType.getName().toLowerCase() + " to collect resources.");
				endTask();
				return;
			}
		}
		else if (num > 1) {
			// Return extra containers
			for (int i=0; i<num-1; i++) {
				Container container = person.findContainer(containerType, false, resourceType); 
				if (container != null) {
					boolean done = container.transfer(rover);
					if (done) {
//						logger.info(person, 5000, "Returned an extra " + containerType.getName().toLowerCase() + " from person back to rover.");
					}
					else
						logger.warning(person, 5000, "Unable to transfer a " + containerType.getName().toLowerCase() + " from person back to rover.");
				}	
			}
		}

		setCollectionRate(collectionRate);
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
		int prospecting = person.getSkillManager().getSkillLevel(SkillType.PROSPECTING);
		
		compositeRate  = collectionRate * ((.5 * agility + strength) / 150D) 
				* (.5 * (eva + prospecting) + .2) ;
		logger.info(person, 5000, mission.getName() +  " - Collection rate: " + Math.round(compositeRate * 10.0)/10.0);
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
				throw new IllegalArgumentException("Task phase is null");
			} else if (COLLECT_RESOURCES.equals(getPhase())) {
				time = collectResources(time);
			}
		}
		return time;

	}

	/**
	 * Checks how many containers a person is carrying.
	 *
	 * @return true if carrying a container of this type.
	 */
	private int numContainers() {
		return person.findNumContainersOfType(containerType);
	}
	
	/**
	 * Takes the least full container from the rover.
	 *
	 * @throws Exception if error taking container.
	 */
	private boolean takeContainer() {
		Container container = ContainerUtil.findLeastFullContainer(rover, containerType, resourceType);

		if (container != null) {
			boolean success = container.transfer(person);
			if (success) {
//				logger.info(person, 5000, "Getting hold of a " + containerType.getName().toLowerCase() + " from rover.");
			}
			else 
				logger.warning(person, 5000, "Unable to transfer a " + containerType.getName().toLowerCase() + " from rover to person.");
			return success;
		} 
		else
			logger.warning(person, 5000, "Could not get hold of a " + containerType.getName().toLowerCase() + " from rover.");
		
		return false;
	}

	/**
	 * Performs the collect resources phase of the task.
	 *
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error collecting resources.
	 */
	private double collectResources(double time) {
		
		if (checkReadiness(time) > 0) {
			return time;
		}

		// Check container
		Container container = person.findContainer(containerType, false, resourceType);
		if (container == null) {
			if (resourceType == ResourceUtil.ICE_ID) {
				checkLocation("No container available for ice.");
			}
			else if (resourceType == ResourceUtil.REGOLITH_ID) {
				checkLocation("No container available for regolith.");
			}
			return time;
		}

		double remainCap = container.getAmountResourceRemainingCapacity(resourceType);
		if (remainCap <= 0.01) {
			remainCap = 0;
			checkLocation("Container capacity maxed out.");
		}
		
		double collectedAtThisSite = mission.getCollectedAtCurrentSite();
		
		double remainingSamplesNeeded = targettedAmount - collectedAtThisSite;
		if (remainingSamplesNeeded <= 0.01) {
			remainingSamplesNeeded = 0;
			checkLocation("No more samples needed.");
		}
		
		double sampleLimit = Math.min(remainingSamplesNeeded, remainCap);
		
		double samplesCollected = time * compositeRate;

		// Modify collection rate by areology and prospecting skill.
		int areologySkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
		int prospecting = person.getSkillManager().getEffectiveSkillLevel(SkillType.PROSPECTING);
		
		if (areologySkill + prospecting == 0) {
			samplesCollected /= 2D;
		}
		else {
			samplesCollected += samplesCollected * .25D * (areologySkill + prospecting);
		}

		// Modify collection rate by polar region if ice collecting.
		if (resourceType == ResourceUtil.ICE_ID
			&& surfaceFeatures.inPolarRegion(person.getCoordinates())) {
			samplesCollected *= 10D;
		}

		// Add experience points
		addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);
		
		// Collect resources
		if (samplesCollected <= sampleLimit) {
			container.storeAmountResource(resourceType, samplesCollected);
		} 
		else {
			container.storeAmountResource(resourceType, sampleLimit);
			checkLocation("Samples collected exceeded set limits.");
		}
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
		container.retrieveAmountResource(resourceType, amount);

      	int newResourceID = resourceType;
      	
    	// Remap regoliths by allowing the possibility of misclassifying regolith types
		if (resourceType == ResourceUtil.REGOLITH_ID) {
			int rand = RandomUtil.getRandomInt(10);
			
			// Reassign as the other 3 types of regoliths
			if (rand == 8) {			
				newResourceID = ResourceUtil.REGOLITHB_ID;
			}
			else if (rand == 9) {						
				newResourceID = ResourceUtil.REGOLITHC_ID;
			}
			else if (rand == 10) {					
				newResourceID = ResourceUtil.REGOLITHD_ID;
			}
			else
				newResourceID = resourceType;
		}
		
		// Add to the daily output
		rover.getAssociatedSettlement().addOutput(newResourceID, amount, effort);
		// Store the amount in the settlement
		rover.storeAmountResource(newResourceID, amount);
		mission.recordResourceCollected(resourceType, newResourceID);
	}
    
	/**
	 * Checks if a person can perform an CollectResources task.
	 *
	 * @param member        the member to perform the task
	 * @param rover         the rover the person will EVA from
	 * @param containerType the container class to collect resources in.
	 * @param resourceType  the resource to collect.
	 * @return true if person can perform the task.
	 */
	public static boolean canCollectResources(Worker member, Rover rover, 
			EquipmentType containerType, Integer resourceType) {

		boolean result = false;

		if (member instanceof Person person) {

			// Check if person can exit the rover.
			if (!ExitAirlock.canExitAirlock(person, rover.getAirlock()))
				return false;

			// Check if sunlight is insufficient
			if (EVAOperation.isGettingDark(person))
				return false;

			// Check if person's medical condition will not allow task.
			if (person.getPerformanceRating() < .2D)
				return false;

			if (person.isSuperUnfit())
				return false;
			
			// Checks if the person has an available container with remaining capacity for resource.
			Container container = ContainerUtil.findLeastFullContainer(person, containerType, resourceType);
			if (container == null) {
				// Checks if the rover has an available container with remaining capacity for resource.
				container = ContainerUtil.findLeastFullContainer(rover, containerType, resourceType);
			}
			// Transfer that container from rover to person
			boolean containerAvailable = false;
			double carryMass = 0D;
			
			if (container != null) {
				containerAvailable = true;
				
				// Check if container and full EVA suit can be carried by person 
				// or is too heavy.
				carryMass = container.getBaseMass() + container.getStoredMass();

			} else {
				logger.warning(person, 5000, "No " + containerType.getName().toLowerCase() + " available.");
				return false;
			}

			EVASuit suit = EVASuitUtil.findRegisteredOrGoodEVASuit(person);
			if (suit != null) {
				// Mass include everything
				carryMass += suit.getMass();
			}
			double carryCapacity = person.getCarryingCapacity();
			boolean canCarryEquipment = (carryCapacity >= carryMass);

			result = (containerAvailable && canCarryEquipment);
		}

		return result;
	}
	
	/**
	 * Releases workers inventory.
	 */
	@Override
	protected void clearDown() {
		// Take container for collecting resource.
		int num = numContainers();
		if (num >= 1) {
			// Return extra containers
			for (int i=0; i<num; i++) {
				Container container = person.findContainer(containerType, false, resourceType); 
				if (container != null) {
					boolean done = container.transfer(rover);
					if (done) {
//						logger.info(person, 5000, "Done transferring a " + containerType.getName().toLowerCase() + " from person back to rover.");
						double amount = container.getAmountResourceStored(resourceType);
						if (amount > 0) {
							unloadContainer(container, amount, getTimeCompleted());
						}
					}
					else
						logger.warning(person, 5000, "Unable to transfer a " + containerType.getName().toLowerCase()  + " from person back to rover.");
				}	
			}
		}
		
//		if (rover != null) {
//			// Task may end early before a Rover is selected
//			returnEquipmentToVehicle(rover);
//		}
		
		// Remove pressure suit and put on garment
		if (person.unwearPressureSuit(rover)) {
			person.wearGarment(rover);
		}
	
		// Assign thermal bottle
		person.assignThermalBottle();

		super.clearDown();
	}
}
