/*
 * Mars Simulation Project
 * ProposeScientificStudyMeta.java
 * @date 2023-08-12
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;
import java.util.Set;

import com.mars_sim.core.authority.PreferenceCategory;
import com.mars_sim.core.authority.PreferenceKey;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.Role;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.ProposeScientificStudy;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.tools.Msg;

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
        return new ProposeScientificStudy(person);
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

		double base = 0D;
		RoleType roleType = RoleType.MAYOR; // Use Mayor as a useless default
		Role role = person.getRole();
		if (role != null) {
			roleType = role.getType();
		}
		ScientificStudy study = person.getStudy();
		if (study != null) {
			// Check if study is in proposal phase.
			if (study.getPhase().equals(ScientificStudy.PROPOSAL_PHASE)) {
				// Once a person starts a study in the proposal phase,
				// there's a greater chance to continue on the proposal so give a high base score
				base = switch(roleType) {
					case CHIEF_OF_SCIENCE -> 100D;
					case SCIENCE_SPECIALIST -> 75D;
					default -> 25D;
				};
			}
		}
		else {
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
		if (base <= 0) {
			return EMPTY_TASKLIST;
		}

		RatingScore result = new RatingScore(base);
		result.addModifier(SCIENCE_MODIFIER, settlement.getPreferenceModifier(
								new PreferenceKey(PreferenceCategory.SCIENCE, science.name())));
		// Crowding modifier
		if (person.isInSettlement()) {
			Building b = BuildingManager.getAvailableBuilding(study, person);
			result = assessBuildingSuitability(result, b, person);
		}

	    result.addModifier(GOODS_MODIFIER, settlement.getGoodsManager().getResearchFactor());
	    result = assessPersonSuitability(result, person);

        return createTaskJobs(result);
    }
}