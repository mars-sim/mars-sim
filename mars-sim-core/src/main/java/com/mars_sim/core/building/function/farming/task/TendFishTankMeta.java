/*
 * Mars Simulation Project
 * TendFishTankMeta.java
 * @date 2023-12-07
 * @author Barry Evans
 */
package com.mars_sim.core.building.function.farming.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.farming.Fishery;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the Tend Fish Tank task.
 */
public class TendFishTankMeta extends MetaTask implements SettlementMetaTask {

	private static final int BASE_SCORE = 50;
	
    /**
     * Represents a Job needed in a Fishery
     */
    private static class FishTaskJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        private Fishery tank;

        public FishTaskJob(SettlementMetaTask owner, Fishery tank, RatingScore score) {
            super(owner, "Tend Fish Tank", tank.getBuilding(), score);
            this.tank = tank;
        }

        @Override
        public Task createTask(Person person) {
            return new TendFishTank(person, tank, TendFishTank.selectActivity(tank, true));
        }

        @Override
        public Task createTask(Robot robot) {
            return new TendFishTank(robot, tank, TendFishTank.selectActivity(tank, false));
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendFishTank"); //$NON-NLS-1$
	
    public TendFishTankMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.TENDING_FARM);
		setPreferredJob(JobType.BOTANIST, JobType.BIOLOGIST, JobType.CHEMIST);
		setPreferredRole(RoleType.SCIENCE_SPECIALIST, RoleType.CREW_SCIENTIST, RoleType.CHIEF_OF_SCIENCE,
				RoleType.CHIEF_OF_SUPPLY_RESOURCE);
		setTrait(TaskTrait.ARTISTIC, TaskTrait.RELAXATION);
        addPreferredRobot(RobotType.GARDENBOT);
	}

    
    /**
     * Gets the score for a Settlement task for a person. 
     * Considers the number of Person farmers and any personal preferences.
     * 
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        RatingScore factor = RatingScore.ZERO_RATING;
        if (p.isInSettlement()) {
			factor = super.assessPersonSuitability(t, p);
            if (factor.getScore() == 0) {
                return factor;
            }
            
            // Crowding modifier.
            Building b = ((FishTaskJob)t).tank.getBuilding();
            assessBuildingSuitability(factor, b, p);
		}
		return factor;
	}

    /**
     * For a robot the over crowding probability is considered.
     * 
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot r)  {
        return TaskUtil.assessRobot(t, r);
    }

    /**
     * Scans the settlement tanks for any that need tending. 
     * Creates one task per applicable Fishery function.
     * 
     * @param settlement Source to scan
     * @return List of applicable tasks
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> tasks = new ArrayList<>();

        for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.FISHERY)) {
            Fishery fishTank = building.getFishery();
            
            RatingScore result = new RatingScore("base", BASE_SCORE);
            
            var keeping = fishTank.getHousekeeping();
            result.addBase("maintenance", 
            		2 * (200 - keeping.getAverageCleaningScore() - keeping.getAverageInspectionScore()));

            result.addBase("surplus", Math.abs(fishTank.getSurplusStock()));
            
            result.addBase("fish.weeds", fishTank.getWeedDemand() * 15);
            
            if (result.getScore() > 0) {
                tasks.add(new FishTaskJob(this, fishTank, result));
            }
        }

        return tasks;
    }
}
