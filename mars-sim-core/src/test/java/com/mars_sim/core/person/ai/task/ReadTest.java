package com.mars_sim.core.person.ai.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.meta.ReadMeta;

public class ReadTest extends MarsSimUnitTest{
    @Test
    public void testCreateTaskRecreation() {
        var s = buildSettlement("Read");
        var d = buildRecreation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var p = buildPerson("reader", s, JobType.ENGINEER, d, FunctionType.RECREATION);

        var task = Read.createTask(p);

        assertFalse(task.isDone(), "Task created");

        var skill = task.getReading();
        var origSkill = p.getSkillManager().getSkill(skill).getCumulativeExperience();
        assertNotNull(skill, "Skill selected");

        executeTask(p, task, 1000);
        assertTrue(task.isDone(), "Task completed");

        var newSkill = p.getSkillManager().getSkill(skill).getCumulativeExperience();
        assertGreaterThan("Skill improved", origSkill, newSkill);
    }

    @Test
    public void testCreateTaskDining() {
        var s = buildSettlement("Read");
        var d = buildFunction(s.getBuildingManager(), "Lander Hab", BuildingCategory.LIVING, FunctionType.DINING, LocalPosition.DEFAULT_POSITION, 0D, true);
        var p = buildPerson("reader", s, JobType.ENGINEER, d, FunctionType.DINING);

        var task = Read.createTask(p);

        assertFalse(task.isDone(), "Task created");
    }

    @Test
    public void testCreateTaskBed() {
        var s = buildSettlement("Read");
        var d = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var p = buildPerson("reader", s, JobType.ENGINEER, d, FunctionType.LIVING_ACCOMMODATION);

        assertNotNull(p.getBed(), "Person has bed");
        var task = Read.createTask(p);
        assertFalse(task.isDone(), "Task created");
    }

    @Test
    public void testReadMeta() {
        var s = buildSettlement("Read");
        var d = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var p = buildPerson("reader", s, JobType.ENGINEER, d, FunctionType.LIVING_ACCOMMODATION);

        var mt = new ReadMeta();
        var tasks = mt.getTaskJobs(p);
        assertEquals(1, tasks.size(), "Read tasks found");
    }
}
