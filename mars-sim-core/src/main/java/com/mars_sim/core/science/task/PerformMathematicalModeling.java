/*
 * Mars Simulation Project
 * PerformMathematicalModeling.java
 * @date 2023-08-11
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * A task for performing mathematical modeling in a laboratory for a scientific study.
 */
public class PerformMathematicalModeling extends LabTask {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
  
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.performMathematicalModeling"); //$NON-NLS-1$
    
    private static final ExperienceImpact IMPACT = new ExperienceImpact(20D,
                        NaturalAttributeType.ACADEMIC_APTITUDE, false, 0.2D,
                        Set.of(SkillType.MATHEMATICS));
    
    /** Task phases. */
    private static final TaskPhase MODELING = new TaskPhase(Msg.getString(
            "Task.phase.modeling")); //$NON-NLS-1$

	/**
	 * Create a Task to perform lab research. This will select the most appropirate Scientific Study for the Person
	 * and create an appropriate Task.
	 * @param person
	 * @return
	 */
	public static PerformMathematicalModeling createTask(Person person) {
		var study = determineStudy(person);
		if ((study != null) && (study.getContribution(person) != null)) {
			return new PerformMathematicalModeling(person, study, IMPACT);
		}

		return null;
	}

	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	private PerformMathematicalModeling(Person person, ScientificStudy study, ExperienceImpact impact) {
		// Use task constructor.
		super(NAME, person, study, impact, 10D + RandomUtil.getRandomDouble(50D),
		MODELING, "performMathematicalModeling");

	}

	/**
	 * Determines the scientific study that will be researched.
	 * 
	 * @return study or null if none available.
	 */
	private static ScientificStudy determineStudy(Person person) {
        List<ScientificStudy> possibleStudies = new ArrayList<>();

        // Add primary study if mathematics and in research phase.
         ScientificStudy primaryStudy = person.getStudy();
        if (primaryStudy != null
            && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase()) 
            && !primaryStudy.isPrimaryResearchCompleted() 
            && ScienceType.MATHEMATICS == primaryStudy.getScience()) {
        	// Primary study added twice to double chance of random selection.
        	possibleStudies.add(primaryStudy);
        	possibleStudies.add(primaryStudy);
        }

        // Add all collaborative studies with mathematics and in research phase.
        for (ScientificStudy collabStudy : person.getCollabStudies()) {
            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase()) &&
                    !collabStudy.isCollaborativeResearchCompleted(person)) {
                ScienceType collabScience = collabStudy.getContribution(person);
                if (ScienceType.MATHEMATICS == collabScience) {
                    possibleStudies.add(collabStudy);
                }
            }
        }

        // Randomly select study.
        return RandomUtil.getRandomElement(possibleStudies);
    }
}
