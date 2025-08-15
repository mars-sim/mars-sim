/*
 * Mars Simulation Project
 * RestingMedicalRecoveryMeta.java
 * @date 2021-12-22
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
 * Meta task for the RestingMedicalRecoveryMeta task.
 */
public class RestingMedicalRecoveryMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.restingMedicalRecovery"); //$NON-NLS-1$

    public RestingMedicalRecoveryMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		
		addAllCrewRoles();
	}

    @Override
    public Task constructInstance(Person person) {
        return RestingMedicalRecovery.createTask(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        if (person.isOutside()) {
        	return EMPTY_TASKLIST;
        }

        // Check if person has a health problem that requires bed rest for recovery.
        Set<HealthProblem> resting = RestingMedicalRecovery.getRestingProblems(person);
        if (resting.isEmpty()) {
            return EMPTY_TASKLIST;
        }

        double result = 500D;

        int hunger = (int) person.getPhysicalCondition().getHunger();
        result = result - (hunger - 333) / 3.0;

        // Determine if any available medical aids can be used for bed rest.
        if (MedicalHelper.determineMedicalAid(person, resting) != null) {
            result+= 100D;
        }

        return createTaskJobs(new RatingScore(result));
    }
}
