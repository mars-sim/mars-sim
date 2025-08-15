package com.mars_sim.core.person.health.task;

import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
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
        
        System.out.println("# of spots: " + spots.size());

        // How to declare  more than just one activity spot ?
        
        List<ActivitySpot> list = new ArrayList<>(spots);
        
        ActivitySpot spot = list.get(0);
        
        System.out.println("Location: " + spot.getPos());
        
        
        var patient = buildPerson("Patient", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        // Laceration is self heal
        var hp = SelfTreatHealthProblemTest.addComplaint(this, patient, ComplaintType.DEHYDRATION);
        patient.getPhysicalCondition().recordDead(hp, true, PhysicalCondition.STANDARD_QUOTE_0);
        var death = patient.getPhysicalCondition().getDeathDetails();

        var doctor = buildPerson("Doctor", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);
        doctor.getSkillManager().addNewSkill(SkillType.MEDICINE, 20);

        var task = ExamineBody.createTask(doctor, death);
        assertEquals("At preparing phase", ExamineBody.PREPARING, task.getPhase());
        
        assertFalse("Task created", task.isDone());
     
        System.out.println("Doctor is at " + doctor.getBuildingLocation());
        
//        assertTrue("At medical", task.sendToMedicalAid());
        
        executeTaskUntilSubTask(doctor, task, 20);

        assertEquals("At preparing phase", ExamineBody.PREPARING, task.getPhase());
        System.out.println("Phase: " + task.getPhase());
        
        executeTask(doctor, task, 100);
      
        System.out.println("Phase: " + task.getPhase());
      
        executeTask(doctor, task, 50);
        
        System.out.println("Phase: " + task.getPhase());
        
        assertGreaterThan("Estimate time for examination", 0D, death.getEstTimeExam());

        executeTask(doctor, task, 100);
        
        System.out.println("Phase: " + task.getPhase());
        assertEquals("At preparing phase", ExamineBody.EXAMINING, task.getPhase());
        
        assertGreaterThan("Time completed in examination", 0D, death.getTimeSpentExam());
       
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
