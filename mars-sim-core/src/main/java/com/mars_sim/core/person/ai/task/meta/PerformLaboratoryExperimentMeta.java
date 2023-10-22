/*
 * Mars Simulation Project
 * PerformLaboratoryExperimentMeta.java
 * @date 2023-08-12
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;
import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.PerformLaboratoryExperiment;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskProbabilityUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Lab;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the PerformLaboratoryExperiment task.
 */
public class PerformLaboratoryExperimentMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.performLaboratoryExperiment"); //$NON-NLS-1$


    // Load experimental sciences.
    private static Set<ScienceType> experimentalSciences = ScienceType.getExperimentalSciences();

    public PerformLaboratoryExperimentMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);

		setFavorite(FavoriteType.LAB_EXPERIMENTATION);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.ACADEMICS);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return new PerformLaboratoryExperiment(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        // Probability affected by the person's stress and fatigue.
        if (!person.isInside()
            || !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
        	return EMPTY_TASKLIST;
        }

        ScienceType jobScience = TaskProbabilityUtil.getPersonJobScience(person);
        double base = 0D;

        // Add probability for researcher's primary study (if any).
        ScientificStudy primaryStudy = person.getStudy();
        if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())
                && !primaryStudy.isPrimaryResearchCompleted()
                && experimentalSciences.contains(primaryStudy.getScience())) {
            Lab lab = PerformLaboratoryExperiment.getLocalLab(person, primaryStudy.getScience());
            if (lab != null) {
                double primaryResult = 50D;

                // Get lab building crowding modifier.
                primaryResult *= PerformLaboratoryExperiment.getLabCrowdingModifier(person, lab);

                // If researcher's current job isn't related to study science, divide by two.
                if ((jobScience != null) && (primaryStudy.getScience() != jobScience)) {
                    primaryResult /= 2D;
                }

                base += primaryResult;
            }
        }

	    // Add probability for each study researcher is collaborating on.
	    for(ScientificStudy collabStudy : person.getCollabStudies()) {
            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())
                    && !collabStudy.isCollaborativeResearchCompleted(person)) {
                ScienceType collabScience = collabStudy.getContribution(person);
                if (experimentalSciences.contains(collabScience)) {
                    Lab lab = PerformLaboratoryExperiment.getLocalLab(person, collabScience);
                    if (lab != null) {
                        double collabResult = 25D;

                        // Get lab building crowding modifier.
                        collabResult *= PerformLaboratoryExperiment.getLabCrowdingModifier(person, lab);

                        // If researcher's current job isn't related to study science, divide by two.
                        if ((jobScience != null) && (collabScience != jobScience)) {
                            collabResult /= 2D;
                        }

                        base += collabResult;
                    }
                }
	        }
        }

        if (base <= 0) {
            return EMPTY_TASKLIST;
        }
	       
        RatingScore result = new RatingScore(base);
	    result.addModifier(GOODS_MODIFIER, person.getAssociatedSettlement().getGoodsManager().getResearchFactor());
        result = assessPersonSuitability(result, person);

        return createTaskJobs(result);
    }
}
