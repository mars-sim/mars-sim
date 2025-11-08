package com.mars_sim.core.science.task;
import static com.mars_sim.core.test.SimulationAssertions.assertGreaterThan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.social.RelationshipType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;

public class AssistScientificStudyResearcherTest extends MarsSimUnitTest {
    @Test
    public void testCreateTask() {
        Settlement s = buildSettlement("Research", true);
        var research = createActiveResearch(s, ScienceType.ASTROBIOLOGY, JobType.ASTROBIOLOGIST);

        var assistant = buildPerson("Helper", s);
        assistant.setJob(JobType.ASTROBIOLOGIST, "Boss");
        assistant.getPhysicalCondition().setPerformanceFactor(1);
        assistant.getSkillManager().addNewSkill(SkillType.ASTROBIOLOGY, 1); // Must have higher skill

        var t = AssistScientificStudyResearcher.createTask(assistant);
        assertFalse(t.isDone(), "Task is not false");
        assertEquals(research, t.getAssisted(), "Assisted research");
        assertEquals(assistant, research.getResearchAssistant(), "Assistant ");

        // Assistant task does very little but changes relationship
        var researcher = research.getResearcher();
        RelationshipUtil.changeOpinion(researcher, assistant, RelationshipType.FACE_TO_FACE_COMMUNICATION, 1);
        var origOpinion = researcher.getRelation().getOpinion(assistant);
        executeTaskForDuration(assistant, t, 1000);
        var newOpinion = researcher.getRelation().getOpinion(assistant);
        assertGreaterThan("Opinion of researcher", origOpinion.getAverage(), newOpinion.getAverage());

        research.endTask(); // End research task should end this assistance task
        executeTaskForDuration(assistant, t, 100);
        assertTrue(t.isDone(), "Assistance complete");
        assertNull(research.getResearchAssistant(), "Assistant releases ");
    }

    @Test
    public void testMetaTask() {
        Settlement s = buildSettlement("Research", true);
        createActiveResearch(s, ScienceType.ASTROBIOLOGY, JobType.ASTROBIOLOGIST);

        var p = buildPerson("Helper", s);
        p.setJob(JobType.ASTROBIOLOGIST, "Boss");
        p.getPhysicalCondition().setPerformanceFactor(1);
        p.getSkillManager().addNewSkill(SkillType.ASTROBIOLOGY, 1); // Must have higher skill

        var mt = new AssistScientificStudyResearcherMeta();
        var tasks = mt.getTaskJobs(p);

        assertFalse(tasks.isEmpty(), "Tasks found for researcher");
    }

    @Test
    public void testMetaTaskTooSkilled() {
        Settlement s = buildSettlement("Research", true);
        createActiveResearch(s, ScienceType.ASTROBIOLOGY, JobType.ASTROBIOLOGIST);

        var p = buildPerson("Helper", s);
        p.setJob(JobType.ASTROBIOLOGIST, "Boss");
        p.getPhysicalCondition().setPerformanceFactor(1);
        p.getSkillManager().addNewSkill(SkillType.ASTROBIOLOGY, 10); // Must have higher skill

        var mt = new AssistScientificStudyResearcherMeta();
        var tasks = mt.getTaskJobs(p);

        assertTrue(tasks.isEmpty(), "Tasks found for researcher");
    }

    private PerformLaboratoryResearch createActiveResearch(Settlement s, ScienceType science, JobType researchJob) {
        var study = LabTaskTest.buildStudyToResearchPhase(s, getContext(), science, researchJob);

        var p = study.getPrimaryResearcher();
        p.getSkillManager().addNewSkill(science.getSkill(), 2); // Must have higher skill
        p.getPhysicalCondition().setPerformanceFactor(1);
        var task  = PerformLaboratoryResearch.createTask(p);
        p.getMind().getTaskManager().replaceTask(task);

        return task;
    }
}
