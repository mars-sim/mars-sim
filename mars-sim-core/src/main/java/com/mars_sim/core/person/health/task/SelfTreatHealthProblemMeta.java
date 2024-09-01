/*
 * Mars Simulation Project
 * SelfTreatMedicalProblemMeta.java
 * @date 2021-12-05
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.List;
import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the SelfTreatHealthProblem task.
 */
public class SelfTreatHealthProblemMeta extends FactoryMetaTask {

	private static final double VALUE = 1000.0;

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.selfTreatHealthProblem"); //$NON-NLS-1$

    public SelfTreatHealthProblemMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
	}


    @Override
    public Task constructInstance(Person person) {
        return SelfTreatHealthProblem.createTask(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        double result = 0D;

        
        if (person.isInside()) {
	        // Check if person has health problems that can be self-treated.
        	var problems = MedicalHelper.getTreatableHealthProblems(person, person.getPhysicalCondition().getProblems(),
                                                        true);

	        boolean hasSelfTreatableProblems = !problems.isEmpty();

	        // Check if person has available medical aids.
	        boolean hasAvailableMedicalAids = hasAvailableMedicalAids(person, problems);

	        if (hasSelfTreatableProblems && hasAvailableMedicalAids) {
	            result = VALUE * problems.size();
	        }

	        double pref = person.getPreference().getPreferenceScore(this);

	        if (pref > 0)
	        	result = result * 3D;

	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();

	        if (result < 0) result = 0;

        }

        return createTaskJobs(new RatingScore(result));
    }

   
    /**
     * Checks if a person has any available local medical aids for self treating health problems.
     * @param person the person.
     * @param problems 
     * @return true if available medical aids.
     */
    private boolean hasAvailableMedicalAids(Person person, Set<HealthProblem> problems) {
        return MedicalHelper.determineMedicalAid(person, problems) != null;
    }
}
