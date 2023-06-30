/*
 * Mars Simulation Project
 * ExploreSite.java
 * @date 2023-06-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.environment.ExploredLocation;
import org.mars_sim.msp.core.environment.MineralMap;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

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

	/** Task phases. */
	private static final TaskPhase EXPLORING = new TaskPhase(Msg.getString("Task.phase.exploring")); //$NON-NLS-1$

	// Static members
	/** The average labor time it takes to find the resource. */
	public static final double LABOR_TIME = 50D;

	private static final double AVERAGE_ROCK_SAMPLES_COLLECTED_SITE = 40 + RandomUtil.getRandomDouble(20);
	public static final double AVERAGE_ROCK_SAMPLE_MASS = 5 + RandomUtil.getRandomDouble(5);
	private static final double ESTIMATE_IMPROVEMENT_FACTOR = 5 + RandomUtil.getRandomDouble(5);

	private static final int ROCK_SAMPLES_ID = ResourceUtil.rockSamplesID;

	// Data members
	private double totalCollected = 0;
	private double numSamplesCollected = AVERAGE_ROCK_SAMPLES_COLLECTED_SITE / AVERAGE_ROCK_SAMPLE_MASS;
	
	// Future: should keep track of the actual total exploring site time and use it below.
	// The longer it stays, the more samples are collected and better the mining estimation
	private double chance = 0.5;

	private double skill;
	
	private ExploredLocation site;
	private Rover rover;

	/**
	 * Constructor.
	 *
	 * @param person the person performing the task.
	 * @param site   the site to explore.
	 * @param rover  the mission rover.
	 * @throws exception if error creating task.
	 */
	public ExploreSite(Person person, ExploredLocation site, Rover rover) {
		// Use EVAOperation parent constructor.
		super(NAME, person, true, LABOR_TIME + RandomUtil.getRandomDouble(-5D, 5D), SkillType.AREOLOGY);

		addAdditionSkill(SkillType.PROSPECTING);
		
		// Initialize data members.
		this.site = site;
		this.rover = rover;

		// Determine location for field work.
		setRandomOutsideLocation(rover);

		// Take specimen containers for rock samples.
		if (!hasSpecimenContainer()) {
			takeSpecimenContainer();
		}
		// If specimen containers are not available, end task.
		else {
			logger.warning(person, "No more specimen box for collecting rock samples.");
			endTask();
		}
		
		skill = (getEffectiveSkillLevel() + person.getSkillManager().getSkillLevel(SkillType.PROSPECTING)) / 2D;

		// Add task phase
		addPhase(EXPLORING);
	}
	
	
	/**
	 * Checks if a person can explore a site.
	 *
	 * @param member the member
	 * @param rover  the rover
	 * @return true if person can explore a site.
	 */
	public static boolean canExploreSite(Worker member, Rover rover) {

		if (member instanceof Person) {
			Person person = (Person) member;

			// Check if person can exit the rover.
			if (!ExitAirlock.canExitAirlock(person, rover.getAirlock()))
				return false;

			if (EVAOperation.isGettingDark(person)) {
				logger.fine(person, "Ended exploring site due to getting dark.");
				return false;
			}

			if (EVAOperation.isHungryAtMealTime(person)) {
				logger.fine(person, "Ended exploring site due to being hungry at meal time.");
				return false;
			}

			if (EVAOperation.isExhausted(person)) {
				logger.fine(person, "Ended exploring site due to being exhausted.");
				return false;
			}

			if (person.getPhysicalCondition().computeFitnessLevel() < 3)
				return false;

			// Check if person's medical condition will not allow task.
            return !(person.getPerformanceRating() < .2D);
		}

		return true;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return EXPLORING;
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
		
		// Add to the cumulative combined site time
		((Exploration)person.getMission()).addSiteTime(time);
		
		if (checkReadiness(time, false) > 0)
			return time;

		if (totalCollected >= AVERAGE_ROCK_SAMPLES_COLLECTED_SITE) {
			checkLocation();
			return time;
		}

		int rand = RandomUtil.getRandomInt(5);

		if (rand == 0) {
			// Improve mineral concentration estimates.
			improveMineralConcentrationEstimates(time);
		}
		else if (rand == 1){
			// Collect rocks.
			collectRocks(time);
		}
		else {
			site.improveCertainty(skill);
			
			// Checks if the site has been claimed
			if (!site.isClaimed()
				&& site.isCertaintyAverageOver50()) {
					site.setClaimed(true);
			}
		}
		
		// FUTURE: Add other site exploration activities later.

		// Add experience points
		addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);

		// Check if site duration has ended or there is reason to cut the exploring
		// phase short and return to the rover.
		if (addTimeOnSite(time)) {
			checkLocation();
			return remainingTime;
		}

		return remainingTime;
	}

	/**
	 * Collects rock samples if chosen.
	 *
	 * @param time the amount of time available (millisols).
	 * @throws Exception if error collecting rock samples.
	 */
	private void collectRocks(double time) {
		if (hasSpecimenContainer()) {
			
			double siteTime = ((Exploration)person.getMission()).getSiteTime();
			chance = numSamplesCollected * siteTime / 8000.0;
			
			double probability = (1 + site.getNumEstimationImprovement()) * chance * time 
					* (getEffectiveSkillLevel() + person.getSkillManager().getSkillLevel(SkillType.PROSPECTING)) / 2D;
			if (probability > .9)
				probability = .9;

			if (RandomUtil.getRandomDouble(1.0D) <= probability) {
				
				Container box = person.findContainer(EquipmentType.SPECIMEN_BOX, false, -1);
				int rockId = box.getResource();
				if (rockId == -1) {
					// Box is empty so choose at random
					int randomNum = RandomUtil.getRandomInt(((ResourceUtil.rockIDs).length) - 1);
					rockId = ResourceUtil.rockIDs[randomNum];
					logger.info(person, 10_000, "Type of rocks collected: " + ResourceUtil.ROCKS[randomNum] + ".");
				}

				double mass = RandomUtil.getRandomDouble(AVERAGE_ROCK_SAMPLE_MASS / 2D, AVERAGE_ROCK_SAMPLE_MASS * 2D);
				double cap = box.getAmountResourceRemainingCapacity(rockId);
				if (mass <= cap) {
					double excess = box.storeAmountResource(rockId, mass);
					totalCollected += mass - excess;
				}
			}
		}
	}

	/**
	 * Improves the mineral concentration estimates of an explored site.
	 *
	 * @param time the amount of time available (millisols).
	 */
	private void improveMineralConcentrationEstimates(double time) {

		double siteTime = ((Exploration)person.getMission()).getSiteTime();
			
		double probability = (time * siteTime / 100.0) 
				* skill
				* ESTIMATE_IMPROVEMENT_FACTOR;
		
		if (probability > .9)
			probability = .9;
		
		if ((site.getNumEstimationImprovement() == 0) || (RandomUtil.getRandomDouble(1.0D) <= probability)) {
			// Call to improve the site estimate first
			improveSiteEstimates(site, skill);
			
			logger.log(person, Level.INFO, 5_000,
					"Exploring site at " + site.getLocation().getFormattedString()				
					+ ". # of estimation made: "
					+ site.getNumEstimationImprovement() + ".");
		}
	}

	/**
	 * Improves the mineral estimates for a particular site. Reviewer has a certain
	 * skill rating.
	 * 
	 * @param site
	 * @param skill
	 */
	public static void improveSiteEstimates(ExploredLocation site, double skill) {

		int imp = site.getNumEstimationImprovement();
		MineralMap mineralMap = surfaceFeatures.getMineralMap();
		Map<String, Double> estimatedMineralConcentrations = site.getEstimatedMineralConcentrations();

		for (String mineralType : estimatedMineralConcentrations.keySet()) {
			double conc = mineralMap.getMineralConcentration(mineralType, site.getLocation());			
			double estimate = estimatedMineralConcentrations.get(mineralType);
			double diff = Math.abs(conc - estimate);
			
			double certainty = site.getDegreeCertainty(mineralType);
			double variance = .5 + RandomUtil.getRandomDouble(.5 * certainty) / 100;
			
			// Note that rand can 'overshoot' the target
			double rand = RandomUtil.getRandomDouble(1D * skill * imp / 50 * variance);
			if (rand > diff * 1.25)
				rand = diff * 1.25;
			if (estimate == conc)
				;// do nothing;
			else if (estimate < conc)
				estimate += rand;
			else
				estimate -= rand;
			
//			logger.info("Improving " + mineralType 
//					+ " estimation - rand: " + Math.round(rand * 100.0)/100.0
//					+ "   conc: " + Math.round(conc * 100.0)/100.0
//					+ "   estimate: " + Math.round(estimate * 100.0)/100.0	
//					+ "   diff: " + Math.round(diff * 100.0)/100.0
//					);
			
			estimatedMineralConcentrations.put(mineralType, estimate);
		}
		
		// Add to site mineral concentration estimation improvement number.
		site.incrementNumImprovement();
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
		Container container = ContainerUtil.findLeastFullContainer(
											rover, EquipmentType.SPECIMEN_BOX,
											ROCK_SAMPLES_ID);

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
	}
}
