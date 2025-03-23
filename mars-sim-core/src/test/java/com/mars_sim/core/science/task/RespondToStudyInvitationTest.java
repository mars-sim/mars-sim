package com.mars_sim.core.science.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;

public class RespondToStudyInvitationTest extends AbstractMarsSimUnitTest {

    public void testRejectInvite() {
        var s = buildSettlement("Study", true);

        // Collabarotr is a chef
        var c = buildPerson("Collab1", s);
        c.setJob(JobType.CHEF, "Boss");

        // Create a study in invite phase
        var study = getStudy(c, ScienceType.CHEMISTRY, JobType.CHEMIST);

        BuildingManager.addPersonToActivitySpot(c, study.getPrimaryResearcher().getBuildingLocation(), FunctionType.RESEARCH);

        var t = new RespondToStudyInvitation(c);
        assertFalse("Task started", t.isDone());

        executeTaskForDuration(c, t, t.getTimeLeft() * 1.1);
        assertTrue("Task completed", t.isDone());
        assertTrue("Collabortors is empty", study.getCollaborativeResearchers().isEmpty());
        assertTrue("Collabortor responded", study.hasInvitedResearcherResponded(c));
    }

    private ScientificStudy getStudy(Person c, ScienceType science, JobType researchJob) {
        var study = InviteStudyCollaboratorTest.buildStudyToInvitePhase(c.getAssociatedSettlement(),
                                        this, science, researchJob);
        study.addInvitedResearcher(c);
        study.addProposalWorkTime(study.getTotalProposalWorkTimeRequired());
        study.timePassing(createPulse(0, 0, false, false));
        assertEquals("Study advanced to invitation phase", StudyStatus.INVITATION_PHASE,
                            study.getPhase());
        return study;
    }

    public void testAcceptInvite() {
        var s = buildSettlement("Study", true);
        var c = buildPerson("Collab1", s);
        c.setJob(JobType.CHEMIST, "Boss");

        // Create a study in invite phase
        var study = getStudy(c, ScienceType.CHEMISTRY, JobType.CHEMIST);

        BuildingManager.addPersonToActivitySpot(c, study.getPrimaryResearcher().getBuildingLocation(), FunctionType.RESEARCH);


        var t = new RespondToStudyInvitation(c);
        assertFalse("Task started", t.isDone());

        executeTaskForDuration(c, t, t.getTimeLeft() * 1.1);
        assertTrue("Task completed", t.isDone());
        assertTrue("Collabortor in study", study.getCollaborativeResearchers().contains(c));
        assertTrue("Collabortor responded", study.hasInvitedResearcherResponded(c));
    }

    public void testMetaTask() {
        var s = buildSettlement("Study", true);
        var c = buildPerson("Collab1", s);
        c.setJob(JobType.CHEMIST, "Boss");

        // Create a study in invite phase
        var study = getStudy(c, ScienceType.CHEMISTRY, JobType.CHEMIST);

        BuildingManager.addPersonToActivitySpot(c, study.getPrimaryResearcher().getBuildingLocation(), FunctionType.RESEARCH);

        RespondToStudyInvitationMeta mt = new RespondToStudyInvitationMeta();
        var tasks = mt.getTaskJobs(c);
        assertFalse("Tasks found", tasks.isEmpty());
    }
}
