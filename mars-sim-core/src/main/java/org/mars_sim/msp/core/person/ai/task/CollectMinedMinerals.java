/*
 * Mars Simulation Project
 * CollectMinedMinerals.java
 * @date 2023-06-30
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.util.logging.Level;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EVASuitUtil;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * Task for collecting minerals that have been mined at a site.
 */
public class CollectMinedMinerals extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CollectMinedMinerals.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.collectMinedMinerals"); //$NON-NLS-1$

	/** Simple Task name */
	static final String SIMPLE_NAME = CollectMinedMinerals.class.getSimpleName();
	
	/** Task phases. */
	private static final TaskPhase COLLECT_MINERALS = new TaskPhase(Msg.getString("Task.phase.collectMinerals")); //$NON-NLS-1$

	/** Rate of mineral collection (kg/millisol). */
	private static final double MINERAL_SELECTION_RATE = .2;

	/** The average labor time it takes to find the mineral */
	public static final double LABOR_TIME = 40D;
	
	// Data members
	private Rover rover; // Rover used.
	protected AmountResource mineralType;

	/**
	 * Constructor
	 * 
	 * @param person      the person performing the task.
	 * @param rover       the rover used for the EVA operation.
	 * @param mineralType the type of mineral to collect.
	 */
	public CollectMinedMinerals(Person person, Rover rover, AmountResource mineralType) {

		// Use EVAOperation parent constructor.
		super(NAME, person, true, LABOR_TIME + RandomUtil.getRandomDouble(-5, 5),
					SkillType.AREOLOGY);
		
		addAdditionSkill(SkillType.PROSPECTING);
	       
		// Initialize data members.
		this.rover = rover;
		this.mineralType = mineralType;

		// Determine location for collection site.
		setRandomOutsideLocation(rover);

		// Take bags for collecting mined minerals.
		if (!hasBags()) {
			boolean hasBag = takeBag();

			// If bags are not available, end task.
			if (!hasBag) {
				logger.log(person, Level.INFO, 5_000,
						"Unable to find more bags to collect mined minerals.", null);
				endTask();
			}
		}

		// Add task phases
		addPhase(COLLECT_MINERALS);
	}

	/**
	 * Checks if the person is carrying any bags.
	 * 
	 * @return true if carrying bags.
	 */
	private boolean hasBags() {
		return worker.containsEquipment(EquipmentType.LARGE_BAG);
	}

	/**
	 * Takes the most least full bag from the rover.
	 * 
	 * @throws Exception if error taking bag.
	 */
	private boolean takeBag() {
		Container bag = ContainerUtil.findLeastFullContainer(rover,
											EquipmentType.LARGE_BAG,
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
			checkLocation();
			return time;
		}
		
		// Check if site duration has ended or there is reason to cut the collect
		// minerals phase short and return to the rover.
		if (shouldEndEVAOperation(false) || addTimeOnSite(time)) {
			checkLocation();
			return time;
		}
		
		Mining mission = (Mining) worker.getMission();

		if (mission.getMiningSite().isEmpty()) {
			checkLocation();
			return time;
		}
		
		double mineralsExcavated = mission.getMineralExcavationAmount(mineralType);
		double remainingPersonCapacity = 0;

//		double roverRemainingCap = rover.getAmountResourceRemainingCapacity(mineralType.getID());
//		
//		double weight = 0;
//		if (person != null)
//			weight = person.getMass();
//		else if (robot != null)
//			weight = robot.getMass();
//		
//		if (roverRemainingCap < weight + 5) {
//			checkLocation();
//			return time;
//		}
			
		remainingPersonCapacity = worker.getRemainingCargoCapacity();

		double concentration = mission.getMiningSite().getEstimatedMineralConcentrations().get(mineralType.getName());	
		double reserve = mission.getMiningSite().getRemainingMass();
		double certainty = mission.getMiningSite().getDegreeCertainty(mineralType.getName());
		double variance = .5 + RandomUtil.getRandomDouble(.5 * certainty) / 100;
		
		double mineralsCollected = variance * time * reserve * MINERAL_SELECTION_RATE * concentration;

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

		logger.info(worker, "Collected " + Math.round(mineralsCollected * 1000.0)/1000.0 
				+ " kg " + mineralType.getName() + ".");
		
		mission.collectMineral(mineralType, mineralsCollected);
		
		if (((mineralsExcavated - mineralsCollected) <= 0D) || (mineralsCollected >= remainingPersonCapacity)) {
			checkLocation();
		}

		// Check for an accident during the EVA operation.
		checkForAccident(time);

		return 0;
	}
	
	@Override
	protected void clearDown() {
		if (rover != null) {
			// Task may end early before a Rover is selected
			returnEquipmentToVehicle(rover);
		}
	}

	/**
	 * Checks if a person can perform a CollectMinedMinerals task.
	 * 
	 * @param member      the member to perform the task
	 * @param rover       the rover the person will EVA from
	 * @param mineralType the resource to collect.
	 * @return true if person can perform the task.
	 */
	public static boolean canCollectMinerals(Worker member, Rover rover, AmountResource mineralType) {

		boolean result = false;

		if (member instanceof Person) {
			Person person = (Person) member;

			// Check if person can exit the rover.
			if (!ExitAirlock.canExitAirlock(person, rover.getAirlock()))
				return false;

//			if (EVAOperation.isGettingDark(person)) {
//				return false;
//			}

			if (!isEnoughSunlightForEVA(person.getCoordinates())) {
				return false;
			}
			
			// Check if person's medical condition will not allow task.
			if (person.getPerformanceRating() < .2D)
				return false;

			// Checks if available bags with remaining capacity for resource.
			Container bag = ContainerUtil.findLeastFullContainer(rover,
																EquipmentType.LARGE_BAG,
																mineralType.getID());
			boolean bagAvailable = (bag != null);

			// Check if bag and full EVA suit can be carried by person or is too heavy.
			double carryMass = 0D;
			if (bag != null) {
				carryMass += bag.getBaseMass();
			}

			EVASuit suit = EVASuitUtil.findRegisteredOrGoodEVASuit(person);
			if (suit != null) {
				carryMass += suit.getMass();
				carryMass += suit.getAmountResourceRemainingCapacity(ResourceUtil.oxygenID);
				carryMass += suit.getAmountResourceRemainingCapacity(ResourceUtil.waterID);
			}
			double carryCapacity = person.getCarryingCapacity();
			boolean canCarryEquipment = (carryCapacity >= carryMass);

			result = (bagAvailable && canCarryEquipment);
		}

		return result;
	}
}
