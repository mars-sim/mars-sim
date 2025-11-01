package com.mars_sim.core.science.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.MarsSimContext;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Settlement;

public class LabTaskTest extends AbstractMarsSimUnitTest {

    static ScientificStudy buildStudyToResearchPhase(Settlement s, MarsSimContext context,
                                ScienceType science, JobType researchJob) {
        var study = InviteStudyCollaboratorTest.buildStudyToInvitePhase(s, context, science, researchJob);

        // No collaborators so advance further
        study.timePassing(context.createPulse(context.getSim().getMasterClock().getMarsTime(), false, false));
        assertEquals("Study start phase", StudyStatus.RESEARCH_PHASE, study.getPhase());
        return study;
    }

    public void testStudyFieldSamples() {
        var s = buildSettlement("Study", true);
        var study = buildStudyToResearchPhase(s, this, ScienceType.CHEMISTRY, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        // Add some rocks to test
        int rockId = ResourceUtil.ROCK_IDS[0];
        s.storeAmountResource(rockId, 100D);

        var task  = StudyFieldSamples.createTask(p);
        assertNotNull("Task created", task);
        assertEquals("Task assigned Study", study, task.getStudy());
        assertNotNull("Task assigned Lab", task.getLaboratory());

        // Do all the required research
        var origResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        executeTaskForDuration(p, task, study.getTotalPrimaryResearchWorkTimeRequired());
        assertTrue("Lab task is done", task.isDone());
        var newResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        assertGreaterThan("Primary research advanced", origResearchCompleted, newResearchCompleted);

        // Check collected resources increased
        assertGreaterThan("Collected samples", 0D, s.getResourceCollected(rockId));
    }

    public void testPerformPrimaryLabResearch() {
        var s = buildSettlement("Study", true);
        var study = buildStudyToResearchPhase(s, this, ScienceType.ASTROBIOLOGY, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        var task  = PerformLaboratoryResearch.createTask(p);
        assertNotNull("Task created", task);
        assertEquals("Task assigned Study", study, task.getStudy());
        assertNotNull("Task assigned Lab", task.getLaboratory());

        // Do all the required research
        var origResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        executeTaskForDuration(p, task, study.getTotalPrimaryResearchWorkTimeRequired());
        assertTrue("Lab task is done", task.isDone());
        var newResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        assertGreaterThan("Primary research advanced", origResearchCompleted, newResearchCompleted);
    }

    public void testPerformCollabLabResearch() {
        var s = buildSettlement("Study", true);
        var study = buildStudyToResearchPhase(s, this, ScienceType.ASTROBIOLOGY, JobType.ASTROBIOLOGIST);
        var prim = study.getPrimaryResearcher();
        var collab = buildPerson("Collab", s, JobType.ASTROBIOLOGIST, prim.getBuildingLocation(), FunctionType.RESEARCH);   
        study.addCollaborativeResearcher(collab, ScienceType.ASTROBIOLOGY);

        var task  = PerformLaboratoryResearch.createTask(collab);
        assertNotNull("Task created", task);
        assertEquals("Task assigned Study", study, task.getStudy());
        assertNotNull("Task assigned Lab", task.getLaboratory());

        // Do all the required research
        var origResearchCompleted = study.getCollaborativeResearchWorkTimeCompleted(collab);
        executeTaskForDuration(collab, task, study.getTotalCollaborativeResearchWorkTimeRequired());
        assertTrue("Lab task is done", task.isDone());
        var newResearchCompleted = study.getCollaborativeResearchWorkTimeCompleted(collab);
        assertGreaterThan("Collaborative research advanced", origResearchCompleted, newResearchCompleted);
    }

    public void testMathModelling() {
        var s = buildSettlement("Study", true);

        var study = buildStudyToResearchPhase(s, this, ScienceType.MATHEMATICS, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        var task  = PerformMathematicalModeling.createTask(p);
        assertNotNull("Task created", task);
        assertEquals("Task assigned Study", study, task.getStudy());
        assertNotNull("Task assigned Lab", task.getLaboratory());

        // Do all the required research
        var origResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        executeTaskForDuration(p, task, study.getTotalPrimaryResearchWorkTimeRequired());
        assertTrue("Lab task is done", task.isDone());
        var newResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        assertGreaterThan("Primary research advanced", origResearchCompleted, newResearchCompleted);
    }

    public void testLabMetaTask() {
        var s = buildSettlement("Study", true);

        var study = buildStudyToResearchPhase(s, this, ScienceType.ASTROBIOLOGY, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        var lmt = new PerformLaboratoryResearchMeta();
        assertFalse("Found tasks for PerformLabResearch meta tasks", lmt.getTaskJobs(p).isEmpty());
    }

    public void testStudyFieldMetaTask() {
        var s = buildSettlement("Study", true);

        var study = buildStudyToResearchPhase(s, this, ScienceType.CHEMISTRY, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        // Add some rocks to test
        int rockId = ResourceUtil.ROCK_IDS[0];
        s.storeAmountResource(rockId, 100D);

        var mmt = new StudyFieldSamplesMeta();
        assertFalse("Found tasks for Study Field meta tasks", mmt.getTaskJobs(p).isEmpty());
    }
    
    public void testMathMetaTask() {
        var s = buildSettlement("Study", true);

        var study = buildStudyToResearchPhase(s, this, ScienceType.MATHEMATICS, JobType.BOTANIST);
        var p = study.getPrimaryResearcher();

        var mmt = new PerformMathematicalModelingMeta();
        assertFalse("Found tasks for Perform Math Modelling meta tasks", mmt.getTaskJobs(p).isEmpty());
    }
}
