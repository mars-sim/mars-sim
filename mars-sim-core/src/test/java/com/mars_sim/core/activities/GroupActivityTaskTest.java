package com.mars_sim.core.activities;


import java.util.Set;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.time.EventSchedule;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.mapdata.location.LocalPosition;

public class GroupActivityTaskTest extends AbstractMarsSimUnitTest {
    public final static ExperienceImpact IMPACT = new ExperienceImpact(1D,
                    NaturalAttributeType.CONVERSATION, false, 0D,
                    Set.of(SkillType.MANAGEMENT));

    public void testPerformMappedPhase() {
        var s = buildSettlement();
        var accom = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH, 0);

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

        assertFalse("Activity not done after start", task.isDone());
        assertEquals("Activity waiting phase", GroupActivityTask.WAITING, task.getPhase());

        // Advance to active phase
        t = t.addTime(info.waitDuration());
        ga.execute(t);

        // Person takes part
        double origSkill = p.getSkillManager().getCumulativeExperience(testedSkill);
        double returnedTime = task.performTask(info.activityDuration()/3);
        assertFalse("Activity not done", task.isDone());
        assertEquals("Activity active phase", GroupActivityTask.ACTIVE, task.getPhase());
        double newSkill = p.getSkillManager().getCumulativeExperience(testedSkill);
        assertGreaterThan("Skill has increased", origSkill, newSkill);
        origSkill = newSkill;
        assertEquals("Activity consumed all time", 0D, returnedTime);

        // Second execute
        returnedTime = task.performTask(info.activityDuration()/3);
        assertFalse("Activity no done 2nd", task.isDone());
        assertEquals("Activity active phase 2nd", GroupActivityTask.ACTIVE, task.getPhase());
        newSkill = p.getSkillManager().getCumulativeExperience(testedSkill);
        assertGreaterThan("Skill has increased 2nd", origSkill, newSkill);
        assertEquals("Activity consumed all time 2nd", 0D, returnedTime);

        // Advance to completed phase
        t = t.addTime(info.activityDuration());
        ga.execute(t);
        double offered = info.activityDuration()/4;
        returnedTime = task.performTask(offered); // Short of duration but will still complete
        assertTrue("Activity complete", task.isDone());
        assertEquals("Activity consumed no time", offered, returnedTime);



    }
}
