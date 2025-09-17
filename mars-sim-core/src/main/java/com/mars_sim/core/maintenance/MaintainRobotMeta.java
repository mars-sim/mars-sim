/*
 * Mars Simulation Project
 * MaintainRobotMeta.java
 * @date 2025-09-05
 * @author Manny Kung
 */
package com.mars_sim.core.maintenance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.malfunction.MalfunctionManager;
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

/**
 * Meta task for the maintaining robots task.
 */
public class MaintainRobotMeta extends MetaTask implements SettlementMetaTask {
	// Default logger
	// May add back private static final SimLogger logger = SimLogger.getLogger(MaintainRobotMeta.class.getName());

	/**
     * Represents a job needed for internal maintenance on a robot.
     */
	private static class MaintainRobotJob extends SettlementTask {

		private static final long serialVersionUID = 1L;

        public MaintainRobotJob(SettlementMetaTask owner, Robot target, RatingScore score) {
            super(owner, "Robot Maintenance", target, score);
        }

		/**
         * The robot needing maintenance is the focus.
         */
        private Robot getRobot() {
            return (Robot) getFocus();
        }

        @Override
        public Task createTask(Person person) {
            return new MaintainRobot(person, getRobot());
        }

        @Override
        public Task createTask(Robot robot) {
            return new MaintainRobot(robot, getRobot());
        }
    }

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainRobot"); //$NON-NLS-1$
	
	private static final double ROBOT_FACTOR = 10D;
	
    public MaintainRobotMeta() {
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
		// Avoid having a repair bot maintaining itself
		if (r.equals((Robot)t.getFocus()))
			return RatingScore.ZERO_RATING;
		
		var factor = TaskUtil.assessRobot(t, r);
		if (factor.getScore() >= 50)
			factor.addModifier("robot.expert", ROBOT_FACTOR);
		else 
			factor.addModifier(NAME, ROBOT_FACTOR);
		return factor;
    }

	/**
	 * Gets a collection of Tasks for any Vehicle maintenance that is required.
	 * 
	 * @param settlement Settlement to scan for vehicles
	 */
	@Override
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		List<SettlementTask> tasks = new ArrayList<>();

		Robot worstRobot = null;
		double highestScore = 0D;
		RatingScore score = new RatingScore(0);
		
		for (Robot robot : getAllDownRobotCandidates(settlement)) {
			
			MalfunctionManager manager = robot.getMalfunctionManager();
			
			boolean partsPosted = manager.hasMaintenancePartsInStorage(settlement);
			
			score = MaintenanceUtil.scoreMaintenance(manager, robot, partsPosted);

			if (score.getScore() > highestScore) {
				worstRobot = robot;
				highestScore = score.getScore();
			}
		}
		
		if (highestScore > 0) {
			tasks.add(new MaintainRobotJob(this, worstRobot, score));
		}
		
		// Reset them
		worstRobot = null;
		highestScore = 0D;
		
		for (Robot robot : getAllGoodRobotCandidates(settlement)) {
				
			MalfunctionManager manager = robot.getMalfunctionManager();
			
			boolean hasMalfunction = manager.hasMalfunction();
			
			// Note: Look for entities that are NOT malfunction since
			//       malfunctioned entities are being taken care of by the two Repair*Malfunction tasks
			if (!hasMalfunction) {
			
				boolean partsPosted = manager.hasMaintenancePartsInStorage(settlement);
				
				score = MaintenanceUtil.scoreMaintenance(manager, robot, partsPosted);
	
				if (score.getScore() > highestScore) {
					worstRobot = robot;
					highestScore = score.getScore();
				}
			}
		}
		
		if (highestScore > 0) {
			tasks.add(new MaintainRobotJob(this, worstRobot, score));
		}

		return tasks;
	}
	
	/**
	 * Gets all robot requiring maintenance.
	 * 
	 * @param home the settlement checking.
	 * @return collection of robot available for maintenance.
	 */
	private static List<Robot> getAllGoodRobotCandidates(Settlement home) {
		
		return home.getAllAssociatedRobots().stream()
			.filter(r -> !isPiloting(r) && !r.getSystemCondition().isInMaintenance())
			.collect(Collectors.toList());
	}

	/**
	 * Gets all robot requiring maintenance.
	 * 
	 * @param home the settlement checking.
	 * @return collection of robot available for maintenance.
	 */
	private static List<Robot> getAllDownRobotCandidates(Settlement home) {
		
		return home.getAllAssociatedRobots().stream()
			.filter(r -> !isPiloting(r) && r.getSystemCondition().isInMaintenance())
			.collect(Collectors.toList());
	}
	
	/**
	 * Is the robot piloting a drone ?
	 * 
	 * @param r
	 * @return
	 */
	private static boolean isPiloting(Robot r) {
		String taskName = r.getBotMind().getBotTaskManager().getTaskName().toLowerCase();
		boolean result = taskName.contains("operat")
				|| taskName.contains("pilot");
		
		return result;		
	}
}
