/*
 * Mars Simulation Project
 * ProposeScientificStudyMeta.java
 * @date 2023-08-12
 * @author Scott Davis
 */
package com.mars_sim.core.science.task;

import java.util.List;
import java.util.Set;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.Role;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.meta.ScienceParameters;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.StudyStatus;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for proposing a scientific study.
 */
public class ProposeScientificStudyMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.proposeScientificStudy"); //$NON-NLS-1$

    public ProposeScientificStudyMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC, TaskTrait.LEADERSHIP);
		setPreferredJob(JobType.ACADEMICS);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST,
				RoleType.CREW_SCIENTIST);
	}

    @Override
    public Task constructInstance(Person person) {
        return ProposeScientificStudy.createTask(person);
    }

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

		Settlement settlement = person.getAssociatedSettlement();

		ScienceType science = TaskUtil.getPersonJobScience(person);
        if ((science == null)
			|| !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)
			|| !person.isInside()) {
        	return EMPTY_TASKLIST;
		}

		double base;
		RoleType roleType = RoleType.MAYOR; // Use Mayor as a useless default
		Role role = person.getRole();
		if (role != null) {
			var assigned = role.getType();
			roleType = (assigned != null ? assigned : roleType);
		}
		ScientificStudy study = person.getResearchStudy().getStudy();
		if (study == null) {
			// Probability of starting a new scientific study based on Job
			int pop = settlement.getInitialPopulation();
			Set<JobType> targetJobs = JobType.SCIENTISTS;
			JobType job = person.getMind().getJob();

			if (pop <= 6) {
				targetJobs = JobType.ACADEMICS;
			}
			else if (pop <= 12) {
				targetJobs = JobType.INTELLECTUALS;
			}
			if (!targetJobs.contains(job)) {
				return EMPTY_TASKLIST;
			}
		
			// Check person has a science role
			base = switch(roleType) {
				case CHIEF_OF_SCIENCE, SCIENCE_SPECIALIST, CREW_SCIENTIST -> 20D;
				case CHIEF_OF_COMPUTING, COMPUTING_SPECIALIST -> 15D;
				case CHIEF_OF_AGRICULTURE, AGRICULTURE_SPECIALIST -> 10D;
				default -> 5D;
			};
		}

		// Check if assigned study is in proposal phase.
		else if (study.getPhase() == StudyStatus.PROPOSAL_PHASE) {
			// Once a person starts a study in the proposal phase,
			// there's a greater chance to continue on the proposal so give a high base score
			base = switch(roleType) {
				case CHIEF_OF_SCIENCE -> 5D;
				case SCIENCE_SPECIALIST -> 2.5D;
				default -> 1D;
			};
			science = study.getScience();
		}
		else {
			return EMPTY_TASKLIST;
		}

		RatingScore result = new RatingScore(base);
		result.addModifier(SCIENCE_MODIFIER, settlement.getPreferences()
								.getDoubleValue(ScienceParameters.INSTANCE, science.name(), 1D));
		// Crowding modifier
		if (person.isInSettlement()) {
			Building b = BuildingManager.getAvailableBuilding(science, person);
			result = assessBuildingSuitability(result, b, person);
		}

		result = applyCommerceFactor(result, settlement, CommerceType.RESEARCH);
	    result = assessPersonSuitability(result, person);

        return createTaskJobs(result);
    }
}