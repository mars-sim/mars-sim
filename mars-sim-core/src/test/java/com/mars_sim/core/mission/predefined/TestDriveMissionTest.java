/**
 * Mars Simulation Project
 * TestDriveMissionTest.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package com.mars_sim.core.mission.predefined;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;


import java.util.Collection;
import java.util.Map;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.mission.MissionProject;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.task.LoadingController;

public class TestDriveMissionTest extends MarsSimUnitTest {
    /**
     *
     */
    private static final String MISSION_NAME = "test-mission";

    @Test
    public void testCreation() {
        Settlement home = buildSettlement("mock");
        buildGarage(home.getBuildingManager(), new LocalPosition(0,0), BUILDING_LENGTH, 1);
        buildRover(home, "Rover 1", null);
        Person leader = buildPerson("Leader", home);
        for(int i = 0; i < 1 + MissionProject.MIN_POP; i++) {
            buildPerson("Support" + i, home);
        }
        MissionVehicleProject mp = new TestDriveMission(MISSION_NAME, leader);

        // Check vehicle details
        assertFalse(mp.isDone(), "Mission active");
        Vehicle assigned = mp.getVehicle();
        assertNotNull(assigned, "Assign Vehicle");
        assertEquals(mp, assigned.getMission(), "Vehicle mission");

        // Check route
        assertEquals(2, mp.getNavpoints().size(), "Mission navpoints");
        assertEquals(TestDriveMission.TRAVEL_DIST * 2, mp.getTotalDistanceProposed(), 0.01D, "Mission distance");

        // Check Members
        Collection<Worker> members = mp.getMembers();
        assertEquals(2, members.size(), "Mission members");
        assertTrue(members.contains(leader), "Member " + leader.getName());

        // Run to the loading stage
        assertTrue(executeMission(leader, assigned, mp, 10), "Initial stage completed");

        // Check the plan has content
        LoadingController plan = assigned.getLoadingPlan();
        assertNotNull(plan, "Loading plan created");
        Map<Integer, Double> resources = plan.getAmountManifest(true);
        assertTrue(resources.get(ResourceUtil.OXYGEN_ID).doubleValue() > 0D, "Plan has Oxygen");
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
        ClockPulse pulse = createPulse(1, 1, false, false);
        while(starting.equals(mp.getPhaseDescription()) && (count < maxSteps)) {
            pulse = pulse.addElapsed(0.1D); // Must use a big pulse
            
            leader.timePassing(pulse);
            v.timePassing(pulse);

            count++;
        }
        return (count != maxSteps);
    }
}
