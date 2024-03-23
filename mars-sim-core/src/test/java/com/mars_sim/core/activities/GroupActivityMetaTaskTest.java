package com.mars_sim.core.activities;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.mapdata.location.LocalPosition;

public class GroupActivityMetaTaskTest extends AbstractMarsSimUnitTest{

    private final static GroupActivityInfo ONE = new GroupActivityInfo("One", 800, 10, 50, 0, 1D, 100, BuildingCategory.LIVING);

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
}
