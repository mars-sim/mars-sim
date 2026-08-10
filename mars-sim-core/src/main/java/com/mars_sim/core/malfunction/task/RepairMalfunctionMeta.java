/*
 * Mars Simulation Project
 * RepairMalfunctionMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package com.mars_sim.core.malfunction.task;

import java.io.Serializable;
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
import com.mars_sim.core.person.ai.task.util.AbstractTaskJob;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the RepairMalfunction task. It acts in 2 roles:
 * - SettlementMetaTask to create tasks for the shared task board that handle malfunctions inside a Settlement
 * - WorkerMetaTask to create individual Tasks to repair when a Person is inside a Vehicle
 */
public class RepairMalfunctionMeta extends FactoryMetaTask implements SettlementMetaTask {
	
	record RepairNeeded(Malfunctionable broken, Malfunction malfunction, int demand, boolean eva, RatingScore score)
				implements Serializable {
					
		Task createRobotRepair(Robot robot) {
			if (eva) {
				throw new IllegalStateException("Robots cannot perform eva repairs");
			}
			return new RepairInsideMalfunction(robot, broken, malfunction);
		}

		Task createPersonRepair(Person person) {
			if (eva) {
				return new RepairEVAMalfunction(person, broken, malfunction);
			}
			return new RepairInsideMalfunction(person, broken, malfunction);
		}

		String getDescription() {
			return "Repair " + (eva ? "EVA " : "") + malfunction.getMalfunctionMeta().getName();
		}
	}

	/**
	 * This is for use as a Repair that goes onto the Settlement Backlog.
	 */
	private static class SettlementRepairTask extends SettlementTask {
		
		private static final long serialVersionUID = 1L;
		private RepairNeeded repair;

		public SettlementRepairTask(SettlementMetaTask ownerTask, Settlement owner, RepairNeeded repair) {
			super(ownerTask, owner, repair.getDescription(), repair.broken,
					repair.score);
			setDemand(repair.demand);
			setEVA(repair.eva);
			this.repair = repair;
		}

		@Override
		public Task createTask(Person person) {
			return repair.createPersonRepair(person);
		}

		@Override
		public Task createTask(Robot robot) {
			return repair.createRobotRepair(robot);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + repair.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			SettlementRepairTask other = (SettlementRepairTask) obj;
			return repair.equals(other.repair);
		}
	}

	/**
	 * This is a repair task for a specific malfunction on a specific entity. This is for individuals.
	 */
	private static class RepairTaskJob extends AbstractTaskJob {
		
		private static final long serialVersionUID = 1L;

		private RepairNeeded repair;

		public RepairTaskJob(RepairNeeded repair) {
			super(repair.getDescription(), repair.score);
			this.repair = repair;
		}
		
		@Override
		public Task createTask(Person person) {
			return repair.createPersonRepair(person);
		}

		@Override
		public Task createTask(Robot robot) {
			return repair.createRobotRepair(robot);
		}
	}
	
    /** Task name */
    private static final String NAME = Msg.getString("Task.description.repairMalfunction"); //$NON-NLS-1$

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
			for (RepairNeeded t: getRepairTasks(source, partStore)) {
				RatingScore score = new RatingScore(t.score);
				score.addModifier("inside", 3D); //Repairs in Vehicles are important
				tasks.add(new RepairTaskJob(t));
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

		List<SettlementTask> settlementTasks = new ArrayList<>();
		for(var r : getRepairTasks(source, settlement.getEquipmentInventory())) {
			settlementTasks.add(new SettlementRepairTask(this, settlement, r));
		}
		return settlementTasks;
	}

	/**
	 * Creates any repair tasks needed for a set of Malfunctionable.
	 * 
	 * @param source Source of repair tasks
	 * @param partStore Where any needed Parts come from
	 */
    private List<RepairNeeded> getRepairTasks(Collection<Malfunctionable> source, EquipmentOwner partStore) {

		List<RepairNeeded> tasks = new ArrayList<>();
		
        // Add probability for all malfunctionable entities in person's local.
        for (Malfunctionable entity : source) {
			if (entity instanceof Robot) {
				// Note: robot's malfunction is not currently modeled
				// vehicle malfunctions are handled by other meta tasks
				continue;
			}

			// Get the malfunction manager
			MalfunctionManager manager = entity.getMalfunctionManager();
			
			// Create repair tasks for all active malfunctions
			for(var m : manager.getMalfunctions()) {
				var inside = createRepair(partStore, entity, m, MalfunctionRepairWork.INSIDE);
				if (inside != null) {
					tasks.add(inside);
				}

				var outside = createRepair(partStore, entity, m, MalfunctionRepairWork.EVA);
				if (outside != null) {
					tasks.add(outside);
				}
			}
		}
		return tasks;
	}

	/**
     * Creates a repair for a Malfunction.
     * 
	 * @param partsStore Where are spare parts coming from
	 * @param entity Entity suffering the malfunction
	 * @param malfunction The problem to fix
	 * @param workType Type of work to check for.
     * @return It may return null if the Malfunction need no further repair work
     */
    private RepairNeeded createRepair(EquipmentOwner partsStore, Malfunctionable entity,
											Malfunction malfunction, MalfunctionRepairWork workType) {    
		if (!malfunction.isWorkDone(workType)
				&& (malfunction.numRepairerSlotsEmpty(workType) > 0)) {
			RatingScore score = new RatingScore(WEIGHT);
	        score.addModifier("severity", malfunction.getSeverity());
	        
	        if (RepairHelper.hasRepairParts(partsStore, malfunction)) {
	    		score.addModifier("parts", 2);
	    	}
		
			return new RepairNeeded(entity, malfunction,
									malfunction.numRepairerSlotsEmpty(workType),
									(workType == MalfunctionRepairWork.EVA),
									score);
		}
		return null;
	}
}
