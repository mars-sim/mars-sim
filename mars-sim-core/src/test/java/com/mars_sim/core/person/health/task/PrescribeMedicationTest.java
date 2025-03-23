package com.mars_sim.core.person.health.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.BodyRegionType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.RadiationType;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;

public class PrescribeMedicationTest extends AbstractMarsSimUnitTest {
    private Person createRadiationPatient(Settlement s, Building sb) {
        var p = buildPerson("Patient", s, JobType.ENGINEER, sb, FunctionType.MEDICAL_CARE);
        var pc = p.getPhysicalCondition();
        var e = p.getPhysicalCondition().getRadiationExposure();

        // Simualte a mass radiation dose
        e.addDose(RadiationType.SEP, BodyRegionType.SKIN, 2000D);
        var pulse = createPulse(1, 0, true, true);
        pc.timePassing(pulse, s);
        assertTrue("Patient is radiation sick", e.isSick());
        assertTrue("Patient is radiation poisoned", pc.isRadiationPoisoned());

        return p;
    }



    public void testDetermineRadiation() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        buildPerson("Health1", s, JobType.ENGINEER, sb, FunctionType.MEDICAL_CARE);

        var p = createRadiationPatient(s, sb);

        var doctor = buildPerson("Docter", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var found = PrescribeMedication.determinePatient(doctor);
        assertEquals("Found stressed out patient", p, found);
    }

    public void testCreationRadiation() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var p = createRadiationPatient(s, sb);
        var doctor = buildPerson("Docter", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var task = new PrescribeMedication(doctor);
        assertFalse("Task is active", task.isDone());
        assertEquals("Found patient", p, task.getPatient());

        // Complet eto the end
        executeTask(doctor, task, 2000);
        assertTrue("Task completed", task.isDone());
        var meds = p.getPhysicalCondition().getMedicationList();
        assertEquals("Patient has medication", 1, meds.size());
        
        // Check has radiationmedication
        var radMeds = meds.get(0);
        assertEquals("Radiation meds", ComplaintType.RADIATION_SICKNESS, radMeds.getComplaintType());
    }

    public void testPersonMeta() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var doctor = buildPerson("Docter", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        buildPerson("Health1", s, JobType.ENGINEER, sb, FunctionType.MEDICAL_CARE);

        var mt = new PrescribeMedicationMeta();

        var tasks = mt.getTaskJobs(doctor);
        assertTrue("No medication tasks found", tasks.isEmpty());

        // Add a radiation person
        createRadiationPatient(s, sb);
        tasks = mt.getTaskJobs(doctor);
        assertEquals("One medication task found", 1, tasks.size());
    }

    public void testRobotMeta() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var doctor = buildRobot("Medic", s, RobotType.MEDICBOT, sb, FunctionType.MEDICAL_CARE);
        buildPerson("Health1", s, JobType.ENGINEER, sb, FunctionType.MEDICAL_CARE);

        var mt = new PrescribeMedicationMeta();

        var tasks = mt.getTaskJobs(doctor);
        assertTrue("No medication tasks found", tasks.isEmpty());

        // Add a radiation person
        createRadiationPatient(s, sb);
        tasks = mt.getTaskJobs(doctor);
        assertEquals("One medication task found", 1, tasks.size());
    }
}
