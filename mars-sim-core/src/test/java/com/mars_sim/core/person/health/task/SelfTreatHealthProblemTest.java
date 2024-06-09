package com.mars_sim.core.person.health.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.SelfTreatHealthProblem;
import com.mars_sim.core.person.ai.task.meta.SelfTreatHealthProblemMeta;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.LocalPosition;

public class SelfTreatHealthProblemTest extends AbstractMarsSimUnitTest {
    private Building buildMediCare(Settlement s) {
        return buildFunction(s.getBuildingManager(), "Infirmary", BuildingCategory.MEDICAL, FunctionType.MEDICAL_CARE, LocalPosition.DEFAULT_POSITION, 0D, true);
    }

    private void addComplaint(Person p, ComplaintType ct) {
        var c = getSim().getMedicalManager().getComplaintByName(ct);
        var pc = p.getPhysicalCondition();
        pc.addMedicalComplaint(c);
    }

    public void testCreateTask() {
        var s = buildSettlement("Hospital");
        var sb = buildMediCare(s);
        var p = buildPerson("Ill", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        // Laceration is self heal
        addComplaint(p, ComplaintType.LACERATION);

        var pc = p.getPhysicalCondition();
        assertFalse("Has no complaint", pc.getProblems().isEmpty());

        var task = SelfTreatHealthProblem.createTask(p);
        assertFalse("Task created", task.isDone());

        executeTask(p, task, 1000);

        assertTrue("Task completed", task.isDone());
        assertEquals("Complaints remaining", 1, pc.getProblems().size());

        var hp = pc.getProblems().get(0);
        assertTrue("Complaint in recovery", hp.getRecovering());
    }


    public void testMetaTask() {
        var s = buildSettlement("Hospital");
        var sb = buildMediCare(s);
        var p = buildPerson("Ill", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new SelfTreatHealthProblemMeta();

        // Broken bone is not self heal
        addComplaint(p, ComplaintType.BROKEN_BONE);
        var tasks = mt.getTaskJobs(p);
        assertTrue("No self heal tasks", tasks.isEmpty());

        // Laceration is self heal
        addComplaint(p, ComplaintType.LACERATION);
        tasks = mt.getTaskJobs(p);
        assertFalse("Self heal tasks", tasks.isEmpty());
    }
}
