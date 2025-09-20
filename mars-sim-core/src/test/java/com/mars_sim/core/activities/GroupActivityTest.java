package com.mars_sim.core.activities;


import java.util.List;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.activities.GroupActivity.ActivityState;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.events.ScheduledEventManager.ScheduledEvent;
import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.structure.GroupActivityType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.EventSchedule;
import com.mars_sim.core.time.MarsTime;

public class GroupActivityTest extends AbstractMarsSimUnitTest {

    private static final GroupActivityInfo REPEATING = new GroupActivityInfo("Repeat", 10, 50,
                                                            new EventSchedule(0, 2, 250),
                                                            0.5D, 100,
                                                            TaskScope.ANY_HOUR, BuildingCategory.LIVING,
                                                            GroupActivityInfo.DEFAULT_IMPACT);
    private static final GroupActivityInfo ONE = new GroupActivityInfo("One", 10, 50, new EventSchedule(1, 0, 800),
                                                            0.5D, 100,
                                                            TaskScope.NONWORK_HOUR, BuildingCategory.LIVING,
                                                            GroupActivityInfo.DEFAULT_IMPACT);

    public void testOneOffCycle() {
        var s = buildSettlement();
        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH, 0);

        var t = new MarsTime(1, 1, 1, 0, 1);

        var ga = new GroupActivity(ONE, s, t);
        assertEquals("Scheduled actvity", ActivityState.SCHEDULED, ga.getState());
        assertEquals("First meeting", t.addTime(ONE.calendar().getTimeOfDay() + (1000 * ONE.calendar().getFirstSol())), ga.getStartTime());

        // Advance to pending
        t = t.addTime(ONE.calendar().getTimeOfDay());
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
        t = t.addTime(REPEATING.calendar().getTimeOfDay());
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
        assertEquals("Activity no event", REPEATING.calendar().getFrequency() * 1000, advance);
        assertNull("Released meeting place", ga.getMeetingPlace());
    }

    public void testInitialEvent() throws CoordinatesException {
        // Simplest first with zero time and no offset
        var now = new MarsTime(1, 1, 1, 0, 1);
        var locn = CoordinatesFormat.fromString("0.0 90.0");

        var s = buildSettlement("East", false, locn);

        new GroupActivity(ONE, s, now);
        testFutureEvent("Initial Event", s, ONE, now, 1, (int)((ONE.calendar().getFirstSol() + 1) * 1000D));
    }

    private GroupActivity testFutureEvent(String message, Settlement s, GroupActivityInfo info,
                                MarsTime now, int minDuration, int maxDuration) {
        var offset = s.getTimeZone().getMSolOffset();

        var fm = s.getFutureManager();
        List<ScheduledEvent> matched = fm.getEvents().stream()
                                    .filter(ev -> ev.getHandler() instanceof GroupActivity)
                                    .filter(ga -> ((GroupActivity)ga.getHandler()).getDefinition().equals(info))
                                    .toList();
        assertEquals("Expected events - " + message, 1, matched.size());
        var event = matched.get(0);
        assertEquals("Scheduled start of event - "+ message, (info.calendar().getTimeOfDay() + offset) % 1000,
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
        var template = simConfig.getSettlementTemplateConfiguration().getItem(s.getTemplate());
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
        var template = simConfig.getSettlementTemplateConfiguration().getItem(s.getTemplate());
        var schedued = sConfig.getActivityByPopulation(template.getDefaultPopulation());
        var birthday = schedued.specials().get(GroupActivityType.BIRTHDAY);
  
        // Check the time of the party is within an earth year
        var ga = testFutureEvent("Birthday event", s, birthday, now,
                            1, (int)(MarsTime.SOLS_PER_EARTHDAY*365*1000));

        assertEquals("Birthday for person", p, ga.getInstigator());
        assertEquals("Birth definition", birthday, ga.getDefinition());

    }
}