package com.mars_sim.core.activities;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;
import static com.mars_sim.core.test.SimulationAssertions.assertLessThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;


import java.util.List;

import com.mars_sim.core.test.MarsSimUnitTest;
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

public class GroupActivityTest extends MarsSimUnitTest {

    private static final GroupActivityInfo REPEATING = new GroupActivityInfo("Repeat", 10, 50,
                                                            new EventSchedule(0, 2, 250),
                                                            0.5D, 100,
                                                            TaskScope.ANY_HOUR, BuildingCategory.LIVING,
                                                            GroupActivityInfo.DEFAULT_IMPACT);
    private static final GroupActivityInfo ONE = new GroupActivityInfo("One", 10, 50, new EventSchedule(1, 0, 800),
                                                            0.5D, 100,
                                                            TaskScope.NONWORK_HOUR, BuildingCategory.LIVING,
                                                            GroupActivityInfo.DEFAULT_IMPACT);

    @Test
    public void testOneOffCycle() {
        var s = buildSettlement("mock");
        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH);

        var t = new MarsTime(1, 1, 1, 0, 1);

        var ga = new GroupActivity(ONE, s, t);
        assertEquals(ActivityState.SCHEDULED, ga.getState(), "Scheduled actvity");
        assertEquals(t.addTime(ONE.calendar().getTimeOfDay() + (1000 * ONE.calendar().getFirstSol())), ga.getStartTime(), "First meeting");

        // Advance to pending
        t = t.addTime(ONE.calendar().getTimeOfDay());
        int advance = ga.execute(t);
        assertEquals(ActivityState.PENDING, ga.getState(), "Pending actvity");
        assertEquals(ONE.waitDuration(), advance, "Wait duration");
        Building meeting = ga.getMeetingPlace();
        assertNotNull(meeting, "Allocated meeting place");
        assertEquals(ONE.place(), meeting.getCategory(), "Selected meeting place");

        // Advance to active
        t = t.addTime(ONE.waitDuration());
        advance = ga.execute(t);
        assertEquals(ActivityState.ACTIVE, ga.getState(), "Active actvity");
        assertEquals(ONE.activityDuration(), advance, "Active duration");

        // Advance to end
        t = t.addTime(ONE.activityDuration());
        advance = ga.execute(t);
        assertEquals(ActivityState.DONE, ga.getState(), "Active actvity");
        assertEquals(-1, advance, "Activity no event");
        meeting = ga.getMeetingPlace();
        assertNull(meeting, "Released meeting place");
    }

    @Test
    public void testRepeatCycle() {
        var s = buildSettlement("mock");
        buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, BUILDING_LENGTH);

        var t = new MarsTime(1, 1, 1, 0, 1);

        var ga = new GroupActivity(REPEATING, s, t);

        // Advance to pending
        t = t.addTime(REPEATING.calendar().getTimeOfDay());
        int advance = ga.execute(t);
        assertEquals(ActivityState.PENDING, ga.getState(), "Pending actvity");
        assertEquals(REPEATING.waitDuration(), advance, "Wait duration");

        // Advance to active
        t = t.addTime(REPEATING.waitDuration());
        advance = ga.execute(t);
        assertEquals(ActivityState.ACTIVE, ga.getState(), "Active actvity");
        assertEquals(REPEATING.activityDuration(), advance, "Active duration");

        // Advance to end
        t = t.addTime(REPEATING.activityDuration());
        advance = ga.execute(t);
        assertEquals(ActivityState.SCHEDULED, ga.getState(), "Active actvity");
        assertEquals(REPEATING.calendar().getFrequency() * 1000, advance, "Activity no event");
        assertNull(ga.getMeetingPlace(), "Released meeting place");
    }

    @Test
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
        assertEquals(1, matched.size(), "Expected events - " + message);
        var event = matched.get(0);
        assertEquals((info.calendar().getTimeOfDay() + offset) % 1000, event.getWhen().getMillisolInt(), "Scheduled start of event - "+ message);
        int toEvent = (int) event.getWhen().getTimeDiff(now);
        assertGreaterThan("Scheduled start more than minimum duation - " + message, minDuration, toEvent);
        assertLessThan("Scheduled start less than max duration - " + message, maxDuration, toEvent);

        return (GroupActivity)matched.get(0).getHandler();
    }

    @Test
    public void testCreateSpecialActivity() {
        var s = buildSettlement("mock");
        var now = getSim().getMasterClock().getMarsTime();

        // Need to check the template
        var sConfig = getConfig().getSettlementConfiguration();
        var template = getConfig().getSettlementTemplateConfiguration().getItem(s.getTemplate());
        var schedued = sConfig.getActivityByPopulation(template.getDefaultPopulation());
        var expected = schedued.specials().get(GroupActivityType.ANNOUNCEMENT);
        assertNotNull(expected, "Mock settlement supports Announcements");

        var ga = GroupActivity.createPersonActivity("Promotion", GroupActivityType.ANNOUNCEMENT, s, null, 4,
                                now);

        assertNotNull(ga, "Announcement activity created");
        assertNull(ga.getInstigator(), "Announcement no instigator");
        assertEquals(expected, ga.getDefinition(), "Announcement definition");

        // Check the time
        testFutureEvent("Announcement event", s, ga.getDefinition(), now, 4000, 5000);
    }

    @Test
    public void testCreatePersonBirthday() {
        var s = buildSettlement("mock");
        var p = buildPerson("Birthday", s);
        var now = getSim().getMasterClock().getMarsTime();

        // Need to check the template
        var sConfig = getConfig().getSettlementConfiguration();
        var template = getConfig().getSettlementTemplateConfiguration().getItem(s.getTemplate());
        var schedued = sConfig.getActivityByPopulation(template.getDefaultPopulation());
        var birthday = schedued.specials().get(GroupActivityType.BIRTHDAY);
  
        // Check the time of the party is within an earth year
        var ga = testFutureEvent("Birthday event", s, birthday, now,
                            1, (int)(MarsTime.SOLS_PER_EARTHDAY*365*1000));

        assertEquals(p, ga.getInstigator(), "Birthday for person");
        assertEquals(birthday, ga.getDefinition(), "Birth definition");

    }
}
