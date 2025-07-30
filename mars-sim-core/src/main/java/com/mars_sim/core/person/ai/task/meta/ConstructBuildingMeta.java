/*
 * Mars Simulation Project
 * ConstructBuildingMeta.java
 * @date 2025-07-30
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.task.ConstructBuilding;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the ConstructBuilding task.
 */
public class ConstructBuildingMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.constructBuilding"); //$NON-NLS-1$

	private static final double WEIGHT = 100D;

    public ConstructBuildingMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.STRENGTH, TaskTrait.ARTISTIC);
		setPreferredJob(JobType.ARCHITECT, JobType.ENGINEER, JobType.TECHNICIAN);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ConstructBuilding(person);
    }

    /**
     * Assesses a person constructing a building. 
     * Assessment is based on role/job & number of construction missions.
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        // Probability affected by the person's stress and fatigue.
        if (person.isOutside()) {
        	return EMPTY_TASKLIST;
        }
        
        if (!person.getPhysicalCondition().isEVAFit()) {
            return EMPTY_TASKLIST;
        }
        
        if (EVAOperation.isGettingDark(person)) {
            return EMPTY_TASKLIST;
        }
        
        // Check all building construction missions occurring at the settlement.
        List<ConstructionMission> missions = ConstructBuilding.
                    getAllMissionsNeedingAssistance(person.getAssociatedSettlement());
		if (missions.isEmpty())
			return EMPTY_TASKLIST;
		
		// Avoid call getWalkableEgressAirlock since it involve pathfinding.
        if (EVAOperation.getWalkableEgressAirlock(person) == null) {
            return EMPTY_TASKLIST;
        }

        var score = new RatingScore(WEIGHT * missions.size());
        score = assessPersonSuitability(score, person);
        return createTaskJobs(score);
    }
}
