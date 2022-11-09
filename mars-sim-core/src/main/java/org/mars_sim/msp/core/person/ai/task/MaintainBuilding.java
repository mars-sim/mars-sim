/*
 * Mars Simulation Project
 * MaintainBuilding.java
 * @date 2022-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The task for performing preventive maintenance on buildings.
 */
public class MaintainBuilding extends Task  {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MaintainBuilding.class.getName());
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.maintainBuilding"); //$NON-NLS-1$

    private static final String DETAIL = "Task.description.maintainBuilding.detail";
    
	/** Task phases. */
	private static final TaskPhase MAINTAIN = new TaskPhase(Msg.getString("Task.phase.maintain")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	// Data members
	/** Entity to be maintained. */
	private Malfunctionable entity;

	/**
	 * Constructor.
	 *
	 * @param person the person to perform the task
	 */
	public MaintainBuilding(Person person, Building entity) {
		super(NAME, person, true, false, STRESS_MODIFIER, SkillType.MECHANICS, 100D,
				RandomUtil.getRandomDouble(5, 20));

		if (person.isOutside()) {
			endTask();
			return;
		}

		init(entity);
	}

	public MaintainBuilding(Robot robot, Building entity) {
		super(NAME, robot, true, false, STRESS_MODIFIER, SkillType.MECHANICS, 100D,
				RandomUtil.getRandomDouble(10, 40));

		if (robot.isOutside()) {
			endTask();
			return;
		}

		init(entity);
	}

	/**
	 * Setup the maintenance activity.
	 * @param entity Target for work.
	 */
	private void init(Building building) {
		this.entity = building;

		if (!MaintainBuilding.hasMaintenanceParts(worker.getSettlement(), entity)) {		
			clearTask("No parts");
			return;
		}
		MalfunctionManager manager = building.getMalfunctionManager();
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		if (effectiveTime < 10D) {
			clearTask("Maintenance already done");
			return;
		}

		String des = Msg.getString(DETAIL, entity.getName()); //$NON-NLS-1$
		setDescription(des);
		logger.info(worker, 30_000, des + ".");
		// Walk to random location in building.
		walkToRandomLocInBuilding(building, false);

		// Initialize phase.
		addPhase(MAINTAIN);
		setPhase(MAINTAIN);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (MAINTAIN.equals(getPhase())) {
			return maintainPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the maintain phase.
	 *
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double maintainPhase(double time) {
    	double remainingTime = 0;
    	
		// If worker is incapacitated, end task.
		if (worker.getPerformanceRating() <= .1) {
			endTask();
			return time;
		}

		MalfunctionManager manager = entity.getMalfunctionManager();

		// If equipment has malfunction, end task.
		if (manager.hasMalfunction()) {
			endTask();
			return time * .75;
		}

		if (isDone()) {
			endTask();
			return time;
		}

		// Determine effective work time based on "Mechanic" skill.
		double workTime = time;
		int mechanicSkill = getEffectiveSkillLevel();
		if (mechanicSkill == 0) {
			workTime /= 2;
		}
		if (mechanicSkill > 1) {
			workTime += workTime * (.4D * mechanicSkill);
		}

		Unit containerUnit = worker.getTopContainerUnit();

		if (containerUnit instanceof MarsSurface) {
			return time;
		}
			
		if (MaintainBuilding.hasMaintenanceParts(containerUnit, entity)) {

			Map<Integer, Integer> parts = new HashMap<>(manager.getMaintenanceParts());
			Iterator<Integer> j = parts.keySet().iterator();

			if (containerUnit.getUnitType() == UnitType.SETTLEMENT) {
				while (j.hasNext()) {
					Integer part = j.next();
					int number = parts.get(part);
					((Settlement)containerUnit).retrieveItemResource(part, number);
			        // Add repair parts if necessary.
					manager.maintainWithParts(part, number);
				}
			}
		}

		else {
			endTask();
			return time * .75;
		}

		// Add work to the maintenance
		manager.addMaintenanceWorkTime(workTime);

		// Add experience points
		addExperience(time);

		// Check if an accident happens during maintenance.
		checkForAccident(entity, time, 0.005D);

		return remainingTime;
	}

	/**
	 * Gets the entity the person is maintaining. Returns null if none.
	 *
	 * @return entity
	 */
	public Malfunctionable getEntity() {
		return entity;
	}

	/**
	 * Checks if a malfunctionable is an inhabitable building.
	 *
	 * @param malfunctionable the malfunctionable.
	 * @return true if inhabitable building.
	 */
	private boolean isInhabitableBuilding(Malfunctionable malfunctionable) {
		boolean result = false;
		if (malfunctionable instanceof Building) {
			Building building = (Building) malfunctionable;
			if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
				result = true;
			}
		}
		return result;
	}


	/**
	 * Checks if there are enough local parts to perform maintenance.
	 *
	 * @param worker          the person performing the maintenance.
	 * @param malfunctionable the entity needing maintenance.
	 * @return true if enough parts.
	 * @throws Exception if error checking parts availability.
	 */
	public static boolean hasMaintenanceParts(Worker worker, Malfunctionable malfunctionable) {
		Unit unit = null;

		if (worker.isInSettlement())
			// This is also the case when the person is in a garage
			unit = worker.getSettlement();

		else if (worker.isRightOutsideSettlement())
			unit = worker.getNearbySettlement();
		else if (worker.isInVehicle())
			unit = worker.getVehicle();

		if (unit != null)
			return hasMaintenanceParts(unit, malfunctionable);

		return false;
	}

	/**
	 * Checks if a part is available for starting the maintenance.
	 * Note: it doesn't mean all the parts need to be available to initiate the maintenance.
	 *
	 * @param settlement the settlement holding the needed parts.
	 * @param malfunctionable the entity needing maintenance.
	 * @return true if a part is available or no parts are needed.
	 */
	public static boolean hasMaintenanceParts(Settlement settlement, Malfunctionable malfunctionable) {
		boolean result = false;

		Map<Integer, Integer> parts = malfunctionable.getMalfunctionManager().getMaintenanceParts();
		if (parts.isEmpty())
			return true;
		Iterator<Integer> i = parts.keySet().iterator();
		while (i.hasNext()) {
			Integer part = i.next();
			int number = parts.get(part);
			if (settlement.getItemResourceStored(part) >= number) {
				return true;
			}
		}

		return result;
	}

	/**
	 * Checks if there are enough local parts to perform maintenance.
	 *
	 * @param unit the unit holding the needed parts.
	 * @param malfunctionable the entity needing maintenance.
	 * @return true if a part is available or no parts are needed.
	 * @throws Exception if error checking parts availability.
	 */
	static boolean hasMaintenanceParts(Unit unit, Malfunctionable malfunctionable) {
		boolean result = false;

		if (unit.getUnitType() == UnitType.SETTLEMENT) {
			return hasMaintenanceParts((Settlement)unit, malfunctionable);
		}
		else {
			Map<Integer, Integer> parts = malfunctionable.getMalfunctionManager().getMaintenanceParts();
			if (parts.isEmpty())
				return true;
			Iterator<Integer> i = parts.keySet().iterator();
			while (i.hasNext()) {
				Integer part = i.next();
				int number = parts.get(part);
				if (((Vehicle)unit).getItemResourceStored(part) >= number) {
					return true;
				}
			}
		}

		return result;
	}
}
