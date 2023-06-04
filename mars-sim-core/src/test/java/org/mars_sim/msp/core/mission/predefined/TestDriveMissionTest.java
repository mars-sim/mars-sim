/**
 * Mars Simulation Project
 * TestDriveMissionTest.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission.predefined;


import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.mission.MissionVehicleProject;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

public class TestDriveMissionTest extends AbstractMarsSimUnitTest {
    /**
     *
     */
    private static final String MISSION_NAME = "test-mission";

    public void testCreation() {
        Settlement home = buildSettlement();
        buildRover(home, "Rover 1", null);
        Person leader = buildPerson("Leader", home);
        MissionVehicleProject mp = new TestDriveMission(MISSION_NAME, leader);

        // Check vehicle details
        Vehicle assigned = mp.getVehicle();
        assertNotNull("Assign Vehicle", assigned);
        assertEquals("Vehicle mission", mp, assigned.getMission());

        // Check route
        assertEquals("Mission navpoints", 2, mp.getNavpoints().size());
        assertEquals("Mission distance", TestDriveMission.TRAVEL_DIST * 2, mp.getDistanceProposed(),
                                    0.01D);
    }
}
