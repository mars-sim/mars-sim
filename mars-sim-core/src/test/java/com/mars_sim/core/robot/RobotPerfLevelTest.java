package com.mars_sim.core.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RobotPerfLevelTest {
    @Test
    void testFromValue() {
        assertEquals(RobotPerfLevel.DISABLED, RobotPerfLevel.fromValue(0.0));
        assertEquals(RobotPerfLevel.VERY_LOW, RobotPerfLevel.fromValue(0.1));
        assertEquals(RobotPerfLevel.LOW, RobotPerfLevel.fromValue(0.3));
        assertEquals(RobotPerfLevel.MEDIUM_LOW, RobotPerfLevel.fromValue(0.5));
        assertEquals(RobotPerfLevel.MEDIUM, RobotPerfLevel.fromValue(0.7));
        assertEquals(RobotPerfLevel.HIGH, RobotPerfLevel.fromValue(0.85));
        assertEquals(RobotPerfLevel.PEAK, RobotPerfLevel.fromValue(1.0));
    }
}
