package com.mars_sim.core.science.task;


import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.mapdata.location.LocalPosition;

public class LabTaskTest extends AbstractMarsSimUnitTest {
    public void testPerformLabResearch() {
        // Build a settlemtn with a lab and put a Person in it
        var s = buildSettlement();
        var l = buildResearch(s.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
        var p = buildPerson("Researcher", s);
        BuildingManager.addPersonToActivitySpot(p, l, FunctionType.RESEARCH);

        // Build a study
        var study = sim.getScientificStudyManager().createScientificStudy(p, ScienceType.BIOLOGY, 10);
        assertEquals("Person assigned Study", study, p.getStudy());

        // COmplete Proposal
        study.addProposalWorkTime(study.getTotalProposalWorkTimeRequired() + 10D);
        study.timePassing(createPulse(sim.getMasterClock().getMarsTime(), false, false));

        // No collabarators so advance further
        study.timePassing(createPulse(sim.getMasterClock().getMarsTime(), false, false));
        assertEquals("Study start phase", ScientificStudy.RESEARCH_PHASE, study.getPhase());

        var task  = PerformLaboratoryResearch.createTask(p);
        assertNotNull("Task created", task);
        assertEquals("Task assigned Study", study, task.getStudy());
        assertEquals("Task assigned Lab", l.getResearch(), task.getLaboratory());

        // Do all the required research
        var origResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        executeTask(p, task, (int)(study.getTotalPrimaryResearchWorkTimeRequired() * 1.1/MSOLS_PER_EXECUTE));
        assertTrue("Lab task is done", task.isDone());
        var newResearchCompleted = study.getPrimaryResearchWorkTimeCompleted();
        assertGreaterThan("Primary research advanced", origResearchCompleted, newResearchCompleted);
    }
}
