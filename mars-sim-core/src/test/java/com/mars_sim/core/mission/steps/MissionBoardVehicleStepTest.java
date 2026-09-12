package com.mars_sim.core.mission.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.mission.MissionTestHelper;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.test.MarsSimUnitTest;

class MissionBoardVehicleStepTest extends MarsSimUnitTest {

    @Test
    void testNoEjectLeader() {
        var s = buildSettlement("Test 1");

        var r = MissionTestHelper.buildRoster(getContext(), s, 2, EXPLORER_ROVER, false);

		var project = new MissionVehicleProject(null, MissionType.VISIT_LANDMARK, 10, r);
        var st = new MissionBoardVehicleStep(project);
        project.addStep(st);
        project.addStep(new MissionDisembarkStep(project));

        var w1 = r.members().get(0);
        var w2 = r.members().get(1);
        var l = r.leader();
        assertEquals(w1.getMission(), project, "Worker should be assigned to the mission");
        assertEquals(w2.getMission(), project, "Worker should be assigned to the mission");
        assertEquals(l.getMission(), project, "Leader should be assigned to the mission");

        // Put workers on vehicle
        w2.transfer(r.vehicle());
        w1.transfer(r.vehicle());
        
        project.execute(l);

        // Push time to simulate the leader missing mission
        var clock = getSim().getMasterClock();
        var startTime = clock.getMarsTime().addTime(MissionBoardVehicleStep.DEPARTURE_DURATION + 10);
        clock.setMarsTime(startTime);

        // Simualte ejection
        project.execute(l);
        assertFalse(st.isCompleted(), "Boarding step should not be completed after ejection");
        assertNotNull(l.getMission(), "Leader should still be assigned to the mission");
        assertEquals(3, project.getMembers().size(), "Mission should have only one member after ejection");
    }

    @Test
    void testEjectWorker() {
        var s = buildSettlement("Test 1");

        var r = MissionTestHelper.buildRoster(getContext(), s, 2, EXPLORER_ROVER, false);

		var project = new MissionVehicleProject(null, MissionType.VISIT_LANDMARK, 10, r);
        var st = new MissionBoardVehicleStep(project);
        project.addStep(st);
        project.addStep(new MissionDisembarkStep(project));

        var w1 = r.members().get(0);
        var w2 = r.members().get(1);
        var l = r.leader();
        assertEquals(w1.getMission(), project, "Worker should be assigned to the mission");
        assertEquals(w2.getMission(), project, "Worker should be assigned to the mission");
        assertEquals(l.getMission(), project, "Leader should be assigned to the mission");

        // Put leader & 1 worker on vehicle
        l.transfer(r.vehicle());
        w1.transfer(r.vehicle());
        
        project.execute(l);

        // Push time to simulate the worker missing mission
        var clock = getSim().getMasterClock();
        var startTime = clock.getMarsTime().addTime(MissionBoardVehicleStep.DEPARTURE_DURATION + 10);
        clock.setMarsTime(startTime);

        // Simualte ejection
        project.execute(l);
        assertTrue(st.isCompleted(), "Boarding step should be completed after ejection");
        assertNull(w2.getMission(), "Worker should no longer be assigned to the mission");
        assertEquals(2, project.getMembers().size(), "Mission should have only one member after ejection");
    }

    @Test
    void testAllBoard() {
        var s = buildSettlement("Test 1");

        var r = MissionTestHelper.buildRoster(getContext(), s, 2, EXPLORER_ROVER, false);

		var project = new MissionVehicleProject(null, MissionType.VISIT_LANDMARK, 10, r);
        var st = new MissionBoardVehicleStep(project);
        project.addStep(st);
        project.addStep(new MissionDisembarkStep(project));

        var w1 = r.members().get(0);
        var w2 = r.members().get(1);
        var l = r.leader();
        assertEquals(w1.getMission(), project, "Worker should be assigned to the mission");
        assertEquals(w2.getMission(), project, "Worker should be assigned to the mission");
        assertEquals(l.getMission(), project, "Leader should be assigned to the mission");

        // Put leader & 1 worker on vehicle
        l.transfer(r.vehicle());
        w1.transfer(r.vehicle());
        project.execute(l);
        assertFalse(st.isCompleted(), "Boarding step should not be completed when all members are on board");

        // w2 boards
        w2.transfer(r.vehicle());
        project.execute(l);
        
        assertEquals(3, project.getMembers().size(), "Mission should have all members after everyone boards");
        assertTrue(st.isCompleted(), "Boarding step should be completed after everyone boards");
    }
}
