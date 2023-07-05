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

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
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
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the RepairMalfunction task. It acts in 2 roles:
 * - SettlementMetaTask to create tasks for the shared task board that handle malfunctions inside a Settlement
 * - WorkerMetaTask to create individual Tasks to repair when a Person is inside a Vehicle
 */
public class RepairMalfunctionMeta extends FactoryMetaTask implements SettlementMetaTask {
	
	private static class RepairTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

		private Malfunctionable entity;
		private Malfunction mal;
		private boolean eva;

		public RepairTaskJob(SettlementMetaTask owner, Malfunctionable entity, Malfunction mal,
							 int demand, boolean eva, double score) {
			super(owner, "Repair " + (eva ? "EVA " : "") + mal.getMalfunctionMeta().getName()
							+ " @ " + entity, score);
			setDemand(demand);
			this.entity = entity;
			this.mal = mal;
			this.eva = eva;
		}

		@Override
		public Task createTask(Person person) {
			if (eva) {
				return new RepairEVAMalfunction(person, entity, mal);
			}
			return new RepairInsideMalfunction(person, entity, mal);
		}

		@Override
		public Task createTask(Robot robot) {
			if (eva) {
				throw new IllegalStateException("Robots cannot perform eva repairs");
			}
			return new RepairInsideMalfunction(robot, entity, mal);
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
	 * Get repair tasks suitable for this Person as individual tasks if they are inside a Vehicle.
	 * @param person Person looking for Repairs.
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

		List<TaskJob> tasks = new ArrayList<>();

        if (person.isInVehicle()) {
			EquipmentOwner partStore = person.getVehicle();
			Collection<Malfunctionable> source = MalfunctionFactory.getMalfunctionables(person.getVehicle());
			for(SettlementTask t: getRepairTasks(source, partStore)) {
				// Repairs in Vehicles are important so apply constant factor
				double factor = 3D;

				RepairTaskJob rtj = (RepairTaskJob) t;
				tasks.add(new RepairTaskJob(this, rtj.entity, rtj.mal, rtj.getDemand(),
											rtj.eva, rtj.getScore() * factor));
			}
		}

        return tasks;
	}

	
	/**
	 * Robots do not get any individual repairs assigned as they never go in a Vehicle
	 */
    @Override
    public List<TaskJob> getTaskJobs(Robot robot) {
		return null;
	}
	
	/**
     * Get the score for a Settlement task for a person. This considers and EVA factor for eva maintenance.
	 * @param t Task being scored
	 * @parma p Person requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public double getPersonSettlementModifier(SettlementTask t, Person p) {
        double factor = 0D;
        if (p.isInSettlement()) {
			RepairTaskJob mtj = (RepairTaskJob) t;

			factor = getPersonModifier(p);
			if (mtj.eva) {
				// EVA factor is the radition and the EVA modifiers applied extra
				factor *= getRadiationModifier(p.getSettlement());
				factor *= getEVAModifier(p);
			}
		}
		return factor;
	}

    /**
     * For a robot can not do EVA tasks so will return a zero factor in this case.
	 * @param t Task being scored
	 * @parma r Robot requesting work.
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public double getRobotSettlementModifier(SettlementTask t, Robot r) {
        RepairTaskJob mtj = (RepairTaskJob) t;
        if (mtj.eva) {
            return 0D;
        }
        return r.getPerformanceRating();
    }

	/**
	 * Get a collection of Tasks for any vehicle that needs unloading
	 * @param settlement Settlement to scan for vehicles
	 */
	public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		Collection<Malfunctionable> source = MalfunctionFactory.getAssociatedMalfunctionables(settlement);

		return getRepairTasks(source, settlement);
	}

	/**
	 * Create any repair tasks needed for a set of Malfunctionable.
	 * @parma source Source of repair tasks
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
     * Create a repair task for a Malfunction.
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
	        double result = WEIGHT * malfunction.getSeverity();
	        
	        if (RepairHelper.hasRepairParts(partsStore, malfunction)) {
	    		result *= 2;
	    	}
		
			return new RepairTaskJob(this, entity, malfunction,
									malfunction.numRepairerSlotsEmpty(workType),
									(workType == MalfunctionRepairWork.EVA),
									result);
		}
		return null;
	}
}
