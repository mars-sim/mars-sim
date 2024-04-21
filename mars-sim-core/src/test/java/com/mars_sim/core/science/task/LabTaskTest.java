package com.mars_sim.core.science.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.LocalPosition;

public class LabTaskTest extends AbstractMarsSimUnitTest {
    private ScientificStudy buildStudy(ScienceType science) {
        // Build a settlemtn with a lab and put a Person in it
        var s = buildSettlement("Study", true);
        var l = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        var p = buildPerson("Researcher", s);
        BuildingManager.addPersonToActivitySpot(p, l, FunctionType.RESEARCH);

        // Build a study
        var study = sim.getScientificStudyManager().createScientificStudy(p, science, 10);
        assertEquals("Person assigned Study", study, p.getStudy());

        // COmplete Proposal
        study.addProposalWorkTime(study.getTotalProposalWorkTimeRequired() + 10D);
        study.timePassing(createPulse(sim.getMasterClock().getMarsTime(), false, false));

        // No collabarators so advance further
        study.timePassing(createPulse(sim.getMasterClock().getMarsTime(), false, false));
        assertEquals("Study start phase", ScientificStudy.RESEARCH_PHASE, study.getPhase());
        return study;
    }

    public void testStudyFieldSamples() {
        var study = buildStudy(ScienceType.CHEMISTRY);
        var p = study.getPrimaryResearcher();
        var s = p.getAssociatedSettlement();

        // Add some rocks to test
        int rockId = ResourceUtil.rockIDs[0];
        s.storeAmountResource(rockId, 100D);

        var task  = StudyFieldSamples.createTask(p);
        assertNotNull("Task created", task);
        assertEquals("Task assigned Study", study, task.getStudy());
        assertNotNull("Task assigned Lab", task.getLaboratory());

        // Do all the required research
        var origResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        executeTask(p, task, (int)(study.getTotalPrimaryResearchWorkTimeRequired() * 1.1/MSOLS_PER_EXECUTE));
        assertTrue("Lab task is done", task.isDone());
        var newResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        assertGreaterThan("Primary research advanced", origResearchCompleted, newResearchCompleted);

        // Check collected resources increased
        assertGreaterThan("Collected samples", 0D, s.getResourceCollected(rockId));
    }

    public void testPerformLabResearch() {
        var study = buildStudy(ScienceType.BIOLOGY);
        var p = study.getPrimaryResearcher();

        var task  = PerformLaboratoryResearch.createTask(p);
        assertNotNull("Task created", task);
        assertEquals("Task assigned Study", study, task.getStudy());
        assertNotNull("Task assigned Lab", task.getLaboratory());

        // Do all the required research
        var origResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        executeTask(p, task, (int)(study.getTotalPrimaryResearchWorkTimeRequired() * 1.1/MSOLS_PER_EXECUTE));
        assertTrue("Lab task is done", task.isDone());
        var newResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        assertGreaterThan("Primary research advanced", origResearchCompleted, newResearchCompleted);
    }

    public void testMathModelling() {
        var study = buildStudy(ScienceType.MATHEMATICS);
        var p = study.getPrimaryResearcher();

        var task  = PerformMathematicalModeling.createTask(p);
        assertNotNull("Task created", task);
        assertEquals("Task assigned Study", study, task.getStudy());
        assertNotNull("Task assigned Lab", task.getLaboratory());

        // Do all the required research
        var origResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        executeTask(p, task, (int)(study.getTotalPrimaryResearchWorkTimeRequired() * 1.1/MSOLS_PER_EXECUTE));
        assertTrue("Lab task is done", task.isDone());
        var newResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        assertGreaterThan("Primary research advanced", origResearchCompleted, newResearchCompleted);
    }

    public void testLabMetaTask() {
        var study = buildStudy(ScienceType.BIOLOGY);
        var p = study.getPrimaryResearcher();

        var lmt = new PerformLaboratoryResearchMeta();
        assertFalse("Found tasks for PerformLabResearch meta tasks", lmt.getTaskJobs(p).isEmpty());
    }

    public void testStudyFieldMetaTask() {
        var study = buildStudy(ScienceType.CHEMISTRY);
        var p = study.getPrimaryResearcher();

        // Add some rocks to test
        int rockId = ResourceUtil.rockIDs[0];
        p.getAssociatedSettlement().storeAmountResource(rockId, 100D);

        var mmt = new StudyFieldSamplesMeta();
        assertFalse("Found tasks for Study Field meta tasks", mmt.getTaskJobs(p).isEmpty());
    }
    
    public void testMathMetaTask() {
        var study = buildStudy(ScienceType.MATHEMATICS);
        var p = study.getPrimaryResearcher();

        var mmt = new PerformMathematicalModelingMeta();
        assertFalse("Found tasks for Perform Math Modelling meta tasks", mmt.getTaskJobs(p).isEmpty());
    }
}
