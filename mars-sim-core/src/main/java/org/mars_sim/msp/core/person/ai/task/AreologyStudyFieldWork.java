/*
 * Mars Simulation Project
 * AreologyStudyFieldWork.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.logging.Level;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * A task for the EVA operation of performing areology field work at a research
 * site for a scientific study.
 */
public class AreologyStudyFieldWork extends EVAOperation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(AreologyStudyFieldWork.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.areologyFieldWork"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase FIELD_WORK = new TaskPhase(Msg.getString("Task.phase.fieldWork.areology")); //$NON-NLS-1$

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
	public AreologyStudyFieldWork(Person person, Person leadResearcher, ScientificStudy study, Rover rover) {

		// Use EVAOperation parent constructor.
		super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D, SkillType.AREOLOGY);

		// Initialize data members.
		this.leadResearcher = leadResearcher;
		this.study = study;
		this.rover = rover;

		// Determine location for field work.
		Point2D fieldWorkLoc = determineFieldWorkLocation();
		if (fieldWorkLoc != null)
			setOutsideSiteLocation(fieldWorkLoc.getX(), fieldWorkLoc.getY());

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
						" ended " + person.getTaskDescription() + " due to getting too dark "
						+ " at " + person.getCoordinates().getFormattedString());
				return false;
			}

			// Check if person's medical condition will not allow task.
			return (person.getPerformanceRating() >= .3D);
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
			} else if (FIELD_WORK.equals(getPhase())) {
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

		// Check if site duration has ended or there is reason to cut the field work
		// phase short and return to the rover.
		if (shouldEndEVAOperation() || addTimeOnSite(time)) {
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

		return 0D;
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
}
