/**
 * Mars Simulation Project
 * RequestMedicalTreatmentMeta.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the RequestMedicalTreatment task.
 */
public class RequestMedicalTreatmentMeta extends FactoryMetaTask {

	private static final int VALUE = 500;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.requestMedicalTreatment"); //$NON-NLS-1$

    public RequestMedicalTreatmentMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		
		setTrait(TaskTrait.TREATMENT);
	}
    

    @Override
    public Task constructInstance(Person person) {
        return RequestMedicalTreatment.createTask(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        if (person.isOutside()
            || person.getPhysicalCondition().getProblems().isEmpty()) {
        	return EMPTY_TASKLIST;
        }

        // Get problems that need help
        var curable = RequestMedicalTreatment.getRequestableTreatment(person);
        if (curable.isEmpty()) {
            return EMPTY_TASKLIST;
        }
      
        MedicalAid medicalAid = MedicalHelper.determineMedicalAid(person, curable);
        if (medicalAid == null) {
            return EMPTY_TASKLIST;
        }

        return createTaskJobs(new RatingScore(VALUE));
    }
}
