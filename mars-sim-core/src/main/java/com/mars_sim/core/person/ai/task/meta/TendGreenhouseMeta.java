/*
 * Mars Simulation Project
 * TendGreenhouseMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.TendGreenhouse;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.LifeSupport;
import com.mars_sim.core.structure.building.function.farming.Farming;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the Tend Greenhouse task.
 */
public class TendGreenhouseMeta extends MetaTask implements SettlementMetaTask {

    private static class CropTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

        private Farming farm;

        public CropTaskJob(SettlementMetaTask owner, Farming farm, int demand, RatingScore score) {
            super(owner, "Tend crop", farm.getBuilding(), score);
            this.farm = farm;

            setDemand(demand);
        }

        @Override
        public Task createTask(Person person) {
            return new TendGreenhouse(person, farm);
        }

        @Override
        public Task createTask(Robot robot) {
            return new TendGreenhouse(robot, farm);
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendGreenhouse"); //$NON-NLS-1$

    public TendGreenhouseMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.TENDING_FARM);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST, JobType.CHEMIST);
		setPreferredRole(RoleType.SCIENCE_SPECIALIST, RoleType.CHIEF_OF_SCIENCE,
				RoleType.CHIEF_OF_SUPPLY_N_RESOURCES);
		setTrait(TaskTrait.ARTISTIC, TaskTrait.RELAXATION);
        addPreferredRobot(RobotType.GARDENBOT);
	}

    /**
     * Gets the score for a settlement task for a person, considering the number of person farmers
     * and any personal preferences.
     * 
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        RatingScore factor = RatingScore.ZERO_RATING;
        if (p.isInSettlement()) {
            Building b = ((CropTaskJob)t).farm.getBuilding();
            Farming farm = b.getFarming();
            LifeSupport ls = b.getLifeSupport();

            if (farm.getFarmerNum() <= 2 * ls.getOccupantCapacity()) {
    			factor = super.assessPersonSuitability(t, p);
                if (factor.getScore() <= 0) {
                    return factor;
                }

                // Crowding modifier.
                factor = assessBuildingSuitability(factor, b, p);          
            }
		}
		return factor;
	}

    /**
     * Gets the score for a settlement task for a robot. The over crowding probability is considered.
     * 
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot r)  {
        return TaskUtil.assessRobot(t, r);
    }
    
    /**
     * Scans the settlement Farms for any that need tending. CReate one task per applicable Farming function.
     * 
     * @param settlement Source to scan
     * @return List of applicable tasks
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> tasks = new ArrayList<>();

        GoodsManager gm = settlement.getGoodsManager();
        double goodsFactor = gm.getCropFarmFactor();

        for (Building b : settlement.getBuildingManager().getFarmsNeedingWork()) {
            Farming farm = b.getFarming();

            RatingScore score = new RatingScore(farm.getTendingScore());

            // Settlement factors
            score.addModifier(GOODS_MODIFIER, goodsFactor);

            if (score.getScore() > 0) {
                int workTask = farm.getNumNeedTending() / 2; // Each farmer can do 2 crop per visit
                workTask = Math.min(workTask, b.getLifeSupport().getAvailableOccupancy());
                workTask = Math.max(1, workTask);
                tasks.add(new CropTaskJob(this, farm, workTask, score));
            }
        }

        return tasks;
    }
}
