package com.mars_sim.core.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.TestEntityListener;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.test.MarsSimUnitTest;

class BatteryTest extends MarsSimUnitTest {

    @Test
    void testEventFire() {
        var s = buildSettlement("Test");
        var a = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var r = buildRobot("Robby", s, RobotType.GARDENBOT, a, null);

        var b = r.getSystemCondition().getBattery();

        var listener = new TestEntityListener(Battery.BATTERY_EVENT);
        r.addEntityListener(listener);

        var origStatus = b.getBatteryStatus();
        b.consumeEnergy(0.01, 0.1);
        b.timePassing(createPulse(10));
        assertEquals(origStatus, b.getBatteryStatus(), "Battery status should not have changed yet");
        assertEquals(0, listener.getEventsReceived(), "No events should have been fired yet");

        // Force  big change
        var energy = b.getStoredEnergy();
        b.consumeEnergy(energy * 0.5, 0.1);
        b.timePassing(createPulse(10));
        assertEquals(1, listener.getEventsReceived(), "One event should have been fired");
    }
}
