/*
 * Mars Simulation Project
 * CollectResources.java
 * @date 2022-07-18
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
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * The CollectResources class is a task for collecting resources at a site with
 * an EVA from a rover.
 */
public class CollectResources extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CollectResources.class.getName());

	/** The average labor time it takes to find the resource. */
	public static final double LABOR_TIME = 50D;

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
			double targettedAmount, double startingCargo, EquipmentType containerType) {

		// Use EVAOperation parent constructor.
		super("Collecting Resources", person,
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

		// Determine location for collection site.
		setRandomOutsideLocation(rover);

		// Take container for collecting resource.
		if (!hasAContainer()) {
			boolean hasIt = takeContainer();

			// If container is not available, end task.
			if (!hasIt) {
				logger.warning(person, 5000,
						"Unable to find containers to collect resources.");
				endTask();
			}
		}

		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int strength = nManager.getAttribute(NaturalAttributeType.STRENGTH);
		int agility = nManager.getAttribute(NaturalAttributeType.AGILITY);
		
		int eva = person.getSkillManager().getSkillLevel(SkillType.EVA_OPERATIONS);
		int prospecting = person.getSkillManager().getSkillLevel(SkillType.PROSPECTING);
		
		compositeRate  = collectionRate * ((.5 * agility + strength) / 150D) 
				* (.5 * (eva + prospecting) + .2) ;
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
	 * Checks if the person is carrying a container of this type.
	 *
	 * @return true if carrying a container of this type.
	 *
	 */
	private boolean hasAContainer() {
		return person.containsEquipment(containerType);
	}

	/**
	 * Takes the least full container from the rover.
	 *
	 * @throws Exception if error taking container.
	 */
	private boolean takeContainer() {
		Container container = ContainerUtil.findLeastFullContainer(rover, containerType, resourceType);

		if (container != null) {
			return container.transfer(person);
		}
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
		
		if (checkReadiness(time) > 0)
			return time;

		// Collect resources.
		Container container = person.findContainer(containerType, false, resourceType);
		if (container == null) {
			if (resourceType == ResourceUtil.iceID) {
				checkLocation("No container available for ice.");
			}
			else {
				checkLocation("No container available for regolith.");
			}
			return time;
		}

		double remainCap = container.getAmountResourceRemainingCapacity(resourceType);
		if (remainCap <= 0.01) {
			checkLocation("Container capacity maxed out.");
		}
		double collectedAtThisSite = rover.getAmountResourceStored(resourceType) - startingCargo;
		double remainingSamplesNeeded = targettedAmount - collectedAtThisSite;
		if (remainingSamplesNeeded <= 0.01) {
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
		if (areologySkill + prospecting > 1) {
			samplesCollected += samplesCollected * .25D * (areologySkill + prospecting);
		}

		// Modify collection rate by polar region if ice collecting.
		if (resourceType == ResourceUtil.iceID
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
			
			// Checks if available container with remaining capacity for resource.
			Container container = ContainerUtil.findLeastFullContainer(rover, containerType, resourceType);
			boolean containerAvailable = (container != null);

			// Check if container and full EVA suit can be carried by person or is too
			// heavy.
			double carryMass = 0D;
			if (container != null) {
				carryMass += container.getBaseMass() + container.getStoredMass();
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
		if (rover != null) {
			// Task may end early before a Rover is selected
			returnEquipmentToVehicle(rover);
		}

		super.clearDown();
	}
}
