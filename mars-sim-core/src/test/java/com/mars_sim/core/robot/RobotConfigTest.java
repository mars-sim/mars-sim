package com.mars_sim.core.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;

class RobotConfigTest {

    private static RobotConfig robotConfig;

    @BeforeAll
    static void setUpBeforeClass() {
        var conf = SimulationConfig.loadConfig();
        robotConfig = conf.getRobotConfiguration();
    }

    @Test
    void testGetRobotSpecDefault() {
        var rs = robotConfig.getRobotSpec("MedicBot");
        assertNotNull(rs, "Default MedicBot");
        assertEquals(RobotType.MEDICBOT, rs.getRobotType(), "MedicBot type");
        assertEquals("Standard", rs.getMakeModel(), "MedicBot make/model");
    }

    @Test
    void testGetRobotSpecByName() {
        var rs = robotConfig.getRobotSpec("MakerBot-Advanced");
        assertNotNull(rs, "MakerBot-Advanced");
        assertEquals(RobotType.MAKERBOT, rs.getRobotType(), "MakerBot-Advanced type");
        assertEquals("Advanced", rs.getMakeModel(), "MakerBot-Advanced make/model");
        assertEquals("MakerBot-Advanced", rs.getName(), "MakerBot-Advanced name");
    }

    @Test
    void testGetRobotSpecs() {
        assertFalse(robotConfig.getRobotSpecs().isEmpty(), "Robot specs should not be empty");
    }
}
