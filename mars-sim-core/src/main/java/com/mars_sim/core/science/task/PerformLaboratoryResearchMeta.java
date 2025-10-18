/*
 * Mars Simulation Project
 * PerformLaboratoryResearchMeta.java
 * @date 2025-10-11
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Lab;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the PerformLaboratoryResearch task.
 */
public class PerformLaboratoryResearchMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.performLaboratoryResearch"); //$NON-NLS-1$

    public PerformLaboratoryResearchMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);

		setFavorite(FavoriteType.LAB_EXPERIMENTATION, FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC);

		// Jobs are the lab technicians and some scientists
		Set<JobType> jobs = new HashSet<>(JobType.SCIENTISTS);
		jobs.add(JobType.MATHEMATICIAN);
		jobs.add(JobType.COMPUTER_SCIENTIST);
		jobs.add(JobType.METEOROLOGIST);
		setPreferredJob(jobs);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST,
				RoleType.CREW_SCIENTIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return PerformLaboratoryResearch.createTask(person);
    }

    /**
	 * Assess if this Person can perform any laboratory based research. Find all research
     * perosn is collaborating in
	 * 
	 * @param person the Person to perform the task.
	 * @return List of TasksJob specifications.
	 */
    @Override
	public List<TaskJob> getTaskJobs(Person person) {

        ScientificStudy primaryStudy = person.getResearchStudy().getStudy();
        
        if ((primaryStudy == null) || !person.isInSettlement()
            || !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
        	return EMPTY_TASKLIST;
        }
        
        // Add probability for researcher's primary study (if any).
        double base = 0D;
   
        if (primaryStudy != null) {
			boolean isOngoing = (StudyStatus.PROPOSAL_PHASE == primaryStudy.getPhase()
					|| StudyStatus.INVITATION_PHASE == primaryStudy.getPhase()
					|| 	StudyStatus.RESEARCH_PHASE == primaryStudy.getPhase()
					|| StudyStatus.PAPER_PHASE == primaryStudy.getPhase());
				
			if (isOngoing && !primaryStudy.isPrimaryResearchCompleted()) {
				 base += getStudyScore(person, 50D, primaryStudy.getScience());
			}
		}

	    // Add probability for each study researcher is collaborating on.
	    for (ScientificStudy collabStudy : person.getResearchStudy().getCollabStudies()) {
	    	
	    	if (collabStudy != null) {

				boolean isOngoing = (StudyStatus.PROPOSAL_PHASE == collabStudy.getPhase()
						|| StudyStatus.INVITATION_PHASE == collabStudy.getPhase()
						|| 	StudyStatus.RESEARCH_PHASE == collabStudy.getPhase()
						|| StudyStatus.PAPER_PHASE == collabStudy.getPhase());
	    	
		        if (isOngoing && !collabStudy.isCollaborativeResearchCompleted(person)) {
		             ScienceType collabScience = collabStudy.getContribution(person);
		             base += getStudyScore(person, 25D, collabScience);
		        }
			}
        }

        if (base <= 0)
            return EMPTY_TASKLIST;

        var result = new RatingScore(base);
        result = applyCommerceFactor(result, person.getAssociatedSettlement(), CommerceType.RESEARCH);
        result = assessPersonSuitability(result, person);
        return createTaskJobs(result);
    }

    /**
     * Assess the score of doing a study by a person. This involves finding a suitable lab.
     * @param person Being assessed
     * @param base Initial base score
     * @param science Science of the study.
     */
    private double getStudyScore(Person person, double base, ScienceType science) {
        double score = 0D;
        Lab lab = LabTask.getLocalLab(person, science);
        if (lab != null) {
            score = base;

            // Get lab building crowding modifier.
            score *= LabTask.getLabCrowdingModifier(person, lab);

            // If researcher's current job isn't related to study science, divide by two.
            JobType job = person.getMind().getJobType();
            if (job != null) {
                ScienceType jobScience = ScienceType.getJobScience(job);
                if (science != jobScience) {
                    score /= 2D;
                }
            }
        }
        return score;
    }
}
