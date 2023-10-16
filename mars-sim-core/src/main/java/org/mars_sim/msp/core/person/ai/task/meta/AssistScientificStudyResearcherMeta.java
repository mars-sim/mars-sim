/**
 * Mars Simulation Project
 * AssistScientificStudyResearcherMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.Collection;
import java.util.List;

import org.mars_sim.msp.core.data.RatingScore;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.AssistScientificStudyResearcher;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.tools.Msg;
import org.mars_sim.tools.util.RandomUtil;

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
