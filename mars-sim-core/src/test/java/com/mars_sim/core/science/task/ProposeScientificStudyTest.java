package com.mars_sim.core.science.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.LocalPosition;

public class ProposeScientificStudyTest extends AbstractMarsSimUnitTest {
    public void testChefProposal() {
        var s = buildSettlement("Study", true);
        buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        var p = buildPerson("Chef", s);
        p.setJob(JobType.CHEF, "Boss");

        var mt = new ProposeScientificStudyMeta();

        var tasks = mt.getTaskJobs(p);
        assertTrue("Chef has no tasks", tasks.isEmpty());
    }

    public void testBiologistProposal() {
        var s = buildSettlement("Study", true);
        buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        var p = buildPerson("Biologist", s);
        p.setJob(JobType.BIOLOGIST, "Boss");
        var mt = new ProposeScientificStudyMeta();

        var mayorTasks = mt.getTaskJobs(p);
        assertFalse("Biologist has tasks", mayorTasks.isEmpty());
    }
    
    public void testAssignedStudy() {
       
        var s = buildSettlement("Study", true);
        var p = buildPerson("Researcher", s);

        var jobType = JobType.BOTANIST;
        p.setJob(jobType, "Boss");

        var study = ProposeScientificStudy.createStudy(p);

        var mt = new ProposeScientificStudyMeta();
        var tasks = mt.getTaskJobs(p);
        assertFalse("Existing study found", tasks.isEmpty());

        // Advance Study and retest
        study.addProposalWorkTime(study.getTotalProposalWorkTimeRequired());
        study.timePassing(createPulse(0, 0, false, false));
        assertEquals("Study advanced to invitation phase", StudyStatus.INVITATION_PHASE,
                            study.getPhase());
        tasks = mt.getTaskJobs(p);
        assertTrue("Existing study ignored", tasks.isEmpty());                     
    }

    public void testCreateTask() {
       
        var s = buildSettlement("Study");
        var l = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        var p = buildPerson("Researcher", s);
        BuildingManager.addPersonToActivitySpot(p, l, FunctionType.RESEARCH);

        var jobType = JobType.BOTANIST;
        p.setJob(jobType, "Boss");

        var t = ProposeScientificStudy.createTask(p);

        assertNotNull("Propose task created", t);

        var st = p.getStudy();
        assertNotNull("Study created", st);
        assertEquals("Person is primary researcher", p, st.getPrimaryResearcher());
        assertEquals("Science of study", ScienceType.getJobScience(jobType), st.getScience());

        executeTaskForDuration(p, t, st.getTotalProposalWorkTimeRequired() * 2); // Allow for zero  skill

        assertTrue("Proposal done", t.isDone());
        assertGreaterThan("Study proposal work", 0D, st.getProposalWorkTimeCompleted());
    }
}
