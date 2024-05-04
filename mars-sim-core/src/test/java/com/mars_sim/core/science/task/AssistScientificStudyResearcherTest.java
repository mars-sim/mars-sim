package com.mars_sim.core.science.task;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.social.RelationshipType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;

public class AssistScientificStudyResearcherTest extends AbstractMarsSimUnitTest {
    public void testCreateTask() {
        Settlement s = buildSettlement("Research", true);
        var research = createActiveResearch(s, ScienceType.BIOLOGY, JobType.BIOLOGIST);

        var assistant = buildPerson("Helper", s);
        assistant.setJob(JobType.BIOLOGIST, "Boss");
        assistant.getPhysicalCondition().setPerformanceFactor(1);
        assistant.getSkillManager().addNewSkill(SkillType.BIOLOGY, 1); // Must have higher skill

        var t = AssistScientificStudyResearcher.createTask(assistant);
        assertFalse("Task is not false", t.isDone());
        assertEquals("Assisted research", research,  t.getAssisted());
        assertEquals("Assistant ", assistant,  research.getResearchAssistant());

        // Assistent task does very little but changes relationship
        var researcher = research.getResearcher();
        RelationshipUtil.changeOpinion(researcher, assistant, RelationshipType.FACE_TO_FACE_COMMUNICATION, 1);
        var origOpinion = researcher.getRelation().getOpinion(assistant);
        executeTaskForDuration(assistant, t, 1000);
        var newOpinion = researcher.getRelation().getOpinion(assistant);
        assertGreaterThan("Opinion of researcher", origOpinion.getAverage(), newOpinion.getAverage());

        research.endTask(); // End rsearch task should end this assistance task
        executeTaskForDuration(assistant, t, 100);
        assertTrue("Assistance complete", t.isDone());
        assertNull("Assistant releases ", research.getResearchAssistant());
    }

    public void testMetaTask() {
        Settlement s = buildSettlement("Research", true);
        createActiveResearch(s, ScienceType.BIOLOGY, JobType.BIOLOGIST);

        var p = buildPerson("Helper", s);
        p.setJob(JobType.BIOLOGIST, "Boss");
        p.getPhysicalCondition().setPerformanceFactor(1);
        p.getSkillManager().addNewSkill(SkillType.BIOLOGY, 1); // Must have higher skill

        var mt = new AssistScientificStudyResearcherMeta();
        var tasks = mt.getTaskJobs(p);

        assertFalse("Tasks found for researcher", tasks.isEmpty());
    }

    public void testMetaTaskTooSkilled() {
        Settlement s = buildSettlement("Research", true);
        createActiveResearch(s, ScienceType.BIOLOGY, JobType.BIOLOGIST);

        var p = buildPerson("Helper", s);
        p.setJob(JobType.BIOLOGIST, "Boss");
        p.getPhysicalCondition().setPerformanceFactor(1);
        p.getSkillManager().addNewSkill(SkillType.BIOLOGY, 10); // Must have higher skill

        var mt = new AssistScientificStudyResearcherMeta();
        var tasks = mt.getTaskJobs(p);

        assertTrue("Tasks found for researcher", tasks.isEmpty());
    }

    private PerformLaboratoryResearch createActiveResearch(Settlement s, ScienceType science, JobType researchJob) {
        var study = LabTaskTest.buildStudyToResearchPhase(s, this, science, researchJob);

        var p = study.getPrimaryResearcher();
        p.getSkillManager().addNewSkill(science.getSkill(), 2); // Must have higher skill
        p.getPhysicalCondition().setPerformanceFactor(1);
        var task  = PerformLaboratoryResearch.createTask(p);
        p.getMind().getTaskManager().replaceTask(task);

        return task;
    }
}
