/**
 * Mars Simulation Project
 * SalvageBuildingMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.SalvageMission;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.SalvageBuilding;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the SalvageBuilding task.
 */
public class SalvageBuildingMeta extends MetaTask implements SettlementMetaTask {
    /**
     * Represents a Job needed in a Salvage Mission
     */
    private static class SalvageBuildingTaskJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        public SalvageBuildingTaskJob(SettlementMetaTask owner, SalvageMission mission, RatingScore score) {
            super(owner, "Salvage Building", mission, score);
            setEVA(true);
        }

        @Override
        public Task createTask(Person person) {
            return new SalvageBuilding(person, (SalvageMission) getFocus());
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.salvageBuilding"); //$NON-NLS-1$


    public SalvageBuildingMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.ARCHITECT);
	}

    /**
     * Create Settlement tasks for any salvage missions in fight.
     * @param target Being assessed.
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement target) {
        List<SettlementTask>  result = new ArrayList<>();
        if (!EVAOperation.isGettingDark(target)) {
            for(SalvageMission sm : getAllMissionsNeedingAssistance(target)) {
                RatingScore score = new RatingScore(300D);
                result.add(new SalvageBuildingTaskJob(this, sm, score));
            }
        }
        return result;
    }

     /**
     * Gets a list of all building salvage missions that need assistance at a settlement.
     * @param settlement the settlement.
     * @return list of building salvage missions.
     */
    private static List<SalvageMission> getAllMissionsNeedingAssistance(
            Settlement settlement) {

        return missionManager.getMissionsForSettlement(settlement).stream()
                        .filter(SalvageMission.class::isInstance)
                        .map (SalvageMission.class::cast)
                        .toList();
    }

    /**
     * Assess the suitability a person to do this settlement task for salvage.
     * @param person Being assessed
     * @return Rating 
     */
    @Override
    public RatingScore assessPersonSuitability(SettlementTask st, Person p) {
        RatingScore score = RatingScore.ZERO_RATING;
        if(p.isInSettlement()
            && p.getPhysicalCondition().isFitByLevel(1000, 70, 1000)
            && (EVAOperation.getWalkableAvailableAirlock(p, false) != null)) {
            
            score = super.assessPersonSuitability(st, p);
        }
        return score;
    }
}
