/*
 * Mars Simulation Project
 * StudyFieldSamplesMeta.java
 * @date 2023-04-15
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.Unit;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.equipment.ResourceHolder;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.StudyFieldSamples;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Lab;
import com.mars_sim.tools.Msg;

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
		setPreferredJob(JobType.AREOLOGIST, JobType.BIOLOGIST,
						JobType.BOTANIST, JobType.CHEMIST);
	}
    
    @Override
    public Task constructInstance(Person person) {
        return new StudyFieldSamples(person);
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

		Unit container = person.getContainerUnit();
		if (container instanceof ResourceHolder rh) {
			for (int i: ResourceUtil.rockIDs) {
				double stored = rh.getAmountResourceStored(i);
				mostStored = Math.max(stored, mostStored);
			}
		}
		if (mostStored < StudyFieldSamples.SAMPLE_MASS) {
			return EMPTY_TASKLIST;
		}
		double result = mostStored/10.0;
  
		// Create list of possible sciences for studying field samples.
		List<ScienceType> fieldSciences = StudyFieldSamples.getFieldSciences();
		var jobScience = TaskUtil.getPersonJobScience(person);

		// Add probability for researcher's primary study (if any).
		ScientificStudy primaryStudy = person.getStudy();
		if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())
			&& !primaryStudy.isPrimaryResearchCompleted()
			&& fieldSciences.contains(primaryStudy.getScience())) {
			Lab lab = StudyFieldSamples.getLocalLab(person, primaryStudy.getScience());
			if (lab != null) {
				double primaryResult = 50D;

				// Get lab building crowding modifier.
				primaryResult *= StudyFieldSamples.getLabCrowdingModifier(person, lab);
				if (primaryStudy.getScience() != jobScience) {
					primaryResult /= 2D;
				}

				result += primaryResult;
			}
		}
	
	    // Add probability for each study researcher is collaborating on.
	    for(ScientificStudy collabStudy : person.getCollabStudies()) {
	        if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())
	            && !collabStudy.isCollaborativeResearchCompleted(person)) {
	            ScienceType collabScience = collabStudy.getContribution(person);
	            if (fieldSciences.contains(collabScience)) {
					Lab lab = StudyFieldSamples.getLocalLab(person, collabScience);
					if (lab != null) {
						double collabResult = 25D;

						// Get lab building crowding modifier.
						collabResult *= StudyFieldSamples.getLabCrowdingModifier(person, lab);

						if (collabScience != jobScience) {
							collabResult /= 2D;
						}

						result += collabResult;
					}
	            }
	        }
	    }
        
		var score = new RatingScore(result);
		score = assessPersonSuitability(score, person);
        return createTaskJobs(score);
    }
}
