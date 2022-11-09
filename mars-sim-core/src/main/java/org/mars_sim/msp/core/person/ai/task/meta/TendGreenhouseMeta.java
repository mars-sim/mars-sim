/*
 * Mars Simulation Project
 * TendGreenhouseMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

/**
 * Meta task for the Tend Greenhouse task.
 */
public class TendGreenhouseMeta extends MetaTask {

    private static class CropTaskJob implements TaskJob {

        private double score;
        private Farming farm;

        public CropTaskJob(Farming farm, double score) {
            this.farm = farm;
            this.score = score;
        }

        @Override
        public double getScore() {
            return score;
        }

        @Override
        public String getDescription() {
            return "Tend crop @ " + farm.getBuilding().getName();
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

    private static final double VALUE = 2;
    
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TendGreenhouseMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendGreenhouse"); //$NON-NLS-1$

    // Desired number of active farmers per form
    private static final int MAX_FARMERS = 2;

    public TendGreenhouseMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.TENDING_GARDEN);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST);
		setTrait(TaskTrait.ARTISTIC, TaskTrait.RELAXATION);
	}

    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        List<TaskJob> tasks = new ArrayList<>();
        if (person.isInSettlement()) {
            GoodsManager gm = person.getSettlement().getGoodsManager();
            double goodsFactor = gm.getCropFarmFactor() + gm.getTourismFactor();

            for(Building b : person.getSettlement().getBuildingManager().getFarmsNeedingWork()) {
                Farming farm = b.getFarming();
                if (farm.getFarmerNum() < MAX_FARMERS) {
                    double result = getFarmScore(farm);

                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, b);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, b);

                    // Settlement factors
                    result *= goodsFactor;
                    
                    result *= getPersonModifier(person);

                    if (result > 0) {
                        tasks.add(new CropTaskJob(farm, result));
                    }
                }
            }
        }

        return tasks;
    }

    
    @Override
    public List<TaskJob> getTaskJobs(Robot robot) {

        List<TaskJob> tasks = new ArrayList<>();
        if (robot.isInSettlement() && robot.getRobotType() == RobotType.GARDENBOT) {
            GoodsManager gm = robot.getSettlement().getGoodsManager();
            double goodsFactor = gm.getCropFarmFactor() + gm.getTourismFactor();

            for(Building b : robot.getSettlement().getBuildingManager().getFarmsNeedingWork()) {
                Farming farm = b.getFarming();

                double result = getFarmScore(farm);

                // Crowding modifier.
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, b);

                // Settlement factors
                result *= goodsFactor;

                if (result > 0) {
                    tasks.add(new CropTaskJob(farm, result));
                }
            }
        }

        return tasks;
    }

    /**
     * Get a score for a farm
     */
    private static double getFarmScore(Farming farm) {
        // This was originally multipled by VALUE but score was too high
        return farm.getTendingNeed();
    }

}
