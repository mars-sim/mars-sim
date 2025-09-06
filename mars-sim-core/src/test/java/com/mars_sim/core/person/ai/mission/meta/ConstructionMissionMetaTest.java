package com.mars_sim.core.person.ai.mission.meta;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.structure.Settlement;

public class ConstructionMissionMetaTest extends AbstractMarsSimUnitTest {
    private static final String LANDER_HAB = "Lander Hab";

    public void testProbabiltyArchitectSite() {
        var s = buildSettlement();
        var architect = buildPerson("worker", s, JobType.ARCHITECT);
        for(int i = 0; i < ConstructionMission.MIN_PEOPLE; i++) {
            buildPerson("P" + i, s);
        }

        var meta = new ConstructionMissionMeta();

        var score = meta.getProbability(architect);
        assertEquals("No probability", 0D, score.getScore());

        var cm = s.getConstructionManager();
        var landerHab = getConfig().getBuildingConfiguration().getBuildingSpec(LANDER_HAB);
        cm.createNewBuildingSite(landerHab);

        score = meta.getProbability(architect);
        assertEquals("No equipment", 0D, score.getScore());

        addSitePreReqs(s);

        score = meta.getProbability(architect);
        assertTrue("Architect can start", score.getScore() >= 1);
    }

    public void testProbabiltySalvageSite() {
        var s = buildSettlement();
        var a = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);

        var architect = buildPerson("worker", s, JobType.ARCHITECT);
        for(int i = 0; i < ConstructionMission.MIN_PEOPLE; i++) {
            buildPerson("P" + i, s);
        }

        var meta = new ConstructionMissionMeta();

        var cm = s.getConstructionManager();
        cm.createNewSalvageConstructionSite(a);
        addSitePreReqs(s);

        var score = meta.getProbability(architect);
        assertTrue("Architect can start", score.getScore() >= 1);
    }

    public void testProbabiltyDoctor() {
        var s = buildSettlement();
        var doctor = buildPerson("worker", s, JobType.DOCTOR);
        for(int i = 0; i < ConstructionMission.MIN_PEOPLE; i++) {
            buildPerson("P" + i, s);
        }

        var meta = new ConstructionMissionMeta();
        var cm = s.getConstructionManager();
        var landerHab = getConfig().getBuildingConfiguration().getBuildingSpec(LANDER_HAB);
        cm.createNewBuildingSite(landerHab);

        addSitePreReqs(s);

        var score = meta.getProbability(doctor);
        assertEquals("Doctor cannot start", 0D, score.getScore());
    }

    private void addSitePreReqs(Settlement s) {
        buildLUV(s, "luv", null);
        for (int e = 0; e < ConstructionMission.MIN_PEOPLE+1; e++) {
            EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, s);
        }
    }
    
    public void testProbabiltyArchitectQueue() {
        var s = buildSettlement();
        var architect = buildPerson("worker", s, JobType.ARCHITECT);
        for(int i = 0; i < ConstructionMission.MIN_PEOPLE; i++) {
            buildPerson("P" + i, s);
        }

        var meta = new ConstructionMissionMeta();

        var score = meta.getProbability(architect);
        assertEquals("No probability", 0D, score.getScore());

        addSitePreReqs(s);

        // Add a queue should trigger the demand for a mission
        var cm = s.getConstructionManager();
        cm.addBuildingToQueue(LANDER_HAB, null);

        score = meta.getProbability(architect);
        assertTrue("Architect can start", score.getScore() >= 1);
    }
}
