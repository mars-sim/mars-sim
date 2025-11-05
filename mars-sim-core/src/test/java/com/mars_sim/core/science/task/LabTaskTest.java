package com.mars_sim.core.science.task;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;


import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.MarsSimContext;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Settlement;

public class LabTaskTest extends MarsSimUnitTest {

    static ScientificStudy buildStudyToResearchPhase(Settlement s, MarsSimContext context,
                                ScienceType science, JobType researchJob) {
        var study = InviteStudyCollaboratorTest.buildStudyToInvitePhase(s, context, science, researchJob);

        // No collaborators so advance further
        study.timePassing(context.createPulse(context.getSim().getMasterClock().getMarsTime(), false, false));
        assertEquals(StudyStatus.RESEARCH_PHASE, study.getPhase(), "Study start phase");
        return study;
    }

    @Test
    public void testStudyFieldSamples() {
        var s = buildSettlement("Study", true);
        var study = buildStudyToResearchPhase(s, this, ScienceType.CHEMISTRY, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        // Add some rocks to test
        int rockId = ResourceUtil.ROCK_IDS[0];
        s.storeAmountResource(rockId, 100D);

        var task  = StudyFieldSamples.createTask(p);
        assertNotNull(task, "Task created");
        assertEquals(study, task.getStudy(), "Task assigned Study");
        assertNotNull(task.getLaboratory(), "Task assigned Lab");

        // Do all the required research
        var origResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        executeTaskForDuration(p, task, study.getTotalPrimaryResearchWorkTimeRequired());
        assertTrue(task.isDone(), "Lab task is done");
        var newResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        assertGreaterThan("Primary research advanced", origResearchCompleted, newResearchCompleted);

        // Check collected resources increased
        assertGreaterThan("Collected samples", 0D, s.getResourceCollected(rockId));
    }

    @Test
    public void testPerformPrimaryLabResearch() {
        var s = buildSettlement("Study", true);
        var study = buildStudyToResearchPhase(s, this, ScienceType.ASTROBIOLOGY, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        var task  = PerformLaboratoryResearch.createTask(p);
        assertNotNull(task, "Task created");
        assertEquals(study, task.getStudy(), "Task assigned Study");
        assertNotNull(task.getLaboratory(), "Task assigned Lab");

        // Do all the required research
        var origResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        executeTaskForDuration(p, task, study.getTotalPrimaryResearchWorkTimeRequired());
        assertTrue(task.isDone(), "Lab task is done");
        var newResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        assertGreaterThan("Primary research advanced", origResearchCompleted, newResearchCompleted);
    }

    @Test
    public void testPerformCollabLabResearch() {
        var s = buildSettlement("Study", true);
        var study = buildStudyToResearchPhase(s, this, ScienceType.ASTROBIOLOGY, JobType.ASTROBIOLOGIST);
        var prim = study.getPrimaryResearcher();
        var collab = buildPerson("Collab", s, JobType.ASTROBIOLOGIST, prim.getBuildingLocation(), FunctionType.RESEARCH);   
        study.addCollaborativeResearcher(collab, ScienceType.ASTROBIOLOGY);

        var task  = PerformLaboratoryResearch.createTask(collab);
        assertNotNull(task, "Task created");
        assertEquals(study, task.getStudy(), "Task assigned Study");
        assertNotNull(task.getLaboratory(), "Task assigned Lab");

        // Do all the required research
        var origResearchCompleted = study.getCollaborativeResearchWorkTimeCompleted(collab);
        executeTaskForDuration(collab, task, study.getTotalCollaborativeResearchWorkTimeRequired());
        assertTrue(task.isDone(), "Lab task is done");
        var newResearchCompleted = study.getCollaborativeResearchWorkTimeCompleted(collab);
        assertGreaterThan("Collaborative research advanced", origResearchCompleted, newResearchCompleted);
    }

    @Test
    public void testMathModelling() {
        var s = buildSettlement("Study", true);

        var study = buildStudyToResearchPhase(s, this, ScienceType.MATHEMATICS, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        var task  = PerformMathematicalModeling.createTask(p);
        assertNotNull(task, "Task created");
        assertEquals(study, task.getStudy(), "Task assigned Study");
        assertNotNull(task.getLaboratory(), "Task assigned Lab");

        // Do all the required research
        var origResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        executeTaskForDuration(p, task, study.getTotalPrimaryResearchWorkTimeRequired());
        assertTrue(task.isDone(), "Lab task is done");
        var newResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        assertGreaterThan("Primary research advanced", origResearchCompleted, newResearchCompleted);
    }

    @Test
    public void testLabMetaTask() {
        var s = buildSettlement("Study", true);

        var study = buildStudyToResearchPhase(s, this, ScienceType.ASTROBIOLOGY, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        var lmt = new PerformLaboratoryResearchMeta();
        assertFalse(lmt.getTaskJobs(p).isEmpty(), "Found tasks for PerformLabResearch meta tasks");
    }

    @Test
    public void testStudyFieldMetaTask() {
        var s = buildSettlement("Study", true);

        var study = buildStudyToResearchPhase(s, this, ScienceType.CHEMISTRY, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        // Add some rocks to test
        int rockId = ResourceUtil.ROCK_IDS[0];
        s.storeAmountResource(rockId, 100D);

        var mmt = new StudyFieldSamplesMeta();
        assertFalse(mmt.getTaskJobs(p).isEmpty(), "Found tasks for Study Field meta tasks");
    }
    
    @Test
    public void testMathMetaTask() {
        var s = buildSettlement("Study", true);

        var study = buildStudyToResearchPhase(s, this, ScienceType.MATHEMATICS, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        var mmt = new PerformMathematicalModelingMeta();
        assertFalse(mmt.getTaskJobs(p).isEmpty(), "Found tasks for Perform Math Modelling meta tasks");
    }
}
