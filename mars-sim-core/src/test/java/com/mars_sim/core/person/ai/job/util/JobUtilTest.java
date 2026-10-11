/*
 * Mars Simulation Project
 * JobUtilTest.java
 * @date 2026-10-08
 * @author Branislav Blicha
 */
package com.mars_sim.core.person.ai.job.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

public class JobUtilTest extends MarsSimUnitTest {

    private static final int POPULATION = 48;

    private void populate(Settlement home, int population) {
        for (int i = 0; i < population; i++) {
            // Building a person assigns a new job
            Person.create("Settler " + i, home, GenderType.MALE).build();
        }
    }

    @Test
    void testJobsFollowSettlementNeed() {
        var home = buildSettlement("mock", POPULATION);
        populate(home, POPULATION);

        int engineers = JobUtil.numJobs(JobType.ENGINEER, home);
        int tourists = JobUtil.numJobs(JobType.TOURIST, home);
        int equalShare = POPULATION / JobType.values().length;

        // Engineer has by far the highest need so should get more than an equal share
        assertTrue(engineers > 2 * equalShare, "Engineers " + engineers + " above equal share " + equalShare);
        assertTrue(engineers > tourists, "More engineers " + engineers + " than tourists " + tourists);
    }

    @Test
    void testEveryoneGetsAJob() {
        var home = buildSettlement("mock", POPULATION);
        populate(home, POPULATION);

        for (Person p : home.getAllAssociatedPeople()) {
            assertNotNull(p.getMind().getJobType(), p.getName() + " has a job");
        }
    }

    @Test
    void testMaxPositionsFollowNeed() {
        Map<JobType, Double> needs = new EnumMap<>(JobType.class);
        needs.put(JobType.ENGINEER, 6D);
        needs.put(JobType.BOTANIST, 3D);
        needs.put(JobType.TOURIST, 1D);

        var max = JobUtil.getMaxPositions(needs, 20);

        assertEquals(12, max.get(JobType.ENGINEER), "Engineer positions");
        assertEquals(6, max.get(JobType.BOTANIST), "Botanist positions");
        assertEquals(2, max.get(JobType.TOURIST), "Tourist positions");
    }

    @Test
    void testMaxPositionsCoverPopulation() {
        Map<JobType, Double> needs = new EnumMap<>(JobType.class);
        needs.put(JobType.ENGINEER, 5D);
        needs.put(JobType.BOTANIST, 3D);
        needs.put(JobType.TOURIST, 1D);

        for (int pop = 1; pop <= 50; pop++) {
            int total = JobUtil.getMaxPositions(needs, pop).values().stream()
                            .mapToInt(Integer::intValue).sum();
            assertTrue(total >= pop, "Positions " + total + " for population " + pop);
        }
    }

    @Test
    void testMaxPositionsNoNeed() {
        Map<JobType, Double> needs = new EnumMap<>(JobType.class);
        needs.put(JobType.ENGINEER, 0D);
        needs.put(JobType.TOURIST, 0D);

        var max = JobUtil.getMaxPositions(needs, 3);

        assertEquals(2, max.get(JobType.ENGINEER), "Engineer positions shared equally");
        assertEquals(2, max.get(JobType.TOURIST), "Tourist positions shared equally");
    }

    @Test
    void testMaxPositionsZeroNeedJob() {
        Map<JobType, Double> needs = new EnumMap<>(JobType.class);
        needs.put(JobType.ENGINEER, 4D);
        needs.put(JobType.TOURIST, 0D);

        var max = JobUtil.getMaxPositions(needs, 10);

        assertEquals(10, max.get(JobType.ENGINEER), "Engineer gets all positions");
        assertEquals(0, max.get(JobType.TOURIST), "No positions without need");
    }
}
