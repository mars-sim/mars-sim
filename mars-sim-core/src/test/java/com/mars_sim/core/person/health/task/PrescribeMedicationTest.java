package com.mars_sim.core.person.health.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.BodyRegionType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.RadiationType;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;

public class PrescribeMedicationTest extends MarsSimUnitTest {
	
    private Person createRadiationPatient(Settlement s, Building sb) {
        var p = buildPatient("Patient", s, JobType.ENGINEER, sb, FunctionType.MEDICAL_CARE);
        var pc = p.getPhysicalCondition();
        var e = p.getPhysicalCondition().getRadiationExposure();

        // Simulate a mass radiation dose
        e.addDose(RadiationType.SEP, BodyRegionType.SKIN, 2000D);
        var pulse = createPulse(1, 0, true, true);
        pc.timePassing(pulse, s);
        assertTrue(e.isSick(), "Patient is radiation sick");
        assertTrue(pc.isRadiationPoisoned(), "Patient is radiation poisoned");

        return p;
    }

    @Test
    public void testDetermineRadiation() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        buildPerson("Health1", s, JobType.ENGINEER, sb, FunctionType.MEDICAL_CARE);

        var p = createRadiationPatient(s, sb);

        var doctor = buildPerson("Doctor", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var found = PrescribeMedication.determinePatient(doctor);
        assertEquals(p, found, "Found stressed out patient");
    }

    @Test
    public void testCreationRadiation() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var p = createRadiationPatient(s, sb);
        var doctor = buildPerson("Doctor", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var task = new PrescribeMedication(doctor);
        assertEquals(p, task.getPatient(), "Found patient");
        assertFalse(task.isDone(), "Task is active");


        // Complete to the end
        executeTask(doctor, task, 2000);
        assertTrue(task.isDone(), "Task completed");
        var meds = p.getPhysicalCondition().getMedicationList();
        assertEquals(1, meds.size(), "Patient has medication");
        
        // Check has radiation medication
        var radMeds = meds.get(0);
        assertEquals(ComplaintType.RADIATION_SICKNESS, radMeds.getComplaintType(), "Radiation meds");
    }

    @Test
    public void testPersonMeta() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var doctor = buildPerson("Docter", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        buildPerson("Health1", s, JobType.ENGINEER, sb, FunctionType.MEDICAL_CARE);

        var mt = new PrescribeMedicationMeta();

        var tasks = mt.getTaskJobs(doctor);
        assertTrue(tasks.isEmpty(), "No medication tasks found");

        // Add a radiation person
        createRadiationPatient(s, sb);
        tasks = mt.getTaskJobs(doctor);
        assertEquals(1, tasks.size(), "One medication task found");
    }

    @Test
    public void testRobotMeta() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var doctor = buildRobot("Medic", s, RobotType.MEDICBOT, sb, FunctionType.MEDICAL_CARE);
        buildPerson("Health1", s, JobType.ENGINEER, sb, FunctionType.MEDICAL_CARE);

        var mt = new PrescribeMedicationMeta();

        var tasks = mt.getTaskJobs(doctor);
        assertTrue(tasks.isEmpty(), "No medication tasks found");

        // Add a radiation person
        createRadiationPatient(s, sb);
        tasks = mt.getTaskJobs(doctor);
        assertEquals(1, tasks.size(), "One medication task found");
    }
}
