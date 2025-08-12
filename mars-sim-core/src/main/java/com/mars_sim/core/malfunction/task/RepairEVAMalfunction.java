/*
 * Mars Simulation Project
 * RepairEVAMalfunction.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.malfunction.task;

import java.util.logging.Level;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.Malfunction;
import com.mars_sim.core.malfunction.MalfunctionRepairWork;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.malfunction.RepairHelper;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

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
	private static final TaskPhase REPAIRING = new TaskPhase(Msg.getString("Task.phase.repairing"),
														createPhaseImpact(SkillType.MECHANICS));

	private static final String WORK_FORMAT = " (%.1f millisols spent).";
	
	// Data members
	/** The malfunctionable entity being repaired. */
	private Malfunctionable entity;

	/** The malfunction to be repaired. */
	private Malfunction malfunction;

	/** Where to the parts come from */
	private EquipmentOwner partStore;

	public RepairEVAMalfunction(Person person, Malfunctionable entity, Malfunction malfunction) {
		super(NAME, person, 25, REPAIRING);
		setMinimumSunlight(LightLevel.NONE);

		// Check fitness - only if it's not in the state of emergency
		boolean isEmergency = false;
		Settlement s = person.getSettlement();
		if (s == null) {
			isEmergency = CollectionUtils.findSettlement(person.getCoordinates()).getRationing().isAtEmergency();
		}
		else {
			isEmergency = s.getRationing().isAtEmergency();
		}
		if (!isEmergency && person.isSuperUnfit()) {
			endEVA("Super Unfit.");
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
		}
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
		
		if (checkReadiness(time) > 0) {
			return time;
		}
		
        // Check if malfunction repair is done
		if (malfunction.isWorkDone(MalfunctionRepairWork.EVA)) {
			endEVA("Repair done.");
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
			endEVA("Part(s) not available.");
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
			endEVA("Repair done.");
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
		super.clearDown();
	}
}
