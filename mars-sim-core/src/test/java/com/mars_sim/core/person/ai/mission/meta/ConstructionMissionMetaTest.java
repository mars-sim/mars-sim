package com.mars_sim.core.person.ai.mission.meta;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.structure.Settlement;

public class ConstructionMissionMetaTest extends MarsSimUnitTest {
    private static final String LANDER_HAB = "Lander Hab";

    @Test
    public void testProbabiltyArchitectSite() {
        var s = buildSettlement("mock");
        var architect = buildPerson("worker", s, JobType.ARCHITECT);
        architect.setRole(RoleType.ENGINEERING_SPECIALIST);
        for(int i = 0; i < ConstructionMission.MIN_PEOPLE; i++) {
            buildPerson("P" + i, s);
        }

        var meta = new ConstructionMissionMeta();

        var score = meta.getProbability(architect);
        assertEquals(0D, score.getScore(), "No probability");

        var cm = s.getConstructionManager();
        var landerHab = getConfig().getBuildingConfiguration().getBuildingSpec(LANDER_HAB);
        cm.createNewBuildingSite(landerHab);

        score = meta.getProbability(architect);
        assertEquals(0D, score.getScore(), "No equipment");

        addSitePreReqs(s);

        score = meta.getProbability(architect);
        assertTrue(score.getScore() >= 1, "Architect can start");
    }

    @Test
    public void testProbabiltySalvageSite() {
        var s = buildSettlement("mock");
        var a = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);

        var architect = buildPerson("worker", s, JobType.ARCHITECT);
        architect.setRole(RoleType.ENGINEERING_SPECIALIST);
        for(int i = 0; i < ConstructionMission.MIN_PEOPLE; i++) {
            buildPerson("P" + i, s);
        }

        var meta = new ConstructionMissionMeta();

        var cm = s.getConstructionManager();
        cm.createNewSalvageConstructionSite(a);
        addSitePreReqs(s);

        var score = meta.getProbability(architect);
        assertTrue(score.getScore() >= 1, "Architect can start");
    }

    @Test
    public void testProbabiltyDoctor() {
        var s = buildSettlement("mock");
        var doctor = buildPerson("worker", s, JobType.DOCTOR);
        doctor.setRole(RoleType.AGRICULTURE_SPECIALIST);
        for(int i = 0; i < ConstructionMission.MIN_PEOPLE; i++) {
            buildPerson("P" + i, s);
        }

        var meta = new ConstructionMissionMeta();
        var cm = s.getConstructionManager();
        var landerHab = getConfig().getBuildingConfiguration().getBuildingSpec(LANDER_HAB);
        cm.createNewBuildingSite(landerHab);

        addSitePreReqs(s);

        var score = meta.getProbability(doctor);
        assertGreaterThan("Doctor still start but rating is low", 0D, score.getScore());
    }

    private void addSitePreReqs(Settlement s) {
        buildLUV(s, "luv", null);
        for (int e = 0; e < ConstructionMission.MIN_PEOPLE+1; e++) {
            EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, s);
        }
    }
    
    @Test
    public void testProbabiltyArchitectQueue() {
        var s = buildSettlement("mock");
        var architect = buildPerson("worker", s, JobType.ARCHITECT);
        architect.setRole(RoleType.ENGINEERING_SPECIALIST);
        for(int i = 0; i < ConstructionMission.MIN_PEOPLE; i++) {
            buildPerson("P" + i, s);
        }

        var meta = new ConstructionMissionMeta();

        var score = meta.getProbability(architect);
        assertEquals(0D, score.getScore(), "No probability");

        addSitePreReqs(s);

        // Add a queue should trigger the demand for a mission
        var cm = s.getConstructionManager();
        cm.addBuildingToQueue(LANDER_HAB, null);

        score = meta.getProbability(architect);
        assertTrue(score.getScore() >= 1, "Architect can start");
    }
}
