/*
 * Mars Simulation Project
 * CollectResources.java
 * @date 2022-06-30
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.InventoryUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The CollectResources class is a task for collecting resources at a site with
 * an EVA from a rover.
 */
public class CollectResources extends EVAOperation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CollectResources.class.getName());

	/** The average labor time it takes to find the resource. */
	public static final double LABOR_TIME = 50D;

	/** Task phases. */
	private static final TaskPhase COLLECT_RESOURCES = new TaskPhase(Msg.getString("Task.phase.collectResources")); //$NON-NLS-1$

	// Data members
	/** Rover used. */
	protected Rover rover;
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

	/**
	 * Constructor.
	 *
	 * @param taskName        The name of the task.
	 * @param person          The person performing the task.
	 * @param rover           The rover used in the task.
	 * @param resourceType    The resource type to collect.
	 * @param collectionRate  The rate (kg/millisol) of collection.
	 * @param targettedAmount The amount (kg) desired to collect.
	 * @param startingCargo   The starting amount (kg) of resource in the rover
	 *                        cargo.
	 * @param containerType   the type of container to use to collect resource.
	 */
	public CollectResources(String taskName, Person person, Rover rover, Integer resourceType, double collectionRate,
			double targettedAmount, double startingCargo, EquipmentType containerType) {

		// Use EVAOperation parent constructor.
		super(taskName, person, true, LABOR_TIME + RandomUtil.getRandomDouble(-10D, 10D),
				SkillType.AREOLOGY);

		if (!person.isFit()) {
			checkLocation();
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
				logger.log(person, Level.WARNING, 5000,
						"Unable to find containers to collect resources.", null);
				endTask();
			}
		}

		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int strength = nManager.getAttribute(NaturalAttributeType.STRENGTH);
		int agility = nManager.getAttribute(NaturalAttributeType.AGILITY);
		int eva = person.getSkillManager().getSkillLevel(SkillType.EVA_OPERATIONS);

		compositeRate  = collectionRate * ((.5 * agility + strength) / 150D) * (eva + .1) ;

		// Add task phases
		addPhase(COLLECT_RESOURCES);
	}


	@Override
	protected TaskPhase getOutsideSitePhase() {
		return COLLECT_RESOURCES;
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
		double result = 0;
		
		if (checkReadiness(time) > 0)
			return time;

		// Collect resources.
		Container container = person.findContainer(containerType, false, resourceType);
		if (container == null) {
			checkLocation();
			return time;
		}

		double remainingPersonCapacity = person.getAmountResourceRemainingCapacity(resourceType);
		double currentSamplesCollected = rover.getAmountResourceStored(resourceType) - startingCargo;
		double remainingSamplesNeeded = targettedAmount - currentSamplesCollected;
		double sampleLimit = Math.min(remainingSamplesNeeded, remainingPersonCapacity);

		double samplesCollected = time * compositeRate;

		// Modify collection rate by "Areology" skill.
		int areologySkill = person.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
		if (areologySkill == 0) {
			samplesCollected /= 2D;
		}
		if (areologySkill > 1) {
			samplesCollected += samplesCollected * (.2D * areologySkill);
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
			result = 0;
		} else {
			if (sampleLimit >= 0D) {
				container.storeAmountResource(resourceType, sampleLimit);
			}
			setPhase(WALK_BACK_INSIDE);
			result = time - (sampleLimit / collectionRate);
			if (result < 0)
				result = 0;
		}

		return result;
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
	public static boolean canCollectResources(MissionMember member, Rover rover, 
			EquipmentType containerType, Integer resourceType) {

		boolean result = false;

		if (member instanceof Person) {
			Person person = (Person) member;

			// Check if person can exit the rover.
			if (!ExitAirlock.canExitAirlock(person, rover.getAirlock()))
				return false;

			// Check if sunlight is insufficient
			if (EVAOperation.isGettingDark(person))
				return false;

			// Check if person's medical condition will not allow task.
			if (person.getPerformanceRating() < .2D)
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

			EVASuit suit = InventoryUtil.getGoodEVASuit(person);
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
	 * Release workers inventory
	 */
	@Override
	protected void clearDown() {
		if (rover != null) {
			// Task may end early before a Rover is selected
			returnEquipmentToVehicle(rover);
		}
	}
}
