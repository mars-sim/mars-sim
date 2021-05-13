/**
 * Mars Simulation Project
 * RepairMalfunction.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The RepairMalfunction class is a task to repair a malfunction.
 */
public class RepairMalfunction extends Task implements Repair, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(RepairMalfunction.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.repairMalfunction"); //$NON-NLS-1$

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
	public RepairMalfunction(Person person) {
		super(NAME, person, true, false, STRESS_MODIFIER, SkillType.MECHANICS,
			  25D, 100D + RandomUtil.getRandomDouble(10D));

		if (person.isOutside()) {
			endTask();
			return;
		}
		
		// Get the malfunctioning entity.
		entity = getMalfunctionEntity(person);
		if (entity != null) {
			// Add person to location of malfunction if possible.
			addPersonOrRobotToMalfunctionLocation(entity);
			
			// Get an emergency malfunction.
			MalfunctionRepairWork required = MalfunctionRepairWork.EMERGENCY;
			malfunction = entity.getMalfunctionManager().getMostSeriousEmergencyMalfunction();
			if (malfunction == null) {
				//	Get a general malfunction.
				malfunction = entity.getMalfunctionManager().getMostSeriousGeneralMalfunction();
				required = MalfunctionRepairWork.GENERAL;
			}
			
			if (malfunction != null) {
				
				String chief = malfunction.getChiefRepairer(required);
				String deputy = malfunction.getDeputyRepairer(required);
				String myName = person.getName();
				if (chief == null) {
					logger.info(person, "Was appointed as the chief repairer handling the " + required.getName()
							+ " work for '" 
							+ malfunction.getName() + "' on "
							+ entity.getNickName());
					 malfunction.setChiefRepairer(required, myName);						
				}
				else if ((deputy == null) && !chief.equals(myName)) {
					logger.info(person, "Was appointed as the deputy repairer handling the " + required.getName() 
							+ " work for '" 
							+ malfunction.getName() + "' on "
							+ entity.getNickName());
					malfunction.setDeputyRepairer(required, myName);
				}
				// Initialize phase
				addPhase(REPAIRING);
				setPhase(REPAIRING);
	
				logger.log(person, Level.FINEST, 500, "Was about to repair malfunction.");

			} else {
				endTask();
			}			
		} else {
			endTask();
		}
	}

	public RepairMalfunction(Robot robot) {
		super(NAME, robot, true, false, STRESS_MODIFIER, SkillType.MECHANICS,
			  25D, 100D);
		
		if (robot.isOutside()) {
			endTask();
			return;
		}
		
		// Get the malfunctioning entity.
		entity = getMalfunctionEntity(robot);
		if (entity != null) {
			// Add robot to location of malfunction if possible.
			addPersonOrRobotToMalfunctionLocation(entity);
			
			// Initialize phase
			addPhase(REPAIRING);
			setPhase(REPAIRING);

			logger.log(robot, Level.FINEST, 500, "Was about to repair malfunction.");
			
		} else {
			endTask();
			return;
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
		
		// Check if there are no more malfunctions.
		if (!hasMalfunction(entity)) {
			endTask();
			return time;
		}

		double workTime = time;
		if (worker instanceof Robot) {
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
		
		if (malfunction != null) {
			// Add repair parts if necessary.
			if (hasRepairPartsForMalfunction(worker, malfunction)) {
				setDescription(Msg.getString("Task.description.repairMalfunction.detail", malfunction.getName(),
						entity.getNickName())); // $NON-NLS-1$
				
				Unit containerUnit = worker.getTopContainerUnit();
				if (!worker.isOutside()) {
					Inventory inv = containerUnit.getInventory();

					Map<Integer, Integer> parts = new ConcurrentHashMap<>(malfunction.getRepairParts());
					Iterator<Integer> j = parts.keySet().iterator();
					while (j.hasNext()) {
						Integer id = j.next();
						int number = parts.get(id);
						inv.retrieveItemResources(id, number);
						malfunction.repairWithParts(id, number, inv);
					}
				}
			}
		}

		else {
			endTask();
			return time;
		}

		// Add work to malfunction.
		if (malfunction.needEmergencyRepair() && !malfunction.isEmergencyRepairDone()) {
			malfunction.addEmergencyWorkTime(workTime, worker.getName());
			if (malfunction.isEmergencyRepairDone()) {
				logger.log(worker, Level.INFO, 1_000, "Wrapped up the Emergency Repair of "
							+ malfunction.getName()	+ " in "+ entity
							+ String.format(WORK_FORMAT,
									malfunction.getCompletedWorkTime(MalfunctionRepairWork.EMERGENCY)));
			}
		}
		else if (malfunction.needGeneralRepair() && !malfunction.isGeneralRepairDone()) {
			malfunction.addGeneralWorkTime(workTime, worker.getName());

			if (malfunction.isGeneralRepairDone()) {
				logger.log(worker, Level.INFO, 1_000, "Had completed the General Repair of "
						+ malfunction.getName() + " in "+ entity
						+ String.format(WORK_FORMAT,
								malfunction.getCompletedWorkTime(MalfunctionRepairWork.GENERAL)));								
			}
		}
		
		// Add experience
		addExperience(time);

		// Check if an accident happens during repair.
		checkForAccident(entity, time, 0.001D, getEffectiveSkillLevel(), "Repairing " + entity.getNickName());
		
		// Is the whole malfunction completed
		if (malfunction.isFixed()) {
			endTask();
		}
		
		return 0; // Used all available time repairing
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
			if (entity.getMalfunctionManager().hasMalfunction()) {
					result = entity;
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

		Malfunction result = null;

		MalfunctionManager manager = entity.getMalfunctionManager();

		// Check if entity has any malfunctions.
		Iterator<Malfunction> j = manager.getMalfunctions().iterator();
		while (j.hasNext() && (result == null)) {
			Malfunction malfunction = j.next();
			try {
				if (hasRepairPartsForMalfunction(person, person.getTopContainerUnit(),
						malfunction)) {
					result = malfunction;
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}

		// Check if entity needs no EVA and has any normal malfunctions.
		if ((result == null) && !hasEVA(person, entity)) {
			Iterator<Malfunction> k = manager.getGeneralMalfunctions().iterator();
			while (k.hasNext() && (result == null)) {
				Malfunction malfunction = k.next();
				try {
					if (hasRepairPartsForMalfunction(person, malfunction)) {
						result = malfunction;
					}
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}

		if (manager.hasMalfunction() && (result == null)) {
			logger.log(entity, Level.WARNING, 2000, "No parts available for any malfunction");
		}
		return result;
	}

	/**
	 * Gets a malfunctionable entity with a normal malfunction for a user.
	 * 
	 * @param robot the robot.
	 * @return malfunctionable entity.
	 */
	private static Malfunctionable getMalfunctionEntity(Robot robot) {
		Malfunctionable result = null;

		Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
		while (i.hasNext() && (result == null)) {
			Malfunctionable entity = i.next();
			
            if (entity instanceof Vehicle) {
            	// Note that currently robot cannot go outside and board a vehicle
            	continue;
            }
            
			if (entity.getMalfunctionManager().getMostSeriousEmergencyMalfunction() != null
					|| entity.getMalfunctionManager().getMostSeriousGeneralMalfunction() != null) { 
//				!requiresEVA(robot, entity) && 
//				hasMalfunction(robot, entity)) {
					result = entity;
			}
		}

		return result;
	}

	/**
	 * Checks if there are enough repair parts at person's location to fix the
	 * malfunction.
	 * 
	 * @param person        the person checking.
	 * @param containerUnit the unit the person is doing an EVA from.
	 * @param malfunction   the malfunction.
	 * @return true if enough repair parts to fix malfunction.
	 */
	public static boolean hasRepairPartsForMalfunction(Person person, Unit containerUnit, Malfunction malfunction) {

		if (person == null)
			throw new IllegalArgumentException("person is null");

		return hasRepairParts(containerUnit, malfunction);
	}

	
	/**
	 * Check if a malfunctionable entity requires an EVA to repair.
	 * 
	 * @param person the person doing the repair.
	 * @param entity the entity with a malfunction.
	 * @return true if entity requires an EVA repair.
	 */
	public static boolean hasEVA(Person person, Malfunctionable entity) {

		boolean result = false;

		if (entity instanceof Vehicle) {
			// Requires EVA repair on outside vehicles that the person isn't inside.
			Vehicle vehicle = (Vehicle) entity;
			boolean outsideVehicle = BuildingManager.getBuilding(vehicle) == null;
			boolean personNotInVehicle = !vehicle.getInventory().containsUnit(person);
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

	/**
	 * Check if a malfunctionable entity requires an EVA to repair.
	 * 
	 * @param settlement the settlement that needs the repair.
	 * @param entity the entity with a malfunction.
	 * @return true if entity requires an EVA repair.
	 */
	public static boolean hasEVA(Malfunctionable entity) {

		boolean result = false;

		if (entity instanceof Vehicle) {
			// Requires EVA repair on outside vehicles that the person isn't inside.
			Vehicle vehicle = (Vehicle) entity;
			boolean outsideVehicle = BuildingManager.getBuilding(vehicle) == null;
			if (outsideVehicle) {
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
	
	public static boolean hasEVA(Robot robot, Malfunctionable entity) {

		boolean result = false;

		if (entity instanceof Vehicle) {
			// Requires EVA repair on outside vehicles that the person isn't inside.
			Vehicle vehicle = (Vehicle) entity;
			boolean outsideVehicle = BuildingManager.getBuilding(vehicle) == null;
			boolean robotNotInVehicle = !vehicle.getInventory().containsUnit(robot);
			if (outsideVehicle && robotNotInVehicle) {
				result = true;
			}
		} 
		
		else if (entity instanceof Building) {
			// Requires EVA repair on uninhabitable buildings.
			Building building = (Building) entity;
			if (!building.hasFunction(FunctionType.LIFE_SUPPORT)) {
				result = true;
			}
		}

		return result;

	}

	/**
	 * Gets a malfunctional entity with a normal malfunction for a user.
	 * 
	 * @return malfunctional entity.
	 */
	private boolean hasMalfunction(Malfunctionable entity) {
		boolean result = false;

		if (entity.getMalfunctionManager().hasMalfunction())
			return true;
		
//		MalfunctionManager manager = entity.getMalfunctionManager();
//		Iterator<Malfunction> i = manager.getGeneralMalfunctions().iterator();
//		while (i.hasNext() && !result) {
//			if (hasRepairPartsForMalfunction(person, i.next())) {
//				return true;
//			}
//		}
//		
//		if (manager.getMostSeriousEmergencyMalfunction() != null
//				&& hasRepairPartsForMalfunction(person, manager.getMostSeriousEmergencyMalfunction()))
//			return true;
		
		return result;
	}

	/**
	 * Checks if the repair parts are available
	 * 
	 * @param containerUnit
	 * @param malfunction
	 * @return
	 */
	public static boolean hasRepairParts(Unit containerUnit, Malfunction malfunction) {

		boolean result = true;

		if (containerUnit == null)
			throw new IllegalArgumentException("containerUnit is null");

		if (malfunction == null)
			throw new IllegalArgumentException("malfunction is null");

		Inventory inv = containerUnit.getInventory();

		Map<Integer, Integer> repairParts = malfunction.getRepairParts();
		Iterator<Integer> i = repairParts.keySet().iterator();
		while (i.hasNext() && result) {
			Integer part = i.next();
			int number = repairParts.get(part);
			if (inv.getItemResourceNum(part) < number) {
				inv.addItemDemand(part, number);
				result = false;
			}
		}

		return result;
	}

	/**
	 * Checks if there are enough repair parts at person's location to fix the
	 * malfunction.
	 * 
	 * @param worker      the person checking.
	 * @param malfunction the malfunction.
	 * @return true if enough repair parts to fix malfunction.
	 */
	public static boolean hasRepairPartsForMalfunction(Worker worker, Malfunction malfunction) {
		if (worker == null) {
			throw new IllegalArgumentException("worker is null");
		}
		if (malfunction == null) {
			throw new IllegalArgumentException("malfunction is null");
		}

		boolean result = false;
		Unit containerUnit = worker.getTopContainerUnit();

		if (!worker.isOutside()) {
			result = true;
			Inventory inv = containerUnit.getInventory();

			Map<Integer, Integer> repairParts = malfunction.getRepairParts();
			Iterator<Integer> i = repairParts.keySet().iterator();
			while (i.hasNext() && result) {
				Integer part = i.next();
				int number = repairParts.get(part);
				if (inv.getItemResourceNum(part) < number) {
					inv.addItemDemand(part, number);
					result = false;
				}
			}
		}

		return result;
	}

	/**
	 * Checks if there are enough repair parts at person's location to fix the
	 * malfunction.
	 * 
	 * @param settlement      the settlement checking.
	 * @param malfunction the malfunction.
	 * @return true if enough repair parts to fix malfunction.
	 */
	public static boolean hasRepairPartsForMalfunction(Settlement settlement, Malfunction malfunction) {
		if (malfunction == null) {
			throw new IllegalArgumentException("malfunction is null");
		}

		boolean result = true;
	
		Map<Integer, Integer> repairParts = malfunction.getRepairParts();
		Iterator<Integer> i = repairParts.keySet().iterator();
		while (i.hasNext() && result) {
			Integer part = i.next();
			int number = repairParts.get(part);
			if (settlement.getInventory().getItemResourceNum(part) < number) {
				settlement.getInventory().addItemDemand(part, number);
				result = false;
			}
		}

		return result;
	}
	
	public static boolean hasRepairPartsForMalfunction(Robot robot, Malfunction malfunction) {
		if (robot == null) {
			throw new IllegalArgumentException("robot is null");
		}
		if (malfunction == null) {
			throw new IllegalArgumentException("malfunction is null");
		}

		boolean result = false;
		Unit containerUnit = robot.getTopContainerUnit();

		if (!(containerUnit instanceof MarsSurface)) {
			result = true;
			Inventory inv = containerUnit.getInventory();

			Map<Integer, Integer> repairParts = malfunction.getRepairParts();
			Iterator<Integer> i = repairParts.keySet().iterator();
			while (i.hasNext() && result) {
				Integer part = i.next();
				int number = repairParts.get(part);
				if (inv.getItemResourceNum(part) < number) {
					inv.addItemDemand(part, number);
					result = false;
				}
			}
		}

		return result;
	}


	@Override
	public Malfunctionable getEntity() {
		return entity;
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
				if (building.getBuildingType().equalsIgnoreCase(Building.ASTRONOMY_OBSERVATORY)) {
					if (worker.getSettlement().getBuildingConnectors(building).size() > 0) {
						// Walk to malfunctioning building.
						walkToRandomLocInBuilding(building, false);
						isWalk = true;
					}
				} else {
					walkToRandomLocInBuilding(building, false);
					isWalk = true;
				}
			}
		}

		else if (malfunctionable instanceof Rover) {

			if (worker instanceof Person) {
				// Walk to malfunctioning rover.
				walkToRandomLocInRover((Rover) malfunctionable, true);
				isWalk = true;
			} else {
				// robots are not allowed to enter a rover
			}
		}

		if (!isWalk) {
			walkToRandomLocation(true);
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		entity = null;
	}
}
