/**
 * Mars Simulation Project
 * TestDriveMissionTest.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission.predefined;


import java.util.Collection;
import java.util.Map;

import org.mars_sim.msp.core.AbstractMarsSimUnitTest;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.mission.MissionProject;
import org.mars_sim.msp.core.mission.MissionVehicleProject;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.LoadingController;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.vehicle.Vehicle;

public class TestDriveMissionTest extends AbstractMarsSimUnitTest {
    /**
     *
     */
    private static final String MISSION_NAME = "test-mission";

    public void testCreation() {
        Settlement home = buildSettlement();
        buildGarage(home.getBuildingManager(), new LocalPosition(0,0), BUILDING_LENGTH, 1);
        buildRover(home, "Rover 1", null);
        Person leader = buildPerson("Leader", home);
        for(int i = 0; i < 1 + MissionProject.MIN_POP; i++) {
            buildPerson("Support" + i, home);
        }
        MissionVehicleProject mp = new TestDriveMission(MISSION_NAME, leader);

        // Check vehicle details
        assertFalse("Mission active", mp.isDone());
        Vehicle assigned = mp.getVehicle();
        assertNotNull("Assign Vehicle", assigned);
        assertEquals("Vehicle mission", mp, assigned.getMission());

        // Check route
        assertEquals("Mission navpoints", 2, mp.getNavpoints().size());
        assertEquals("Mission distance", TestDriveMission.TRAVEL_DIST * 2, mp.getDistanceProposed(),
                                    0.01D);

        // Check Members
        Collection<Worker> members = mp.getMembers();
        assertEquals("Mission members", 2, members.size());
        assertTrue("Member " + leader.getName(), members.contains(leader));

        // Run to the loading stage
        assertTrue("Initial stage completed", executeMission(leader, assigned, mp, 10));

        // Check the plan has content
        LoadingController plan = mp.getLoadingPlan();
        assertNotNull("Loading plan created", plan);
        Map<Integer, Number> resources = plan.getResourcesManifest();
        assertTrue("Plan has Oxygen", resources.get(ResourceUtil.oxygenID).doubleValue() > 0D);
    }

    /**
     * Apply the time pulses to a Settlement until the mission phase changes.
     * @param leader The leader to do the work
     * @param v Vehicle to simulate
     * @param mp Mission being checked
     * @param maxSteps Maximum steps
     * @return True is the phase changed
     */
    private boolean executeMission(Person leader, Vehicle v, MissionProject mp, int maxSteps) {
        String starting = mp.getPhaseDescription();
        // Leader execute once to get next phase started
        mp.performMission(leader);

        int count = 0;
        ClockPulse pulse = createPulse(1, 1, false);
        while(starting.equals(mp.getPhaseDescription()) && (count < maxSteps)) {
            pulse = pulse.addElapsed(0.1D); // Must use a big pulse
            
            leader.timePassing(pulse);
            v.timePassing(pulse);

            count++;
        }
        return (count != maxSteps);
    }
}
