package com.mars_sim.core.science.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.StudyStatus;

public class ProposeScientificStudyTest extends MarsSimUnitTest {
    @Test
    public void testChefProposal() {
        var s = buildSettlement("Study", true);
        buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var p = buildPerson("Chef", s);
        p.setJob(JobType.CHEF, "Boss");

        var mt = new ProposeScientificStudyMeta();

        var tasks = mt.getTaskJobs(p);
        assertTrue(tasks.isEmpty(), "Chef has no tasks");
    }

    @Test
    public void testBiologistProposal() {
        var s = buildSettlement("Study", true);
        buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var p = buildPerson("Biologist", s);
        p.setJob(JobType.ASTROBIOLOGIST, "Boss");
        var mt = new ProposeScientificStudyMeta();

        var mayorTasks = mt.getTaskJobs(p);
        assertFalse(mayorTasks.isEmpty(), "Biologist has tasks");
    }
    
    @Test
    public void testAssignedStudy() {
       
        var s = buildSettlement("Study", true);
        var p = buildPerson("Researcher", s);

        var jobType = JobType.BOTANIST;
        p.setJob(jobType, "Boss");

        var study = ProposeScientificStudy.createStudy(p);

        var mt = new ProposeScientificStudyMeta();
        var tasks = mt.getTaskJobs(p);
        assertFalse(tasks.isEmpty(), "Existing study found");

        // Advance Study and retest
        study.addProposalWorkTime(study.getTotalProposalWorkTimeRequired());
        study.timePassing(createPulse(0, 0, false, false));
        assertEquals(StudyStatus.INVITATION_PHASE, study.getPhase(), "Study advanced to invitation phase");
        tasks = mt.getTaskJobs(p);
        assertTrue(tasks.isEmpty(), "Existing study ignored");                     
    }

    @Test
    public void testCreateTask() {
       
        var s = buildSettlement("Study");
        var l = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var p = buildPerson("Researcher", s);
        BuildingManager.addToActivitySpot(p, l, FunctionType.RESEARCH);

        var jobType = JobType.BOTANIST;
        p.setJob(jobType, "Boss");

        var t = ProposeScientificStudy.createTask(p);

        assertNotNull(t, "Propose task created");

        var st = p.getResearchStudy().getStudy();
        assertNotNull(st, "Study created");
        assertEquals(p, st.getPrimaryResearcher(), "Person is primary researcher");
        assertEquals(ScienceType.getJobScience(jobType), st.getScience(), "Science of study");

        executeTaskForDuration(p, t, st.getTotalProposalWorkTimeRequired() * 2); // Allow for zero  skill

        assertTrue(t.isDone(), "Proposal done");
        assertGreaterThan("Study proposal work", 0D, st.getProposalWorkTimeCompleted());
    }
}
