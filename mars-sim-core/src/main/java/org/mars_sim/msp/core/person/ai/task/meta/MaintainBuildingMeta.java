/*
 * Mars Simulation Project
 * MaintainBuildingMeta.java
 * @date 2022-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.MaintainBuilding;
import org.mars_sim.msp.core.person.ai.task.MaintainBuildingEVA;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

/**
 * Meta task for maintaining buildings.
 */
public class MaintainBuildingMeta extends MetaTask implements SettlementMetaTask {
	
	/** default logger. */
	// May add back SimLogger logger = SimLogger.getLogger(MaintainBuildingMeta.class.getName());
	
	/**
     * Represents a Job needed for internal maintenance on a Building
     */
    private static class MaintainTaskJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        private Building target;
		private boolean eva;

        public MaintainTaskJob(SettlementMetaTask owner, Building target, boolean eva, double score) {
			super(owner, "Maintain " + (eva ? "via EVA " : "") + "@ " + target.getName(), score);
            this.target = target;
			this.eva = eva;
        }

        @Override
        public Task createTask(Person person) {
			if (eva) {
				return new MaintainBuildingEVA(person, target);
			}
            return new MaintainBuilding(person, target);
        }

        @Override
        public Task createTask(Robot robot) {
			if (eva) {
				// SHould not happen
				throw new IllegalStateException("Robots can not do EVA maintenance");
			}
            return new MaintainBuilding(robot, target);
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
     * Gets the score for a Settlement task for a person. This considers and EVA factor for eva maintenance.
     * 
	 * @param t Task being scored
	 * @param p Person requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public double getPersonSettlementModifier(SettlementTask t, Person p) {
        double factor = 0D;
        if (p.isInSettlement()) {
			MaintainTaskJob mtj = (MaintainTaskJob) t;

			factor = getPersonModifier(p);
			if (mtj.eva) {
				// EVA factor is the radiation and the EVA modifiers applied extra
				factor *= getRadiationModifier(p.getSettlement());
				factor *= getEVAModifier(p);
			}
		}
		return factor;
	}

    /**
     * For a robot can not do EVA tasks so will return a zero factor in this case.
     * 
	 * @param t Task being scored
	 * @param r Robot requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public double getRobotSettlementModifier(SettlementTask t, Robot r) {
		MaintainTaskJob mtj = (MaintainTaskJob) t;
		if (mtj.eva) {
			return 0D;
		}
		return ROBOT_FACTOR;
	}

	/**
	 * Scans the Settlement for any Building that need maintenance.
	 * 
	 * @param settlement Settlement to scan.
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask>  tasks = new ArrayList<>();
	
		for (Building building: settlement.getBuildingManager().getBuildingSet()) {
			double score = scoreMaintenance(building);

			if (score > 0) {
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
	public static double scoreMaintenance(Malfunctionable entity) {
		MalfunctionManager manager = entity.getMalfunctionManager();
		boolean hasNoMalfunction = !manager.hasMalfunction();
		boolean hasPartsInStore = manager.hasMaintenancePartsInStorage(entity.getAssociatedSettlement());

		double score = 0D;
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
			score = (100 - condition) + (effectiveTime - minMaintenance) * 100D / minMaintenance;
			if (hasPartsInStore) {
				// If needed parts are available, double up the speed of the maintenance
				score = score * 2;
			}
		}

//		logger.info(entity, 10_000L, "score: " + score + "  effectiveTime: " 
//				+ effectiveTime + "  minMaintenance: " + minMaintenance);
		return score;
	}
}
