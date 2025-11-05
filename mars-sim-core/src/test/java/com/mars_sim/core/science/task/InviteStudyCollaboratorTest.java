package com.mars_sim.core.science.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.MarsSimContext;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Settlement;

public class InviteStudyCollaboratorTest extends MarsSimUnitTest {
    /**
     * Build a study to the Proposal phase
     */
    static ScientificStudy buildStudyToProposalPhase(Settlement s, MarsSimContext context,
                                ScienceType science, JobType researchJob) {
        // Build a settlement with a lab and put a Person in it
        var l = context.buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        var p = context.buildPerson("Researcher", s);
        p.setJob(researchJob, "Boss");
        BuildingManager.addToActivitySpot(p, l, FunctionType.RESEARCH);

        // Build a study
        var sim = context.getSim();
        var study = sim.getScientificStudyManager().createScientificStudy(p, science, 10);
        assertEquals(study, p.getResearchStudy().getStudy(), "Person assigned Study");
        return study;
    }

    /**
     * Build a study to the Invite phase
     */
    static ScientificStudy buildStudyToInvitePhase(Settlement s, MarsSimContext context,
                                ScienceType science, JobType researchJob) {
        var study = buildStudyToProposalPhase(s, context, science, researchJob);

        // Complete Proposal
        study.addProposalWorkTime(study.getTotalProposalWorkTimeRequired() + 10D);
        study.timePassing(context.createPulse(context.getSim().getMasterClock().getMarsTime(), false, false));
        assertEquals(StudyStatus.INVITATION_PHASE, study.getPhase(), "Study start phase");

        return study;
    }

    @Test
    public void testMetaTask() {
        var s = buildSettlement("Study", true);

        var c = buildPerson("Collab1", s);
        c.setJob(JobType.BOTANIST, "Boss");

        var mt = new InviteStudyCollaboratorMeta();

        // Study ignore until correct phase
        var proposalStudy = buildStudyToProposalPhase(s, this, ScienceType.BOTANY, JobType.BOTANIST);
        var tasks = mt.getTaskJobs(proposalStudy.getPrimaryResearcher());
        assertTrue(tasks.isEmpty(), "No tasks found");

        // Get invite phase study
        var inviteStudy = buildStudyToInvitePhase(s, this, ScienceType.BOTANY, JobType.BOTANIST);
        tasks = mt.getTaskJobs(inviteStudy.getPrimaryResearcher());
        // Note: The researcher won't be able to find anyone to invite
        assertFalse(tasks.isEmpty(), "Tasks found");
        
    }


    @Test
    public void testCreateTask() {
        var s = buildSettlement("Study", true);
        var l = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        var p = buildPerson("Researcher", s);
        var c1 = buildPerson("Collab1", s);
        var c2 = buildPerson("Collab2", s);

        BuildingManager.addToActivitySpot(p, l, FunctionType.RESEARCH);
        BuildingManager.addToActivitySpot(c1, l, FunctionType.RESEARCH);
        BuildingManager.addToActivitySpot(c2, l, FunctionType.RESEARCH);

        var jobType = JobType.BOTANIST;
        p.setJob(jobType, "Boss");
        c1.setJob(jobType, "Boss");
        c2.setJob(jobType, "Boss");

        var study = ProposeScientificStudy.createStudy(p);

        // Advance Study and retest
        study.addProposalWorkTime(study.getTotalProposalWorkTimeRequired());
        study.timePassing(createPulse(0, 0, false, false));
        assertEquals(StudyStatus.INVITATION_PHASE, study.getPhase(), "Study advanced to invitation phase");
        
        // Create invite task
        var task = new InviteStudyCollaborator(p);
        assertFalse(task.isDone(), "Invite task created");

        executeTask(p, task, 20);
        assertTrue(task.isDone(), "Invite task done");

        // Check if he has been invited
        assertTrue(study.getInvitedResearchers().contains(c1), "Collaborator 1 added to study");
        assertTrue(study.getInvitedResearchers().contains(c2), "Collaborator 2 added to study");

    }
}
