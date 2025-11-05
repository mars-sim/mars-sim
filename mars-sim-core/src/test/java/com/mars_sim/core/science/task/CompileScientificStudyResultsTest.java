package com.mars_sim.core.science.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.MarsSimContext;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Settlement;

public class CompileScientificStudyResultsTest extends MarsSimUnitTest {

    /**
     * Build a Study at the Paper phase 
     */
    static ScientificStudy buildStudyToPaperPhase(Settlement s, MarsSimContext context,
                    ScienceType science, JobType researchJob) {
        var study = LabTaskTest.buildStudyToResearchPhase(s, context, science, researchJob);

        // Simulate research phase
        var elapsed = study.getTotalPrimaryResearchWorkTimeRequired();
        study.addPrimaryResearchWorkTime(elapsed);
        var now = context.getSim().getMasterClock().getMarsTime().addTime(elapsed);
        study.timePassing(context.createPulse(now, false, false));
        assertEquals(StudyStatus.PAPER_PHASE, study.getPhase(), "Study advanced to paper phase");
        return study;
    }

    @Test
    public void testCreateTask() {
        var s = buildSettlement("Research");
        var study = buildStudyToPaperPhase(s, getContext(), ScienceType.ASTROBIOLOGY, JobType.ASTROBIOLOGIST);
        var p = study.getPrimaryResearcher();

        var t = CompileScientificStudyResults.createTask(p);
        assertNotNull(t, "Task created");

        var origCompleted = study.getPrimaryPaperWorkTimeCompleted();
        executeTaskForDuration(p, t, 1.1 * t.getTimeLeft());
        assertTrue(t.isDone(), "Task completed");
        var newCompleted = study.getPrimaryPaperWorkTimeCompleted();
        assertGreaterThan("Paper work completed", origCompleted, newCompleted);

    }

    @Test
    public void testMetaTaskAsPrimary() {
        var s = buildSettlement("Research", true);
        var study = buildStudyToPaperPhase(s, getContext(), ScienceType.ASTROBIOLOGY, JobType.ASTROBIOLOGIST);
        var p = study.getPrimaryResearcher();

        var mt = new CompileScientificStudyResultsMeta();
        var tasks = mt.getTaskJobs(p);

        assertFalse(tasks.isEmpty(), "Tasks found for researcher");
    }

    @Test
    public void testMetaTaskAsCollab() {
        var s = buildSettlement("Research", true);
        var study = buildStudyToPaperPhase(s, getContext(), ScienceType.ASTROBIOLOGY, JobType.ASTROBIOLOGIST);
        var p = buildPerson("Collab", s);
        study.addCollaborativeResearcher(p, study.getScience());

        var mt = new CompileScientificStudyResultsMeta();
        var tasks = mt.getTaskJobs(p);

        assertFalse(tasks.isEmpty(), "Tasks found for researcher");
    }
}
