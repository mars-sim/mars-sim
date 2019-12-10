/**
 * Mars Simulation Project
 * ExploreSite.java
 * @version 3.1.0 2017-03-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.SpecimenBox;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.mars.MineralMap;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for the EVA operation of exploring a site.
 */
public class ExploreSite extends EVAOperation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ExploreSite.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.exploreSite"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase EXPLORING = new TaskPhase(Msg.getString("Task.phase.exploring")); //$NON-NLS-1$

	// Static members
	private static final double AVERAGE_ROCK_SAMPLES_COLLECTED_SITE = 10D;
	public static final double AVERAGE_ROCK_SAMPLE_MASS = .5D;
	private static final double ESTIMATE_IMPROVEMENT_FACTOR = 5D;

	// Data members
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
		super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D);

		// Initialize data members.
		this.site = site;
		this.rover = rover;

		// Determine location for field work.
		Point2D exploreLoc = determineExploreLocation();
		setOutsideSiteLocation(exploreLoc.getX(), exploreLoc.getY());

		// Take specimen containers for rock samples.
		if (!hasSpecimenContainer()) {
			takeSpecimenContainer();

			// If specimen containers are not available, end task.
			if (!hasSpecimenContainer()) {
				logger.fine(person.getName() + " was unable to find any specimen containers to collect rock samples.");
				endTask();
			}
		}

		// Add task phase
		addPhase(EXPLORING);
	}

	/**
	 * Determine location to explore.
	 * 
	 * @return field work X and Y location outside rover.
	 */
	private Point2D determineExploreLocation() {

		Point2D newLocation = null;
		boolean goodLocation = false;
		for (int x = 0; (x < 5) && !goodLocation; x++) {
			for (int y = 0; (y < 10) && !goodLocation; y++) {

				double distance = RandomUtil.getRandomDouble(100D) + (x * 100D) + 50D;
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

	/**
	 * Checks if a person can explore a site.
	 * 
	 * @param member the member
	 * @param rover  the rover
	 * @return true if person can explore a site.
	 */
	public static boolean canExploreSite(MissionMember member, Rover rover) {

		if (member instanceof Person) {
			Person person = (Person) member;

			// Check if person can exit the rover.
			if (!ExitAirlock.canExitAirlock(person, rover.getAirlock()))
				return false;

			if (EVAOperation.isGettingDark(person)) {
				logger.fine(person.getName() + " ended exploring site due to getting dark.");
				return false;
			}

			if (EVAOperation.isHungryAtMealTime(person)) {
				logger.fine(person.getName() + " ended exploring site due to meal time.");
				return false;
			}

			// Check if person's medical condition will not allow task.
			if (person.getPerformanceRating() < .2D)
				return false;
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

		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (EXPLORING.equals(getPhase())) {
			return exploringPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Perform the exploring phase of the task.
	 * 
	 * @param time the time available (millisols).
	 * @return remaining time after performing phase (millisols).
	 * @throws Exception if error performing phase.
	 */
	private double exploringPhase(double time) {

		// Check for an accident during the EVA operation.
		checkForAccident(time);

		// Check for radiation exposure during the EVA operation.
		if (isRadiationDetected(time)) {
			setPhase(WALK_BACK_INSIDE);
			return time;
		}

		// Check if site duration has ended or there is reason to cut the exploring
		// phase short and return to the rover.
		if (shouldEndEVAOperation() || addTimeOnSite(time)) {
			setPhase(WALK_BACK_INSIDE);
			return time;
		}

		// Collect rock samples.
		collectRockSamples(time);

		// Improve mineral concentration estimates.
		improveMineralConcentrationEstimates(time);

		// TODO: Add other site exploration activities later.

		// Add experience points
		addExperience(time);

		return 0D;
	}

	/**
	 * Collect rock samples if chosen.
	 * 
	 * @param time the amount of time available (millisols).
	 * @throws Exception if error collecting rock samples.
	 */
	private void collectRockSamples(double time) {
		if (hasSpecimenContainer()) {
			double numSamplesCollected = AVERAGE_ROCK_SAMPLES_COLLECTED_SITE / AVERAGE_ROCK_SAMPLE_MASS;
			double probability = (time / Exploration.EXPLORING_SITE_TIME) * (numSamplesCollected);
			if (RandomUtil.getRandomDouble(1.0D) <= probability) {
				Inventory inv = person.getInventory();
				double rockSampleMass = RandomUtil.getRandomDouble(AVERAGE_ROCK_SAMPLE_MASS * 2D);
				double rockSampleCapacity = inv.getAmountResourceRemainingCapacity(ResourceUtil.rockSamplesID, true,
						false);
				if (rockSampleMass < rockSampleCapacity)
					inv.storeAmountResource(ResourceUtil.rockSamplesID, rockSampleMass, true);
				inv.addAmountSupply(ResourceUtil.rockSamplesID, rockSampleMass);
			}
		}
	}

	/**
	 * Improve the mineral concentration estimates of an explored site.
	 * 
	 * @param time the amount of time available (millisols).
	 */
	private void improveMineralConcentrationEstimates(double time) {
		double probability = (time / Exploration.EXPLORING_SITE_TIME) * getEffectiveSkillLevel()
				* ESTIMATE_IMPROVEMENT_FACTOR;
		if (RandomUtil.getRandomDouble(1.0D) <= probability) {
			MineralMap mineralMap = Simulation.instance().getMars().getSurfaceFeatures().getMineralMap();
			Map<String, Double> estimatedMineralConcentrations = site.getEstimatedMineralConcentrations();
			Iterator<String> i = estimatedMineralConcentrations.keySet().iterator();
			while (i.hasNext()) {
				String mineralType = i.next();
				double actualConcentration = mineralMap.getMineralConcentration(mineralType, site.getLocation());
				double estimatedConcentration = estimatedMineralConcentrations.get(mineralType);
				double estimationDiff = Math.abs(actualConcentration - estimatedConcentration);
				double estimationImprovement = RandomUtil.getRandomDouble(1D * getEffectiveSkillLevel());
				if (estimationImprovement > estimationDiff)
					estimationImprovement = estimationDiff;
				if (estimatedConcentration < actualConcentration)
					estimatedConcentration += estimationImprovement;
				else
					estimatedConcentration -= estimationImprovement;
				estimatedMineralConcentrations.put(mineralType, estimatedConcentration);
			}

			// Add to site mineral concentration estimation improvement number.
			site.addEstimationImprovement();
			LogConsolidated.log(Level.FINE, 5000, sourceName, "[" + person.getLocationTag().getLocale() + "] "
					+ person.getName() + " was exploring the site at " + site.getLocation().getFormattedString() 
					+ ". Estimation Improvement: "
					+ site.getNumEstimationImprovement() + ".");
		}
	}

	/**
	 * Checks if the person is carrying a specimen container.
	 * 
	 * @return true if carrying container.
	 */
	private boolean hasSpecimenContainer() {
		return person.getInventory().containsUnitClass(SpecimenBox.class);
	}

	/**
	 * Takes the least full specimen container from the rover, if any are available.
	 * 
	 * @throws Exception if error taking container.
	 */
	private void takeSpecimenContainer() {
		Unit container = findLeastFullContainer(rover);
		if (container != null) {
			if (person.getInventory().canStoreUnit(container, false)) {
				container.transfer(rover, person);
			}
		}
	}

	/**
	 * Gets the least full specimen container in the rover.
	 * 
	 * @param rover the rover with the inventory to look in.
	 * @return specimen container or null if none.
	 */
	private static SpecimenBox findLeastFullContainer(Rover rover) {
		SpecimenBox result = null;
		double mostCapacity = 0D;

		Iterator<SpecimenBox> i = rover.getInventory().findAllSpecimenBoxes().iterator();
		while (i.hasNext()) {
			SpecimenBox container = i.next();
			try {
				double remainingCapacity = container.getInventory()
						.getAmountResourceRemainingCapacity(ResourceUtil.rockSamplesID, false, false);

				if (remainingCapacity > mostCapacity) {
					result = container;
					mostCapacity = remainingCapacity;
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}

		return result;
	}

	@Override
	protected void addExperience(double time) {
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;

		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		person.getSkillManager().addExperience(SkillType.EVA_OPERATIONS, evaExperience, time);

		// If phase is exploring, add experience to areology skill.
		if (EXPLORING.equals(getPhase())) {
			// 1 base experience point per 10 millisols of exploration time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double areologyExperience = time / 10D;
			areologyExperience += areologyExperience * experienceAptitudeModifier;
			person.getSkillManager().addExperience(SkillType.AREOLOGY, areologyExperience, time);
		}
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(2);
		results.add(SkillType.EVA_OPERATIONS);
		results.add(SkillType.AREOLOGY);
		return results;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
		int areologySkill = manager.getEffectiveSkillLevel(SkillType.AREOLOGY);
		return (int) Math.round((double) (EVAOperationsSkill + areologySkill) / 2D);
	}

	@Override
	public void endTask() {

		// Load specimen container in rover.
		Inventory pInv = person.getInventory();
		if (pInv.containsUnitClass(SpecimenBox.class)) {
			SpecimenBox box = pInv.findASpecimenBox();
			box.transfer(pInv, rover);
		}

		super.endTask();
	}

	@Override
	public void destroy() {
		super.destroy();

		site = null;
		rover = null;
	}
}