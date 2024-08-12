/*
 * Mars Simulation Project
 * ProposeScientificStudy.java
 * @date 2022-07-18
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * A task for proposing a new scientific study.
 */
public class ProposeScientificStudy extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ProposeScientificStudy.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.proposeScientificStudy"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase PROPOSAL_PHASE = new TaskPhase(Msg.getString("Task.phase.proposalPhase")); //$NON-NLS-1$

	/** The scientific study to propose. */
	private ScientificStudy study;

	/**
	 * Create a new Study anda Task to build the proposal for a Person
	 * @param p
	 * @return
	 */
	static Task createTask(Person p) {
		var study = p.getResearchStudy().getStudy();
		if (study == null) {		
			// Create new scientific study.
			study = createStudy(p);
		}

		if (study == null) {
			logger.severe(p, "Not a scientist.");
			return null;
		}

		// Found a suitable study
		var impact = new ExperienceImpact(25D, NaturalAttributeType.ACADEMIC_APTITUDE,
		false, 0.2D, study.getScience().getSkill());

		return new ProposeScientificStudy(p, study, impact);
	}

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	private ProposeScientificStudy(Person person, ScientificStudy study, ExperienceImpact impact) {
		// Skill set set later on based on Study
		super(NAME, person, true, impact, 10D + RandomUtil.getRandomDouble(50D));
		
		this.study = study;
			
		setDescription(
				Msg.getString("Task.description.proposeScientificStudy.detail", study.getScience().getName())); // $NON-NLS-1$

		// If person is in a settlement, try to find a building.
		boolean walk = false;
		if (person.isInSettlement()) {
			Building b = BuildingManager.getAvailableBuilding(study.getScience(), person);
			if (b != null) {
				// Walk to this specific building.
				walkToResearchSpotInBuilding(b, false);
				walk = true;
			}
		}

		if (!walk) {

			if (person.isInVehicle()) {
				// If person is in rover, walk to passenger activity spot.
				if (person.getVehicle() instanceof Rover rover) {
					walkToPassengerActivitySpotInRover(rover, false);
				}
			} else {
				// Walk to random location.
				walkToRandomLocation(true);
			}
		}

		// Initialize phase
		setPhase(PROPOSAL_PHASE);
	}

	/**
	 * Creates a study for a Person using their Job to select the most appropriate 
	 * science.
	 * 
	 * @param p
	 * @return
	 */
	public static ScientificStudy createStudy(Person p) {
		// Create new scientific study.
		ScientificStudy study = null;
		JobType job = p.getMind().getJob();
		ScienceType science = ScienceType.getJobScience(job);
		if (science != null) {
			SkillType skill = science.getSkill();
			int level = p.getSkillManager().getSkillLevel(skill);
			study = scientificStudyManager.createScientificStudy(p, science, level);
		}
		return study;	
	}


	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (PROPOSAL_PHASE.equals(getPhase())) {
			return proposingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the writing study proposal phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double proposingPhase(double time) {
		double remainingTime = 0;
		
		if (study.getPhase() != StudyStatus.PROPOSAL_PHASE) {
			endTask();
			return time;
		}

		if (isDone()) {
			logger.log(person, Level.INFO, 10_000, "Proposed " + study + ".");
			endTask();
			return time;
		}

		if (person.getPhysicalCondition().computeFitnessLevel() < 2) {
			logger.log(person, Level.FINE, 10_000, "Ended proposing scientific study. Not feeling well.");
			endTask();
			return time;
		}
		
		// Determine amount of effective work time based on science skill.
		double workTime = time;
		int scienceSkill = getEffectiveSkillLevel();
		if (scienceSkill == 0) {
			workTime /= 2;
		} else {
			workTime += workTime * (.2D * scienceSkill);
		}

		study.addProposalWorkTime(workTime);

		// Add experience
		addExperience(time);
		
		if (study.isProposalCompleted()) {
			logger.log(worker, Level.INFO, 0, "Finished writing a study proposal for " 
					+ study.getName() + "."); 

			endTask();
			return remainingTime;
		}

		return remainingTime;
	}
}
