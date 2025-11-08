/*
 * Mars Simulation Project
 * PersonConfigTest.java
 * @date 2023-07-23
 * @author Barry Evans
 */

package com.mars_sim.core.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.training.TrainingType;

public class PersonConfigTest {

    private PersonConfig pc;

    @BeforeEach
    public void setUp() {
        SimulationConfig config = SimulationConfig.loadConfig();
        pc = config.getPersonConfig();
    }
    
    /**
     * This test is based on the values defined in PersonConfig or people.xml.
     */
    @Test
    public void testGetTrainingModifier() {


        // Test know values
        assertEquals(5,
                        pc.getTrainingModifier(RoleType.COMPUTING_SPECIALIST, TrainingType.ANTARCTICA_EDEN_ISS), "Case 1");
        assertEquals(1,
                        pc.getTrainingModifier(RoleType.SCIENCE_SPECIALIST, TrainingType.AIRBORNE_AND_RANGER_SCHOOL), "Case 2"); 

        // test mission values
        assertEquals(0,
                        pc.getTrainingModifier(RoleType.AGRICULTURE_SPECIALIST, TrainingType.MILITARY_DEPLOYMENT), "Missing 1"); 

    }

    @Test
    public void testGetValues() {
        assertTrue(pc.getBaseCapacity() > 0, "Base capacity set");
        assertTrue(pc.getCO2ExpelledRate() > 0, "CO2 explled set");

    }

    @Test
    public void testGetDefaultPhysicalChars() {
        var d = pc.getDefaultPhysicalChars();

        assertNotNull(d, "Default physical charactertics");
        assertTrue(d.getAverageHeight() > 0, "Default height");
        assertTrue(d.getAverageWeight() > 0, "Default weight");

    }
}
