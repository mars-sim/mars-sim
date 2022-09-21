/*
 * Mars Simulation Project
 * RepairInsideMalfunction.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.malfunction.RepairHelper;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The RepairInsideMalfunction class is a task to repair a malfunction.
 */
public class RepairInsideMalfunction extends Task implements Repair, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(RepairInsideMalfunction.class.getName());
	
	/** Simple Task name */
	static final String SIMPLE_NAME = RepairInsideMalfunction.class.getSimpleName();
	
	/** Task description name */
	static final String NAME = Msg.getString("Task.description.repairMalfunction"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REPAIRING = new TaskPhase(Msg.getString("Task.phase.repairing")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .5D;

	private static final String WORK_FORMAT = " (%.1f millisols spent).";

	// Data members
	/** Entity being repaired. */
	private Malfunctionable entity;

	private Malfunction malfunction;

	/**
	 * Constructor
	 *
	 * @param person the person to perform the task
	 */
	public RepairInsideMalfunction(Person person) {
		super(NAME, person, true, false, STRESS_MODIFIER, SkillType.MECHANICS,
			  25D, 100D + RandomUtil.getRandomDouble(10D));
		initRepair();
	}


	public RepairInsideMalfunction(Robot robot) {
		super(NAME, robot, true, false, STRESS_MODIFIER, SkillType.MECHANICS,
			  25D, 100D);
		initRepair();
	}

	/**
	 * Chooses the malfunctioning entity.
	 */
	private void chooseEntity() {
		// Get the malfunctioning entity.
		for (Malfunctionable next : MalfunctionFactory.getLocalMalfunctionables(worker)) {
			Malfunction potential = next.getMalfunctionManager().getMostSeriousMalfunctionInNeed(MalfunctionRepairWork.INSIDE);
			if (potential != null) {
				entity = next;
				malfunction = potential;
				break; // Stop searching
			}
		}
	}
	
	/**
	 * Finds a repair with available slots and register for the work.
	 */
	private void initRepair() {
		if (worker.isOutside()) {
			endTask();
			return;
		}

		// Choose the malfunctioning entity
		chooseEntity();

		if (entity != null) {
			// Prep up for repair
			prepareForRepair();
		}
		else {
			for (Malfunctionable next : MalfunctionFactory.getAssociatedMalfunctionables(worker.getSettlement())) {
				List<Malfunction> list = next.getMalfunctionManager().getMalfunctions();
				if (!list.isEmpty()) {
					Malfunction potential = next.getMalfunctionManager().getMalfunctions().get(0);
					entity = next;
					malfunction = potential;
					break; // Stop searching
				}
			}
			
			if (entity != null) {
				// Prep up for repair
				prepareForRepair();
			}
			else {
				logger.warning(worker, 30_000, "Could not find a malfunction to work on in my vicinity.");
				endTask();
			}
		}
	}

	/**
	 * Prepares for a repair.
	 */
	private void prepareForRepair() {
		// Add person to location of malfunction if possible.
		addPersonOrRobotToMalfunctionLocation(entity);

		// Possible the Task could be aborted due to walking problems
		if (!isDone()) {
			RepairHelper.prepareRepair(malfunction, worker, MalfunctionRepairWork.INSIDE, entity);

			// Initialize phase
			addPhase(REPAIRING);
			setPhase(REPAIRING);
		}
	}
	
	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			return 0;
//			throw new IllegalArgumentException("Task phase is null");
		} else if (REPAIRING.equals(getPhase())) {
			return repairingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the repairing phase of the task.
	 *
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double repairingPhase(double time) {

		if (isDone()) {
			return time;
		}

		if (malfunction == null) {
			endTask();
			return time;
		}

		// Someone else has already finished the last part
		if (malfunction.isWorkDone(MalfunctionRepairWork.INSIDE)) {
			endTask();
			return time;
		}

		double workTime = time;
		if (worker.getUnitType() == UnitType.ROBOT) {
			// A robot moves slower than a person and incurs penalty on workTime
			workTime = time / 2;
		}

		// Determine effective work time based on "Mechanic" skill.
		int mechanicSkill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);

		if (mechanicSkill == 0) {
			workTime /= 2;
		} else if (mechanicSkill > 1) {
			workTime += workTime * (.2D * mechanicSkill);
		}

		Unit containerUnit = worker.getTopContainerUnit();
		
		// Add repair parts if necessary.
		if (RepairHelper.hasRepairParts(worker.getTopContainerUnit(), malfunction)) {
			setDescription(Msg.getString("Task.description.repairMalfunction.detail", malfunction.getName(),
					entity.getName())); // $NON-NLS-1$

			if (!worker.isOutside()) {
				logger.log(worker, Level.FINE, 10_000, "Parts for repairing malfunction '" + malfunction + "' available from " + containerUnit.getName() + ".");
				RepairHelper.claimRepairParts(containerUnit, malfunction);
			}
		} 
		else {
			logger.log(worker, Level.FINE, 10_000, "Parts for repairing malfunction '" + malfunction + "' not available from " + containerUnit.getName() + ".");
		}

		// Add work to malfunction.
//		double workTimeLeft = malfunction.addWorkTime(MalfunctionRepairWork.INSIDE, workTime, worker.getName());

		// Add experience
		addExperience(time);

		// Check if an accident happens during repair.
		checkForAccident(entity, time, 0.001D, getEffectiveSkillLevel(), "Repairing " + entity.getName());

		// Add EVA work to malfunction.
		double workTimeLeft = 0D;
		if (!malfunction.isWorkDone(MalfunctionRepairWork.INSIDE)) {
			logger.log(worker, Level.FINE, 10_000, "Performing inside repair on malfunction '" + malfunction + "' at " + containerUnit.getName() + ".");
			// Add work to malfunction.
			workTimeLeft = malfunction.addWorkTime(MalfunctionRepairWork.INSIDE, workTime, worker.getName());
		}
		else {
			// Work fully completed
			logger.log(worker, Level.INFO, 1_000, "Wrapped up inside repair work for '"
					+ malfunction.getName()	+ "' in " + entity
					+ String.format(WORK_FORMAT,
							malfunction.getCompletedWorkTime(MalfunctionRepairWork.INSIDE)));
			
			endTask();
		}

		return workTimeLeft;
	}


	/**
	 * Gets a malfunctionable entity with a normal malfunction for a user.
	 *
	 * @param person the person.
	 * @return malfunctionable entity.
	 */
	public static Malfunctionable getMalfunctionEntity(Person person) {
		Malfunctionable result = null;

		Iterator<Malfunctionable> i = MalfunctionFactory.getLocalMalfunctionables(person).iterator();
		while (i.hasNext() && (result == null)) {
			Malfunctionable entity = i.next();
			
			List<Malfunction> malfunctions = entity.getMalfunctionManager().getMalfunctions();
			for (Malfunction m: malfunctions) {
				if (m.hasWorkType(MalfunctionRepairWork.INSIDE)) {
					if (entity.getUnitType() == UnitType.BUILDING) {
						Building building = (Building) entity;
						if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
							result = entity;
						}
					}
					else if (entity.getUnitType() == UnitType.VEHICLE) {
						Vehicle vehicle = (Vehicle) entity;
						if (vehicle.isInAGarage()) {
							result = entity;
						}
					}
					else if (entity.getUnitType() == UnitType.EQUIPMENT) {
						Equipment equipment = (Equipment) entity;
						if (equipment.isInside()) {
							result = entity;
						}
					}
					else if (entity.getUnitType() == UnitType.SETTLEMENT) {
						logger.warning(entity, "entity is " + entity);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets a reparable malfunction requiring an EVA for a given entity.
	 *
	 * @param person the person to repair.
	 * @param entity the entity with a malfunction.
	 * @return malfunction requiring an EVA repair or null if none found.
	 */
	public static Malfunction getMalfunction(Person person, Malfunctionable entity) {

		MalfunctionManager manager = entity.getMalfunctionManager();

		// Check if entity has any malfunctions.
		for(Malfunction malfunction : manager.getMalfunctions()) {
			try {
				if (RepairHelper.hasRepairParts(person.getTopContainerUnit(),
						malfunction)) {
					return malfunction;
				}
			} catch (Exception e) {
				logger.log(entity, Level.WARNING, 2000, "Problems in top container unit's malfunction: " + e.getMessage());
			}
		}

		// Check if entity needs no EVA and has any normal malfunctions.
		if (!hasEVA(person, entity)) {
			for(Malfunction malfunction : manager.getAllInsideMalfunctions()) {
				try {
					if (RepairHelper.hasRepairParts(person, malfunction)) {
						return malfunction;
					}
				} catch (Exception e) {
					logger.log(entity, Level.WARNING, 2000, "Problems in general malfunction's repair parts: " + e.getMessage());
				}
			}
		}

		if (manager.hasMalfunction()) {
			logger.log(entity, Level.WARNING, 2000, "No parts available for any malfunction");
		}
		return null;
	}


	/**
	 * Check if a malfunctionable entity requires an EVA to repair.
	 *
	 * @param person the person doing the repair.
	 * @param entity the entity with a malfunction.
	 * @return true if entity requires an EVA repair.
	 */
	private static boolean hasEVA(Person person, Malfunctionable entity) {

		boolean result = false;

		if (entity instanceof Vehicle) {
			// Requires EVA repair on outside vehicles that the person isn't inside.
			Vehicle vehicle = (Vehicle) entity;
			boolean outsideVehicle = vehicle.getGarage() == null;
			boolean personNotInVehicle = !((Crewable)vehicle).isCrewmember(person);
			if (outsideVehicle && personNotInVehicle) {
				result = true;
			}
		} else if (entity instanceof Building) {
			// Requires EVA repair on uninhabitable buildings.
			Building building = (Building) entity;
			if (!building.hasFunction(FunctionType.LIFE_SUPPORT)) {
				result = true;
			}
		}

		return result;
	}

	@Override
	public Malfunctionable getEntity() {
		return entity;
	}

	/**
	 * Worker leaves the Malfunction effort
	 */
	@Override
	protected void clearDown() {
		// Leaving the repair effort
		if (malfunction != null) {
			malfunction.leaveWork(MalfunctionRepairWork.INSIDE, worker.getName());
		}
	}

	/**
	 * Adds the person to building if malfunctionable is a building with life
	 * support. Otherwise walk to random location.
	 *
	 * @param malfunctionable the malfunctionable the person is repairing.
	 */
	private void addPersonOrRobotToMalfunctionLocation(Malfunctionable malfunctionable) {

		boolean isWalk = false;
		if (malfunctionable instanceof Building) {
			Building building = (Building) malfunctionable;

			if (worker instanceof Person) {

				if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
					// Walk to malfunctioning building.
					walkToRandomLocInBuilding(building, true);
					isWalk = true;
				}
			}

			else if (worker instanceof Robot) {
				// Note 1 : robot doesn't need life support
				// Note 2 : robot cannot come thru the airlock yet to the astronomy building
				if (building.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATION)
						|| building.hasFunction(FunctionType.EARTH_RETURN)) {
					if (worker.getSettlement().getBuildingConnectors(building).size() > 0) {
						// Walk to malfunctioning building.
						walkToRandomLocInBuilding(building, true);
						isWalk = true;
					}
					else {
						logger.warning(worker, "Can not walk inside " + building.getNickName());
						endTask();
						return;
					}
				} else {
					walkToRandomLocInBuilding(building, true);
					isWalk = true;
				}
			}
		}

		else if (malfunctionable instanceof Rover) {

			if (worker instanceof Person) {
				// Walk to malfunctioning rover.
				walkToRandomLocInRover((Rover) malfunctionable, true);
				isWalk = true;
			}
		}

		if (!isWalk) {
			walkToRandomLocation(true);
		}
	}
}
