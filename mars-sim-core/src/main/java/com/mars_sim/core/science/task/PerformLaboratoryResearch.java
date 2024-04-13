/*
 * Mars Simulation Project
 * PerformLaboratoryResearch.java
 * @date 2023-08-11
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Lab;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * A task for performing research for a scientific study in a laboratory.
 */
public class PerformLaboratoryResearch extends LabTask {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.performLaboratoryResearch"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase RESEARCHING = new TaskPhase(Msg.getString("Task.phase.researching")); //$NON-NLS-1$


	/**
	 * Create a Task to perform lab research. This will select the most appropirate Scientific Study for the Person
	 * and create an appropriate Task.
	 * @param person
	 * @return
	 */
	public static PerformLaboratoryResearch createTask(Person person) {
		var study = determineStudy(person);
		if ((study != null) && (study.getContribution(person) != null)) {
			// Found a suitable study
			var impact = new ExperienceImpact(25D, NaturalAttributeType.ACADEMIC_APTITUDE,
										false, 0.2D,
							Set.of(study.getContribution(person).getSkill()));
			return new PerformLaboratoryResearch(person, study, impact);
		}

		return null;
	}

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	private PerformLaboratoryResearch(Person person, ScientificStudy study, ExperienceImpact impact) {
		// Use task constructor.
		super(NAME, person, study, impact, 10D + RandomUtil.getRandomDouble(50D),
				RESEARCHING, "Task.description.performLaboratoryResearch.detail");

	}

	/**
	 * Determines the scientific study that will be researched.
	 * 
	 * @return study or null if none available.
	 */
	private static ScientificStudy determineStudy(Person person) {
		List<ScientificStudy> possibleStudies = new ArrayList<>();

		// Add primary study if in research phase.
		ScientificStudy primaryStudy = person.getStudy();
		if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())
					&& !primaryStudy.isPrimaryResearchCompleted()) {

			// Check that a lab is available for primary study science.
			Lab lab = getLocalLab(person, primaryStudy.getScience());
			if (lab != null) {

				// Primary study added twice to double chance of random selection.
				possibleStudies.add(primaryStudy);
				possibleStudies.add(primaryStudy);
			}
		}

		// Add all collaborative studies in research phase.
		for(ScientificStudy collabStudy : person.getCollabStudies()) {
			if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())
					&& !collabStudy.isCollaborativeResearchCompleted(person)) {

				// Check that a lab is available for collaborative study science.
				ScienceType collabScience = collabStudy.getContribution(person);

				Lab lab = getLocalLab(person, collabScience);
				if (lab != null) {

					possibleStudies.add(collabStudy);
				}
			}
		}

		// Randomly select study.
		return RandomUtil.getRandomElement(possibleStudies);
	}
}
