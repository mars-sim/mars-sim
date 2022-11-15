/*
 * Mars Simulation Project
 * RepairMalfunctionMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mars_sim.msp.core.Msg;
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
import org.mars_sim.msp.core.person.ai.task.RepairEVAMalfunction;
import org.mars_sim.msp.core.person.ai.task.RepairInsideMalfunction;
import org.mars_sim.msp.core.person.ai.task.util.AbstractTaskJob;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;

/**
 * Meta task for the RepairMalfunction task.
 */
public class RepairMalfunctionMeta extends MetaTask {
	
	private static class RepairEVATaskJob extends AbstractTaskJob {
		private Malfunctionable entity;
		private Malfunction mal;

		public RepairEVATaskJob(Malfunctionable entity, Malfunction mal, double score) {
			super("Repair EVA " + mal.getName() + " @ " + entity, score);
			this.entity = entity;
			this.mal = mal;
		}

		@Override
		public Task createTask(Person person) {
			return new RepairEVAMalfunction(person, entity, mal);
		}
	}

	private static class RepairInsideTaskJob extends AbstractTaskJob {
		private Malfunctionable entity;
		private Malfunction mal;

		public RepairInsideTaskJob(Malfunctionable entity, Malfunction mal, double score) {
			super("Repair " + mal.getName() + " @ " + entity, score);
			this.entity = entity;
			this.mal = mal;
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
	private static SimLogger logger = SimLogger.getLogger(RepairMalfunctionMeta.class.getName());
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.repairMalfunction"); //$NON-NLS-1$

	private static final double WEIGHT = 5D;
	
    public RepairMalfunctionMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANICS);

		addPreferredRobot(RobotType.REPAIRBOT);
	}

	/**
	 * Get repair tasks suitable for this Person to do inside.
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

		List<TaskJob> tasks = null;

        if (person.isInSettlement() || person.isInVehicle()) {
			double factor = getPersonModifier(person);

			double evaFactor = factor * getRadiationModifier(person.getAssociatedSettlement());
			evaFactor *= getEVAModifier(person);

			EquipmentOwner partStore;
			Collection<Malfunctionable> source;
			if (person.isInSettlement()) {
				partStore = person.getSettlement();
				source = MalfunctionFactory.getAssociatedMalfunctionables(person.getSettlement());
			}
			else {
				partStore = RepairHelper.getClosestRepairStore(person);
				source = MalfunctionFactory.getMalfunctionables(person.getVehicle());
			}
			tasks = getRepairTasks(source, partStore, factor, evaFactor);
		}

        return tasks;
	}

	/**
	 * Get the repair tasks suitable for this Robot.
	 */
    @Override
    public List<TaskJob> getTaskJobs(Robot robot) {

		double factor = robot.getPerformanceRating();

		return getRepairTasks(MalfunctionFactory.getAssociatedMalfunctionables(robot.getSettlement()),
								robot.getSettlement(), factor, 0D);
	}
	
	/**
	 * Create any repair tasks needed for a set of Malfunctionable.
	 * @parma source Source of repair tasks
	 * @param partStore Where any needed Parts come from
	 * @param insideFactor Score factor for inside repairs
	 * @param evaFactor Score factor for EVA repairs
	 */
    private static List<TaskJob> getRepairTasks(Collection<Malfunctionable> source, EquipmentOwner partStore, double insideFactor, double evaFactor) {

		List<TaskJob> tasks = new ArrayList<>();
		
        // Add probability for all malfunctionable entities in person's local.
        for (Malfunctionable entity : source) {
			if (entity instanceof Robot) {
				// Note: robot's malfunction is not currently modeled
				// vehicle malfunctions are handled by other meta tasks
				continue;
			}

			MalfunctionManager manager = entity.getMalfunctionManager();
			
			if (manager.hasMalfunction()) {
				// Pick the worst inside malfunction
				Malfunction mal = manager.getMostSeriousMalfunctionInNeed(MalfunctionRepairWork.INSIDE);
				if (mal != null) {
					double score = scoreMalfunction(partStore, mal, MalfunctionRepairWork.INSIDE);
					score *= insideFactor;

					tasks.add(new RepairInsideTaskJob(entity, mal, score));
				}

				if (evaFactor > 0) {
					// Pick any EVA repair activities
					Malfunction evamal = manager.getMostSeriousMalfunctionInNeed(MalfunctionRepairWork.EVA);
					if (evamal != null) {
						double score = scoreMalfunction(partStore, evamal, MalfunctionRepairWork.EVA);
						score *= evaFactor;

						tasks.add(new RepairEVATaskJob(entity, evamal, score));
					}
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
