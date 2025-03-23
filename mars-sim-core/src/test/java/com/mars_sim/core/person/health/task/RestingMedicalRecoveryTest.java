package com.mars_sim.core.person.health.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblemState;

public class RestingMedicalRecoveryTest extends AbstractMarsSimUnitTest {
  public void testCreateSettlementTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var p = buildPerson("Ill", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(this, p, ComplaintType.APPENDICITIS);
        hp.startRecovery();

        assertEquals("Complaint in recovering", HealthProblemState.RECOVERING, hp.getState());
        assertTrue("Health problem needs bed rest", hp.requiresBedRest());

        var task = RestingMedicalRecovery.createTask(p);
        assertFalse("Task created", task.isDone());
        assertFalse("No bed rest at start of task", sb.getMedical().getRestingRecoveryPeople().contains(p));

        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(p, task, 1000);
        executeTask(p, task, 1);
        assertFalse("Task still active after walk", task.isDone());
        assertTrue("Bed rest started", sb.getMedical().getRestingRecoveryPeople().contains(p));

        // Do recovery
        double origRecovery = hp.getRemainingRecovery();
        executeTaskForDuration(p, task, hp.getRemainingRecovery() * 1.1);
        double newRecovery = hp.getRemainingRecovery();
        assertTrue("Task completed", task.isDone());
        assertLessThan("Recovery time reduced", origRecovery, newRecovery);
    }

    public void testMetaTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new RestingMedicalRecoveryMeta();

        // Self heal
        var tasks = mt.getTaskJobs(patient);
        assertTrue("No resting needed", tasks.isEmpty());

        // Make person need bed rest
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.APPENDICITIS);
        hp.startRecovery();

        tasks = mt.getTaskJobs(patient);
        assertFalse("Resting needed", tasks.isEmpty());
    }
}
