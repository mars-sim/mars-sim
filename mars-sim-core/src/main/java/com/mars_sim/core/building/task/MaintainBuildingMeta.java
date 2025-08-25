/*
 * Mars Simulation Project
 * MaintainBuildingMeta.java
 * @date 2025-08-24
 * @author Scott Davis
 */
package com.mars_sim.core.building.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Meta task for maintaining buildings.
 */
public class MaintainBuildingMeta extends MetaTask implements SettlementMetaTask {
		
	// Minimum %age of the inspection window to trigger maintenance
	public static final double INSPECTION_PERCENTAGE = 0.0625;
	// Default logger
	// May add back private static final SimLogger logger = SimLogger.getLogger(MaintainBuildingMeta.class.getName());

	/**
     * Represents a Job needed for internal maintenance on a Building
     */
    private static class MaintainTaskJob extends SettlementTask {

		private static final long serialVersionUID = 1L;


        public MaintainTaskJob(SettlementMetaTask owner, Building target, boolean eva, RatingScore score) {
			super(owner, "Maintain Buildings " + (eva ? "via EVA " : ""), target, score);
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
	
	/**
	 * Constructor.
	 */
    public MaintainBuildingMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.LOADERS);
		addPreferredRobot(RobotType.REPAIRBOT);
		
		addAllCrewRoles();
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
		
		RatingScore score = RatingScore.ZERO_RATING;
		
		boolean hasMalfunction = manager.hasMalfunction();
		
		// Note: Look for entities that are NOT malfunction since
		//       malfunctioned entities are being taken care of by the two Repair*Malfunction tasks
		if (hasMalfunction)
			return score;
		
		boolean partsPosted = manager.hasMaintenancePartsInStorage(entity.getAssociatedSettlement());
		
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		double inspectionWindow = manager.getStandardInspectionWindow();
		
		// Note: Set the probability to be around 1/16 (INSPECTION_PERCENTAGE is 0.0625) of time into the inspection window
		
		// As a result, settlers may begin to do a little bit of inspection whenever possible, even at the beginning of the window 
		// and the inspection is a long way from being due
		
		// This is important since inspection work won't need to become a time crunch at the end
		
		double chance = RandomUtil.getRandomDouble(inspectionWindow * INSPECTION_PERCENTAGE, inspectionWindow);
		
//		logger.info("effectiveTime: " + Math.round(effectiveTime * 1000.0)/1000.0 + " chance: " 
//				+ Math.round(chance * 1000.0)/1000.0 + " window: " + Math.round(inspectionWindow * 1000.0)/1000.0) ;
			
		if ((effectiveTime >= chance)
			// if needed parts have been posted, hurry up to swap out the parts without waiting for 
			// the standard inspection/maintenance due
			|| partsPosted) {	
					
//			logger.info("Voted for maintenance.");
			
			double condition = manager.getAdjustedCondition();
			// Score is based on condition plus %age overdue
			score = new RatingScore("condition", 4 * (100 - condition));
			
			score.addModifier("maint.win", 6 * (effectiveTime / inspectionWindow));
			
			if (partsPosted) {
				// If needed parts are available, double up the speed of the maintenance
				score.addModifier("parts", 2);
			}
		}
 
		return score;
	}
}
