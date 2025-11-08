/*
 * Mars Simulation Project
 * JobTypeTest.java
 * @date 2023-10-22
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.job.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class JobTypeTest {

    @Test
    public void testDoctorGroups() {
        testGroups(JobType.DOCTOR, true, true, true, false, false, false);
    }

    @Test
    public void testEngineerGroups() {
        testGroups(JobType.ENGINEER, false, false, true, false, true, true);
    }

    @Test
    public void testBiologistGroups() {
        testGroups(JobType.ASTROBIOLOGIST, false, true, true, true, false, false);
    }

    @Test
    public void testMathGroups() {
        testGroups(JobType.MATHEMATICIAN, false, false, true, false, false, false);
    }

    private void testGroups(JobType assignee, boolean isMedic, boolean isIntellectual,
                            boolean isAcademic, boolean isScientist,
                            boolean isLoader, boolean isMechanic) {

        assertEquals(isMedic, JobType.MEDICS.contains(assignee), assignee + " is medic");
        assertEquals(isIntellectual, JobType.INTELLECTUALS.contains(assignee), assignee + " is intellectuals");
        assertEquals(isAcademic, JobType.ACADEMICS.contains(assignee), assignee + " is academic");
        assertEquals(isScientist, JobType.SCIENTISTS.contains(assignee), assignee + " is scientist");
        assertEquals(isLoader, JobType.LOADERS.contains(assignee), assignee + " is loader");
        assertEquals(isMechanic, JobType.MECHANICS.contains(assignee), assignee + " is mechanics");
    }
}
