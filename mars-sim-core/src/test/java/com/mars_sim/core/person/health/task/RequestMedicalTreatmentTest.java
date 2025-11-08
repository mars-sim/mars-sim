package com.mars_sim.core.person.health.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblemState;

public class RequestMedicalTreatmentTest extends MarsSimUnitTest {
	
    @Test
    public void testCreateSettlementTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(getContext(), s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(getContext(), patient, ComplaintType.APPENDICITIS);
        var recoveryTime = hp.getComplaint().getRecoveryTreatment().getDuration();

        var task = RequestMedicalTreatment.createTask(patient);
        assertFalse(task.isDone(), "Task created");
     
        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(patient, task, 1000);
        executeTask(patient, task, 30);
        
        assertFalse(sb.getMedical().getProblemsBeingTreated().contains(hp), "Health problem treated at Medical care");
        assertTrue(sb.getMedical().getProblemsAwaitingTreatment().contains(hp), "Health problem not waiting at Medical care");
        assertEquals(RequestMedicalTreatment.WAITING_FOR_TREATMENT, task.getPhase(), "Waiting for treatment");
        
        // Simulate someone helping
        sb.getMedical().startTreatment(hp, recoveryTime);
        executeTask(patient, task, 1);
        
        assertTrue(sb.getMedical().getProblemsBeingTreated().contains(hp), "Health problem treated at Medical care");
        assertFalse(sb.getMedical().getProblemsAwaitingTreatment().contains(hp), "Health problem not waiting at Medical care");
        assertEquals(RequestMedicalTreatment.TREATMENT, task.getPhase(), "Started for treatment");
        
        // Treatment
        hp.timePassing(recoveryTime*1.1, patient.getPhysicalCondition());
        sb.getMedical().stopTreatment(hp);
        executeTask(patient, task, 10);

        assertTrue(task.isDone(), "Task completed");
        assertEquals(1, patient.getPhysicalCondition().getProblems().size(), "Complaints remaining");

        assertEquals(HealthProblemState.RECOVERING, hp.getState(), "Complaint in recovery");
        assertFalse(sb.getMedical().getProblemsBeingTreated().contains(hp), "Health problem removed from Medical care");
    }

    @Test
    public void testCreateVehicle() {
        var s = buildSettlement("Hospital");
        SelfTreatHealthProblemTest.buildMediCare(getContext(), s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR);
        var r = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        patient.transfer(r);
        assertTrue(patient.isInVehicle(), "Person starts in Vehicle");

        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(getContext(), patient, ComplaintType.APPENDICITIS);
        var recoveryTime = hp.getComplaint().getRecoveryTreatment().getDuration();

        var task = RequestMedicalTreatment.createTask(patient);
        assertFalse(task.isDone(), "Task created");
        var sb = r.getSickBay();

        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(patient, task, 1000);
        executeTask(patient, task, 30);
        
        assertFalse(sb.getProblemsBeingTreated().contains(hp), "Health problem treated at Medical care");
        assertTrue(sb.getProblemsAwaitingTreatment().contains(hp), "Health problem not waiting at Medical care");
        assertEquals(RequestMedicalTreatment.WAITING_FOR_TREATMENT, task.getPhase(), "Waiting for treatment");
        
        // Simulate someone helping
        sb.startTreatment(hp, recoveryTime);
        executeTask(patient, task, 1);
        
        assertTrue(sb.getProblemsBeingTreated().contains(hp), "Health problem treated at Medical care");
        assertFalse(sb.getProblemsAwaitingTreatment().contains(hp), "Health problem not waiting at Medical care");
        assertEquals(RequestMedicalTreatment.TREATMENT, task.getPhase(), "Started for treatment");
        
        // Treatment
        hp.timePassing(recoveryTime*1.1, patient.getPhysicalCondition());
        sb.stopTreatment(hp);
        executeTask(patient, task, 10);

        assertTrue(task.isDone(), "Task completed");
        assertEquals(1, patient.getPhysicalCondition().getProblems().size(), "Complaints remaining");

        assertEquals(HealthProblemState.RECOVERING, hp.getState(), "Complaint in recovery");
        assertFalse(sb.getProblemsBeingTreated().contains(hp), "Health problem removed from Medical care");
        assertTrue(patient.isInVehicle(), "Person stays in Vehicle");
    }

    @Test
    public void testMetaTaskSettlement() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(getContext(), s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new RequestMedicalTreatmentMeta();

        // Self heal
        SelfTreatHealthProblemTest.addComplaint(getContext(), patient, ComplaintType.LACERATION);
        var tasks = mt.getTaskJobs(patient);
        assertTrue(tasks.isEmpty(), "No doctor health problems");

        // Not self healing
        SelfTreatHealthProblemTest.addComplaint(getContext(), patient, ComplaintType.APPENDICITIS);
        tasks = mt.getTaskJobs(patient);
        assertFalse(tasks.isEmpty(), "Problems found");
    }
}
