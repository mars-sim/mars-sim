/*
 * Mars Simulation Project
 * CollectMinedMinerals.java
 * @date 2023-09-17
 * @author Scott Davis
 */

package com.mars_sim.core.person.ai.task;

import java.util.logging.Level;

import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.ContainerUtil;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

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
	private static final TaskPhase COLLECT_MINERALS = new TaskPhase(Msg.getString("Task.phase.collectMinerals"),
															createPhaseImpact(SkillType.AREOLOGY, SkillType.PROSPECTING));

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
		super(NAME, person, LABOR_TIME + RandomUtil.getRandomDouble(-5, 5), COLLECT_MINERALS);
		setMinimumSunlight(MineSite.LIGHT_LEVEL);
	       
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
				logger.log(person, Level.WARNING, 5_000,
						"Unable to find more bags to collect mined minerals.");
				endTask();
			}
		}
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

		if (checkReadiness(time) > 0) {
			return time;
		}
		
		Mining mining = (Mining) worker.getMission();

		if (mining.getMiningSite().isEmpty()) {
			checkLocation("No more minerals to mine.");
			return time;
		}
		
		double mineralsExcavated = mining.getMineralExcavationAmount(mineralType);
		double remainingPersonCapacity = 0;
			
		remainingPersonCapacity = worker.getRemainingCargoCapacity();

		double concentration = mining.getMiningSite().getEstimatedMineralConcentrations().get(mineralType.getName());	
		double reserve = mining.getMiningSite().getRemainingMass();
		double certainty = mining.getMiningSite().getDegreeCertainty(mineralType.getName());
		double variance = .5 + RandomUtil.getRandomDouble(.5 * certainty) / 100;
		
		double mineralsCollected = variance * time * reserve * MINERAL_SELECTION_RATE * concentration;

		// Modify collection rate by skill.
		int compositeSkills = getEffectiveSkillLevel();
		if (compositeSkills == 0)
			mineralsCollected /= 2D;
		if (compositeSkills > 1)
			mineralsCollected += mineralsCollected * (.2D * compositeSkills);

		if (mineralsCollected > remainingPersonCapacity)
			mineralsCollected = remainingPersonCapacity;
		if (mineralsCollected > mineralsExcavated)
			mineralsCollected = mineralsExcavated;

		if (mineralsCollected > 0) {
			// Add experience points
			addExperience(time);
	
			// Collect minerals.
			worker.storeAmountResource(mineralType.getID(), mineralsCollected);
	
			logger.info(worker, "Collected " + Math.round(mineralsCollected * 1000.0)/1000.0 
					+ " kg " + mineralType.getName() + ".");
			
			mining.collectMineral(mineralType, mineralsCollected);
			
			if (((mineralsExcavated - mineralsCollected) <= 0D) || (mineralsCollected >= remainingPersonCapacity)) {
				checkLocation("Excavated minerals collected exceeded capacity.");
			}
	
			// Check for an accident during the EVA operation.
			checkForAccident(time);
		}
		
		return 0;
	}
	
	@Override
	protected void clearDown() {
		if (rover != null) {
			// Task may end early before a Rover is selected
			returnEquipmentToVehicle(rover);
		}

		super.clearDown();
	}
}
