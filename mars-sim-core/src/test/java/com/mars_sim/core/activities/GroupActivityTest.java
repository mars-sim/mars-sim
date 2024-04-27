package com.mars_sim.core.activities;


import java.util.List;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.activities.GroupActivity.ActivityState;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.events.ScheduledEventManager.ScheduledEvent;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.structure.GroupActivityType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingCategory;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.mapdata.location.LocalPosition;

public class GroupActivityTest extends AbstractMarsSimUnitTest {

    private final static GroupActivityInfo REPEATING = new GroupActivityInfo("Repeat", 250, 0, 10, 50, 2, 0.5D, 100,
                                                            TaskScope.ANY_HOUR, BuildingCategory.LIVING,
                                                            GroupActivityInfo.DEFAULT_IMPACT);
    private final static GroupActivityInfo ONE = new GroupActivityInfo("One", 800, 1, 10, 50, 0, 0.5D, 100,
                                                            TaskScope.NONWORK_HOUR, BuildingCategory.LIVING,
                                                            GroupActivityInfo.DEFAULT_IMPACT);

    public void testOneOffCycle() {
        var s = buildSettlement();
        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH, 0);

        var t = new MarsTime(1, 1, 1, 0, 1);

        var ga = new GroupActivity(ONE, s, t);
        assertEquals("Scheduled actvity", ActivityState.SCHEDULED, ga.getState());
        assertEquals("First meeting", t.addTime(ONE.startTime() + (1000 * ONE.firstSol())), ga.getStartTime());

        // Advance to pending
        t = t.addTime(ONE.startTime());
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
        t = t.addTime(REPEATING.startTime());
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

    private void testStartEvent(String message, GroupActivityInfo info, MarsTime now, Coordinates locn) {
        var s = buildSettlement();
        s.setCoordinates(locn); // THis will mov ethe local timezine by 250

        new GroupActivity(info, s, now);
        testFutureEvent(message, s, info, now, 1, (int)((info.firstSol() + 1) * 1000D));
    }

    private GroupActivity testFutureEvent(String message, Settlement s, GroupActivityInfo info,
                                MarsTime now, int minDuration, int maxDuration) {
        var offset = MarsSurface.getTimeOffset(s.getCoordinates());

        var fm = s.getFutureManager();
        List<ScheduledEvent> matched = fm.getEvents().stream()
                                    .filter(ev -> ev.getHandler() instanceof GroupActivity)
                                    .filter(ga -> ((GroupActivity)ga.getHandler()).getDefinition().equals(info))
                                    .toList();
        assertEquals("Expected events - " + message, 1, matched.size());
        var event = matched.get(0);
        assertEquals("Scheduled start of event - "+ message, (info.startTime() + offset) % 1000,
                                event.getWhen().getMillisolInt());
        int toEvent = (int) event.getWhen().getTimeDiff(now);
        assertGreaterThan("Scheduled start more than minimum duation - " + message, minDuration, toEvent);
        assertLessThan("Scheduled start less than max duration - " + message, maxDuration, toEvent);

        return (GroupActivity)matched.get(0).getHandler();
    }

    public void testCreateSpecialActivity() {
        var s = buildSettlement();
        var now = sim.getMasterClock().getMarsTime();

        // Need to check the template
        var sConfig = simConfig.getSettlementConfiguration();
        var template = sConfig.getItem(s.getTemplate());
        var schedued = sConfig.getActivityByPopulation(template.getDefaultPopulation());
        var expected = schedued.specials().get(GroupActivityType.ANNOUNCEMENT);
        assertNotNull("Mock settlement supports Announcements", expected);

        var ga = GroupActivity.createPersonActivity("Promotion", GroupActivityType.ANNOUNCEMENT, s, null, 4,
                                now);

        assertNotNull("Announcement activity created", ga);
        assertNull("Announcement no instigator", ga.getInstigator());
        assertEquals("Announcement definition", expected, ga.getDefinition());

        // Check the time
        testFutureEvent("Announcement event", s, ga.getDefinition(), now, 4000, 5000);
    }

    public void testCreatePersonBirthday() {
        var s = buildSettlement();
        var p = buildPerson("Birthday", s);
        var now = sim.getMasterClock().getMarsTime();

        // Need to check the template
        var sConfig = simConfig.getSettlementConfiguration();
        var template = sConfig.getItem(s.getTemplate());
        var schedued = sConfig.getActivityByPopulation(template.getDefaultPopulation());
        var birthday = schedued.specials().get(GroupActivityType.BIRTHDAY);
  
        // Check the time of the party is within an earth year
        var ga = testFutureEvent("Birthday event", s, birthday, now,
                            1, (int)(MarsTime.SOLS_PER_EARTHDAY*365*1000));

        assertEquals("Birthday for person", p, ga.getInstigator());
        assertEquals("Birth definition", birthday, ga.getDefinition());

    }
}