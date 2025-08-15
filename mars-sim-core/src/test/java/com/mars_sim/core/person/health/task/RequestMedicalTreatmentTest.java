package com.mars_sim.core.person.health.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblemState;

public class RequestMedicalTreatmentTest extends AbstractMarsSimUnitTest {
	
    public void testCreateSettlementTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.APPENDICITIS);
        var recoveryTime = hp.getComplaint().getRecoveryTreatment().getDuration();

        var task = RequestMedicalTreatment.createTask(patient);
        assertFalse("Task created", task.isDone());
     
        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(patient, task, 1000);
        executeTask(patient, task, 30);
        
        assertFalse("Health problem treated at Medical care", sb.getMedical().getProblemsBeingTreated().contains(hp));
        assertTrue("Health problem not waiting at Medical care", sb.getMedical().getProblemsAwaitingTreatment().contains(hp));
        assertEquals("Waiting for treatment", RequestMedicalTreatment.WAITING_FOR_TREATMENT, task.getPhase());
        
        // Simulate someone helping
        sb.getMedical().startTreatment(hp, recoveryTime);
        executeTask(patient, task, 1);
        
        assertTrue("Health problem treated at Medical care", sb.getMedical().getProblemsBeingTreated().contains(hp));
        assertFalse("Health problem not waiting at Medical care", sb.getMedical().getProblemsAwaitingTreatment().contains(hp));
        assertEquals("Started for treatment", RequestMedicalTreatment.TREATMENT, task.getPhase());
        
        // Treatment
        hp.timePassing(recoveryTime*1.1, patient.getPhysicalCondition());
        sb.getMedical().stopTreatment(hp);
        executeTask(patient, task, 10);

        assertTrue("Task completed", task.isDone());
        assertEquals("Complaints remaining", 1, patient.getPhysicalCondition().getProblems().size());

        assertEquals("Complaint in recovery", HealthProblemState.RECOVERING, hp.getState());
        assertFalse("Health problem removed from Medical care", sb.getMedical().getProblemsBeingTreated().contains(hp));
    }

    public void testCreateVehicle() {
        var s = buildSettlement("Hospital");
        SelfTreatHealthProblemTest.buildMediCare(this, s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR);
        var r = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION);
        patient.transfer(r);
        assertTrue("Person starts in Vehicle", patient.isInVehicle());

        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.APPENDICITIS);
        var recoveryTime = hp.getComplaint().getRecoveryTreatment().getDuration();

        var task = RequestMedicalTreatment.createTask(patient);
        assertFalse("Task created", task.isDone());
        var sb = r.getSickBay();

        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(patient, task, 1000);
        executeTask(patient, task, 30);
        
        assertFalse("Health problem treated at Medical care", sb.getProblemsBeingTreated().contains(hp));
        assertTrue("Health problem not waiting at Medical care", sb.getProblemsAwaitingTreatment().contains(hp));
        assertEquals("Waiting for treatment", RequestMedicalTreatment.WAITING_FOR_TREATMENT, task.getPhase());
        
        // Simulate someone helping
        sb.startTreatment(hp, recoveryTime);
        executeTask(patient, task, 1);
        
        assertTrue("Health problem treated at Medical care", sb.getProblemsBeingTreated().contains(hp));
        assertFalse("Health problem not waiting at Medical care", sb.getProblemsAwaitingTreatment().contains(hp));
        assertEquals("Started for treatment", RequestMedicalTreatment.TREATMENT, task.getPhase());
        
        // Treatment
        hp.timePassing(recoveryTime*1.1, patient.getPhysicalCondition());
        sb.stopTreatment(hp);
        executeTask(patient, task, 10);

        assertTrue("Task completed", task.isDone());
        assertEquals("Complaints remaining", 1, patient.getPhysicalCondition().getProblems().size());

        assertEquals("Complaint in recovery", HealthProblemState.RECOVERING, hp.getState());
        assertFalse("Health problem removed from Medical care", sb.getProblemsBeingTreated().contains(hp));
        assertTrue("Person stays in Vehicle", patient.isInVehicle());
    }

    public void testMetaTaskSettlement() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new RequestMedicalTreatmentMeta();

        // Self heal
        SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.LACERATION);
        var tasks = mt.getTaskJobs(patient);
        assertTrue("No doctor health problems", tasks.isEmpty());

        // Not self healing
        SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.APPENDICITIS);
        tasks = mt.getTaskJobs(patient);
        assertFalse("Problems found", tasks.isEmpty());
    }
}
