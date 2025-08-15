/*
 * Mars Simulation Project
 * TreatMedicalPatientMeta.java
 * @date 2025-08-14
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.List;
import java.util.Map;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.MedicalAid;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the TreatMedicalPatient task.
 */
public class TreatMedicalPatientMeta extends FactoryMetaTask {
    
	private static final int VALUE = 500;
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.treatMedicalPatient"); //$NON-NLS-1$

    public TreatMedicalPatientMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		
		setTrait(TaskTrait.MEDICAL);
		setPreferredJob(JobType.MEDICS);
	}
   

    @Override
    public Task constructInstance(Person person) {
        return TreatMedicalPatient.createTask(person);
    }

    /**
     * Assesses this person helping someone with treatment.
     * 
     * @param person Being assessed
     * @return Potential suitable tasks
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        Map<HealthProblem,MedicalAid> problems;
        if (person.isInSettlement()) {
            problems = TreatMedicalPatient.findSettlementHealthProblems(person.getSettlement());
        }
        else if (person.isInVehicle()) {
            problems = TreatMedicalPatient.findVehicleHealthProblems(person.getVehicle());
        }
        else {
            return EMPTY_TASKLIST;
        }

        // Filter problem to this that this doctor can handle
        var treatable = MedicalHelper.getTreatableHealthProblems(person, problems.keySet(), false);
        if (treatable.isEmpty()) {
            return EMPTY_TASKLIST;
        }

        // Get the local medical aids to use.
        var result = new RatingScore(VALUE);
        result.addModifier("patients", Math.max(1D, (treatable.size()/0.33)));
        result = assessPersonSuitability(result, person);
        
        return createTaskJobs(result);
    }
}
