package com.mars_sim.core.mission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.TestEntityListener;
import com.mars_sim.core.building.construction.MockMission;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.CollectIce;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.test.MarsSimUnitTest;

class MissionControlTest extends MarsSimUnitTest{
    @Test
    void testGenerateNameSameSettlement() {
        var s = buildSettlement("Test");
        var missionControl = new MissionControl(s);

        var names1 = missionControl.generateNames(MissionType.AREOLOGY);

        var names2 = missionControl.generateNames(MissionType.AREOLOGY);

        assertNotEquals(names1.callSign(), names2.callSign(), "Call signs should be unique");
        assertNotEquals(names1.name(), names2.name(), "Names should be unique");
    }

    @Test
    void testGenerateNameDiffSettlement() {
        var s1 = buildSettlement("Test1");
        var mc1 = new MissionControl(s1);
        var names1 = mc1.generateNames(MissionType.AREOLOGY);

        var s2 = buildSettlement("Test2");
        var mc2 = new MissionControl(s2);
        var names2 = mc2.generateNames(MissionType.AREOLOGY);

        // Only call sign is globally unique
        assertNotEquals(names1.callSign(), names2.callSign(), "Call signs should be unique");
    }

    @Test
    void testReviewPassed() {
        var s = buildSettlement("Test");
        var missionControl = new MissionControl(s);

        var m = new MockMission(s);

        double passScore = 75D;
        MissionPlanning mp = new MissionPlanning(m, 1, passScore);
        mp.setStatus(PlanType.PENDING); // Simulate plan is resdy for review
        assertEquals(PlanType.PENDING, mp.getStatus(), "Mission should start in Preparing status");

        var finalScore = passScore + 10;
        mp.setScore(finalScore);
        missionControl.reviewCompleted(mp);
        assertEquals(finalScore, mp.getScore(), "Mission should be approved");
        assertEquals(PlanType.APPROVED, mp.getStatus(), "Mission should be marked as completed");

        // Check the scoring has changed
        var history = missionControl.getHistoricalMissions();
        var numApproved = history.get(1).get(PlanType.APPROVED.getName()).doubleValue();
        assertEquals(1D, numApproved, "Historical score should match final score");

        assertNull(history.get(1).get(PlanType.NOT_APPROVED.getName()), "There should be no rejected missions");
    }

   @Test
    void testReviewFailed() {
        var s = buildSettlement("Test");
        var missionControl = new MissionControl(s);

        var m = new MockMission(s);

        double passScore = 75D;
        MissionPlanning mp = new MissionPlanning(m, 1, passScore);
        mp.setStatus(PlanType.PENDING); // Simulate plan is resdy for review
        assertEquals(PlanType.PENDING, mp.getStatus(), "Mission should start in Preparing status");

        var finalScore = passScore - 10;
        mp.setScore(finalScore);
        missionControl.reviewCompleted(mp);
        assertEquals(finalScore, mp.getScore(), "Mission should be rejected");
        assertEquals(PlanType.NOT_APPROVED, mp.getStatus(), "Mission should be marked as not approved");
        assertTrue(m.isDone(), "Mission should be marked as done");

        // Check the scoring has changed
        var history = missionControl.getHistoricalMissions();
        var numNotApproved = history.get(1).get(PlanType.NOT_APPROVED.getName()).doubleValue();
        assertEquals(1D, numNotApproved, "Historical score should match final score");

        assertNull(history.get(1).get(PlanType.APPROVED.getName()), "There should be no approved missions");
    }

    
    @Test
    void testActiveCount() {
        var s1 = buildSettlement("Test");
        var mc1 = new MissionControl(s1);

        var s2 = buildSettlement("Test");
        var mc2 = new MissionControl(s2);

        var m = new MockMission(s1);
        mc1.addMission(m);

        var active = mc1.getActiveMissions();
        assertEquals(1, active.size(), "There should be one active mission");
        assertEquals(1, mc1.getAllMissions().size(), "There should be one mission");

        assertTrue(mc2.getAllMissions().isEmpty(), "There should be no missions in other settlement");
    }

    
    @Test
    void testAbortCount() {
        var s1 = buildSettlement("Test");
        var mc1 = new MissionControl(s1);

        var m = new MockMission(s1);
        mc1.addMission(m);

        var active = mc1.getActiveMissions();
        assertFalse(active.isEmpty(), "There should be one active mission");

        m.abortMission(null);
        assertTrue(mc1.getActiveMissions().isEmpty(), "There should be no active mission");
        assertEquals(1, mc1.getAllMissions().size(), "There should be one mission");
    }

