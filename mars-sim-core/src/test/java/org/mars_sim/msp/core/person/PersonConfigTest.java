package org.mars_sim.msp.core.person;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.training.TrainingType;

import junit.framework.TestCase;

public class PersonConfigTest extends TestCase {
    /**
     * Not ethis test is based on the values defiend in person.xml
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
