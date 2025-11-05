package com.mars_sim.core.person.health.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblemState;

public class TreatMedicalPatientTest extends MarsSimUnitTest {

    @Test
    public void testCreateSettlementTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(getContext(), s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(getContext(), patient, ComplaintType.APPENDICITIS);
        sb.getMedical().requestTreatment(hp);
        var recoveryTime = hp.getComplaint().getRecoveryTreatment().getDuration();

        var doctor = buildPerson("Docter", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        doctor.getSkillManager().addNewSkill(SkillType.MEDICINE, 20);

        var task = TreatMedicalPatient.createTask(doctor);
        assertFalse(task.isDone(), "Task created");
     
        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(doctor, task, 1000);
        executeTask(doctor, task, 100);
        
        assertTrue(sb.getMedical().getProblemsBeingTreated().contains(hp), "Health problem treated at Medical care");
        assertFalse(sb.getMedical().getProblemsAwaitingTreatment().contains(hp), "Health problem not waiting at Medical care");

        // Complete treatment
        executeTaskForDuration(doctor, task, recoveryTime * 1.5);

        assertTrue(task.isDone(), "Task completed");
        assertEquals(1, patient.getPhysicalCondition().getProblems().size(), "Complaints remaining");

        assertEquals(HealthProblemState.RECOVERING, hp.getState(), "Complaint in recovery");
        assertFalse(sb.getMedical().getProblemsBeingTreated().contains(hp), "Health problem removed from Medical care");

    }

    @Test
    public void testCreateVehicleTask() {
        var s = buildSettlement("Hospital");
        SelfTreatHealthProblemTest.buildMediCare(getContext(), s);
        var r = buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var sb = r.getSickBay();

        var patient = buildPerson("Patient", s, JobType.DOCTOR);
        patient.transfer(r);

        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(getContext(), patient, ComplaintType.APPENDICITIS);
        sb.requestTreatment(hp);
        var recoveryTime = hp.getComplaint().getRecoveryTreatment().getDuration();

        var doctor = buildPerson("Docter", s, JobType.DOCTOR);
        doctor.transfer(r);
        doctor.getSkillManager().addNewSkill(SkillType.MEDICINE, 20);

        var task = TreatMedicalPatient.createTask(doctor);
        assertFalse(task.isDone(), "Task created");
     
        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(doctor, task, 1000);
        executeTask(doctor, task, 1);
        assertTrue(sb.getProblemsBeingTreated().contains(hp), "Health problem treated at Medical care");
        assertFalse(sb.getProblemsAwaitingTreatment().contains(hp), "Health problem not waiting at Medical care");

        // Complete treatment
        executeTaskForDuration(doctor, task, recoveryTime * 1.5);

        assertTrue(task.isDone(), "Task completed");
        assertEquals(1, patient.getPhysicalCondition().getProblems().size(), "Complaints remaining");

        assertEquals(HealthProblemState.RECOVERING, hp.getState(), "Complaint in recovery");
        assertFalse(sb.getProblemsBeingTreated().contains(hp), "Health problem removed from Medical care");
        assertTrue(doctor.isInVehicle(), "Doctor starts in Vehicle");
        assertTrue(patient.isInVehicle(), "Patient starts in Vehicle");

    }

    @Test
    public void testMetaTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(getContext(), s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new TreatMedicalPatientMeta();

        // Self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(getContext(), patient, ComplaintType.LACERATION);
        sb.getMedical().requestTreatment(hp);

        var doctor = buildPerson("Docter", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        doctor.getSkillManager().addNewSkill(SkillType.MEDICINE, 20);
        var tasks = mt.getTaskJobs(doctor);
        assertTrue(tasks.isEmpty(), "No dotor health problems");

        // Not self healing
        hp = SelfTreatHealthProblemTest.addComplaint(getContext(), patient, ComplaintType.APPENDICITIS);
        sb.getMedical().requestTreatment(hp);
        tasks = mt.getTaskJobs(doctor);
        assertFalse(tasks.isEmpty(), "Problems found");
    }
}