    @Test
    void testEntityManagerListenerMissionAddedAndRemoved() {
        var settlement = buildSettlement("Test");
        var missionControl = new MissionControl(settlement);
        var mission = new MockMission(settlement);

        TestEntityListener listener = new TestEntityListener(MissionControl.MISSION_ADD, MissionControl.MISSION_REMOVED);

        settlement.addEntityListener(listener);

        missionControl.addMission(mission);

        assertEquals(1, listener.getEventsReceived(), "Exactly one mission added event should be fired");
        assertSame(mission, listener.getLastTarget(), "Added event should contain the added mission");
        assertEquals(MissionControl.MISSION_ADD, listener.getLastType(), "Added event should be of type MISSION_ADD");

        missionControl.removeMission(mission);
        assertEquals(2, listener.getEventsReceived(), "Exactly one mission removed event should be fired");
        assertSame(mission, listener.getLastTarget(), "Removed event should contain the removed mission");
        assertEquals(MissionControl.MISSION_REMOVED, listener.getLastType(), "Removed event should be of type MISSION_REMOVED");

        settlement.removeEntityListener(listener);

        var mission1 = new MockMission(settlement);
        missionControl.addMission(mission1);
        assertEquals(2, listener.getEventsReceived(), "No additional mission added event should be fired after listener is removed");

        missionControl.removeMission(mission1);
        assertEquals(2, listener.getEventsReceived(), "No additional mission removed event should be fired after listener is removed");
    }

    @Test
    void testNoMissionCreated() {
        var settlement = buildSettlement("Test", true, 5);
        var missionControl = new MissionControl(settlement);
        var leader = buildPerson("Leader", settlement, RoleType.MISSION_SPECIALIST, JobType.PILOT);

        // Advance 4 sols into the simulation
        var clock = getSim().getMasterClock();
        clock.setMarsTime(clock.getMarsTime().addTime(4000));

        var mission = missionControl.getNewMission(leader);
        assertNull(mission, "Mission should be created");

        var tm = leader.getMind().getTaskManager();
        assertTrue(tm.getMissionProbCache().isEmpty(), "Mission probability cache should be empty after mission creation");
        assertNull(tm.getSelectedMission(), "Selected mission should be set after mission creation");
    }

    @Test
    void testIceMissionCreated() {
        var settlement = buildSettlement("Test", true, 5);
        var missionControl = new MissionControl(settlement);
        var leader = buildPerson("Leader", settlement, RoleType.MISSION_SPECIALIST, JobType.PILOT);

        // Add workers
        for(var p = 0; p < 4; p++) {
            buildPerson("Worker" + p, settlement);
        }

        var r = buildRover(settlement, "Rover", new LocalPosition(0, 0), CARGO_ROVER);

        // Build resoruces for collection ice mission
        for(int i = 0; i < CollectIce.REQUIRED_BARRELS+1; i++) {
            EquipmentFactory.createEquipment(EquipmentType.BARREL, settlement);
        }

        // Create enough suits with spares
        for(int e = 0; e < settlement.getCitizens().size() + 2; e++) {
            EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, settlement);
        }

        Map<Integer, Double> resources = Map.of(ResourceUtil.OXYGEN_ID, 100D,
                                                ResourceUtil.FOOD_ID, 100D,
                                                r.getFuelTypeID(), 100D,
                                                ResourceUtil.WATER_ID, 100D);
        loadSettlementAmounts(settlement, resources);

        // Advance 4 sols into the simulation
        var clock = getSim().getMasterClock();
        clock.setMarsTime(clock.getMarsTime().addTime(4000));
        
        // Disable ice collection mission
        settlement.setMissionDisable(MissionType.COLLECT_ICE, true);
        var mission = missionControl.getNewMission(leader);
        assertNull(mission, "No ice mission possible");

        // Enable ice collection mission
        settlement.setMissionDisable(MissionType.COLLECT_ICE, false);
        mission = missionControl.getNewMission(leader);
        assertNotNull(mission, "Mission should be created");
        assertEquals(MissionType.COLLECT_ICE, mission.getMissionType(), "Mission type created");

        var tm = leader.getMind().getTaskManager();
        assertEquals(1, tm.getMissionProbCache().size(), "Mission probability cache should be empty after mission creation");
        assertEquals(MissionType.COLLECT_ICE, tm.getSelectedMission().getMeta().getType(), "Selected mission should be set after mission creation");
    }
}

