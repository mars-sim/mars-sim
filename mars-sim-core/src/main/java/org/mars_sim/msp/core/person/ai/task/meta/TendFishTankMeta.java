/*
 * Mars Simulation Project
 * TendFishTankMeta.java
 * @date 2022-08-01
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.TendFishTank;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.Fishery;

/**
 * Meta task for the Tend Fish Tank task.
 */
public class TendFishTankMeta extends MetaTask {

    /**
     * Represents a Job needed in a Fishery
     */
    private static class FishTaskJob implements TaskJob {

        private double score;
        private Fishery tank;

        public FishTaskJob(Fishery tank, double score) {
            this.tank = tank;
            this.score = score;
        }

        @Override
        public double getScore() {
            return score;
        }

        @Override
        public String getDescription() {
            return "Tend fishtank @ " + tank.getBuilding().getName();
        }

        @Override
        public Task createTask(Person person) {
            return new TendFishTank(person, tank);
        }

        @Override
        public Task createTask(Robot robot) {
            return new TendFishTank(robot, tank);
        }
    }

    private static final SimLogger logger = SimLogger.getLogger(TendFishTankMeta.class.getName());
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendFishTank"); //$NON-NLS-1$

	private static final int CAP = 3_000;
	
    public TendFishTankMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TENDING_GARDEN);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST);
		setTrait(TaskTrait.ARTISTIC, TaskTrait.RELAXATION);
	}

    /**
     * Create a task for any Fishery that needs assistence
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        List<TaskJob> tasks = new ArrayList<>();

        if (person.isInSettlement()) {
            List<Building> buildings = person.getSettlement().getBuildingManager().getBuildings(FunctionType.FISHERY);
            for(Building building : buildings) {
                Fishery fishTank = building.getFishery();
                double result = (fishTank.getUncleaned().size() + fishTank.getUninspected().size()) *3D;

                result += (fishTank.getSurplusStock() * 10D);
                result += fishTank.getWeedDemand();
                
                // Crowding modifier.
                result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
                result *= TaskProbabilityUtil.getRelationshipModifier(person, building);

                result = applyPersonModifier(result, person);
                if (result > CAP)
                    result = CAP;
                
                if (result > 0) {
                    tasks.add(new FishTaskJob(fishTank, result));
                }
            }
        }
        return tasks;
    }


    /**
     * Create a task for any Fishery that needs assistence
     */
    @Override
    public List<TaskJob> getTaskJobs(Robot robot) {

        List<TaskJob> tasks = new ArrayList<>();

        if (robot.isInSettlement() && (robot.getRobotType() == RobotType.GARDENBOT)) {
            List<Building> buildings = robot.getSettlement().getBuildingManager().getBuildings(FunctionType.FISHERY);
            for(Building building : buildings) {
                double result = 0D;

                Fishery fishTank = building.getFishery();

                // Robots just clean at the moment
                int outstandingTasks = fishTank.getUncleaned().size();

                result += outstandingTasks * 50D;
                // Effort-driven task modifier.
                result *= robot.getPerformanceRating();

                if (result > 0) {
                    tasks.add(new FishTaskJob(fishTank, result));
                }
            }
        }

        return tasks;
	}
}
