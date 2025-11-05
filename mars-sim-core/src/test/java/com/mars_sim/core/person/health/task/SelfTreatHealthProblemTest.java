package com.mars_sim.core.person.health.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.MarsSimContext;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.health.ComplaintType;
import com.mars_sim.core.person.health.HealthProblem;
import com.mars_sim.core.person.health.HealthProblemState;
import com.mars_sim.core.structure.Settlement;

public class SelfTreatHealthProblemTest extends MarsSimUnitTest {
    /**
     * Create a Medical care facility
     * @param context
     * @param s
     * @return
     */
    public static Building buildMediCare(MarsSimContext context, Settlement s) {
        return context.buildFunction(s.getBuildingManager(), "Infirmary", BuildingCategory.MEDICAL, FunctionType.MEDICAL_CARE, LocalPosition.DEFAULT_POSITION, 0D, true);
    }

    public static HealthProblem addComplaint(MarsSimContext context, Person p, ComplaintType ct) {
        var c = context.getSim().getMedicalManager().getComplaintByName(ct);
        var pc = p.getPhysicalCondition();
        return pc.addMedicalComplaint(c);
    }

    @Test
    public void testCreateSettlementTask() {
        var s = buildSettlement("Hospital");
        var sb = buildMediCare(this, s);
        var p = buildPerson("Mr. Physician 0", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        // Laceration is self heal
        var hp = addComplaint(this, p, ComplaintType.LACERATION);

        var pc = p.getPhysicalCondition();
        assertEquals(1, pc.getProblems().size(), "Single health problem");
        assertTrue(pc.getProblems().contains(hp), "Complaint is in persons problems");
        assertEquals(HealthProblemState.DEGRADING, hp.getState(), "Complaint in degrading");

        var task = SelfTreatHealthProblem.createTask(p);
        assertFalse(task.isDone(), "Task created");
        assertTrue(sb.getMedical().getProblemsAwaitingTreatment().contains(hp), "Health problem waiting at Medical care");
        assertFalse(sb.getMedical().getProblemsBeingTreated().contains(hp), "Health problem not treated at Medical care");

        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(p, task, 50);
        executeTask(p, task, 1);
        
        assertFalse(sb.getMedical().getProblemsBeingTreated().contains(hp), "Health problem still not being treated at Medical care yet");
        assertTrue(sb.getMedical().getProblemsAwaitingTreatment().contains(hp), "Health problem waiting at Medical care");

        // Complete treatment
        executeTask(p, task, 1000);

        assertTrue(task.isDone(), "Task completed");
        assertEquals(1, pc.getProblems().size(), "Complaints remaining");

        assertEquals(HealthProblemState.RECOVERING, hp.getState(), "Complaint in recovery");
        assertFalse(sb.getMedical().getProblemsBeingTreated().contains(hp), "Health problem removed from Medical care");

    }

    
    @Test
    public void testCreateVehicleTask() {
        var s = buildSettlement("Hospital");
        buildMediCare(this, s);  // Build a settlement medical center to make sure there is no teleporting
        var r = buildRover(s, "rover", LocalPosition.DEFAULT_POSITION, EXPLORER_ROVER);
        var p = buildPerson("Mr. Physician 1", s, JobType.DOCTOR);
        p.transfer(r);
        assertTrue(p.isInVehicle(), "Person starts in Vehicle");

        // Laceration is self heal
        var hp = addComplaint(this, p, ComplaintType.LACERATION);

        var sb = r.getSickBay();
        var pc = p.getPhysicalCondition();
        assertEquals(1, pc.getProblems().size(), "Single health problem");
        assertTrue(pc.getProblems().contains(hp), "Complaint is in persons problems");
        assertEquals(HealthProblemState.DEGRADING, hp.getState(), "Complaint in degrading");

        var task = SelfTreatHealthProblem.createTask(p);
        
        assertFalse(task.isDone(), "Task created");
        assertTrue(sb.getProblemsAwaitingTreatment().contains(hp), "Health problem waiting at Medical care");
        assertFalse(sb.getProblemsBeingTreated().contains(hp), "Health problem not treated at Medical care");

        // Do the walk; then first step of treatment
        executeTaskUntilSubTask(p, task, 1000);
        executeTask(p, task, 1);
        
        assertTrue(sb.getProblemsBeingTreated().contains(hp), "Health problem treated at Medical care");
        assertFalse(sb.getProblemsAwaitingTreatment().contains(hp), "Health problem not waiting at Medical care");

        // Complete treatment
        executeTask(p, task, 1000);

        assertTrue(task.isDone(), "Task completed");
        assertEquals(1, pc.getProblems().size(), "Complaints remaining");

        assertEquals(HealthProblemState.RECOVERING, hp.getState(), "Complaint in recovery");
        assertFalse(sb.getProblemsBeingTreated().contains(hp), "Health problem removed from Medical care");

        assertTrue(p.isInVehicle(), "Person still in Vehicle");
    }

    @Test
    public void testMetaTask() {
        var s = buildSettlement("Hospital");
        var sb = buildMediCare(this, s);
        var p = buildPerson("Mr. Physician 2", s, JobType.DOCTOR, sb, FunctionType.MEDICAL_CARE);

        var mt = new SelfTreatHealthProblemMeta();

        // Broken bone is not self heal
        addComplaint(this, p, ComplaintType.BROKEN_BONE);
        var tasks = mt.getTaskJobs(p);
        assertTrue(tasks.isEmpty(), "No self heal tasks");

        // Laceration is self heal
        addComplaint(this, p, ComplaintType.LACERATION);
        tasks = mt.getTaskJobs(p);
        assertFalse(tasks.isEmpty(), "Self heal tasks");
    }
}
