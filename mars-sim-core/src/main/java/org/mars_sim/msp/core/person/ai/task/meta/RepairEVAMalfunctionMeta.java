/*
 * Mars Simulation Project
 * RepairEVAMalfunctionMeta.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;
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
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.RepairEVAMalfunction;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskJob;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

public class RepairEVAMalfunctionMeta extends MetaTask {
	private static class RepairEVATaskJob implements TaskJob {
		private Malfunctionable entity;
		private Malfunction mal;
		private double score;

		public RepairEVATaskJob(Malfunctionable entity, Malfunction mal, double score) {
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
			return "Repair EVA " + mal.getName() + " @ " + entity;
		}

		@Override
		public Task createTask(Person person) {
			return new RepairEVAMalfunction(person, entity, mal);
		}

		@Override
		public Task createTask(Robot robot) {
			throw new UnsupportedOperationException("Robts can not do EVA Repair");
		}
	}

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.repairEVAMalfunction"); //$NON-NLS-1$

    public RepairEVAMalfunctionMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.AGILITY, TaskTrait.STRENGTH);
		setPreferredJob(JobType.MECHANICS);
	}

	/**
	 * Get repair tasks suitable for this Person to do via EVA.
	 */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

		List<TaskJob> tasks = null;

        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isNominallyFit())
        	return tasks;

        if (person.isInside() && EVAOperation.getWalkableAvailableAirlock(person, false) == null)
			return tasks;

        if (person.isInVehicle()) {
        	// Get the malfunctioning entity.
        	tasks = getEVAMalfunctionInVehicle(person);
        }

        else if (person.isInSettlement()) {
			// Check if it is night time.
			// Even if it's night time, technicians/engineers are assigned to man that work shift
			// to take care of the the repair.
			tasks = getSettlementProbability(person);
		}
        
		return tasks;
	}

	/**
	 * Get a list of repair tasks needing EVA from a Vehicle
	 */
	private List<TaskJob> getEVAMalfunctionInVehicle(Person person) {
		EquipmentOwner partStore = RepairHelper.getClosestRepairStore(person);

		for (Malfunctionable entity : MalfunctionFactory.getLocalMalfunctionables(person)) {
			// Check if entity has any EVA malfunctions.
			for (Malfunction malfunction : entity.getMalfunctionManager().getAllEVAMalfunctions()) {
				if (RepairHelper.hasRepairParts(partStore, malfunction)) {
					double score = RepairInsideMalfunctionMeta.scoreMalfunction(person.getVehicle(), malfunction,
																	MalfunctionRepairWork.EVA);
					if (score > 0) {
						score *= getPersonModifier(person);
						List<TaskJob> tasks = new ArrayList<>();
						tasks.add(new RepairEVATaskJob(entity, malfunction, score));
						return tasks;
					}
				}	
			}
		}

		return null;
	}


	/**
	 * Get a list of repair tasks that need EVA for a person in a Settlement. Check the radation
	 * exposure.
	 */
	private List<TaskJob> getSettlementProbability(Person person) {
		List<TaskJob> tasks = new ArrayList<>();
		Settlement settlement = person.getSettlement();

		// Check for radiation events
		boolean[] exposed = settlement.getExposed();
		if (exposed[2]) { // SEP can give lethal dose of radiation
			return tasks;
		}

		double radFactor = 1D;
		if (exposed[0]) {
			radFactor = radFactor / 10; // Baseline can give a fair amount dose of radiation
		}

		if (exposed[1]) {// GCR can give nearly lethal dose of radiation
			radFactor = radFactor / 20;
		}

        // Add probability for all malfunctionable entities in person's local.
        for (Malfunctionable entity : MalfunctionFactory.getAssociatedMalfunctionables(settlement)) {
        	
            if (entity instanceof Robot) {
            	// Note: robot's malfunction is not currently modeled
            	// vehicle malfunctions are handled by other meta tasks
            	continue;
            }
	
            MalfunctionManager manager = entity.getMalfunctionManager();
            
            if (manager.hasMalfunction()) {
            	// Pick the worst malfunction
            	Malfunction mal = manager.getMostSeriousMalfunctionInNeed(MalfunctionRepairWork.EVA);

            	if (mal != null) {
					double score = RepairInsideMalfunctionMeta.scoreMalfunction(settlement, mal, MalfunctionRepairWork.EVA);
					if (score > 0) {
						score *= getPersonModifier(person);
						score *= radFactor;
						tasks.add(new RepairEVATaskJob(entity, mal, score));
					}
	            }
            }
        }

		return tasks;
	}
}
