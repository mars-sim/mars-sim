package com.mars_sim.core.person.health.task;
import static com.mars_sim.core.test.SimulationAssertions.assertLessThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblemState;

public class RestingMedicalRecoveryTest extends MarsSimUnitTest {
  @Test
  public void testCreateSettlementTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var p = buildPerson("Ill", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(this, p, ComplaintType.APPENDICITIS);
        hp.startRecovery();

        assertEquals(HealthProblemState.RECOVERING, hp.getState(), "Complaint in recovering");
        assertTrue(hp.requiresBedRest(), "Health problem needs bed rest");

        var task = RestingMedicalRecovery.createTask(p);
        assertFalse(task.isDone(), "Task created");
        assertFalse(sb.getMedical().getRestingRecoveryPeople().contains(p), "No bed rest at start of task");

        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(p, task, 1000);
        executeTask(p, task, 1);
        assertFalse(task.isDone(), "Task still active after walk");
        assertTrue(sb.getMedical().getRestingRecoveryPeople().contains(p), "Bed rest started");

        // Do recovery
        double origRecovery = hp.getRemainingRecovery();
        executeTaskForDuration(p, task, hp.getRemainingRecovery() * 1.1);
        double newRecovery = hp.getRemainingRecovery();
        assertTrue(task.isDone(), "Task completed");
        assertLessThan("Recovery time reduced", origRecovery, newRecovery);
    }

    @Test
    public void testMetaTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new RestingMedicalRecoveryMeta();

        // Self heal
        var tasks = mt.getTaskJobs(patient);
        assertTrue(tasks.isEmpty(), "No resting needed");

        // Make person need bed rest
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.APPENDICITIS);
        hp.startRecovery();

        tasks = mt.getTaskJobs(patient);
        assertFalse(tasks.isEmpty(), "Resting needed");
    }
}
