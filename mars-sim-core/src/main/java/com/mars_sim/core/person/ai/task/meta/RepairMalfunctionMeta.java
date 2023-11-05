/*
 * Mars Simulation Project
 * RepairMalfunctionMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.malfunction.Malfunction;
import com.mars_sim.core.malfunction.MalfunctionFactory;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.MalfunctionRepairWork;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.malfunction.RepairHelper;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.RepairEVAMalfunction;
import com.mars_sim.core.person.ai.task.RepairInsideMalfunction;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the RepairMalfunction task. It acts in 2 roles:
 * - SettlementMetaTask to create tasks for the shared task board that handle malfunctions inside a Settlement
 * - WorkerMetaTask to create individual Tasks to repair when a Person is inside a Vehicle
 */
public class RepairMalfunctionMeta extends FactoryMetaTask implements SettlementMetaTask {
	
	private static class RepairTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

		private Malfunction mal;

		public RepairTaskJob(SettlementMetaTask owner, Malfunctionable entity, Malfunction mal,
							 int demand, boolean eva, RatingScore score) {
			super(owner, "Repair " + (eva ? "EVA " : "") + mal.getMalfunctionMeta().getName(), entity,
						score);
			setDemand(demand);
			setEVA(eva);
			this.mal = mal;
		}
		
		/**
		 * The Malfunctionable with the fault is the focus of this Task.
		 */
		Malfunctionable getProblem() {
			return (Malfunctionable) getFocus();
		}


		@Override
		public Task createTask(Person person) {
			if (isEVA()) {
				return new RepairEVAMalfunction(person, getProblem(), mal);
			}
			return new RepairInsideMalfunction(person, getProblem(), mal);
		}

		@Override
		public Task createTask(Robot robot) {
			if (isEVA()) {
				throw new IllegalStateException("Robots cannot perform eva repairs");
			}
			return new RepairInsideMalfunction(robot, getProblem(), mal);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (super.equals(obj)) {
				// Check the actual malfunction to distinguish between 2 repiar task on the same entity
				RepairTaskJob other = (RepairTaskJob) obj;
				if (mal == null) {
					if (other.mal != null)
						return false;
				} else if (!mal.equals(other.mal))
					return false;
				return true;
			}
			return false;
		}
	}
	
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
	 * Gets repair tasks suitable for this Person as individual tasks if they are inside a Vehicle.
	 * 
	 * @param person Person looking for Repairs.
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

		List<TaskJob> tasks = new ArrayList<>();

        if (person.isInVehicle()) {
			EquipmentOwner partStore = person.getVehicle();
			Collection<Malfunctionable> source = MalfunctionFactory.getMalfunctionables(person.getVehicle());
			for (SettlementTask t: getRepairTasks(source, partStore)) {
				RepairTaskJob rtj = (RepairTaskJob) t;
				RatingScore score = new RatingScore(rtj.getScore());
				score.addModifier("inside", 3D); //Repairs in Vehicles are important
				tasks.add(new RepairTaskJob(this, rtj.getProblem(), rtj.mal, rtj.getDemand(),
											rtj.isEVA(), score));
			}
		}

        return tasks;
	}

	
	/**
	 * Robots do not get any individual repairs assigned as they never go in a Vehicle.
	 */
    @Override
    public List<TaskJob> getTaskJobs(Robot robot) {
		return Collections.emptyList();
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
        return TaskUtil.assessRobot(t, r);
    }
	
	/**
	 * Gets a collection of Tasks for any vehicle that needs unloading.
	 * 
	 * @param settlement Settlement to scan for vehicles
	 */
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		Collection<Malfunctionable> source = MalfunctionFactory.getAssociatedMalfunctionables(settlement);

		return getRepairTasks(source, settlement);
	}

	/**
	 * Creates any repair tasks needed for a set of Malfunctionable.
	 * 
	 * @param source Source of repair tasks
	 * @param partStore Where any needed Parts come from
	 */
    private List<SettlementTask> getRepairTasks(Collection<Malfunctionable> source, EquipmentOwner partStore) {

		List<SettlementTask> tasks = new ArrayList<>();
		
        // Add probability for all malfunctionable entities in person's local.
        for (Malfunctionable entity : source) {
			if (entity instanceof Robot) {
				// Note: robot's malfunction is not currently modeled
				// vehicle malfunctions are handled by other meta tasks
				continue;
			}

			MalfunctionManager manager = entity.getMalfunctionManager();
			
			if (manager.hasMalfunction()) {
				// Create repair tasks for all active malfunctions
				for(Malfunction mal : manager.getMalfunctions()) {
					SettlementTask task = createRepairTask(partStore, entity, mal, MalfunctionRepairWork.INSIDE);
					if (task != null) {
						tasks.add(task);
					}

					// Pick any EVA repair activities
					task = createRepairTask(partStore, entity, mal, MalfunctionRepairWork.EVA);
					if (task != null) {
						tasks.add(task);
					}
				}
			}
		}
		return tasks;
	}

	/**
     * Creates a repair task for a Malfunction.
     * 
	 * @param partsStore Where are spare parts coming from
	 * @param entity Entity suffering the malfunction
	 * @param malfunction The problem to fix
	 * @param workType Type of work to check for.
     * @return It may return null if the Malfunction need no further repair work
     */
    private SettlementTask createRepairTask(EquipmentOwner partsStore, Malfunctionable entity,
											Malfunction malfunction,
											MalfunctionRepairWork workType) {    
		if (!malfunction.isWorkDone(workType)
				&& (malfunction.numRepairerSlotsEmpty(workType) > 0)) {
			RatingScore score = new RatingScore(WEIGHT);
	        score.addModifier("severity", malfunction.getSeverity());
	        
	        if (RepairHelper.hasRepairParts(partsStore, malfunction)) {
	    		score.addModifier("parts", 2);
	    	}
		
			return new RepairTaskJob(this, entity, malfunction,
									malfunction.numRepairerSlotsEmpty(workType),
									(workType == MalfunctionRepairWork.EVA),
									score);
		}
		return null;
	}
}
