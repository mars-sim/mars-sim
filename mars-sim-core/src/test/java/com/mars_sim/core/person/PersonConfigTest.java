/*
 * Mars Simulation Project
 * PersonConfigTest.java
 * @date 2023-07-23
 * @author Barry Evans
 */

package com.mars_sim.core.person;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.training.TrainingType;

import junit.framework.TestCase;

public class PersonConfigTest extends TestCase {
    /**
     * This test is based on the values defined in PersonConfig or people.xml.
     */
    public void testGetTrainingModifier() {
        SimulationConfig config = SimulationConfig.instance();
        config.loadConfig();
        PersonConfig pc = config.getPersonConfig();

        // Test know values
        assertEquals("Case 1", 5,
                        pc.getTrainingModifier(RoleType.COMPUTING_SPECIALIST, TrainingType.ANTARCTICA_EDEN_ISS));
        assertEquals("Case 2", 1,
                        pc.getTrainingModifier(RoleType.SCIENCE_SPECIALIST, TrainingType.AIRBORNE_AND_RANGER_SCHOOL)); 

        // test mission values
        assertEquals("Missing 1", 0,
                        pc.getTrainingModifier(RoleType.AGRICULTURE_SPECIALIST, TrainingType.MILITARY_DEPLOYMENT)); 

    }
}
