/*
 * Mars Simulation Project
 * PerformMathematicalModeling.java
 * @date 2023-08-11
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.Set;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

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
                        SkillType.MATHEMATICS);
    
    /** Task phases. */
    private static final TaskPhase MODELING = new TaskPhase(Msg.getString(
            "Task.phase.modeling")); //$NON-NLS-1$

    static final Set<ScienceType> MODELLING_SCIENCE = Set.of(ScienceType.MATHEMATICS);

	/**
	 * Create a Task to perform lab research. This will select the most appropirate Scientific Study for the Person
	 * and create an appropriate Task.
	 * @param person
	 * @return
	 */
	public static PerformMathematicalModeling createTask(Person person) {
		var study = determineStudy(person, MODELLING_SCIENCE);
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
}
