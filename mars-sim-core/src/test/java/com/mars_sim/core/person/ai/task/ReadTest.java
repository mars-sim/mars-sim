package com.mars_sim.core.person.ai.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.meta.ReadMeta;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.LocalPosition;

public class ReadTest extends AbstractMarsSimUnitTest{
    public void testCreateTaskRecreation() {
        var s = buildSettlement("Read");
        var d = buildRecreation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
        var p = buildPerson("reader", s, JobType.ENGINEER, d, FunctionType.RECREATION);

        var task = Read.createTask(p);

        assertFalse("Task created", task.isDone());

        var skill = task.getReading();
        var origSkill = p.getSkillManager().getSkill(skill).getCumulativeExperience();
        assertNotNull("Skill selected", skill);

        executeTask(p, task, 1000);
        assertTrue("Task completed", task.isDone());

        var newSkill = p.getSkillManager().getSkill(skill).getCumulativeExperience();
        assertGreaterThan("Skill improved", origSkill, newSkill);
    }

    public void testCreateTaskDining() {
        var s = buildSettlement("Read");
        var d = buildFunction(s.getBuildingManager(), "Lander Hab", BuildingCategory.LIVING, FunctionType.DINING, LocalPosition.DEFAULT_POSITION, 0D, true);
        var p = buildPerson("reader", s, JobType.ENGINEER, d, FunctionType.DINING);

        var task = Read.createTask(p);

        assertFalse("Task created", task.isDone());
    }

    public void testCreateTaskBed() {
        var s = buildSettlement("Read");
        var d = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
        var p = buildPerson("reader", s, JobType.ENGINEER, d, FunctionType.LIVING_ACCOMMODATION);

        assertNotNull("Person has bed", p.getBed());
        var task = Read.createTask(p);
        assertFalse("Task created", task.isDone());
    }

    public void testReadMeta() {
        var s = buildSettlement("Read");
        var d = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
        var p = buildPerson("reader", s, JobType.ENGINEER, d, FunctionType.LIVING_ACCOMMODATION);

        var mt = new ReadMeta();
        var tasks = mt.getTaskJobs(p);
        assertEquals("Read tasks found", 1, tasks.size());
    }
}
