/*
 * Mars Simulation Project
 * TendGreenhouseMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

/**
 * Meta task for the Tend Greenhouse task.
 */
public class TendGreenhouseMeta extends MetaTask implements SettlementMetaTask {

    private static class CropTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

        private Farming farm;

        public CropTaskJob(SettlementMetaTask owner, Farming farm, double score) {
            super(owner, "Tend crop @ " + farm.getBuilding().getName(), score);
            this.farm = farm;
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

    // Desired number of active farmers per form
    private static final int MAX_FARMERS = 2;

    public TendGreenhouseMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TENDING_GARDEN);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST);
		setTrait(TaskTrait.ARTISTIC, TaskTrait.RELAXATION);

        addPreferredRobot(RobotType.GARDENBOT);
	}

    /**
     * Get the score for a Settlement task for a person. THis considers the number of Person farmers
     * and any personal preferences.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public double getPersonSettlementModifier(SettlementTask t, Person p) {
        double factor = 0D;
        if (p.isInSettlement()) {
            Building b = ((CropTaskJob)t).farm.getBuilding();

            // Do not calculate farmers until we need it as expensive
            Farming farm = b.getFarming();
            if (farm.getFarmerNum() < MAX_FARMERS) {
                factor = 1D;

                // Crowding modifier.
                factor *= getBuildingModifier(b, p);
                                    
                factor *= getPersonModifier(p);
            }
		}
		return factor;
	}

    /**
     * For a robot the over crowding probability is considered
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public double getRobotSettlementModifier(SettlementTask t, Robot r) {
        Farming f = ((CropTaskJob)t).farm;
        
        // Crowding modifier.
        return TaskProbabilityUtil.getCrowdingProbabilityModifier(r, f.getBuilding());
    }

    /**
     * Scan the settlement Farms for any that need tending. CReate one task per applicable Farming function.
     * @param settlement Source to scan
     * @return List of applicable tasks
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> tasks = new ArrayList<>();

        GoodsManager gm = settlement.getGoodsManager();
        double goodsFactor = gm.getCropFarmFactor() + gm.getTourismFactor();

        for(Building b : settlement.getBuildingManager().getFarmsNeedingWork()) {
            Farming farm = b.getFarming();

            // Using the raw tending needs creates too large scores.
            double result = farm.getTendingNeed()/10D;

            // Settlement factors
            result *= goodsFactor;

            if (result > 0) {
                tasks.add(new CropTaskJob(this, farm, result));
            }
        }

        return tasks;
    }
}
