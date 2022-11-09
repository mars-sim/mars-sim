/*
 * Mars Simulation Project
 * RepairMalfunctionMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.malfunction.RepairHelper;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.RepairInsideMalfunction;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the RepairMalfunction task.
 */
public class RepairInsideMalfunctionMeta extends MetaTask {

	private static class RepairTaskJob implements TaskJob {
		private Malfunctionable entity;
		private Malfunction mal;
		private double score;

		public RepairTaskJob(Malfunctionable entity, Malfunction mal, double score) {
			this.entity = entity;
			this.mal = mal;
			this.score = score;
		}

		@Override
		public double getScore() {
			return score;
		}

		@Override
		public String getDescription() {
			return "Repair " + mal.getName() + " @ " + entity;
		}

		@Override
		public Task createTask(Person person) {
			return new RepairInsideMalfunction(person, entity, mal);
		}

		@Override
		public Task createTask(Robot robot) {
			return new RepairInsideMalfunction(robot, entity, mal);
		}

	}

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(RepairInsideMalfunctionMeta.class.getName());
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.repairMalfunction"); //$NON-NLS-1$

	private static final double WEIGHT = 5D;
	
    public RepairInsideMalfunctionMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANICS);
	}

	/**
	 * Get repair tasks suitable for this Person to do inside.
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

		List<TaskJob> tasks = null;

        if (person.isInSettlement()) {
			double factor = getPersonModifier(person);

			tasks = getRepairTasks(person.getSettlement(), factor, MalfunctionRepairWork.INSIDE);
		}

        return tasks;
	}

	/**
	 * Get the repair takss suitable for this Robot.
	 */
    @Override
    public List<TaskJob> getTaskJobs(Robot robot) {

		List<TaskJob> tasks = null;

        if (robot.getRobotType() == RobotType.REPAIRBOT) {
			double factor = robot.getPerformanceRating();

			tasks = getRepairTasks(robot.getSettlement(), factor, MalfunctionRepairWork.INSIDE);
		}

        return tasks;
	}
	
	/**
	 * Creates a list of Task Jobs for the active malfunctions at a Settlement. Apply a factor to the scores
	 */
    public static List<TaskJob> getRepairTasks(Settlement settlement, double factor, MalfunctionRepairWork workType) {

		List<TaskJob> tasks = new ArrayList<>();
		
        // Add probability for all malfunctionable entities in person's local.
        for (Malfunctionable entity : MalfunctionFactory.getAssociatedMalfunctionables(settlement)) {
        	
            if (entity instanceof Robot && entity instanceof Vehicle) {
            	// Note: robot's malfunction is not currently modeled
            	// vehicle malfunctions are handled by other meta tasks
            	continue;
            }
	
            MalfunctionManager manager = entity.getMalfunctionManager();
            
            if (manager.hasMalfunction()) {
            	// Pick the worst malfunction
            	Malfunction mal = manager.getMostSeriousMalfunctionInNeed(workType);

            	if (mal != null) {
					double score = scoreMalfunction(settlement, mal, workType);
					score *= factor;

					tasks.add(new RepairTaskJob(entity, mal, score));
	            }
            }
        }

		return tasks;
    }
    
    /**
     * Gets the initial score of the malfunction.
     * 
     * @param malfunction
     * @return
     */
    public static double scoreMalfunction(EquipmentOwner partsStore, Malfunction malfunction, MalfunctionRepairWork workType) {    
    	double result = 0D;
		if (!malfunction.isWorkDone(workType)
				&& (malfunction.numRepairerSlotsEmpty(workType) > 0)) {
	        result = WEIGHT * malfunction.getSeverity();
	        
	        if (RepairHelper.hasRepairParts(partsStore, malfunction)) {
	    		result *= 2;
	    	}
		}
    	
		return result;
    }
}
