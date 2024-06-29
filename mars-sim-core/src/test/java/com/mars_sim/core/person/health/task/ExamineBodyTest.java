package com.mars_sim.core.person.health.task;

import static org.junit.Assert.assertNotEquals;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.structure.building.function.FunctionType;

public class ExamineBodyTest extends AbstractMarsSimUnitTest {

    public void testCreateSettlementTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.APPENDICITIS);
        patient.getPhysicalCondition().recordDead(hp, true, "Opps");
        var death = patient.getPhysicalCondition().getDeathDetails();

        var doctor = buildPerson("Docter", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        doctor.getSkillManager().addNewSkill(SkillType.MEDICINE, 20);

        var task = ExamineBody.createTask(doctor, death);
        assertFalse("Task created", task.isDone());
     
        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(doctor, task, 1000);
        executeTask(doctor, task, 1);
        assertGreaterThan("Estimate time for examination", 0D, death.getEstTimeExam());
        assertGreaterThan("Time completed in examination", 0D, death.getTimeExam());
       
        // Complete exam
        executeTaskUntilPhase(doctor, task, 1000);
        assertFalse("Task still going", task.isDone());
        assertEquals("Report next", ExamineBody.RECORDING, task.getPhase());

        // Complete report
        executeTaskUntilPhase(doctor, task, 1000);
        assertTrue("Task completed", task.isDone());
        assertTrue("Examination completed", death.getExamDone());
        assertTrue("Buried completed", patient.isBuried());

        assertNotEquals("Cause has been recorded", PhysicalCondition.TBD, death.getCause());
    }

    public void testMetaTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new ExamineBodyMeta();

        // Self heal
        var tasks = mt.getSettlementTasks(s);
        assertTrue("No bodies to examine", tasks.isEmpty());

        // Make person dead
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.APPENDICITIS);
        patient.getPhysicalCondition().recordDead(hp, true, "Opps");

        tasks = mt.getSettlementTasks(s);
        assertFalse("Bodies to examine found", tasks.isEmpty());
    }
}
