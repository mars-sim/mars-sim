/**
 * Mars Simulation Project
 * CompileScientificStudyResultsMeta.java
 * @version 3.2.0 2021-06-20
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
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the CompileScientificStudyResults task.
 */
public class CompileScientificStudyResultsMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.compileScientificStudyResults"); //$NON-NLS-1$


    public CompileScientificStudyResultsMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC);
		setPreferredJob(JobType.ACADEMICS);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST,
				RoleType.CREW_SCIENTIST);
		
	}

    @Override
    public Task constructInstance(Person person) {
        return CompileScientificStudyResults.createTask(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)
            || !person.isInside()) {
        	return EMPTY_TASKLIST;
        }


        // Does the researcher have a science associated to their job
        ScienceType jobScience = TaskUtil.getPersonJobScience(person);

        // Add probability for researcher's primary study (if any).
        double base = 0D;
        ScienceType targetScience = null;
        ScientificStudy primaryStudy = person.getStudy();
        if ((primaryStudy != null)
            && StudyStatus.PAPER_PHASE == primaryStudy.getPhase()
            && !primaryStudy.isPrimaryPaperCompleted()) {
            double primaryResult = 50D;

            targetScience = primaryStudy.getScience();
            // If researcher's current job isn't related to study science, divide by two.
            if ((jobScience != null) && (targetScience != jobScience)) {
                primaryResult /= 2D;
            }

            base += primaryResult;
        }

	    // Add probability for each study researcher is collaborating on.
	    for(ScientificStudy collabStudy : person.getCollabStudies()) {
            if (StudyStatus.PAPER_PHASE.equals(collabStudy.getPhase())
                    && !collabStudy.isCollaborativePaperCompleted(person)) {
                ScienceType collabScience = collabStudy.getContribution(person);
                if (targetScience == null) {
                    targetScience = collabScience;
                }

                double collabResult = 25D;

                // If researcher's current job isn't related to study science, divide by two.
                if ((jobScience != null) && (collabScience != jobScience)) {
                    collabResult /= 2D;
                }

                base += collabResult;
            }
        }

        if (base <= 0) {
            return EMPTY_TASKLIST;
        }

	    RatingScore result = new RatingScore(base);
        Building b = BuildingManager.getAvailableBuilding(targetScience, person);
        result = assessBuildingSuitability(result, b, person);
        result = applyCommerceFactor(result, person.getAssociatedSettlement(), CommerceType.RESEARCH);
        result = assessPersonSuitability(result, person);

        return createTaskJobs(result);
    }
}
