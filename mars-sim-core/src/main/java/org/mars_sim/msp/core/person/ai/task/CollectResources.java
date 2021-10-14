/*
 * Mars Simulation Project
 * CollectResources.java
 * @date 2021-10-12
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
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
		super(taskName, person, true, LABOR_TIME + RandomUtil.getRandomDouble(10D) - RandomUtil.getRandomDouble(10D),
				SkillType.AREOLOGY);
		
		if (!person.isFit()) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
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
		Point2D collectionSiteLoc = determineCollectionSiteLocation();
		setOutsideSiteLocation(collectionSiteLoc.getX(), collectionSiteLoc.getY());

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
	        
		compositeRate  = collectionRate * ((.5 * agility + strength) / 150D) * (eva + .1)/ 5D ;
	        
		// Add task phases
		addPhase(COLLECT_RESOURCES);
	}

	/**
	 * Determine location for the collection site.
	 * 
	 * @return site X and Y location outside rover.
	 */
	private Point2D determineCollectionSiteLocation() {

		Point2D newLocation = null;
		boolean goodLocation = false;
		for (int x = 0; (x < 5) && !goodLocation; x++) {
			for (int y = 0; (y < 10) && !goodLocation; y++) {

				double distance = RandomUtil.getRandomDouble(50D) + (x * 100D) + 50D;
				double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
				double newXLoc = rover.getXLocation() - (distance * Math.sin(radianDirection));
				double newYLoc = rover.getYLocation() + (distance * Math.cos(radianDirection));
				Point2D boundedLocalPoint = new Point2D.Double(newXLoc, newYLoc);

				newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), boundedLocalPoint.getY(),
						rover);
				goodLocation = LocalAreaUtil.isLocationCollisionFree(newLocation.getX(), newLocation.getY(),
						person.getCoordinates());
			}
		}

		return newLocation;
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
		Unit container = findLeastFullContainer(rover.getInventory(), containerType, resourceType);
		if (container != null) {
			return container.transfer(rover, person);
		}
		return false;
	}

	/**
	 * Gets the least full container in the rover.
	 * 
	 * @param inv           the inventory to look in.
	 * @param containerType the container class to look for.
	 * @param resourceType  the resource for capacity.
	 * @return container.
	 */
	private static Unit findLeastFullContainer(Inventory inv, EquipmentType containerType, Integer resource) {
		Unit result = null;
		double mostCapacity = 0D;

		for(Equipment container : inv.findAllEquipmentType(containerType)) {
			double remainingCapacity = container.getAmountResourceRemainingCapacity(resource);
			if (remainingCapacity > mostCapacity) {
				result = container;
				mostCapacity = remainingCapacity;
			}
		}

		return result;
	}

	/**
	 * Perform the collect resources phase of the task.
	 * 
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error collecting resources.
	 */
	private double collectResources(double time) {
		
		// Check for radiation exposure during the EVA operation.
		if (isDone() || isRadiationDetected(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}
		
        // Check if there is a reason to cut short and return.
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
		}

		double remainingPersonCapacity = person.getAmountResourceRemainingCapacity(resourceType);
		double currentSamplesCollected = rover.getInventory().getAmountResourceStored(resourceType, false) - startingCargo;
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
		if (resourceType == ResourceUtil.iceID) {
			if (surfaceFeatures.inPolarRegion(person.getCoordinates())) {
				samplesCollected *= 3D;
			}
		}

		// Add experience points
		addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);
		
		// Collect resources.
		if (samplesCollected <= sampleLimit) {
			person.storeAmountResource(resourceType, samplesCollected);
			return 0D;
		} else {
			if (sampleLimit >= 0D) {
				person.storeAmountResource(resourceType, sampleLimit);
			}
			setPhase(WALK_BACK_INSIDE);
			return time - (sampleLimit / collectionRate);
		}

	}

	/**
	 * Release workers inventory 
	 */
	@Override
	protected void clearDown() {

		// Unload containers to rover's inventory.
		if (containerType != null) {
			if (person.containsEquipment(containerType)) {
				// Load containers in rover.
				Iterator<Equipment> i = person.findAllEquipmentType(containerType).iterator();
				while (i.hasNext()) {
					// Place this equipment within a rover outside on Mars
					i.next().transfer(person, rover);
				}
			}
		}
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
	public static boolean canCollectResources(MissionMember member, Rover rover, EquipmentType containerType,
			Integer resourceType) {

		boolean result = false;

		if (member instanceof Person) {
			Person person = (Person) member;

			// Check if person can exit the rover.
			if (!ExitAirlock.canExitAirlock(person, rover.getAirlock()))
				return false;

			if (EVAOperation.isGettingDark(person)) {
				return false;
			}
			
			// Check if person's medical condition will not allow task.
			if (person.getPerformanceRating() < .2D)
				return false;

			// Checks if available container with remaining capacity for resource.
			Unit container = findLeastFullContainer(rover.getInventory(), containerType, resourceType);
			boolean containerAvailable = (container != null);

			// Check if container and full EVA suit can be carried by person or is too
			// heavy.
			double carryMass = 0D;
			if (container != null) {
				carryMass += container.getMass();
			}
			EVASuit suit = rover.getInventory().findAnEVAsuit(person);
			if (suit != null) {
				carryMass += suit.getMass();
				carryMass += suit.getAmountResourceRemainingCapacity(ResourceUtil.oxygenID);
				carryMass += suit.getAmountResourceRemainingCapacity(ResourceUtil.waterID);
			}
			double carryCapacity = person.getCarryingCapacity();
			boolean canCarryEquipment = (carryCapacity >= carryMass);

			result = (containerAvailable && canCarryEquipment);
		}

		return result;
	}
}
