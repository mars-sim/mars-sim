package com.mars_sim.core.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.TestEntityListener;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.test.MarsSimUnitTest;

class SystemConditionTest extends MarsSimUnitTest{
    @Test
    void testPerformanceEvent() {
        var s = buildSettlement("Test");
        var rs = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var r = buildRobot("robby", s, RobotType.CHEFBOT, rs, null);
        r.initialize();
        var sc = r.getSystemCondition();

        var listener = new TestEntityListener(SystemCondition.PERFORMANCE_EVENT);
        r.addEntityListener(listener);
        sc.tuneUpPerformance(10);  // Max out performance
        sc.timePassing(createPulse(1)); // Short pulse to reduce performance
        assertEquals(0, listener.getEventsReceived(), "No performance event should be fired");

        // Now reduce performance enough to trigger a performance event
        var origLevel = sc.getPerformanceLevel();
        sc.timePassing(createPulse(1)); // Short pulse to reduce performance
        assertEquals(0, listener.getEventsReceived(), "No performance event should be fired 2nd");
        assertEquals(origLevel, sc.getPerformanceLevel(), "Performance level should not have changed yet");

        // Create Fatigue in parts for performance to drop below threshold
        for(var ms : r.getMalfunctionManager().getMaintenanceScopeCollection()) {
            ms.addFatigue(100);
        }

        sc.timePassing(createPulse(180)); // Long pulse to reduce performance
        assertEquals(1, listener.getEventsReceived(), "Performance event should be fired");
        assertNotEquals(origLevel, sc.getPerformanceLevel(), "Performance level should have changed");
    }
}
