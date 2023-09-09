/*
 * Mars Simulation Project
 * TendGreenhouseMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskProbabilityUtil;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

/**
 * Meta task for the Tend Greenhouse task.
 */
public class TendGreenhouseMeta extends MetaTask implements SettlementMetaTask {

    private static class CropTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

        private Farming farm;

        public CropTaskJob(SettlementMetaTask owner, Farming farm, int demand, double score) {
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
		setFavorite(FavoriteType.TENDING_GARDEN);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST, JobType.CHEMIST);
		
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
	public double getPersonSettlementModifier(SettlementTask t, Person p) {
        double factor = 0D;
        if (p.isInSettlement()) {
            Building b = ((CropTaskJob)t).farm.getBuilding();
            Farming farm = b.getFarming();
            LifeSupport ls = b.getLifeSupport();
            factor = 1;

            if (farm.getFarmerNum() <= 2 * ls.getOccupantCapacity()) {
    
                // Crowding modifier.
                factor *= getBuildingModifier(b, p);
                                    
                factor *= 2 * (1 + getPersonModifier(p));
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
	public double getRobotSettlementModifier(SettlementTask t, Robot r) {
        Farming f = ((CropTaskJob)t).farm;
        
        // Crowding modifier.
        return TaskProbabilityUtil.getCrowdingProbabilityModifier(r, f.getBuilding());
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

            double score = farm.getTendingScore();

            // Settlement factors
            score *= goodsFactor ;

            if (score > 0) {
                int workTask = farm.getNumNeedTending() / 2; // Each farmer can do 2 crop per visit
                workTask = Math.min(workTask, b.getLifeSupport().getAvailableOccupancy());
                workTask = Math.max(1, workTask);
                tasks.add(new CropTaskJob(this, farm, workTask, score));
            }
        }

        return tasks;
    }
}
