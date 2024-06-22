/*
 * Mars Simulation Project
 * ScientificStudyFieldWork.java
 * @date 2023-09-17
 * @author Barry Evans
 */
package com.mars_sim.core.science.task;

import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.ExitAirlock;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * A task for the EVA operation of performing field work at a research
 * site for a scientific study.
 */
public  class ScientificStudyFieldWork extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ScientificStudyFieldWork.class.getName());

    private static final String BIOLOGY_NAME = Msg.getString("Task.description.biologyFieldWork"); //$NON-NLS-1$
    private static final TaskPhase BIOLOGY_WORK = new TaskPhase(Msg.getString("Task.phase.fieldWork.biology"),
							createPhaseImpact(SkillType.BIOLOGY)); //$NON-NLS-1$
	private static final String AREOLOGY_NAME = Msg.getString("Task.description.areologyFieldWork"); //$NON-NLS-1$
	private static final TaskPhase AREOLOGY_WORK = new TaskPhase(Msg.getString("Task.phase.fieldWork.areology"), //$NON-NLS-1$
							createPhaseImpact(SkillType.AREOLOGY));
	public static final LightLevel LIGHT_LEVEL = LightLevel.LOW;

	// Data members
	private Person leadResearcher;
	private ScientificStudy study;
	private Rover rover;
	private TaskPhase fieldWork;

	/**
	 * Constructor.
	 * 
	 * @param person         the person performing the task.
	 * @param leadResearcher the researcher leading the field work.
	 * @param study          the scientific study the field work is for.
	 * @param rover          the rover
	 */
	protected ScientificStudyFieldWork(String name, TaskPhase fieldwork, Person person, Person leadResearcher,
									   ScientificStudy study, Rover rover) {

		// Use EVAOperation parent constructor.
		super(name, person, RandomUtil.getRandomDouble(50D) + 10D, fieldwork);
		
		setMinimumSunlight(LIGHT_LEVEL);

		// Initialize data members.
		this.leadResearcher = leadResearcher;
		this.study = study;
		this.rover = rover;

		// Determine location for field work.
		setRandomOutsideLocation(rover);

		// Add task phases
		this.fieldWork = fieldwork;
	}

	public static ScientificStudyFieldWork createFieldStudy(ScienceType science, Person person, Person leadResearcher,
									ScientificStudy study, Rover rover) {
		String name;
		TaskPhase fieldwork;

		switch(science) {
			case BIOLOGY:
				name = BIOLOGY_NAME;
				fieldwork = BIOLOGY_WORK;
				break;
			case AREOLOGY:
				name = AREOLOGY_NAME;
				fieldwork = AREOLOGY_WORK;
				break;
			
			default:
				throw new IllegalArgumentException("Science type can not have field study " + science.getName());
		}
		return new ScientificStudyFieldWork(name, fieldwork, person, leadResearcher, study, rover);
	}
	
	/**
	 * Checks if a person can research a site.
	 * 
	 * @param member the member.
	 * @param rover  the rover
	 * @return true if person can research a site.
	 */
	public static boolean canResearchSite(Worker member, Rover rover) {

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
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);
		if (!isDone()) {
			if (getPhase() == null) {
				throw new IllegalArgumentException("Task phase is null");
			} else if (fieldWork.equals(getPhase())) {
				time = fieldWorkPhase(time);
			} 
		}
		return time;
	}

	/**
	 * Performs the field work phase of the task.
	 * 
	 * @param time the time available (millisols).
	 * @return remaining time after performing phase (millisols).
	 */
	private double fieldWorkPhase(double time) {
		double remainingTime = 0;
		
		if (checkReadiness(time) > 0) {
			return time;
		}	
		
		// Check if the study is completed
		if (performStudy(time)) {
			checkLocation("Study completed.");
			return time;
		}

		// Add research work to the scientific study for lead researcher.
		addResearchWorkTime(time);

		// Add experience points
		addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);

		return remainingTime;
	}

	/**
	 * Performs any specific study activities.
	 * 
	 * @param time Time to do the study; maybe used by overriding classes
	 * @return Is the field work completed
	 */
	protected boolean performStudy(double time) {
		return false;
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
	
	protected Rover getRover() {
		return rover;
	}
	
	/**
	 * Transfers the Specimen box to the Vehicle.
	 */
	@Override
	protected void clearDown() {
		super.clearDown();
		
		if (rover != null) {
			// Task may end early before a Rover is selected
			returnEquipmentToVehicle(rover);
		}
	}
}
