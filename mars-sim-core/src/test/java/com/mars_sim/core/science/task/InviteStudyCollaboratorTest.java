package com.mars_sim.core.science.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.LocalPosition;

public class InviteStudyCollaboratorTest extends AbstractMarsSimUnitTest {
    /**
     * Build a study to the Proposal phase
     */
    static ScientificStudy buildStudyToProposalPhase(Settlement s, MarsSimContext context,
                                ScienceType science, JobType researchJob) {
        // Build a settlemtn with a lab and put a Person in it
        var l = context.buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        var p = context.buildPerson("Researcher", s);
        p.setJob(researchJob, "Boss");
        BuildingManager.addPersonToActivitySpot(p, l, FunctionType.RESEARCH);

        // Build a study
        var sim = context.getSim();
        var study = sim.getScientificStudyManager().createScientificStudy(p, science, 10);
        assertEquals("Person assigned Study", study, p.getResearchStudy().getStudy());
        return study;
    }

    /**
     * Build a study to the Invite phase
     */
    static ScientificStudy buildStudyToInvitePhase(Settlement s, MarsSimContext context,
                                ScienceType science, JobType researchJob) {
        var study = buildStudyToProposalPhase(s, context, science, researchJob);

        // COmplete Proposal
        study.addProposalWorkTime(study.getTotalProposalWorkTimeRequired() + 10D);
        study.timePassing(context.createPulse(context.getSim().getMasterClock().getMarsTime(), false, false));
        assertEquals("Study start phase", StudyStatus.INVITATION_PHASE, study.getPhase());

        return study;
    }

    public void testMetaTask() {
        var s = buildSettlement("Study", true);

        var c = buildPerson("Collab1", s);
        c.setJob(JobType.BOTANIST, "Boss");

        var mt = new InviteStudyCollaboratorMeta();

        // Study ignore until corrct phase
        var proposalStudy = buildStudyToProposalPhase(s, this, ScienceType.BOTANY, JobType.BOTANIST);
        var tasks = mt.getTaskJobs(proposalStudy.getPrimaryResearcher());
        assertTrue("No tasks found", tasks.isEmpty());

        // Get invite phase study
        var inviteStudy = buildStudyToInvitePhase(s, this, ScienceType.BOTANY, JobType.BOTANIST);
        tasks = mt.getTaskJobs(inviteStudy.getPrimaryResearcher());
        assertFalse("Tasks found", tasks.isEmpty());
    }


    public void testCreateTask() {
        var s = buildSettlement("Study", true);
        var l = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        var p = buildPerson("Researcher", s);
        var c1 = buildPerson("Collab1", s);
        var c2 = buildPerson("Collab2", s);

        BuildingManager.addPersonToActivitySpot(p, l, FunctionType.RESEARCH);
        BuildingManager.addPersonToActivitySpot(c1, l, FunctionType.RESEARCH);
        BuildingManager.addPersonToActivitySpot(c2, l, FunctionType.RESEARCH);

        var jobType = JobType.BOTANIST;
        p.setJob(jobType, "Boss");
        c1.setJob(jobType, "Boss");
        c2.setJob(jobType, "Boss");

        var study = ProposeScientificStudy.createStudy(p);

        // Advance Study and retest
        study.addProposalWorkTime(study.getTotalProposalWorkTimeRequired());
        study.timePassing(createPulse(0, 0, false, false));
        assertEquals("Study advanced to invitation phase", StudyStatus.INVITATION_PHASE,
                            study.getPhase());
        
        // Create invite task
        var task = new InviteStudyCollaborator(p);
        assertFalse("Invite task created", task.isDone());

        executeTask(p, task, 20);
        assertTrue("Invite task done", task.isDone());

        // Checkhow has been invited
        assertTrue("Collaborator 1 added to study", study.getInvitedResearchers().contains(c1));
        assertTrue("Collaborator 2 added to study", study.getInvitedResearchers().contains(c2));

    }
}
