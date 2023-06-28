/*
 * Mars Simulation Project
 * RepairEVAMalfunction.java
 * @date 2022-08-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.logging.Level;

import org.mars_sim.msp.core.LocalBoundedObject;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.malfunction.RepairHelper;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;

/**
 * The RepairEVAMalfunction class is a task to repair a malfunction requiring an
 * EVA.
 */
public class RepairEVAMalfunction extends EVAOperation implements Repair {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(RepairEVAMalfunction.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.repairEVAMalfunction"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REPAIRING = new TaskPhase(Msg.getString("Task.phase.repairing")); //$NON-NLS-1$

	private static final String WORK_FORMAT = " (%.1f millisols spent).";
	
	// Data members
	/** The malfunctionable entity being repaired. */
	private Malfunctionable entity;

	/** The malfunction to be repaired. */
	private Malfunction malfunction;

	/** Where to the parts come from */
	private EquipmentOwner partStore;

	public RepairEVAMalfunction(Person person, Malfunctionable entity, Malfunction malfunction) {
		super(NAME, person, true, 25, SkillType.MECHANICS);

		if (!person.isNominallyFit()) {
			checkLocation();
        	return;
		}

		if (malfunction.numRepairerSlotsEmpty(MalfunctionRepairWork.EVA) == 0
					|| malfunction.isWorkDone(MalfunctionRepairWork.EVA)) {
			logger.warning(person, "EVA Repair not needed @ " + malfunction.getName());
			endTask();
			return;
		}

		this.entity = entity;
		this.malfunction = malfunction;
		this.partStore = RepairHelper.getClosestRepairStore(person);

		// Start if found
		setDescription(Msg.getString("Task.description.repairEVAMalfunction.detail", malfunction.getName(),
				entity.getName())); // $NON-NLS-1$

		// Determine location for repairing malfunction.
		setOutsideLocation((LocalBoundedObject) entity);

		// Can fail to get a path and Task will be Done
		if (!isDone()) {
			setPhase(WALK_TO_OUTSIDE_SITE);

			RepairHelper.prepareRepair(malfunction, person, MalfunctionRepairWork.EVA, entity);

			// Initialize phase
			addPhase(REPAIRING);
		}
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return REPAIRING;
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);
		if (!isDone()) {
			if (getPhase() == null) {
				throw new IllegalArgumentException("Task phase is null");
			}
			else if (REPAIRING.equals(getPhase())) {
				time = repairMalfunctionPhase(time);
			}
		}
		return time;
	}

	/**
	 * Perform the repair malfunction phase of the task.
	 *
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 */
	private double repairMalfunctionPhase(double time) {
		double remainingTime = 0;
		
		boolean endTask = isDone();

		// Check for radiation exposure during the EVA operation.
		endTask |= (isRadiationDetected(time)
					|| malfunction.isWorkDone(MalfunctionRepairWork.EVA));
		endTask |= malfunction.isWorkDone(MalfunctionRepairWork.EVA);
		endTask |= (shouldEndEVAOperation(false) || addTimeOnSite(time));
		endTask |= (person != null && person.isSuperUnFit());
		
		if (endTask) {
			// Return all the time
			checkLocation();
    		return time;
        }

		double workTime = time;

		// Determine effective work time based on "Mechanic" skill.
		int mechanicSkill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);

		if (mechanicSkill == 0)
			workTime /= 2;
		if (mechanicSkill > 1)
			workTime += workTime * (.2D * mechanicSkill);

		if (RepairHelper.hasRepairParts(partStore, malfunction)) {
			logger.log(worker, Level.FINE, 10_000, "Parts for repairing malfunction '" + malfunction + "' available @ " + entity.getName() + ".");
			RepairHelper.claimRepairParts(partStore, malfunction);
		}

		else {
			logger.log(worker, Level.FINE, 10_000, "Parts for repairing malfunction '" + malfunction + "' not available @ " + entity.getName() + ".");
			checkLocation();
            return remainingTime;
		}


		// Add experience points
		addExperience(time);

		// Check if an accident happens during repair.
		checkForAccident(time);

		double workTimeLeft = 0D;
		// Check if there are no more malfunctions.
		if (!malfunction.isWorkDone(MalfunctionRepairWork.EVA)) {
			logger.log(worker, Level.FINE, 10_000, "Performing EVA repair on malfunction '" + malfunction + "' @ " + entity.getName() + ".");
			// Add EVA work to malfunction.
			workTimeLeft = malfunction.addWorkTime(MalfunctionRepairWork.EVA, workTime, worker.getName());
		}
		else {
			logger.log(worker, Level.INFO, 1_000, "Wrapped up EVA repair work for '" 
					+ malfunction.getName() + "' in " + entity 
					+ String.format(WORK_FORMAT,
							malfunction.getCompletedWorkTime(MalfunctionRepairWork.EVA)));
			checkLocation();
		}

		return workTimeLeft;
	}

	@Override
	public Malfunctionable getEntity() {
		return entity;
	}

	/**
	 * Worker leaves the malfunction effort
	 */
	@Override
	protected void clearDown() {
		// Leaving the repair effort
		if (malfunction != null) {
			malfunction.leaveWork(MalfunctionRepairWork.EVA, worker.getName());
		}
	}
}
