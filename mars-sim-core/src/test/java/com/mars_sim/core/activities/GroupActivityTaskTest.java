package com.mars_sim.core.activities;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.time.EventSchedule;
import com.mars_sim.core.time.MarsTime;

public class GroupActivityTaskTest extends MarsSimUnitTest {
    public final static ExperienceImpact IMPACT = new ExperienceImpact(1D,
                    NaturalAttributeType.CONVERSATION, false, 0D,
                    SkillType.MANAGEMENT);

    @Test
    public void testPerformMappedPhase() {
        var s = buildSettlement("mock");
        var accom = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH);

        var t = new MarsTime(1, 1, 1, 0, 1);

        var calendar = new EventSchedule(0, 0, 800);
        var info = new GroupActivityInfo("One", 10, 50, calendar, 1D, 100,
        TaskScope.NONWORK_HOUR, BuildingCategory.LIVING, GroupActivityInfo.DEFAULT_IMPACT);

        // Build activity and move onto waiting phase
        var ga = new GroupActivity(info, s, t);
        t = t.addTime(calendar.getTimeOfDay());
        ga.execute(t);

        SkillType testedSkill = SkillType.MANAGEMENT;

        // Assign person to Task
        Person p = buildPerson("Worker", s);
        p.getPhysicalCondition().setPerformanceFactor(1);
        p.setCurrentBuilding(accom); // Put person in correct buildign to avoid walking sub task
        p.getSkillManager().addNewSkill(testedSkill, 1);

        var task = new GroupActivityTask(ga, p);

        assertFalse(task.isDone(), "Activity not done after start");
        assertEquals(GroupActivityTask.WAITING, task.getPhase(), "Activity waiting phase");

        // Advance to active phase
        t = t.addTime(info.waitDuration());
        ga.execute(t);

        // Person takes part
        double origSkill = p.getSkillManager().getCumulativeExperience(testedSkill);
        double returnedTime = task.performTask(info.activityDuration()/3);
        assertFalse(task.isDone(), "Activity not done");
        assertEquals(GroupActivityTask.ACTIVE, task.getPhase(), "Activity active phase");
        double newSkill = p.getSkillManager().getCumulativeExperience(testedSkill);
        assertGreaterThan("Skill has increased", origSkill, newSkill);
        origSkill = newSkill;
        assertEquals(0D, returnedTime, "Activity consumed all time");

        // Second execute
        returnedTime = task.performTask(info.activityDuration()/3);
        assertFalse(task.isDone(), "Activity no done 2nd");
        assertEquals(GroupActivityTask.ACTIVE, task.getPhase(), "Activity active phase 2nd");
        newSkill = p.getSkillManager().getCumulativeExperience(testedSkill);
        assertGreaterThan("Skill has increased 2nd", origSkill, newSkill);
        assertEquals(0D, returnedTime, "Activity consumed all time 2nd");

        // Advance to completed phase
        t = t.addTime(info.activityDuration());
        ga.execute(t);
        double offered = info.activityDuration()/4;
        returnedTime = task.performTask(offered); // Short of duration but will still complete
        assertTrue(task.isDone(), "Activity complete");
        assertEquals(offered, returnedTime, "Activity consumed no time");



    }
}
