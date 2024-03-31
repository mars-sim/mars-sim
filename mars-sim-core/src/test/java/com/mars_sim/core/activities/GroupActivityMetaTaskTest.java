package com.mars_sim.core.activities;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.social.RelationshipType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.structure.GroupActivityType;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.mapdata.location.LocalPosition;

public class GroupActivityMetaTaskTest extends AbstractMarsSimUnitTest{

    private final static GroupActivityInfo ONE = new GroupActivityInfo("One", 800, 0, 10, 50, 0, 1D, 100,
                        TaskScope.NONWORK_HOUR, BuildingCategory.LIVING);

    public void testGetSettlementTasks() {
        var s = buildSettlement();
        buildPerson("P1", s);
        buildPerson("P2", s);

        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH, 0);

        var t = new MarsTime(1, 1, 1, 0, 1);
        var ga = new GroupActivity(ONE, s, t);

        // Find a meeting as the test case
        var events = s.getFutureManager().getEvents().stream()
                        .filter(e -> e.getHandler() instanceof GroupActivity)
                        .toList();
        assertFalse("Group Activity found", events.isEmpty());

        var selected = events.get(0);
        assertEquals("Future event is correct activity", ga, selected.getHandler());

        var mt = new GroupActivityMetaTask();

        var tasks = mt.getSettlementTasks(s);
        assertTrue("No initial tasks is empty", tasks.isEmpty());

        // Advance activity to pending
        var time = selected.getWhen();
        ga.execute(time);
        tasks = mt.getSettlementTasks(s);
        assertEquals("One task for Pending state", 1, tasks.size());
        assertEquals("Demand Pending state", (int)(s.getNumCitizens() * ONE.percentagePop()), tasks.get(0).getDemand());
        
        // Check tasks are for Any hour
        var anyHour = tasks.stream().filter(h -> h.getScope() == ONE.scope()).toList();
        assertEquals("All Task are Any Hour", tasks.size(), anyHour.size());

        // Advance activity to active
        time = time.addTime(ga.getDefinition().waitDuration());
        ga.execute(time);
        tasks = mt.getSettlementTasks(s);
        assertEquals("One task for Active state", 1, tasks.size());
        assertEquals("Demand Active state", (int)(s.getNumCitizens() * ONE.percentagePop()), tasks.get(0).getDemand());

        // Advance activity to end
        time = time.addTime(ga.getDefinition().activityDuration());
        ga.execute(time);
        tasks = mt.getSettlementTasks(s);
        assertTrue("No tasks after activity complete", tasks.isEmpty());
    }

    public void testPersonSuitability() {
        var s = buildSettlement();
        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH, 0);

        // Create a friendship group where friend has a better opniino of the instigator thn the enemy
        var i = buildPerson("instigator", s);
        var e = buildPerson("enermy", s);
        RelationshipUtil.changeOpinion(e, i, RelationshipType.FACE_TO_FACE_COMMUNICATION, 1D);
        var eOpinion = e.getRelation().getOpinion(i).getAverage();
        var f = buildPerson("friend", s);
        RelationshipUtil.changeOpinion(f, i, RelationshipType.FACE_TO_FACE_COMMUNICATION, eOpinion + 10D);

        assertGreaterThan("Friend is more popular than enermy",
                                    e.getRelation().getOpinion(i).getAverage(),
                                    f.getRelation().getOpinion(i).getAverage());


        // Create an activity and 
        var now = sim.getMasterClock().getMarsTime();
        var ga = GroupActivity.createPersonActivity("Promotion", GroupActivityType.ANNOUNCEMENT, s,
                                                i, 0, now);

        // Move the activity on to the started state so it generates MetaTasks                                        
        var time = ga.getStartTime();
        ga.execute(time);
        var mt = new GroupActivityMetaTask();
        var tasks = mt.getSettlementTasks(s);
        var selected = tasks.get(0);

        // Evulate the Task for each person
        var iScore = mt.assessPersonSuitability(selected, i);
        var fScore = mt.assessPersonSuitability(selected, f);
        var eScore = mt.assessPersonSuitability(selected, e);

        // Check friend has a better score
        assertGreaterThan("Friend better score than enermy", eScore.getScore(), fScore.getScore());
        
        // Check instigator has a very high score
        assertGreaterThan("Insitigator score", 900D, iScore.getScore());

    }
}
