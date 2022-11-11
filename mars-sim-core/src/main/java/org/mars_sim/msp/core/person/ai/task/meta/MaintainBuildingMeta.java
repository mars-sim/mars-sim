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
import org.mars_sim.msp.core.person.ai.task.MaintainBuildingEVA;
import org.mars_sim.msp.core.person.ai.task.util.AbstractTaskJob;
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
     * Represents a Job needed for intenral maintenance on a Building
     */
    private static class MaintainInsideTaskJob extends AbstractTaskJob {

        private Building target;

        public MaintainInsideTaskJob(Building target, double score) {
			super("Maintain @ " + target.getName(), score);
            this.target = target;
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
     * Represents a Job needed for external maintenance on a Building
     */
    private static class MaintainEVATaskJob extends AbstractTaskJob {

        private Building target;

        public MaintainEVATaskJob(Building target, double score) {
			super("EVA Maintenance @ " + target.getName(), score);
            this.target = target;
        }

        @Override
        public Task createTask(Person person) {
            return new MaintainBuildingEVA(person, target);
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
    }

	@Override
	public List<TaskJob> getTaskJobs(Person person) {
        
		Settlement settlement = person.getSettlement();
		
		if (settlement != null) {
            double factor = getPersonModifier(person);

			// EVA factor is the radition and teh EVA modifiers applied extra
			double evaFactor = getRadiationModifier(settlement) * factor;
			evaFactor *= getEVAModifier(person);

			return getSettlementTasks(settlement, factor, evaFactor);
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
			return getSettlementTasks(settlement, ROBOT_FACTOR, 0D);
		}
		return null;
	}

	/**
	 * Find any buildings that need maintenance. These can crete either inside or EVA maintenance
	 */
	private List<TaskJob> getSettlementTasks(Settlement settlement, double insideFactor, double evaFactor) {
		List<TaskJob>  tasks = new ArrayList<>();
	
		for (Building building: settlement.getBuildingManager().getBuildings()) {
			double score = scoreMaintenance(building);

			boolean habitableBuilding = building.hasFunction(FunctionType.LIFE_SUPPORT);
			if (habitableBuilding) {
				score *= insideFactor;
				if (score > 0) {
					tasks.add(new MaintainInsideTaskJob(building, score));
				}
			}
			else if (evaFactor > 0) {
				score *= evaFactor;
				if (score > 0) {
					tasks.add(new MaintainEVATaskJob(building, score));
				}
			}
		}

		return tasks;
	}

	/**
	 * Score the building  in terms of need for maintenance. Considers malfunction, condition & time
	 * since last maintenance.
	 * @param building
	 */
	public static double scoreMaintenance(Building building) {
		MalfunctionManager manager = building.getMalfunctionManager();
		boolean hasNoMalfunction = !manager.hasMalfunction();
		boolean hasParts = manager.hasMaintenanceParts(building.getSettlement());

		double score = 0D;
		double condition = manager.getAdjustedCondition();
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		double minMaintenance = manager.getMaintenancePeriod();
		// Note: look for buildings that are NOT malfunction since
		// malfunctioned building are being taken care of by the two Repair*Malfunction tasks
		if (hasNoMalfunction && hasParts && (effectiveTime >= (minMaintenance * 0.9D))) {
			// Score is based on condition plus %age overdue
			score = (100 - condition) + ((effectiveTime - minMaintenance)*100D/minMaintenance);
		}

		return score;
	}
}
