package com.mars_sim.core.person.health.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Set;
import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.function.ActivitySpot;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.ComplaintType;

public class ExamineBodyTest extends MarsSimUnitTest {

    @Test
    public void testCreateSettlementTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        
        Set<ActivitySpot> spots = sb.getFunction(FunctionType.MEDICAL_CARE).getActivitySpots();
        
        assertEquals(2, spots.size(), "# of spots");
        
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        assertEquals(sb, patient.getBuildingLocation(), "Patient is at ");
        
        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.DEHYDRATION);
        patient.getPhysicalCondition().recordDead(hp, true, PhysicalCondition.STANDARD_QUOTE_0);
        var death = patient.getPhysicalCondition().getDeathDetails();

        var doctor = buildPerson("Doctor", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        assertEquals(sb, doctor.getBuildingLocation(), "Doctor is at ");
        
        doctor.getSkillManager().addNewSkill(SkillType.MEDICINE, 20);

        var task = ExamineBody.createTask(doctor, death);
        assertEquals(ExamineBody.PREPARING, task.getPhase(), "At preparing phase");
        
        assertFalse(task.isDone(), "Task created");
      
        executeTaskUntilSubTask(doctor, task, 20);

        assertEquals(ExamineBody.PREPARING, task.getPhase(), "At preparing phase");
   
        assertEquals(0D, death.getEstTimeExam(), "Estimated Time for examination");
        assertEquals(0D, death.getTimeSpentExam(), "Time completed in examination");
        
        executeTask(doctor, task, 100);
  
        assertEquals(ExamineBody.PREPARING, task.getPhase(), "At preparing phase");
        
        assertFalse(task.isDone(), "Task still going");
        
        executeTask(doctor, task, 200);
   
        assertEquals(ExamineBody.EXAMINING, task.getPhase(), "At examining phase");
 
        assertFalse(task.isDone(), "Task still going");
        
        assertGreaterThan("Estimated Time for examination", 0D, death.getEstTimeExam());
        assertGreaterThan("Time completed in examination", 0D, death.getTimeSpentExam());
      
        executeTask(doctor, task, 200);    

        assertGreaterThan("Estimated Time for examination", 0D, death.getEstTimeExam());
        assertGreaterThan("Time completed in examination", 0D, death.getTimeSpentExam());
  
        // Complete exam
        assertEquals(null, task.getPhase(), "Report next");

        // Complete report
        executeTaskUntilPhase(doctor, task, 1000);
        assertTrue(task.isDone(), "Task completed");
        assertTrue(death.getExamDone(), "Examination completed");
        assertTrue(patient.isBuried(), "Buried completed");

        assertNotEquals("Cause has been recorded", PhysicalCondition.TBD, death.getCause());
    }

    @Test
    public void testExamineBodyMetaTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new ExamineBodyMeta();

        // Self heal
        var tasks = mt.getSettlementTasks(s);
        assertTrue(tasks.isEmpty(), "No bodies to examine");

        // Make person dead
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.STARVATION);
        patient.getPhysicalCondition().recordDead(hp, true, PhysicalCondition.STANDARD_QUOTE_1);

        tasks = mt.getSettlementTasks(s);
        assertFalse(tasks.isEmpty(), "Bodies to examine found");
    }
}
