/*
 * Mars Simulation Project
 * StudyFieldSamplesMeta.java
 * @date 2023-04-15
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Lab;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the StudyFieldSamples task.
 */
public class StudyFieldSamplesMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.studyFieldSamples"); //$NON-NLS-1$
    
    public StudyFieldSamplesMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		
		setFavorite(FavoriteType.FIELD_WORK);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.AREOLOGIST, JobType.ASTROBIOLOGIST,
						JobType.BOTANIST, JobType.CHEMIST, JobType.METEOROLOGIST, JobType.REPORTER);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST,
				RoleType.CREW_SCIENTIST);
	}
    
    @Override
    public Task constructInstance(Person person) {
        return StudyFieldSamples.createTask(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)
			|| !person.isInside()) {
        	return EMPTY_TASKLIST;
		}
	
		// Check that there are available field samples to study.
		double mostStored = 0D;

		var container = person.getContainerUnit();
		if (container instanceof ResourceHolder rh) {
			for (int i: ResourceUtil.ROCK_IDS) {
				double stored = rh.getSpecificAmountResourceStored(i);
				mostStored = Math.max(stored, mostStored);
			}
		}
		if (mostStored < StudyFieldSamples.SAMPLE_MASS) {
			return EMPTY_TASKLIST;
		}
		double result = mostStored/10.0;
  
		// Create list of possible sciences for studying field samples.
		var jobScience = TaskUtil.getPersonJobScience(person);

		// Add probability for researcher's primary study (if any).
		ScientificStudy primaryStudy = person.getResearchStudy().getStudy();
		
		if (primaryStudy != null) {
			boolean isOngoing = (StudyStatus.PROPOSAL_PHASE == primaryStudy.getPhase()
					|| StudyStatus.INVITATION_PHASE == primaryStudy.getPhase()
					|| 	StudyStatus.RESEARCH_PHASE == primaryStudy.getPhase()
					|| StudyStatus.PAPER_PHASE == primaryStudy.getPhase());
				
			if (isOngoing
				&& !primaryStudy.isPrimaryResearchCompleted()
				&& StudyFieldSamples.FIELD_SCIENCES.contains(primaryStudy.getScience())) {
				Lab lab = LabTask.getLocalLab(person, primaryStudy.getScience());
				if (lab != null) {
					double primaryResult = 50D;
	
					// Get lab building crowding modifier.
					primaryResult *= LabTask.getLabCrowdingModifier(person, lab);
					if (primaryStudy.getScience() == jobScience) {
						primaryResult /= 2D;
					}
	
					result += primaryResult;
				}
			}
		}
	
	    // Add probability for each study researcher is collaborating on.
	    for (ScientificStudy collabStudy : person.getResearchStudy().getCollabStudies()) {
	    	
			if (collabStudy != null) {

				boolean isOngoing = (StudyStatus.PROPOSAL_PHASE == collabStudy.getPhase()
						|| StudyStatus.INVITATION_PHASE == collabStudy.getPhase()
						|| 	StudyStatus.RESEARCH_PHASE == collabStudy.getPhase()
						|| StudyStatus.PAPER_PHASE == collabStudy.getPhase());
	    	
		        if (isOngoing
		            && !collabStudy.isCollaborativeResearchCompleted(person)) {
		            ScienceType collabScience = collabStudy.getContribution(person);
		            if (StudyFieldSamples.FIELD_SCIENCES.contains(collabScience)) {
						Lab lab = LabTask.getLocalLab(person, collabScience);
						if (lab != null) {
							double collabResult = 25D;
	
							// Get lab building crowding modifier.
							collabResult *= LabTask.getLabCrowdingModifier(person, lab);
	
							if (collabScience == jobScience) {
								collabResult /= 2D;
							}
	
							result += collabResult;
						}
		            }
		        }
			}
	    }
	    
		var score = new RatingScore(result);
		score = assessPersonSuitability(score, person);
        return createTaskJobs(score);
    }
}
