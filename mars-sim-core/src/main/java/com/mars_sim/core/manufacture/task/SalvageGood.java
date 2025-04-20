/**
 * Mars Simulation Project
 * SalvageGood.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package com.mars_sim.core.manufacture.task;

import java.util.Iterator;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.Manufacture;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.manufacture.ManufactureUtil;
import com.mars_sim.core.manufacture.SalvageProcess;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A task for salvaging a malfunctionable piece of equipment back down
 * into parts.
 */
public class SalvageGood extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.salvageGood"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase SALVAGE = new TaskPhase(Msg.getString(
            "Task.phase.salvage")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	// Data members
	/** The manufacturing workshop the person is using. */
	private Manufacture workshop;
	/** The salvage process. */
	private SalvageProcess process;

	/**
	 * Constructor.
	 * 
	 * @param person the person to perform the task
	 */
	public SalvageGood(Person person, Building building) {
		super(NAME, person, true, false, STRESS_MODIFIER,
				SkillType.MATERIALS_SCIENCE, 100D, 10D + RandomUtil.getRandomDouble(40D));

		// Get available manufacturing workshop if any.
		this.workshop = building.getManufacture();

		// Walk to manufacturing workshop.
		walkToTaskSpecificActivitySpotInBuilding(building, FunctionType.MANUFACTURE, false);

		// Determine salvage process.
		process = determineSalvageProcess();
		if (process != null) {
			setDescription(process.toString());
		}
		else {
			endTask();
		}

		// Initialize phase
		addPhase(SALVAGE);
		setPhase(SALVAGE);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		}
		else if (SALVAGE.equals(getPhase())) {
			return salvagePhase(time);
		}
		else {
			return time;
		}
	}

	/**
	 * Performs the salvaging phase.
	 * 
	 * @param time the time to perform (millisols)
	 * @return remaining time after performing (millisols)
	 */
	private double salvagePhase(double time) {

		// Check if workshop has malfunction.
		Malfunctionable entity = workshop.getBuilding();
		if (entity.getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

		// Check if salvage has been completed.
		if (process.getWorkTimeRemaining() <= 0D) {
			endTask();
			return time;
		}

		// Determine amount of effective work time based on "Materials Science" skill.
		double workTime = time;
		int skill = getEffectiveSkillLevel();
		if (skill == 0) {
			workTime /= 2;
		}
		else {
			workTime += workTime * (.2D * skill);
		}

		// Apply work time to salvage process.
		if (!process.addWorkTime(workTime, skill)) {
			endTask();
		}

		// Add experience
		addExperience(time);

		// Check for accident in workshop.
		checkForAccident(entity, time, 0.01);

		return 0D;
	}

	/**
	 * Checks if manufacturing building has any salvage processes requiring work.
	 * 
	 * @param manufacturingBuilding the manufacturing building.
	 * @param skill the materials science skill level of the person.
	 * @return true if processes requiring work.
	 */
	public static boolean hasSalvageProcessRequiringWork(Building manufacturingBuilding,
			int skill) {

		boolean result = false;

		Manufacture manufacturingFunction = manufacturingBuilding.getManufacture();
		Iterator<SalvageProcess> i = manufacturingFunction.getSalvageProcesses().iterator();
		while (i.hasNext()) {
			SalvageProcess process = i.next();
			boolean workRequired = (process.getWorkTimeRemaining() > 0D);
			boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
			if (workRequired && skillRequired) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Gets an available running salvage process.
	 * 
	 * @return process or null if none.
	 */
	private SalvageProcess getRunningSalvageProcess() {

		int skillLevel = getEffectiveSkillLevel();

		for(var p : workshop.getSalvageProcesses()) {
			if ((p.getInfo().getSkillLevelRequired() <= skillLevel) &&
					(p.getWorkTimeRemaining() > 0D)) {
				return p;
			}
		}
		
		return null;
	}

	/**
	 * Creates a new salvage process if possible.
	 * 
	 * @return the new salvage process or null if none.
	 */
	private SalvageProcess createNewSalvageProcess() {
		SalvageProcess result = null;

		if (workshop.getCurrentTotalProcesses() < workshop.getNumPrintersInUse()) {

			int skillLevel = getEffectiveSkillLevel();
			int techLevel = workshop.getTechLevel();

			// Randomly determine process based on value weights.
			var s = workshop.getBuilding().getSettlement();
			var selectedProcess = s.getManuManager().claimNextProcess(techLevel, skillLevel, false);

			if (selectedProcess != null && selectedProcess.getInfo() instanceof SalvageProcessInfo spi) {
				// Salvage unit preselected?
				var salvagedUnit = selectedProcess.getTarget();
				if (salvagedUnit == null) {
					salvagedUnit = ManufactureUtil.findUnitForSalvage(spi, s);
					if (salvagedUnit == null) {
						return null;
					}
				}
				
				result = new SalvageProcess(spi, workshop, salvagedUnit);
				result.startProcess();
			}
		}

		return result;
	}

	/**
	 * Determines a salvage process used for the task.
	 * 
	 * @return salvage process or null if none determined.
	 */
	private SalvageProcess determineSalvageProcess() {
		SalvageProcess p = getRunningSalvageProcess();
		if (p == null) {
			p = createNewSalvageProcess();
		}
		return p;
	}
}
