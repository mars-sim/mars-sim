/*
 * Mars Simulation Project
 * CollectMinedMinerals.java
 * @date 2021-10-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.InventoryUtil;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * Task for collecting minerals that have been mined at a site.
 */
public class CollectMinedMinerals extends EVAOperation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CollectMinedMinerals.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.collectMinedMinerals"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase COLLECT_MINERALS = new TaskPhase(Msg.getString("Task.phase.collectMinerals")); //$NON-NLS-1$

	/** Rate of mineral collection (kg/millisol). */
	private static final double MINERAL_COLLECTION_RATE = 10D;

	/** The average labor time it takes to find the minerel */
	public static final double LABOR_TIME = 50D;
	
	// Data members
	private Rover rover; // Rover used.
	protected AmountResource mineralType;

	private static int oxygenID = ResourceUtil.oxygenID;
	private static int waterID = ResourceUtil.waterID;

	/**
	 * Constructor
	 * 
	 * @param person      the person performing the task.
	 * @param rover       the rover used for the EVA operation.
	 * @param mineralType the type of mineral to collect.
	 */
	public CollectMinedMinerals(Person person, Rover rover, AmountResource mineralType) {

		// Use EVAOperation parent constructor.
		super(NAME, person, true, LABOR_TIME + RandomUtil.getRandomDouble(10D) - RandomUtil.getRandomDouble(10D),
					SkillType.AREOLOGY);
		
		// Initialize data members.
		this.rover = rover;
		this.mineralType = mineralType;

		// Determine location for collection site.
		Point2D collectionSiteLoc = determineCollectionSiteLocation();
		setOutsideSiteLocation(collectionSiteLoc.getX(), collectionSiteLoc.getY());

		// Take bags for collecting mined minerals.
		if (!hasBags()) {
			boolean hasBag = takeBag();

			// If bags are not available, end task.
			if (hasBag) {
				logger.log(person, Level.INFO, 5_000,
						"Unable to find more bags to collect mined minerals.", null);
				endTask();
			}
		}

		// Add task phases
		addPhase(COLLECT_MINERALS);
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
							worker.getCoordinates());

			}
		}

		return newLocation;
	}

	/**
	 * Checks if the person is carrying any bags.
	 * 
	 * @return true if carrying bags.
	 */
	private boolean hasBags() {
		return worker.containsEquipment(EquipmentType.BAG);
	}

	/**
	 * Takes the most least full bag from the rover.
	 * 
	 * @throws Exception if error taking bag.
	 */
	private boolean takeBag() {
		Container bag = ContainerUtil.findLeastFullContainer(rover,
											EquipmentType.BAG,
											mineralType.getID());
		if (bag != null) {
			if (person != null) {
				return bag.transfer(person);
			} else if (robot != null) {
				return bag.transfer(robot);
			}
		}
		return false;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return COLLECT_MINERALS;
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);
		if (!isDone()) {
			if (getPhase() == null) {
				throw new IllegalArgumentException("Task phase is null");
			} else if (COLLECT_MINERALS.equals(getPhase())) {
				time = collectMineralsPhase(time);
			}
		}
		return time;
	}

	/**
	 * Perform the collect minerals phase of the task.
	 * 
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error collecting minerals.
	 */
	private double collectMineralsPhase(double time) {

		// Check for radiation exposure during the EVA operation.
		if (isDone() || isRadiationDetected(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}
		
		// Check if site duration has ended or there is reason to cut the collect
		// minerals phase short and return to the rover.
		if (shouldEndEVAOperation() || addTimeOnSite(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}
		
		Mining mission = (Mining) worker.getMission();

		double mineralsExcavated = mission.getMineralExcavationAmount(mineralType);
		double remainingPersonCapacity = 0;

		double roverRemainingCap = rover.getAmountResourceRemainingCapacity(mineralType.getID());
		
		double weight = 0;
		if (person != null)
			weight = person.getMass();
		else if (robot != null)
			weight = robot.getMass();
		
		if (roverRemainingCap < weight + 5) {
			setPhase(WALK_BACK_INSIDE);
			return .5 * time;
		}
			
		remainingPersonCapacity = worker.getAmountResourceRemainingCapacity(mineralType.getID());

		double mineralsCollected = time * MINERAL_COLLECTION_RATE;

		// Modify collection rate by "Areology" skill.
		int areologySkill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.AREOLOGY);
		if (areologySkill == 0)
			mineralsCollected /= 2D;
		if (areologySkill > 1)
			mineralsCollected += mineralsCollected * (.2D * areologySkill);

		if (mineralsCollected > remainingPersonCapacity)
			mineralsCollected = remainingPersonCapacity;
		if (mineralsCollected > mineralsExcavated)
			mineralsCollected = mineralsExcavated;

		// Add experience points
		addExperience(time);

		// Collect minerals.
		worker.storeAmountResource(mineralType.getID(), mineralsCollected);

		mission.collectMineral(mineralType, mineralsCollected);
		
		if (((mineralsExcavated - mineralsCollected) <= 0D) || (mineralsCollected >= remainingPersonCapacity)) {
			setPhase(WALK_BACK_INSIDE);
		}

		// Check for an accident during the EVA operation.
		checkForAccident(time);

		return 0D;
	}

	@Override
	protected void clearDown() {
		returnEquipmentToVehicle(rover);
	}

	/**
	 * Checks if a person can perform a CollectMinedMinerals task.
	 * 
	 * @param member      the member to perform the task
	 * @param rover       the rover the person will EVA from
	 * @param mineralType the resource to collect.
	 * @return true if person can perform the task.
	 */
	public static boolean canCollectMinerals(MissionMember member, Rover rover, AmountResource mineralType) {

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

			// Checks if available bags with remaining capacity for resource.
			Container bag = ContainerUtil.findLeastFullContainer(rover,
																EquipmentType.BAG,
																mineralType.getID());
			boolean bagAvailable = (bag != null);

			// Check if bag and full EVA suit can be carried by person or is too heavy.
			double carryMass = 0D;
			if (bag != null) {
				carryMass += bag.getBaseMass();
			}

			EVASuit suit = InventoryUtil.getGoodEVASuit(person.getContainerUnit(), person);
			if (suit != null) {
				carryMass += suit.getMass();
				carryMass += suit.getAmountResourceRemainingCapacity(oxygenID);
				carryMass += suit.getAmountResourceRemainingCapacity(waterID);
			}
			double carryCapacity = person.getCarryingCapacity();
			boolean canCarryEquipment = (carryCapacity >= carryMass);

			result = (bagAvailable && canCarryEquipment);
		}

		return result;
	}
}
