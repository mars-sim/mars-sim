/*
 * Mars Simulation Project
 * ExploreSite.java
 * @date 2025-10-11
 * @author Scott Davis
 */
package com.mars_sim.core.mission.task;

import java.util.Map;
import java.util.logging.Level;

import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mineral.MineralMap;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Exploration;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * A task for the EVA operation of exploring a site.
 */
public class ExploreSite extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ExploreSite.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.exploreSite"); //$NON-NLS-1$

	/** Simple Task name */
	public static final String SIMPLE_NAME = ExploreSite.class.getSimpleName();
	
	/** Task phases. */
	private static final TaskPhase EXPLORING = new TaskPhase(Msg.getString("Task.phase.exploring"),
									createPhaseImpact(SkillType.AREOLOGY, SkillType.PROSPECTING));

	// Static members
	/** The average labor time it takes to find the resource. */
	public static final double LABOR_TIME = 50D;

	private static final double AVERAGE_ROCK_COLLECTED_SITE = 100 + RandomUtil.getRandomDouble(-20, 20);
	public static final double AVERAGE_ROCK_MASS = 3 + RandomUtil.getRandomDouble(-1, 1);
	private static final double ESTIMATE_IMPROVEMENT_FACTOR = 5 + RandomUtil.getRandomDouble(5);

    public static final LightLevel LIGHT_LEVEL = LightLevel.LOW;

	// Data members
	private int rockId = -1;
    
    private double totalCollected = 0;
	private double rocksToBeCollected = AVERAGE_ROCK_COLLECTED_SITE / AVERAGE_ROCK_MASS;
	
	private Exploration mission;
	private MineralSite site;
	private Rover rover;

	/**
	 * Constructor.
	 *
	 * @param person the person performing the task.
	 * @param site   the site to explore.
	 * @param rover  the mission rover.
	 * @throws exception if error creating task.
	 */
	public ExploreSite(Person person, MineralSite site, Rover rover, Exploration mission) {
		// Use EVAOperation parent constructor.
		super(NAME, person, LABOR_TIME + RandomUtil.getRandomDouble(-5D, 5D), EXPLORING);

		setMinimumSunlight(LIGHT_LEVEL);
		
		// Initialize data members.
		this.site = site;
		this.rover = rover;
		this.mission = mission;

		if (site == null) {
			logger.severe(person, 5_000, "Site not available.");
			endTask();
		}
		
		// Determine location for field work.
		setRandomOutsideLocation(rover);

		// Box is empty so choose a rock type at random.
		int randomNum = RandomUtil.getRandomInt(((ResourceUtil.ROCK_IDS).length) - 1);
		rockId = ResourceUtil.ROCK_IDS[randomNum];
	
		// Take specimen containers for rock samples.
		if (!hasSpecimenContainer()) {
			boolean hasBox = takeSpecimenContainer();

			if (!hasBox) {
				// If specimen containers are not available, end task.
				logger.log(person, Level.WARNING, 5_000,
						"No more specimen box for collecting rocks.");
				endTask();
			}
			else {
				logger.info(person, 5_000, "Expected to collect " 
						+ Math.round(rocksToBeCollected * 10.0)/10.0 + " kg rocks.");
			}
		}		
	}
	
	
	/**
	 * Checks if a person can explore a site.
	 *
	 * @param member the member
	 * @return true if person can explore a site.
	 */
	public static boolean canExploreSite(Worker member) {
		// Note: hasEVASuitProblem requires a person to have donned the suit already
		// and thus is not suitable for calling (EVAOperation.hasEVASuitProblem()) here
		
		if (member instanceof Person person && !person.isEVAFit()) {
			logger.info(person, 20_000, "Not EVA fit to explore the site.");
			return false;
		}

		return true;
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);
		if (!isDone()) {
			if (getPhase() == null) {
				throw new IllegalArgumentException("Task phase is null");
			} else if (EXPLORING.equals(getPhase())) {
				time = exploringPhase(time);
			}
		}
		return time;
	}

	/**
	 * Performs the exploring phase of the task.
	 *
	 * @param time the time available (millisols).
	 * @return remaining time after performing phase (millisols).
	 * @throws Exception if error performing phase.
	 */
	private double exploringPhase(double time) {
		double remainingTime = 0;
		
		if (checkReadiness(time) > 0)
			return time;

		// Add to the cumulative combined site time
		((Exploration)person.getMission()).addSiteTime(time);
		
		if (totalCollected > AVERAGE_ROCK_COLLECTED_SITE) {
			endEVA("Rocks collected exceeded the set average.");
			return time;
		}

		int skill = getEffectiveSkillLevel();
		int num = (int)(site.getNumEstimationImprovement()/5.0);
		int value = (100 - num)/2;
		int rand = RandomUtil.getRandomInt(100);
		if (value > 0 && rand < value) {
			// Improve mineral concentration estimates.
			improveMineralConcentrationEstimates(time, skill);
		}
		else if (value > 0 && rand < .75 * value){
			// Collect rocks.
			collectRocks(time * skill);
		}
		else {
			boolean isOver50 = site.isCertaintyAverageOver(50);
			if (!isOver50) {
				site.improveCertainty(skill);	
			}
			else {
				// Collect rocks.
				collectRocks(time * skill);

				// Checks if the site has been claimed
				if (!site.isClaimed()) {
					worker.getAssociatedSettlement().getExplorations().claimSite(site);
				}
			}
		}
		
		// FUTURE: Add other site exploration activities later.

		// Add experience points
		addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);

		return remainingTime;
	}

	/**
	 * Collects rocks.
	 *
	 * @param timeSkill time multiplying skill
	 * @throws Exception if error collecting rock samples.
	 */
	private void collectRocks(double timeSkill) {
		
		if (hasSpecimenContainer()) {

			Container box = person.findContainer(EquipmentType.SPECIMEN_BOX, false, rockId);
			
			if (box != null) {
				double mass = AVERAGE_ROCK_MASS * timeSkill * RandomUtil.getRandomDouble(.5, 2);
				double cap = box.getRemainingCombinedCapacity(rockId);
				if (mass <= cap) {
					double excess = box.storeAmountResource(rockId, mass);
					mission.recordResourceCollected(rockId, mass);
					double collected = mass - excess;
					totalCollected += collected;
					logger.info(person, 10_000, "Collected " + Math.round(collected * 100.0)/100.0 
							+ " kg " + ResourceUtil.findAmountResourceName(rockId) + " into a specimen box.");
				}
				else {
					double excess = box.storeAmountResource(rockId, cap);
					mission.recordResourceCollected(rockId, cap);
					double collected = cap - excess;
					totalCollected += collected;
					endEVA("Specimen box full.");
				}
			}
			else {
				endEVA("No specimen box available for " + ResourceUtil.findAmountResourceName(rockId) + ".");
			}
		}
		else {
			endEVA("No specimen boxes available.");
		}
	}

	/**
	 * Improves the mineral concentration estimates of an explored site.
	 *
	 * @param time the amount of time available (millisols).
	 * @param skill
	 * @return number of improvement
	 */
	private int improveMineralConcentrationEstimates(double time, int skill) {

		int numImprovement = site.getNumEstimationImprovement();
		
		double onSiteBonus = 2D;
		
		double siteTime = ((Exploration)person.getMission()).getCurrentSiteTime();
			
		double probability = (time * siteTime / 100.0) 
				* skill
				* ESTIMATE_IMPROVEMENT_FACTOR;
		
		if (probability > .9)
			probability = .9;

		if (numImprovement < 10  || (RandomUtil.getRandomDouble(1.0D) <= probability)) {
			// Call to improve the site estimate first
			numImprovement = improveSiteEstimates(site, skill * onSiteBonus);
			
			logger.log(person, Level.INFO, 5_000,
					"Exploring site at " + site.getLocation().getFormattedString()				
					+ ". # of estimation made: "
					+ numImprovement + ".");
		}
		
		return numImprovement;
	}

	/**
	 * Improves the mineral estimates for a particular site. Reviewer has a certain
	 * skill rating.
	 * 
	 * @param site
	 * @param skill
	 * @return number of improvement
	 */
	public static int improveSiteEstimates(MineralSite site, double skill) {

		int imp = site.getNumEstimationImprovement();
		MineralMap mineralMap = surfaceFeatures.getMineralMap();
		Map<String, Double> estimatedMineralConcentrations = site.getEstimatedMineralConcentrations();

		for (var entry : estimatedMineralConcentrations.entrySet()) {
			var mineralType = entry.getKey();
			double conc = mineralMap.getMineralConcentration(mineralType, site.getLocation());			
			double estimate = entry.getValue();
			double diff = Math.abs(conc - estimate);
			
			double certainty = site.getDegreeCertainty(mineralType);
			double variance = .5 + RandomUtil.getRandomDouble(.5 * certainty) / 100;
			
			// Note that rand can 'overshoot' the target
			double rand = RandomUtil.getRandomDouble(1D * skill * imp / 50 * variance);
			if (rand > diff * 1.25)
				rand = diff * 1.25;
			if (estimate < conc)
				estimate += rand;
			else if (estimate > conc)
				estimate -= rand;

			estimatedMineralConcentrations.put(mineralType, estimate);
		}
		
		// Add to site mineral concentration estimation improvement number.
		site.incrementNumImprovement(1);
		
		return site.getNumEstimationImprovement();
	}

	/**
	 * Checks if the person is carrying a specimen container.
	 *
	 * @return true if carrying container.
	 */
	private boolean hasSpecimenContainer() {
		return person.containsEquipment(EquipmentType.SPECIMEN_BOX);
	}

	/**
	 * Takes the least full specimen container from the rover, if any are available.
	 *
	 * @return true if the person receives a specimen container.
	 */
	private boolean takeSpecimenContainer() {
		
		Container container = rover.findContainer(EquipmentType.SPECIMEN_BOX, true, rockId);
		if (container != null) {
			return container.transfer(person);
		}
		return false;
	}

	/**
	 * Transfers the Specimen box to the Vehicle.
	 */
	@Override
	protected void clearDown() {
		if (rover != null) {
			// Task may end early before a Rover is selected
			returnEquipmentToVehicle(rover);
		}
		
		// Remove pressure suit and put on garment
		if (person.unwearPressureSuit(rover)) {
			person.wearGarment(rover);
		}
	
		// Assign thermal bottle
		person.assignThermalBottle();
				
		super.clearDown();
	}
}
