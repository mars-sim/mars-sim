/**
 * Mars Simulation Project
 * AssistScientificStudyResearcherMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.Collection;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.AssistScientificStudyResearcher;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * Meta task for the AssistScientificStudyResearcher task.
 */
public class AssistScientificStudyResearcherMeta extends FactoryMetaTask {

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.assistScientificStudyResearcher"); //$NON-NLS-1$
    
    public AssistScientificStudyResearcherMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.RESEARCH);
		setTrait(TaskTrait.ACADEMIC);
		
		setPreferredJob(JobType.ACADEMICS);
		setPreferredRole(RoleType.CHIEF_OF_SCIENCE, RoleType.SCIENCE_SPECIALIST);
	}

	@Override
    public Task constructInstance(Person person) {
        return new AssistScientificStudyResearcher(person);
    }

	/**
	 * Assess if a Person can assist a scietific study.The assessment is based on who
	 * is doing research already.
	 * @param person Being assessed
	 * @return Potential Task jobs for this Person
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
        
        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)
			|| !person.isInside()) {
        	return EMPTY_TASKLIST;
        }
        
		// Find potential researchers.
		Collection<Person> potentialResearchers = AssistScientificStudyResearcher.getBestResearchers(person);
		int size = potentialResearchers.size();
		if (size == 0)
        	return EMPTY_TASKLIST;
	        
	    RatingScore result = new RatingScore((double)size * RandomUtil.getRandomInt(1, 10));
        Person researcher = (Person) potentialResearchers.toArray()[0];

		// If assistant is in a settlement, use crowding modifier.
		Building building = BuildingManager.getBuilding(researcher);
		assessBuildingSuitability(result, building, person);
		assessPersonSuitability(result, person);
		result.addModifier(GOODS_MODIFIER,
					person.getAssociatedSettlement().getGoodsManager().getResearchFactor());
		
        return createTaskJobs(result);
    }
}
