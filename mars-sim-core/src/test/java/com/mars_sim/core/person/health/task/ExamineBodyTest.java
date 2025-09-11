package com.mars_sim.core.person.health.task;

import static org.junit.Assert.assertNotEquals;

import java.util.Set;
import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.function.ActivitySpot;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.ComplaintType;

public class ExamineBodyTest extends AbstractMarsSimUnitTest {

    public void testCreateSettlementTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        
        Set<ActivitySpot> spots = sb.getFunction(FunctionType.MEDICAL_CARE).getActivitySpots();
        
        assertEquals("# of spots", 2, spots.size());
        
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        assertEquals("Patient is at ", sb, patient.getBuildingLocation());
        
        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.DEHYDRATION);
        patient.getPhysicalCondition().recordDead(hp, true, PhysicalCondition.STANDARD_QUOTE_0);
        var death = patient.getPhysicalCondition().getDeathDetails();

        var doctor = buildPerson("Doctor", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        assertEquals("Doctor is at ", sb, doctor.getBuildingLocation());
        
        doctor.getSkillManager().addNewSkill(SkillType.MEDICINE, 20);

        var task = ExamineBody.createTask(doctor, death);
        assertEquals("At preparing phase", ExamineBody.PREPARING, task.getPhase());
        
        assertFalse("Task created", task.isDone());
      
        executeTaskUntilSubTask(doctor, task, 20);

        assertEquals("At preparing phase", ExamineBody.PREPARING, task.getPhase());
   
        assertEquals("Estimated Time for examination", 0D, death.getEstTimeExam());
        assertEquals("Time completed in examination", 0D, death.getTimeSpentExam());
        
        executeTask(doctor, task, 100);
  
        assertEquals("At preparing phase", ExamineBody.PREPARING, task.getPhase());
        
        assertFalse("Task still going", task.isDone());
        
        executeTask(doctor, task, 200);
   
        assertEquals("At examining phase", ExamineBody.EXAMINING, task.getPhase());
 
        assertFalse("Task still going", task.isDone());
        
        assertGreaterThan("Estimated Time for examination", 0D, death.getEstTimeExam());
        assertGreaterThan("Time completed in examination", 0D, death.getTimeSpentExam());
      
        executeTask(doctor, task, 200);    

        assertGreaterThan("Estimated Time for examination", 0D, death.getEstTimeExam());
        assertGreaterThan("Time completed in examination", 0D, death.getTimeSpentExam());
  
        // Complete exam
        assertEquals("Report next", null, task.getPhase());

        // Complete report
        executeTaskUntilPhase(doctor, task, 1000);
        assertTrue("Task completed", task.isDone());
        assertTrue("Examination completed", death.getExamDone());
        assertTrue("Buried completed", patient.isBuried());

        assertNotEquals("Cause has been recorded", PhysicalCondition.TBD, death.getCause());
    }

    public void testExamineBodyMetaTask() {
        var s = buildSettlement("Hospital");
        var sb = SelfTreatHealthProblemTest.buildMediCare(this, s);
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new ExamineBodyMeta();

        // Self heal
        var tasks = mt.getSettlementTasks(s);
        assertTrue("No bodies to examine", tasks.isEmpty());

        // Make person dead
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.STARVATION);
        patient.getPhysicalCondition().recordDead(hp, true, PhysicalCondition.STANDARD_QUOTE_1);

        tasks = mt.getSettlementTasks(s);
        assertFalse("Bodies to examine found", tasks.isEmpty());
    }
}
