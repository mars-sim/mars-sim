/*
 * Mars Simulation Project
 * MaintainBuildingMeta.java
 * @date 2022-09-18
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.MaintainBuilding;
import com.mars_sim.core.person.ai.task.MaintainBuildingEVA;
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
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * Meta task for maintaining buildings.
 */
public class MaintainBuildingMeta extends MetaTask implements SettlementMetaTask {
		
	/**
     * Represents a Job needed for internal maintenance on a Building
     */
    private static class MaintainTaskJob extends SettlementTask {

		private static final long serialVersionUID = 1L;


        public MaintainTaskJob(SettlementMetaTask owner, Building target, boolean eva, RatingScore score) {
			super(owner, "Building Maintenance " + (eva ? "via EVA " : ""), target, score);
			setEVA(eva);
        }

		/**
		 * The Building undergoing maintenance is the focus of this Task.
		 */
		private Building getBuilding() {
			return (Building) getFocus();
		}

        @Override
        public Task createTask(Person person) {
			if (isEVA()) {
				return new MaintainBuildingEVA(person, getBuilding());
			}
            return new MaintainBuilding(person, getBuilding());
        }

        @Override
        public Task createTask(Robot robot) {
			if (isEVA()) {
				// SHould not happen
				throw new IllegalStateException("Robots can not do EVA maintenance");
			}
            return new MaintainBuilding(robot, getBuilding());
        }
    }


	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainBuilding"); //$NON-NLS-1$
	
	private static final double ROBOT_FACTOR = 2D;
	
    public MaintainBuildingMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANICS);

		addPreferredRobot(RobotType.REPAIRBOT);
    }

    /**
     * For a robot can not do EVA tasks so will return a zero factor in this case.
     * 
	 * @param t Task being scored
	 * @param r Robot requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot r)  {
        var factor = TaskUtil.assessRobot(t, r);
		if (factor.getScore() > 0)
			factor.addModifier("robot.expert", ROBOT_FACTOR);
        return factor;
    }
	
	/**
	 * Scans the Settlement for any Building that need maintenance.
	 * 
	 * @param settlement Settlement to scan.
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();
	
		for (Building building: settlement.getBuildingManager().getBuildingSet()) {
			RatingScore score = scoreMaintenance(building);

			if (score.getScore() > 0) {
				boolean habitableBuilding = building.hasFunction(FunctionType.LIFE_SUPPORT);
				tasks.add(new MaintainTaskJob(this, building, !habitableBuilding, score));
			}
		}

		return tasks;
	}

	/**
	 * Scores the entity in terms of need for maintenance. Considers malfunction, condition & time
	 * since last maintenance.
	 * 
	 * @param entity
	 * @return A score on the need for maintenance
	 */
	public static RatingScore scoreMaintenance(Malfunctionable entity) {
		MalfunctionManager manager = entity.getMalfunctionManager();
		boolean hasNoMalfunction = !manager.hasMalfunction();
		boolean hasPartsInStore = manager.hasMaintenancePartsInStorage(entity.getAssociatedSettlement());

		RatingScore score = RatingScore.ZERO_RATING;
		double condition = manager.getAdjustedCondition();
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		double minMaintenance = manager.getMaintenancePeriod();
		
		// Note: look for entities that are NOT malfunction since
		// malfunctioned entities are being taken care of by the two Repair*Malfunction tasks
		
		// About a quarter of time into the next inspection/maintenance that will be due,
		// One can begin to do a little bit of inspection whenever possible
		if ((hasNoMalfunction && effectiveTime >= minMaintenance * 0.25 * RandomUtil.getRandomDouble(0.8, 1.2))
			// if needed parts have been posted, hurry up to swap out the parts without waiting for 
			// the standard inspection/maintenance due
			|| hasPartsInStore) {
			// Score is based on condition plus %age overdue
			score = new RatingScore("condition", 100 - condition);
			score.addModifier("due", 1D +
								((effectiveTime - minMaintenance) / minMaintenance));
			if (hasPartsInStore) {
				// If needed parts are available, double up the speed of the maintenance
				score.addModifier("parts", 2);
			}
		}
 
		return score;
	}
}
