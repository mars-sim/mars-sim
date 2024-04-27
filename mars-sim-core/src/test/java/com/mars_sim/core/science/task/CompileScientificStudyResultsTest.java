package com.mars_sim.core.science.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Settlement;

public class CompileScientificStudyResultsTest extends AbstractMarsSimUnitTest {

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
        assertEquals("Study advanced to paper phase", StudyStatus.PAPER_PHASE,
                            study.getPhase());
        return study;
    }

    public void testCreateTask() {
        var s = buildSettlement("Research");
        var study = buildStudyToPaperPhase(s, this, ScienceType.BIOLOGY, JobType.BIOLOGIST);
        var p = study.getPrimaryResearcher();

        var t = CompileScientificStudyResults.createTask(p);
        assertNotNull("Task created", t);

        var origCompleted = study.getPrimaryPaperWorkTimeCompleted();
        executeTaskForDuration(p, t, 1.1 * t.getTimeLeft());
        assertTrue("Task completed", t.isDone());
        var newCompleted = study.getPrimaryPaperWorkTimeCompleted();
        assertGreaterThan("Paper work completed", origCompleted, newCompleted);

    }

    public void testMetaTask() {
        var s = buildSettlement("Research", true);
        var study = buildStudyToPaperPhase(s, this, ScienceType.BIOLOGY, JobType.BIOLOGIST);
        var p = study.getPrimaryResearcher();

        var mt = new CompileScientificStudyResultsMeta();
        var tasks = mt.getTaskJobs(p);

        assertFalse("Tasks found for researcher", tasks.isEmpty());
    }
}
