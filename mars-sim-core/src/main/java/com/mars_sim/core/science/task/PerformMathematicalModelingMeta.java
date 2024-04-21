/*
 * Mars Simulation Project
 * PerformMathematicalModelingMeta.java
 * @Date 2021-12-20
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.List;

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
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Lab;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the PerformMathematicalModeling task.
 */
public class PerformMathematicalModelingMeta extends FactoryMetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.performMathematicalModeling"); //$NON-NLS-1$
    
    public PerformMathematicalModelingMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.MATHEMATICIAN, JobType.PHYSICIST, 
				JobType.COMPUTER_SCIENTIST, JobType.ENGINEER);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST,
				RoleType.CREW_SCIENTIST);
	}


    @Override
    public Task constructInstance(Person person) {
        return PerformMathematicalModeling.createTask(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        ScientificStudy primaryStudy = person.getStudy();
        if ((primaryStudy == null) 
            || !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)
            || !person.isInside()) {
        	return EMPTY_TASKLIST;
        }

        // Check the science
        double result = 0D;
        var jobScience = TaskUtil.getPersonJobScience(person);

        // Add probability for researcher's primary study (if any).
        if (ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())
            && !primaryStudy.isPrimaryResearchCompleted()
            && ScienceType.MATHEMATICS == primaryStudy.getScience()) {
            Lab lab = LabTask.getLocalLab(person, ScienceType.MATHEMATICS);
            if (lab != null) {
                double primaryResult = 50D;
                // Get lab building crowding modifier.
                primaryResult *= LabTask.getLabCrowdingModifier(person, lab);
                if (primaryStudy.getScience() != jobScience) {
                    primaryResult /= 2D;
                }

                result += primaryResult;
                
                // Check if person is in a moving rover.
                if (person.isInVehicle() && Vehicle.inMovingRover(person)) {
                    // the bonus for being inside a vehicle since there's little things to do
                    result += 20D;
                }
            }
        }

	    // Add probability for each study researcher is collaborating on.
	    for(ScientificStudy collabStudy : person.getCollabStudies()) {
            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())
                    && !collabStudy.isCollaborativeResearchCompleted(person)) {
                ScienceType collabScience = collabStudy.getContribution(person);
                if (ScienceType.MATHEMATICS == collabScience) {
                    Lab lab = LabTask.getLocalLab(person, ScienceType.MATHEMATICS);
                    if (lab != null) {
                        double collabResult = 25D;
                        // Get lab building crowding modifier.
                        collabResult *= LabTask.getLabCrowdingModifier(person, lab);
                        // If researcher's current job isn't related to study science, divide by two.
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
        score = applyCommerceFactor(score, person.getAssociatedSettlement(), CommerceType.RESEARCH);
        return createTaskJobs(score);
    }
}
