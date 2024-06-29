/**
 * Mars Simulation Project
 * PrescribeMedicationMeta.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package com.mars_sim.core.person.health.task;

import java.util.Collection;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the PrescribeMedication task.
 */
public class PrescribeMedicationMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.prescribeMedication"); //$NON-NLS-1$
    private static final double PATIENT_SCORE = 100D;
    
    public PrescribeMedicationMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		
		setPreferredJob(JobType.MEDICS);

        addPreferredRobot(RobotType.MEDICBOT);
	}
    
    @Override
    public Task constructInstance(Person person) {
        return new PrescribeMedication(person);
    }

    @Override
    public Task constructInstance(Robot robot) {
        return new PrescribeMedication(robot);
    }

    /**
	 * Gets the list of Medication tasks that are needed that this Person can perform
     * all individually scored.
	 * 
	 * @param person the Person to perform the task.
	 * @return List of TasksJob specifications.
	 */
    @Override
	public List<TaskJob> getTaskJobs(Person person) {
		return createMedicationJobs(person);
	}

    /**
	 * Gets the list of Medication tasks that are needed that this Robot can perform
	 * 
	 * @param robot the robot to perform the task.
	 * @return List of TasksJob specifications.
	 */
    @Override
	public List<TaskJob> getTaskJobs(Robot robot) {
        return createMedicationJobs(robot);
    }

	private List<TaskJob> createMedicationJobs(Worker pharmacist) {
        // Get possible patient list.
        // Note: Doctor can also prescribe medication for himself.
        Collection<Person> patientList = PrescribeMedication.determinePatients(pharmacist);

        // Determine patient.
        long patients = patientList.stream()
            .filter(PrescribeMedication::needsMedication)
            .count();

        return createTaskJobs(new RatingScore(patients * PATIENT_SCORE));
    }
}
