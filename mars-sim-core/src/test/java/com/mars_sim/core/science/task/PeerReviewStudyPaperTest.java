package com.mars_sim.core.science.task;
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
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Settlement;

public class PeerReviewStudyPaperTest extends MarsSimUnitTest {
    @Test
    public void testCreateReviewTask() {
        var s = buildSettlement("Study", true);
        var study = buildStudyToPeerReviewPhase(s, ScienceType.BOTANY, JobType.BOTANIST);
        var r = buildPerson("Reviewer", s);
        r.setJob(JobType.BOTANIST, "Boss");
        BuildingManager.addToActivitySpot(r, study.getPrimaryResearcher().getBuildingLocation(), FunctionType.RESEARCH);

        var t = PeerReviewStudyPaper.createTask(r);
        assertNotNull(t, "Review task created");
        assertFalse(t.isDone(), "Review task started");

        executeTaskForDuration(r, t, t.getTimeLeft() * 1.1);
        assertTrue(t.isDone(), "Review task completed");

        double elasped = study.getTotalPeerReviewTimeRequired() * 1.1;
        study.timePassing(createPulse(getSim().getMasterClock().getMarsTime().addTime(elasped), false, false));
        assertTrue(study.isCompleted(), "Study complete");
    }

    private ScientificStudy buildStudyToPeerReviewPhase(Settlement s, ScienceType science, JobType researchJob) {
        
        var study = CompileScientificStudyResultsTest.buildStudyToPaperPhase(s, getContext(), science, researchJob);

        // Simulate paper phase
        var elapsed = study.getTotalPrimaryPaperWorkTimeRequired();
        study.addPrimaryPaperWorkTime(elapsed);
        var now = getSim().getMasterClock().getMarsTime().addTime(elapsed);
        study.timePassing(createPulse(now, false, false));
        assertEquals(StudyStatus.PEER_REVIEW_PHASE, study.getPhase(), "Study advanced to review phase");
        return study;
    }

    @Test
    public void testMetaTask() {
        var s = buildSettlement("Study", true);
        var study = buildStudyToPeerReviewPhase(s, ScienceType.ASTROBIOLOGY, JobType.ASTROBIOLOGIST);

        var l = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var r = buildPerson("Reviewer", s);
        r.setJob(JobType.ASTROBIOLOGIST, "Boss");
        BuildingManager.addToActivitySpot(r, l, FunctionType.RESEARCH);

        var mt = new PeerReviewStudyPaperMeta();
        var tasks = mt.getTaskJobs(r);
        assertEquals(1, tasks.size(), "Found 1 task");

        // Add a starting study which shouldbe ignored
        var startStudy = ProposeScientificStudy.createStudy(study.getPrimaryResearcher());
        assertNotNull(startStudy, "Starting study");
        tasks = mt.getTaskJobs(r);
        assertEquals(1, tasks.size(), "Still found 1 task");

    }
}
