package com.mars_sim.core.science.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;

public class RespondToStudyInvitationTest extends MarsSimUnitTest {

    @Test
    public void testRejectInvite() {
        var s = buildSettlement("Study", true);

        // Collabarotr is a chef
        var c = buildPerson("Collab1", s);
        c.setJob(JobType.CHEF, "Boss");

        // Create a study in invite phase
        var study = getStudy(c, ScienceType.CHEMISTRY, JobType.CHEMIST);

        BuildingManager.addToActivitySpot(c, study.getPrimaryResearcher().getBuildingLocation(), FunctionType.RESEARCH);

        var t = new RespondToStudyInvitation(c);
        assertFalse(t.isDone(), "Task started");

        executeTaskForDuration(c, t, t.getTimeLeft() * 1.1);
        assertTrue(t.isDone(), "Task completed");
        assertTrue(study.getCollaborativeResearchers().isEmpty(), "Collabortors is empty");
        assertTrue(study.hasInvitedResearcherResponded(c), "Collabortor responded");
    }

    private ScientificStudy getStudy(Person c, ScienceType science, JobType researchJob) {
        var study = InviteStudyCollaboratorTest.buildStudyToInvitePhase(c.getAssociatedSettlement(),
                                        this, science, researchJob);
        study.addInvitedResearcher(c);
        study.addProposalWorkTime(study.getTotalProposalWorkTimeRequired());
        study.timePassing(createPulse(0, 0, false, false));
        assertEquals(StudyStatus.INVITATION_PHASE, study.getPhase(), "Study advanced to invitation phase");
        return study;
    }

    @Test
    public void testAcceptInvite() {
        var s = buildSettlement("Study", true);
        var c = buildPerson("Collab1", s);
        c.setJob(JobType.CHEMIST, "Boss");

        // Create a study in invite phase
        var study = getStudy(c, ScienceType.CHEMISTRY, JobType.CHEMIST);

        BuildingManager.addToActivitySpot(c, study.getPrimaryResearcher().getBuildingLocation(), FunctionType.RESEARCH);


        var t = new RespondToStudyInvitation(c);
        assertFalse(t.isDone(), "Task started");

        executeTaskForDuration(c, t, t.getTimeLeft() * 1.1);
        assertTrue(t.isDone(), "Task completed");
        assertTrue(study.getCollaborativeResearchers().contains(c), "Collabortor in study");
        assertTrue(study.hasInvitedResearcherResponded(c), "Collabortor responded");
    }

    @Test
    public void testMetaTask() {
        var s = buildSettlement("Study", true);
        var c = buildPerson("Collab1", s);
        c.setJob(JobType.CHEMIST, "Boss");

        // Create a study in invite phase
        var study = getStudy(c, ScienceType.CHEMISTRY, JobType.CHEMIST);

        BuildingManager.addToActivitySpot(c, study.getPrimaryResearcher().getBuildingLocation(), FunctionType.RESEARCH);

        RespondToStudyInvitationMeta mt = new RespondToStudyInvitationMeta();
        var tasks = mt.getTaskJobs(c);
        assertFalse(tasks.isEmpty(), "Tasks found");
    }
}
