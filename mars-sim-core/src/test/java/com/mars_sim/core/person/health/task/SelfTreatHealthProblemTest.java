package com.mars_sim.core.person.health.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.HealthProblemState;
import com.mars_sim.core.science.task.MarsSimContext;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.LocalPosition;

public class SelfTreatHealthProblemTest extends AbstractMarsSimUnitTest {
    /**
     * Create a Medical care facility
     * @param context
     * @param s
     * @return
     */
    static Building buildMediCare(MarsSimContext context, Settlement s) {
        return context.buildFunction(s.getBuildingManager(), "Infirmary", BuildingCategory.MEDICAL, FunctionType.MEDICAL_CARE, LocalPosition.DEFAULT_POSITION, 0D, true);
    }

    static HealthProblem addComplaint(MarsSimContext context, Person p, ComplaintType ct) {
        var c = context.getSim().getMedicalManager().getComplaintByName(ct);
        var pc = p.getPhysicalCondition();
        return pc.addMedicalComplaint(c);
    }

    public void testCreateSettlementTask() {
        var s = buildSettlement("Hospital");
        var sb = buildMediCare(this, s);
        var p = buildPerson("Ill", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        // Laceration is self heal
        var hp = addComplaint(this, p, ComplaintType.LACERATION);

        var pc = p.getPhysicalCondition();
        assertEquals("Single health problem", 1, pc.getProblems().size());
        assertTrue("Complaint is in persons problems", pc.getProblems().contains(hp));
        assertEquals("Complaint in degrading", HealthProblemState.DEGRADING, hp.getState());

        var task = SelfTreatHealthProblem.createTask(p);
        assertFalse("Task created", task.isDone());
        assertTrue("Health problem waiting at Medical care", sb.getMedical().getProblemsAwaitingTreatment().contains(hp));
        assertFalse("Health problem not treated at Medical care", sb.getMedical().getProblemsBeingTreated().contains(hp));

        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(p, task, 1000);
        executeTask(p, task, 1);
        assertTrue("Health problem treated at Medical care", sb.getMedical().getProblemsBeingTreated().contains(hp));
        assertFalse("Health problem not waiting at Medical care", sb.getMedical().getProblemsAwaitingTreatment().contains(hp));

        // Complete treatment
        executeTask(p, task, 1000);

        assertTrue("Task completed", task.isDone());
        assertEquals("Complaints remaining", 1, pc.getProblems().size());

        assertEquals("Complaint in recovery", HealthProblemState.RECOVERING, hp.getState());
        assertFalse("Health problem removed from Medical care", sb.getMedical().getProblemsBeingTreated().contains(hp));

    }

    
    public void testCreateVehicleTask() {
        var s = buildSettlement("Hospital");
        buildMediCare(this, s);  // Build a settlement medical center to make sure there is no teleporting
        var r = buildRover(s, "rover", LocalPosition.DEFAULT_POSITION);
        var p = buildPerson("Ill", s, JobType.DOCTOR);
        p.transfer(r);
        assertTrue("Person starts in Vehicle", p.isInVehicle());

        // Laceration is self heal
        var hp = addComplaint(this, p, ComplaintType.LACERATION);

        var sb = r.getSickBay();
        var pc = p.getPhysicalCondition();
        assertEquals("Single health problem", 1, pc.getProblems().size());
        assertTrue("Complaint is in persons problems", pc.getProblems().contains(hp));
        assertEquals("Complaint in degrading", HealthProblemState.DEGRADING, hp.getState());

        var task = SelfTreatHealthProblem.createTask(p);
        assertFalse("Task created", task.isDone());
        assertTrue("Health problem waiting at Medical care", sb.getProblemsAwaitingTreatment().contains(hp));
        assertFalse("Health problem not treated at Medical care", sb.getProblemsBeingTreated().contains(hp));

        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(p, task, 1000);
        executeTask(p, task, 1);
        assertTrue("Health problem treated at Medical care", sb.getProblemsBeingTreated().contains(hp));
        assertFalse("Health problem not waiting at Medical care", sb.getProblemsAwaitingTreatment().contains(hp));

        // Complete treatment
        executeTask(p, task, 1000);

        assertTrue("Task completed", task.isDone());
        assertEquals("Complaints remaining", 1, pc.getProblems().size());

        assertEquals("Complaint in recovery", HealthProblemState.RECOVERING, hp.getState());
        assertFalse("Health problem removed from Medical care", sb.getProblemsBeingTreated().contains(hp));

        assertTrue("Person still in Vehicle", p.isInVehicle());
    }

    public void testMetaTask() {
        var s = buildSettlement("Hospital");
        var sb = buildMediCare(this, s);
        var p = buildPerson("Ill", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new SelfTreatHealthProblemMeta();

        // Broken bone is not self heal
        addComplaint(this, p, ComplaintType.BROKEN_BONE);
        var tasks = mt.getTaskJobs(p);
        assertTrue("No self heal tasks", tasks.isEmpty());

        // Laceration is self heal
        addComplaint(this, p, ComplaintType.LACERATION);
        tasks = mt.getTaskJobs(p);
        assertFalse("Self heal tasks", tasks.isEmpty());
    }
}
