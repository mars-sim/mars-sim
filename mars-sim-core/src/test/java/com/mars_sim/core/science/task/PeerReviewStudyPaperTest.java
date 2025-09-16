package com.mars_sim.core.science.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Settlement;

public class PeerReviewStudyPaperTest extends AbstractMarsSimUnitTest {
    public void testCreateReviewTask() {
        var s = buildSettlement("Study", true);
        var study = buildStudyToPeerReviewPhase(s, ScienceType.BOTANY, JobType.BOTANIST);
        var r = buildPerson("Reviewer", s);
        r.setJob(JobType.BOTANIST, "Boss");
        BuildingManager.addToActivitySpot(r, study.getPrimaryResearcher().getBuildingLocation(), FunctionType.RESEARCH);

        var t = PeerReviewStudyPaper.createTask(r);
        assertNotNull("Review task created", t);
        assertFalse("Review task started", t.isDone());

        executeTaskForDuration(r, t, t.getTimeLeft() * 1.1);
        assertTrue("Review task completed", t.isDone());

        double elasped = study.getTotalPeerReviewTimeRequired() * 1.1;
        study.timePassing(createPulse(sim.getMasterClock().getMarsTime().addTime(elasped), false, false));
        assertTrue("Study complete", study.isCompleted());
    }

    private ScientificStudy buildStudyToPeerReviewPhase(Settlement s, ScienceType science, JobType researchJob) {
        
        var study = CompileScientificStudyResultsTest.buildStudyToPaperPhase(s, this, science, researchJob);

        // Simulate paper phase
        var elapsed = study.getTotalPrimaryPaperWorkTimeRequired();
        study.addPrimaryPaperWorkTime(elapsed);
        var now = getSim().getMasterClock().getMarsTime().addTime(elapsed);
        study.timePassing(createPulse(now, false, false));
        assertEquals("Study advanced to review phase", StudyStatus.PEER_REVIEW_PHASE,
                            study.getPhase());
        return study;
    }

    public void testMetaTask() {
        var s = buildSettlement("Study", true);
        var study = buildStudyToPeerReviewPhase(s, ScienceType.ASTROBIOLOGY, JobType.ASTROBIOLOGIST);

        var l = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        var r = buildPerson("Reviewer", s);
        r.setJob(JobType.ASTROBIOLOGIST, "Boss");
        BuildingManager.addToActivitySpot(r, l, FunctionType.RESEARCH);

        var mt = new PeerReviewStudyPaperMeta();
        var tasks = mt.getTaskJobs(r);
        assertEquals("Found 1 task", 1, tasks.size());

        // Add a starting study which shouldbe ignored
        var startStudy = ProposeScientificStudy.createStudy(study.getPrimaryResearcher());
        assertNotNull("Starting study", startStudy);
        tasks = mt.getTaskJobs(r);
        assertEquals("Still found 1 task", 1, tasks.size());

    }
}
