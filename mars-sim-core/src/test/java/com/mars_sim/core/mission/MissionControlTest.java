package com.mars_sim.core.mission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.building.construction.MockMission;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.PlanType;
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
        var numApproved = history.get(1).get(PlanType.APPROVED.name()).doubleValue();
        assertEquals(1D, numApproved, "Historical score should match final score");

        assertNull(history.get(1).get(PlanType.NOT_APPROVED.name()), "There should be no rejected missions");
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
        var numNotApproved = history.get(1).get(PlanType.NOT_APPROVED.name()).doubleValue();
        assertEquals(1D, numNotApproved, "Historical score should match final score");

        assertNull(history.get(1).get(PlanType.APPROVED.name()), "There should be no approved missions");
    }

    
    @Test
    void testActiveCount() {
        var s1 = buildSettlement("Test");
        var mc1 = new MissionControl(s1);

        var s2 = buildSettlement("Test");
        var mc2 = new MissionControl(s2);

        var m = new MockMission(s1);

        getSim().getMissionManager().addMission(m);

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

        getSim().getMissionManager().addMission(m);

        var active = mc1.getActiveMissions();
        assertFalse(active.isEmpty(), "There should be one active mission");

        m.abortMission(null);
        assertTrue(mc1.getActiveMissions().isEmpty(), "There should be no active mission");
        assertEquals(1, mc1.getAllMissions().size(), "There should be one mission");
    }
}

