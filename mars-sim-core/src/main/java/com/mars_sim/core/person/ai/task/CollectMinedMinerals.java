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
	protected int resourceType;
	
	/** The container type to use to collect resource. */
	protected EquipmentType containerType = EquipmentType.LARGE_BAG;
	
	private Rover rover;
	
	protected AmountResource mineralType;
	
	private Mining mining;
	
	/**
	 * Constructor.
	 * 
	 * @param person      the person performing the task.
	 * @param rover       the rover used for the EVA operation.
	 * @param mineralType the type of mineral to collect.
	 */
	public CollectMinedMinerals(Person person, Rover rover, AmountResource mineralType) {

		// Use EVAOperation parent constructor.
		super(NAME, person, LABOR_TIME + RandomUtil.getRandomDouble(-5, 5), COLLECT_MINERALS);
		
		mining = (Mining) worker.getMission();
		if (mining == null) {
			logger.log(person, Level.WARNING, 0, "No mining mission assigned.");
			endTask();
		}
		
		setMinimumSunlight(MineSite.LIGHT_LEVEL);
	       
		// Initialize data members.
		this.rover = rover;
		this.mineralType = mineralType;
		this.resourceType = mineralType.getID();	
		
		// Determine location for collection site.
		setRandomOutsideLocation(rover);

		// Take container for collecting resource.
		int num = numContainers();
		if (num == 0) {
			boolean hasIt = takeContainer();

			// If container is not available, end task.
			if (!hasIt) {
				logger.warning(person, 5000,
						"Unable to find a " + containerType.getName() + " to collect resources.");
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
					if (done)
						logger.info(person, 0, "Returned an extra " + containerType.getName() + " from person back to rover.");
					else
						logger.warning(person, 0, "Unable to transfer " + containerType.getName() + " from person back to rover.");
				}	
			}
		}
	}

	/**
	 * Checks how many containers a person is carrying.
	 *
	 * @return true if carrying a container of this type.
	 *
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
				logger.info(person, 5000, "Getting hold of a " + containerType.getName() + " from rover.");
			}
			else 
				logger.warning(person, "Unable to transfer " + containerType.getName() + " from rover to person.");
			return success;
		} 
		else
			logger.warning(person, 5000, "Could not get hold of a " + containerType.getName() + " from rover.");
		
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

		if (mining == null) {
			logger.log(person, Level.WARNING, 0, "No mining mission assigned.");
			endTask();
			return time;
		}
		
		if (mining != null && mining.getMiningSite().isEmpty()) {
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
