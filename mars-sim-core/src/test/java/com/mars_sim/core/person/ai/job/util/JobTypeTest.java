/*
 * Mars Simulation Project
 * JobTypeTest.java
 * @date 2023-10-22
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.job.util;

import junit.framework.TestCase;

public class JobTypeTest extends TestCase  {

    public void testDoctorGroups() {
        testGroups(JobType.DOCTOR, true, true, true, false, false, false);
    }

    public void testEngineerGroups() {
        testGroups(JobType.ENGINEER, false, false, true, false, true, true);
    }

    public void testBiologistGroups() {
        testGroups(JobType.BIOLOGIST, false, true, true, true, false, false);
    }

    public void testMathGroups() {
        testGroups(JobType.MATHEMATICIAN, false, false, true, false, false, false);
    }

    private void testGroups(JobType assignee, boolean isMedic, boolean isIntellectual,
                            boolean isAcademic, boolean isScientist,
                            boolean isLoader, boolean isMechanic) {

        assertEquals(assignee + " is medic", isMedic, JobType.MEDICS.contains(assignee));
        assertEquals(assignee + " is intellectuals", isIntellectual, JobType.INTELLECTUALS.contains(assignee));
        assertEquals(assignee + " is academic", isAcademic, JobType.ACADEMICS.contains(assignee));
        assertEquals(assignee + " is scientist", isScientist, JobType.SCIENTISTS.contains(assignee));
        assertEquals(assignee + " is loader", isLoader, JobType.LOADERS.contains(assignee));
        assertEquals(assignee + " is mechanics", isMechanic, JobType.MECHANICS.contains(assignee));
    }
}
