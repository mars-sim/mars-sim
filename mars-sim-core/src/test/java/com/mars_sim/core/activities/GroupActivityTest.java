package com.mars_sim.core.activities;


import java.util.List;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.activities.GroupActivity.ActivityState;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.events.ScheduledEventManager.ScheduledEvent;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.mapdata.location.LocalPosition;

public class GroupActivityTest extends AbstractMarsSimUnitTest {

    private final static GroupActivityInfo REPEATING = new GroupActivityInfo("Repeat", 250, 10, 50, 2, 0.5D, 100, BuildingCategory.LIVING);
    private final static GroupActivityInfo ONE = new GroupActivityInfo("One", 800, 10, 50, 0, 0.5D, 100, BuildingCategory.LIVING);

    public void testOneOffCycle() {
        var s = buildSettlement();
        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH, 0);

        var t = new MarsTime(1, 1, 1, 0, 1);

        var ga = new GroupActivity(ONE, s, t);
        assertEquals("Scheduled actvity", ActivityState.SCHEDULED, ga.getState());

        // Advance to pending
        t = t.addTime(ONE.scheduledStart());
        int advance = ga.execute(t);
        assertEquals("Pending actvity", ActivityState.PENDING, ga.getState());
        assertEquals("Wait duration", ONE.waitDuration(), advance);
        Building meeting = ga.getMeetingPlace();
        assertNotNull("Allocated meeting place", meeting);
        assertEquals("Selected meeting place", ONE.place(), meeting.getCategory());

        // Advance to active
        t = t.addTime(ONE.waitDuration());
        advance = ga.execute(t);
        assertEquals("Active actvity", ActivityState.ACTIVE, ga.getState());
        assertEquals("Active duration", ONE.activityDuration(), advance);

        // Advance to end
        t = t.addTime(ONE.activityDuration());
        advance = ga.execute(t);
        assertEquals("Active actvity", ActivityState.DONE, ga.getState());
        assertEquals("Activity no event", -1, advance);
        meeting = ga.getMeetingPlace();
        assertNull("Released meeting place", meeting);
    }

    public void testRepeatCycle() {
        var s = buildSettlement();
        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH, 0);

        var t = new MarsTime(1, 1, 1, 0, 1);

        var ga = new GroupActivity(REPEATING, s, t);

        // Advance to pending
        t = t.addTime(REPEATING.scheduledStart());
        int advance = ga.execute(t);
        assertEquals("Pending actvity", ActivityState.PENDING, ga.getState());
        assertEquals("Wait duration", REPEATING.waitDuration(), advance);

        // Advance to active
        t = t.addTime(REPEATING.waitDuration());
        advance = ga.execute(t);
        assertEquals("Active actvity", ActivityState.ACTIVE, ga.getState());
        assertEquals("Active duration", REPEATING.activityDuration(), advance);

        // Advance to end
        t = t.addTime(REPEATING.activityDuration());
        advance = ga.execute(t);
        assertEquals("Active actvity", ActivityState.SCHEDULED, ga.getState());
        assertEquals("Activity no event", REPEATING.solFrequency() * 1000, advance);
        assertNull("Released meeting place", ga.getMeetingPlace());
    }

    public void testInitialEvent() {
        // Simplest first with zero time and no offset
        var t = new MarsTime(1, 1, 1, 0, 1);
        testStartEvent("Standard @ 0", ONE, t, new Coordinates("0.0 N", "0.0 E"));

        t = new MarsTime(1, 1, 1, 500, 1);
        testStartEvent("Standard @ 500", ONE, t, new Coordinates("0.0 N", "0.0 E"));
        testStartEvent("Quarter @ 500", ONE, t, new Coordinates("0.0 N", "90.0 E"));
        testStartEvent("Thirds @ 500", ONE, t, new Coordinates("0.0 N", "270.0 E"));

    }

    private void testStartEvent(String message, GroupActivityInfo info, MarsTime t, Coordinates locn) {
        var s = buildSettlement();
        s.setCoordinates(locn); // THis will mov ethe local timezine by 250
        var offset = MarsSurface.getTimeOffset(locn);

        new GroupActivity(info, s, t);
        var fm = s.getFutureManager();
        List<ScheduledEvent> matched = fm.getEvents().stream()
                                    .filter(ev -> ev.getHandler() instanceof GroupActivity)
                                    .toList();
        assertEquals("Expected events - " + message, 1, matched.size());
        var event = matched.get(0);
        assertEquals("Scheduled start of event - "+ message, (info.scheduledStart() + offset) % 1000,
                                event.getWhen().getMillisolInt());
        double toEvent = event.getWhen().getTimeDiff(t);
        assertTrue("Scheduled start in future - " + message, toEvent >= 0D);
        assertTrue("Scheduled start within 1 sol - " + message, toEvent < 1000D);
    }
}