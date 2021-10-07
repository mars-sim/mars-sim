/*
 * Mars Simulation Project
 * MeteorologyStudyFieldWork.java.java
 * @date 2021-08-28
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
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.equipment.SpecimenBox;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.MeteorologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for the EVA operation of performing meteorology field work at a
 * research site for a scientific study.
 */
public class MeteorologyStudyFieldWork extends EVAOperation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(MeteorologyStudyFieldWork.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.meteorologyFieldWork"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase FIELD_WORK = new TaskPhase(Msg.getString("Task.phase.fieldWork.meteorology")); //$NON-NLS-1$

	// Static members
	private static final double AVERAGE_ROCKS_COLLECTED_SITE = 40 + RandomUtil.getRandomDouble(20);
	public static final double AVERAGE_ROCKS_MASS = .5D + RandomUtil.getRandomDouble(.5);

	// https://en.wikipedia.org/wiki/Volcanology_of_Mars
	// http://www.space.com/198-mars-volcanoes-possibly-active-pictures-show.html
	private static final TaskPhase STUDY_VOLCANIC_ACTIVITIES = new TaskPhase(
			Msg.getString("Task.phase.fieldWork.volcanic")); //$NON-NLS-1$

	// http://www.ibtimes.com/marsquake-seismic-activity-red-planet-bodes-well-life-414690
	private static final TaskPhase STUDY_SEISMIC_ACTIVITIES = new TaskPhase(
			Msg.getString("Task.phase.fieldWork.seismic")); //$NON-NLS-1$

	// Aeolian or Eolian science is where meteorology meets geology
	// http://www.lpi.usra.edu/publications/slidesets/winds/
	// Modification of the martian surface by the wind (atmospheric dust storms,
	// dust devils, and perhaps even tornados)
	private static final TaskPhase STUDY_AEOLIAN_ACTIVITIES = new TaskPhase(
			Msg.getString("Task.phase.fieldWork.aeolian")); //$NON-NLS-1$

	// Data members
	private double totalCollected = 0;
	private double numSamplesCollected = AVERAGE_ROCKS_COLLECTED_SITE / AVERAGE_ROCKS_MASS;
	private double chance = numSamplesCollected / MeteorologyFieldStudy.FIELD_SITE_TIME;
	
	private Person leadResearcher;
	private ScientificStudy study;
	private Rover rover;

	/**
	 * Constructor
	 * 
	 * @param person         the person performing the task.
	 * @param leadResearcher the researcher leading the field work.
	 * @param study          the scientific study the field work is for.
	 * @param rover          the rover
	 */
	public MeteorologyStudyFieldWork(Person person, Person leadResearcher, ScientificStudy study, Rover rover) {

		// Use EVAOperation parent constructor.
		super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D, SkillType.METEOROLOGY);

		// Initialize data members.
		this.leadResearcher = leadResearcher;
		this.study = study;
		this.rover = rover;

		// Determine location for field work.
		Point2D fieldWorkLoc = determineFieldWorkLocation();
		setOutsideSiteLocation(fieldWorkLoc.getX(), fieldWorkLoc.getY());

		// Take specimen containers for rock samples.
		if (!hasSpecimenContainer()) {
			takeSpecimenContainer();
		}
		
		// Add task phases
		addPhase(FIELD_WORK);
	}

	/**
	 * Determine location for field work.
	 * 
	 * @return field work X and Y location outside rover.
	 */
	private Point2D determineFieldWorkLocation() {

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
				goodLocation = LocalAreaUtil.isLocationCollisionFree(newLocation.getX(), newLocation.getY(),
						person.getCoordinates());
			}
		}

		return newLocation;
	}

	/**
	 * Checks if a person can research a site.
	 * 
	 * @param member the member.
	 * @param rover  the rover
	 * @return true if person can research a site.
	 */
	public static boolean canResearchSite(MissionMember member, Rover rover) {

		if (member instanceof Person) {
			Person person = (Person) member;

			// Check if person can exit the rover.
			if (!ExitAirlock.canExitAirlock(person, rover.getAirlock()))
				return false;

			if (isGettingDark(person)) {
				logger.log(person, Level.FINE, 5_000,
						"Ended " + person.getTaskDescription() + " : too dark to continue with the EVA.");
				return false;
			}

			// Check if person's medical condition will not allow task.
            return !(person.getPerformanceRating() < .3D);
		}

		return true;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return FIELD_WORK;
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);
		if (!isDone()) {
			if (getPhase() == null) {
				throw new IllegalArgumentException("Task phase is null");
			}
			else if (FIELD_WORK.equals(getPhase())) {
				time = fieldWorkPhase(time);
			}
		}
		return time;
	}

	/**
	 * Perform the field work phase of the task.
	 * 
	 * @param time the time available (millisols).
	 * @return remaining time after performing phase (millisols).
	 */
	private double fieldWorkPhase(double time) {

		// Check for radiation exposure during the EVA operation.
		if (isDone() || isRadiationDetected(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}
		
		// Check if site duration has ended or there is reason to cut the field
		// work phase short and return to the rover.
		if (shouldEndEVAOperation() || addTimeOnSite(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}

		// Collect rock samples.
		if (totalCollected < AVERAGE_ROCKS_COLLECTED_SITE)
			collectRocks(time);
		else {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return time;
		}
		
		// Add research work to the scientific study for lead researcher.
		addResearchWorkTime(time);

		// Add experience points
		addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);

		// Check if site duration has ended or there is reason to cut the exploring
		// phase short and return to the rover.
		if (addTimeOnSite(time)) {
			if (person.isOutside())
        		setPhase(WALK_BACK_INSIDE);
        	else
        		endTask();
			return 0;
		}
		
		return 0D;
	}

	/**
	 * Collect rocks if chosen.
	 * 
	 * @param time the amount of time available (millisols).
	 * @throws Exception if error collecting rocks.
	 */
	private void collectRocks(double time) {
		if (hasSpecimenContainer()) {
			double probability = chance * time;
			logger.info(person, 10_000, "collectRockSamples::probability: " + probability);
			
			if (RandomUtil.getRandomDouble(1.0D) <= chance * time) {
				
				int randomNum = RandomUtil.getRandomInt((ResourceUtil.rockIDs).length);
				int randomRock = ResourceUtil.rockIDs[randomNum];
				logger.info(person, 10_000, "collectRockSamples::randomRock: " + ResourceUtil.ROCKS[randomNum]);
				
				Inventory pInv = person.getInventory();
		        SpecimenBox box = pInv.findASpecimenBox();
				double mass = RandomUtil.getRandomDouble(AVERAGE_ROCKS_MASS * 2D);
				double cap = box.getAmountResourceRemainingCapacity(randomRock);
				if (mass < cap) {
					double excess = box.storeAmountResource(randomRock, mass);
					totalCollected += mass - excess;
				}
			}
		}
	}
	
	/**
	 * Checks if the person is carrying a specimen container.
	 * 
	 * @return true if carrying container.
	 */
	private boolean hasSpecimenContainer() {
		return person.getInventory().containsEquipment(EquipmentType.SPECIMEN_BOX);
	}
	
	/**
	 * Adds research work time to the scientific study for the lead researcher.
	 * 
	 * @param time the time (millisols) performing field work.
	 */
	private void addResearchWorkTime(double time) {
		// Determine effective field work time.
		double effectiveFieldWorkTime = time;
		int skill = getEffectiveSkillLevel();
		if (skill == 0) {
			effectiveFieldWorkTime /= 2D;
		} else if (skill > 1) {
			effectiveFieldWorkTime += effectiveFieldWorkTime * (.2D * skill);
		}

		// If person isn't lead researcher, divide field work time by two.
		if (!person.equals(leadResearcher)) {
			effectiveFieldWorkTime /= 2D;
		}

		// Add research to study for primary or collaborative researcher.
		if (study.getPrimaryResearcher().equals(leadResearcher)) {
			study.addPrimaryResearchWorkTime(effectiveFieldWorkTime);
		} else {
			study.addCollaborativeResearchWorkTime(leadResearcher, effectiveFieldWorkTime);
		}
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
				double remainingCapacity = container.getAmountResourceRemainingCapacity(ResourceUtil.rockSamplesID);

				if (remainingCapacity > mostCapacity) {
					result = container;
					mostCapacity = remainingCapacity;
				}
			} catch (Exception e) {
	          	logger.log(Level.SEVERE, "Problems calling getAmountResourceRemainingCapacity(): "+ e.getMessage());
			}
		}

		return result;
	}
	
	/**
	 * Transfer the Specimen box to the Vehicle
	 */
	@Override
	protected void clearDown() {
		logger.info(person, 10_000, "clearDown::totalCollected: " + totalCollected);
		// Load specimen container in rover.
		Inventory pInv = person.getInventory();
		if (pInv.containsEquipment(EquipmentType.SPECIMEN_BOX)) {
			SpecimenBox box = pInv.findASpecimenBox();
			box.transfer(pInv, rover);
		}
	}
}
