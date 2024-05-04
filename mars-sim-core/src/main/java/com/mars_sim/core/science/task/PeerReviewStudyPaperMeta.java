/*
 * Mars Simulation Project
 * PeerReviewStudyPaperMeta.java
 * @date 2022-07-26
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.List;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the PeerReviewStudyPaper task.
 */
public class PeerReviewStudyPaperMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.peerReviewStudyPaper"); //$NON-NLS-1$

    public PeerReviewStudyPaperMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC, TaskTrait.TEACHING);
		setPreferredJob(JobType.ACADEMICS);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST,
				RoleType.CREW_SCIENTIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return PeerReviewStudyPaper.createTask(person);
    }

	/**
	 * Assesses a person doing a review of a study paper. Assessment is based on the Person having a job
	 * that is associated with the science subject of a Study that is in the review phase.
	 * 
	 * @param person Being assessed
	 * @return List of reviews needed.
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        if (!person.isInside() ||
				!person.getPhysicalCondition().isFitByLevel(800, 80, 800)) {
            return EMPTY_TASKLIST;
		}

		var jobScience = TaskUtil.getPersonJobScience(person);
	    if (jobScience == null) {
			return EMPTY_TASKLIST;
		}		

	    // Get all studies in the peer review phase.
		double base = 0D;
        ScientificStudyManager sm = Simulation.instance().getScientificStudyManager();
	    for(ScientificStudy study : sm.getAllStudies(false)) {
			// Study needs peer review phase and person cannot be contributing
			// plus Person must have a job that is suitable for the Study subject
	        if (StudyStatus.PEER_REVIEW_PHASE == study.getPhase()
	        	&& !person.equals(study.getPrimaryResearcher())
				&& !study.getCollaborativeResearchers().contains(person)
				&& (study.getScience() == jobScience)) {
				base += 50D;
			}
		}

		RatingScore result = new RatingScore(base);
		result = applyCommerceFactor(result, person.getAssociatedSettlement(), CommerceType.RESEARCH);
		result = assessPersonSuitability(result, person);
        return createTaskJobs(result);
    }

}
