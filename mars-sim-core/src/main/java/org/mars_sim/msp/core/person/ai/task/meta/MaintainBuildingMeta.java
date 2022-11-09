/*
 * Mars Simulation Project
 * MaintainBuildingMeta.java
 * @date 2022-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.MaintainBuilding;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * Meta task for maintaining buildings.
 */
public class MaintainBuildingMeta extends MetaTask {
/**
     * Represents a Job needed in a Fishery
     */
    private static class MaintainTaskJob implements TaskJob {

        private double score;
        private Building target;

        public MaintainTaskJob(Building target, double score) {
            this.target = target;
            this.score = score;
        }

        @Override
        public double getScore() {
            return score;
        }

        @Override
        public String getDescription() {
            return "Maintain @ " + target.getName();
        }

        @Override
        public Task createTask(Person person) {
            return new MaintainBuilding(person, target);
        }

        @Override
        public Task createTask(Robot robot) {
            return new MaintainBuilding(robot, target);
        }
    }
	/**
	 *
	 */
	private static final double MIN_MAINTENANCE_PERIOD = 100D;

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainBuilding"); //$NON-NLS-1$
	
	private static final double ROBOT_FACTOR = 50;

	// Lower threshold to avoid excessive jobs being created
	private static final double LOW_THRESHOLD = 10D;
	
    public MaintainBuildingMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANICS);
    }

	@Override
	public List<TaskJob> getTaskJobs(Person person) {
        
		Settlement settlement = person.getSettlement();
		
		if (settlement != null) {
            double factor = getPersonModifier(person);
			return getSettlementTasks(settlement, factor);
		}
		return null;
	}

	/**
	 * Get any maintenance jobs that can be performbed by a Robot. TThe Robot
	 * must be a Repairbot.
	 * @param robot Robot looking for work
	 */
	@Override
	public List<TaskJob> getTaskJobs(Robot robot) {
        
		Settlement settlement = robot.getSettlement();
		if ((settlement != null) && (robot.getRobotType() == RobotType.REPAIRBOT)) {
			return getSettlementTasks(settlement, ROBOT_FACTOR);
		}
		return null;
	}

	/**
	 * Find any buildings that need internal maintenance
	 */
	private List<TaskJob> getSettlementTasks(Settlement settlement, double factor) {
		List<TaskJob>  tasks = new ArrayList<>();
	
		for (Building building: settlement.getBuildingManager().getBuildings()) {
			
			MalfunctionManager manager = building.getMalfunctionManager();
			boolean hasNoMalfunction = !manager.hasMalfunction();
			boolean hasParts = MaintainBuilding.hasMaintenanceParts(settlement, building);
			boolean inhabitableBuilding = building.hasFunction(FunctionType.LIFE_SUPPORT);

			double condition = manager.getAdjustedCondition();
			double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
			// Note: look for buildings that are NOT malfunction since
			// malfunctioned building are being taken care of by the two Repair*Malfunction tasks
			if (hasNoMalfunction && inhabitableBuilding && hasParts && (effectiveTime >= MIN_MAINTENANCE_PERIOD)) {
				double score = (100 - condition) + (effectiveTime - MIN_MAINTENANCE_PERIOD);
				score *= (factor * 2);
				if (score >= LOW_THRESHOLD) {
					tasks.add(new MaintainTaskJob(building, score));
				}
			}
		}

		return tasks;
	}
}
