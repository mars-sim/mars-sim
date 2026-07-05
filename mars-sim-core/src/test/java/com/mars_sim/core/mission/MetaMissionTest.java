package com.mars_sim.core.mission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.test.MarsSimUnitTest;

class MetaMissionTest extends MarsSimUnitTest {

    @Test
    @DisplayName("Test leader suitability based on job type")
    void testLeaderJob() {
        var s = buildSettlement("test");
        var b = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var l = buildPerson("leader", s, JobType.TRADER, b, null);
        var w = buildPerson("worker", s, JobType.CHEF, b, null);

        var meta = new MockMetaMission(2, 3, Set.of(JobType.TRADER), Set.of(JobType.PILOT));

        var lScore = meta.getLeaderSuitability(l);
        var wScore = meta.getLeaderSuitability(w);

        assertTrue(lScore > wScore, "Leader suitability score for leader should be higher than worker");
    }

    @Test
    @DisplayName("Test leader suitability based on leadership attribute")
    void testLeadership() {
        var s = buildSettlement("test");
        var b = buildAccommodation(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0);
        var l = buildPerson("leader", s, JobType.TRADER, b, null);
        l.getNaturalAttributeManager().setAttribute(NaturalAttributeType.LEADERSHIP, 20);
        var sl = buildPerson("super leader", s, JobType.TRADER, b, null);
        sl.getNaturalAttributeManager().setAttribute(NaturalAttributeType.LEADERSHIP, 40);

        var meta = new MockMetaMission(2, 3, Set.of(JobType.TRADER), Set.of(JobType.PILOT));

        var lScore = meta.getLeaderSuitability(l);
        var slScore = meta.getLeaderSuitability(sl);

        assertTrue(slScore > lScore, "Leader suitability score for super leader should be higher than leader");
    }

    @Test
    @DisplayName("Test max missions increase with population growth")
    void testMaxMissionsByPopulationGrowth() {
        int populationThreshold = 4;
        int populationRatio = 3;
        var settlement = buildSettlement("test");

        var meta = new MockMetaMission(2, 3, Set.of(JobType.TRADER), Set.of(JobType.PILOT),
                        populationRatio, populationThreshold);

        int personCounter = 1;
        while (settlement.getNumCitizens() < (populationThreshold + 1)) {
            buildPerson("citizen-" + personCounter++, settlement);
        }

        int citizens = settlement.getNumCitizens();
        assertTrue(citizens > populationThreshold, "Settlement population should be over the threshold");
        assertEquals(1, meta.getMaxMissions(citizens),
                        "One Person over threshold should allow one concurrent missions");

        for (int expectedMissions = 2; expectedMissions <= 3; expectedMissions++) {
            for (int i = 0; i < populationRatio; i++) {
                buildPerson("citizen-" + personCounter++, settlement);
            }
            assertEquals(expectedMissions, meta.getMaxMissions(settlement.getNumCitizens()),
                            "Max missions should increase after each additional population ratio block");
        }
    }

}