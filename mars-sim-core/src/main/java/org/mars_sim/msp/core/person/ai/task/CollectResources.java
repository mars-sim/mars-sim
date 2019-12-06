/**
 * Mars Simulation Project
 * CollectResources.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
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

	private static Logger logger = Logger.getLogger(CollectResources.class.getName());

//	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
//			 logger.getName().length());

	/** The average labor time it takes to find the resource. */
	public static final double LABOR_TIME = 50D;
	
    private static String sourceName = logger.getName();
    
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
	protected Integer containerType;

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
			double targettedAmount, double startingCargo, Integer containerType) {

		// Use EVAOperation parent constructor.
		super(taskName, person, true, LABOR_TIME + RandomUtil.getRandomDouble(10D) - RandomUtil.getRandomDouble(10D));

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
		if (!hasContainers()) {
			takeContainer();

			// If container is not available, end task.
			if (!hasContainers()) {        
				LogConsolidated.log(logger, Level.FINE, 5000, sourceName, 
	        		"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " was not able to find containers to collect resources.", null);
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
				goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(),
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

		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (COLLECT_RESOURCES.equals(getPhase())) {
			return collectResources(time);
		} else {
			return time;
		}
	}

	/**
	 * Adds experience to the person's skills used in this task.
	 * 
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {

		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;

		// Experience points adjusted by person's "Experience Aptitude" attribute.
//		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		person.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);

		// If phase is collect resource, add experience to areology skill.
		if (COLLECT_RESOURCES.equals(getPhase())) {
			// 1 base experience point per 10 millisols of collection time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double areologyExperience = time / 10D;
			areologyExperience += areologyExperience * experienceAptitudeModifier;
			person.getSkillManager().addExperience(SkillType.AREOLOGY, areologyExperience, time);
		}
	}

	/**
	 * Checks if the person is carrying any containers.
	 * 
	 * @return true if carrying containers.
	 */
	private boolean hasContainers() {
		return person.getInventory().containsUnitClass(containerType);
	}

	/**
	 * Takes the least full container from the rover.
	 * 
	 * @throws Exception if error taking container.
	 */
	private void takeContainer() {
		Unit container = findLeastFullContainer(rover.getInventory(), containerType, resourceType);
		if (container != null && person.getInventory().canStoreUnit(container, false)) {
			container.transfer(rover, person);
		}
	}

	/**
	 * Gets the least full container in the rover.
	 * 
	 * @param inv           the inventory to look in.
	 * @param containerType the container class to look for.
	 * @param resourceType  the resource for capacity.
	 * @return container.
	 */
	private static Unit findLeastFullContainer(Inventory inv, Integer containerType, Integer resource) {
		Unit result = null;
		double mostCapacity = 0D;

		Iterator<Unit> i = inv.findAllUnitsOfClass(containerType).iterator();
		while (i.hasNext()) {
			Unit container = i.next();
			double remainingCapacity = container.getInventory().getAmountResourceRemainingCapacity(resource, true, false);
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

		// Check for an accident during the EVA operation.
		checkForAccident(time);

		// Check for radiation exposure during the EVA operation.
		if (isRadiationDetected(time)) {
			setPhase(WALK_BACK_INSIDE);
			return time;
		}

		// Check if site duration has ended or there is reason to cut the collect
		// resources phase short and return to the rover.
		if (shouldEndEVAOperation() || addTimeOnSite(time)) {
			setPhase(WALK_BACK_INSIDE);
			return time;
		}

		double remainingPersonCapacity = person.getInventory().getAmountResourceRemainingCapacity(resourceType, true, false);
		double currentSamplesCollected = rover.getInventory().getAmountResourceStored(resourceType, false) - startingCargo;
		double remainingSamplesNeeded = targettedAmount - currentSamplesCollected;
		double sampleLimit = remainingPersonCapacity;
		if (remainingSamplesNeeded < remainingPersonCapacity) {
			sampleLimit = remainingSamplesNeeded;
		}

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

		// Collect resources.
		if (samplesCollected <= sampleLimit) {
			person.getInventory().storeAmountResource(resourceType, samplesCollected, true);
			return 0D;
		} else {
			if (sampleLimit >= 0D) {
				person.getInventory().storeAmountResource(resourceType, sampleLimit, true);
//				person.getInventory().addAmountSupply(resourceType, sampleLimit);
			}
			setPhase(WALK_BACK_INSIDE);
			return time - (sampleLimit / collectionRate);
		}

	}

	@Override
	public void endTask() {

		// Unload containers to rover's inventory.
		Inventory pInv = person.getInventory();
		if (pInv.containsUnitClass(containerType)) {
			// Load containers in rover.
			Iterator<Unit> i = pInv.findAllUnitsOfClass(containerType).iterator();
			while (i.hasNext()) {
				// Place this equipment within a rover outside on Mars
				i.next().transfer(pInv, rover);
//				pInv.retrieveUnit(container);
//				rover.getInventory().storeUnit(container);
			}
		}

		super.endTask();
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
	public static boolean canCollectResources(MissionMember member, Rover rover, Integer containerType,
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
			if (person.getPerformanceRating() < .5D)
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
			EVASuit suit = rover.getInventory().findAnEVAsuit(); //(EVASuit) rover.getInventory().findUnitOfClass(EVASuit.class);
			if (suit != null) {
				carryMass += suit.getMass();
				carryMass += suit.getInventory().getAmountResourceRemainingCapacity(ResourceUtil.oxygenID, false, false);
				carryMass += suit.getInventory().getAmountResourceRemainingCapacity(ResourceUtil.waterID, false, false);
			}
			double carryCapacity = person.getInventory().getGeneralCapacity();
			boolean canCarryEquipment = (carryCapacity >= carryMass);

			result = (containerAvailable && canCarryEquipment);
		}

		return result;
	}

	/**
	 * Gets the effective skill level a person has at this task.
	 * 
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
		int areologySkill = manager.getEffectiveSkillLevel(SkillType.AREOLOGY);
		return (int) Math.round((double) (EVAOperationsSkill + areologySkill) / 2D);
	}

	/**
	 * Gets a list of the skills associated with this task. May be empty list if no
	 * associated skills.
	 * 
	 * @return list of skills
	 */
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(2);
		results.add(SkillType.EVA_OPERATIONS);
		results.add(SkillType.AREOLOGY);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();

		rover = null;
		resourceType = null;
		containerType = null;
	}
}