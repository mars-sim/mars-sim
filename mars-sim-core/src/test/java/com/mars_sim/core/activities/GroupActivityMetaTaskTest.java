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
import com.mars_sim.core.person.ai.social.Relation;
import com.mars_sim.core.person.ai.social.RelationshipType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.structure.GroupActivityType;
import com.mars_sim.core.time.EventSchedule;

public class GroupActivityMetaTaskTest extends MarsSimUnitTest{

    private static final GroupActivityInfo ONE = new GroupActivityInfo("One", 10, 50,
                                new EventSchedule(0, 0, 800), 1D, 100,
                                TaskScope.NONWORK_HOUR, BuildingCategory.LIVING, GroupActivityInfo.DEFAULT_IMPACT);

    @Test
    public void testGetSettlementTasks() {
        var s = buildSettlement("mock");
        buildPerson("P1", s);
        buildPerson("P2", s);

        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH, 0);

        var t = getSim().getMasterClock().getMarsTime();
        var ga = new GroupActivity(ONE, s, t);

        // Find a meeting as the test case
        var events = s.getFutureManager().getEvents().stream()
                        .filter(e -> e.getHandler() instanceof GroupActivity)
                        .toList();
        assertFalse(events.isEmpty(), "No Group Activities");

        // Find my added activity
        var matched = events.stream()
                    .filter(g -> (ga.equals(g.getHandler())))
                    .toList();
        assertEquals(1, matched.size(), "Future event matches found");
        var selected = matched.get(0);

        var mt = new GroupActivityMetaTask();

        var tasks = mt.getSettlementTasks(s);
        assertTrue(tasks.isEmpty(), "No initial tasks is empty");

        // Advance activity to pending
        var time = selected.getWhen();
        ga.execute(time);
        tasks = mt.getSettlementTasks(s);
        assertEquals(1, tasks.size(), "One task for Pending state");
        assertEquals((int)(s.getNumCitizens() * ONE.percentagePop()), tasks.get(0).getDemand(), "Demand Pending state");
        
        // Check tasks are for Any hour
        var anyHour = tasks.stream().filter(h -> h.getScope() == ONE.scope()).toList();
        assertEquals(tasks.size(), anyHour.size(), "All Task are Any Hour");

        // Advance activity to active
        time = time.addTime(ga.getDefinition().waitDuration());
        ga.execute(time);
        tasks = mt.getSettlementTasks(s);
        assertEquals(1, tasks.size(), "One task for Active state");
        assertEquals((int)(s.getNumCitizens() * ONE.percentagePop()), tasks.get(0).getDemand(), "Demand Active state");

        // Advance activity to end
        time = time.addTime(ga.getDefinition().activityDuration());
        ga.execute(time);
        tasks = mt.getSettlementTasks(s);
        assertTrue(tasks.isEmpty(), "No tasks after activity complete");
    }

    @Test
    public void testPersonSuitability() {
        var s = buildSettlement("mock");
        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH, 0);

        // Create a friendship group where friend has a better opinion of the instigator and the enemy
        var i = buildPerson("instigator", s);
        var p1 = buildPerson("p1", s);
        RelationshipUtil.changeOpinion(p1, i, RelationshipType.FACE_TO_FACE_COMMUNICATION, Relation.MAX_OPINION);
        var p2 = buildPerson("p2", s);
        RelationshipUtil.changeOpinion(p2, i, RelationshipType.FACE_TO_FACE_COMMUNICATION, 0);
        Person e;
        Person f;
        if (p1.getRelation().getOpinion(i).getAverage() < 
                                    p2.getRelation().getOpinion(i).getAverage()) {
            e = p1;
            f = p2;
        }
        else {
            e = p2;
            f = p1;
        }

        // Create an activity  
        var now = getSim().getMasterClock().getMarsTime();
        var ga = GroupActivity.createPersonActivity("Promotion", GroupActivityType.ANNOUNCEMENT, s,
                                                i, 0, now);

        // Move the activity on to the started state so it generates MetaTasks                                        
        var time = ga.getStartTime();
        ga.execute(time);
        var mt = new GroupActivityMetaTask();
        var tasks = mt.getSettlementTasks(s);
        var selected = tasks.get(0);

        // Evaluate the Task for each person
        var iScore = mt.assessPersonSuitability(selected, i);
        var fScore = mt.assessPersonSuitability(selected, f);
        var eScore = mt.assessPersonSuitability(selected, e);

        // Check friend has a better score
        assertGreaterThan("Friend better score than enemy", eScore.getScore(), fScore.getScore());
        
        // Check instigator has a very high score
        assertGreaterThan("Instigator score", 900D, iScore.getScore());

    }
}
