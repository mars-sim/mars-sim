/*
 * Mars Simulation Project
 * CollectMinedMinerals.java
 * @date 2023-09-17
 * @author Scott Davis
 */

package com.mars_sim.core.mission.task;

import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.ContainerUtil;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.objectives.MiningObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * Task for collecting minerals that have been mined at a site.
 */
public class CollectMinedMinerals extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(CollectMinedMinerals.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.collectMinedMinerals"); //-NLS-1$

	/** Simple Task name */
	static final String SIMPLE_NAME = CollectMinedMinerals.class.getSimpleName();
	
	/** Task phases. */
	private static final TaskPhase COLLECT_MINERALS = new TaskPhase(Msg.getString("Task.phase.collectMinerals"),
															createPhaseImpact(SkillType.AREOLOGY, SkillType.PROSPECTING));

	/** The average labor time it takes to find the mineral */
	public static final double LABOR_TIME = 40D;

	public static final double COLLECTION_RATE = 0.3D;
	
	// Data members
	protected int resourceType;
	
	/** The container type to use to collect resource. */
	private EquipmentType containerType = EquipmentType.LARGE_BAG;
	
	private int mineralType;
	private double maxAmount;
	private double totalCollected = 0D;
	
	private MiningObjective objective;
	private Rover rover;
	
	/**
	 * Constructor.
	 * 
	 * @param person      the person performing the task.
	 * @param objective The objective
	 * @param rover Base for the collection
	 * @param mineralType the type of mineral to collect.
	 */
	public CollectMinedMinerals(Person person, MiningObjective objective, Rover rover, int mineralType) {

		// Use EVAOperation parent constructor.
		super(NAME, person, LABOR_TIME + RandomUtil.getRandomDouble(-5, 5), COLLECT_MINERALS);

		setMinimumSunlight(MineSite.LIGHT_LEVEL);
	       
		// Initialize data members.
		this.objective = objective;
		this.mineralType = mineralType;
		this.maxAmount = objective.getMineralStats().get(mineralType).getAvailable();
		this.rover = rover;

		// Determine location for collection site.
		setRandomOutsideLocation(rover);

		// Take container for collecting resource.
		int num = numContainers();
		if (num == 0) {
			boolean hasIt = takeContainer(rover);

			// If container is not available, end task.
			if (!hasIt) {
				logger.warning(person, 5000,
						"Unable to find a " + containerType.getName() + " to collect resources.");
				endTask();
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
	 */
	private boolean takeContainer(Rover rover) {
		
		Container container = ContainerUtil.findLeastFullContainer(rover, containerType, resourceType);

		if (container != null) {
			boolean success = container.transfer(person);
			if (!success) {
				logger.warning(person, "Unable to transfer " + containerType.getName() + " from rover to person.");
			}
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

		double remainingPersonCapacity = 0;
			
		remainingPersonCapacity = worker.getRemainingCargoCapacity();

		// Modify collection rate by skill.
		double mineralsCollected = time * COLLECTION_RATE;
		int compositeSkills = getEffectiveSkillLevel();
		if (compositeSkills == 0)
			mineralsCollected /= 2D;
		if (compositeSkills > 1)
			mineralsCollected += mineralsCollected * (.2D * compositeSkills);

		mineralsCollected = Math.min(mineralsCollected, remainingPersonCapacity);
		mineralsCollected = Math.min(mineralsCollected, maxAmount);
		maxAmount -= mineralsCollected;
		totalCollected += mineralsCollected;

		if (mineralsCollected > 0) {
			// Add experience points
			addExperience(time);
	
			// Collect minerals.
			worker.storeAmountResource(mineralType, mineralsCollected);
			
			if ((maxAmount <= 0D) || (mineralsCollected >= remainingPersonCapacity)) {
				checkLocation("Excavated minerals collected exceeded capacity.");
			}
	
			// Check for an accident during the EVA operation.
			checkForAccident(time);
		}
		
		return 0;
	}
	
	@Override
	protected void clearDown() {
		objective.recordResourceCollected(mineralType, totalCollected);

		// Task may end early before a Rover is selected
		returnEquipmentToVehicle(rover);
		
		super.clearDown();
	}
}
