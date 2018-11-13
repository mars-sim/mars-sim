/**
 * Mars Simulation Project
 * RepairMalfunction.java
 * @version 3.1.0 2017-03-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
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
	private static Logger logger = Logger.getLogger(RepairMalfunction.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.repairMalfunction"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REPAIRING = new TaskPhase(Msg.getString("Task.phase.repairing")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .3D;

	// Data members
	/** Entity being repaired. */
	private Malfunctionable entity;

	/**
	 * Constructor
	 * 
	 * @param person the person to perform the task
	 */
	public RepairMalfunction(Person person) {
		super(NAME, person, true, false, STRESS_MODIFIER, true, 10D + RandomUtil.getRandomDouble(50D));

		// Get the malfunctioning entity.
		entity = getMalfunctionEntity(person);
		if (entity != null) {
			// Add person to location of malfunction if possible.
			addPersonOrRobotToMalfunctionLocation(entity);
		} else {
			endTask();
		}

		// Initialize phase
		addPhase(REPAIRING);
		setPhase(REPAIRING);

		logger.fine(person.getName() + " repairing malfunction.");
	}

	public RepairMalfunction(Robot robot) {
		super(NAME, robot, true, false, STRESS_MODIFIER, true, 10D + RandomUtil.getRandomDouble(50D));

		// Get the malfunctioning entity.
		entity = getMalfunctionEntity(robot);
		if (entity != null) {
			// Add robot to location of malfunction if possible.
			addPersonOrRobotToMalfunctionLocation(entity);
		} else {
			endTask();
		}

		// Initialize phase
		addPhase(REPAIRING);
		setPhase(REPAIRING);

		logger.fine(robot.getName() + " repairing malfunction.");
	}

	/**
	 * Gets a malfunctional entity with a normal malfunction for a user.
	 * 
	 * @param person the person.
	 * @return malfunctional entity.
	 */
	private static Malfunctionable getMalfunctionEntity(Person person) {
		Malfunctionable result = null;

		Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
		while (i.hasNext() && (result == null)) {
			Malfunctionable entity = i.next();
			if (!requiresEVA(person, entity)) {
				if (hasMalfunction(person, entity)) {
					result = entity;
				}
			}
		}

		return result;
	}

	private static Malfunctionable getMalfunctionEntity(Robot robot) {
		Malfunctionable result = null;

		Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
		while (i.hasNext() && (result == null)) {
			Malfunctionable entity = i.next();
			if (!requiresEVA(robot, entity)) {
				if (hasMalfunction(robot, entity)) {
					result = entity;
				}
			}
		}

		return result;
	}

	/**
	 * Check if a malfunctionable entity requires an EVA to repair.
	 * 
	 * @param person the person doing the repair.
	 * @param entity the entity with a malfunction.
	 * @return true if entity requires an EVA repair.
	 */
	public static boolean requiresEVA(Person person, Malfunctionable entity) {

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

	public static boolean requiresEVA(Robot robot, Malfunctionable entity) {

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
	private static boolean hasMalfunction(Person person, Malfunctionable entity) {
		boolean result = false;

		MalfunctionManager manager = entity.getMalfunctionManager();
		Iterator<Malfunction> i = manager.getNormalMalfunctions().iterator();
		while (i.hasNext() && !result) {
			if (hasRepairPartsForMalfunction(person, i.next())) {
				result = true;
			}
		}

		return result;
	}

	private static boolean hasMalfunction(Robot robot, Malfunctionable entity) {
		boolean result = false;

		MalfunctionManager manager = entity.getMalfunctionManager();
		Iterator<Malfunction> i = manager.getNormalMalfunctions().iterator();
		while (i.hasNext() && !result) {
			if (hasRepairPartsForMalfunction(robot, i.next())) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Checks if there are enough repair parts at person's location to fix the
	 * malfunction.
	 * 
	 * @param person      the person checking.
	 * @param malfunction the malfunction.
	 * @return true if enough repair parts to fix malfunction.
	 */
	public static boolean hasRepairPartsForMalfunction(Person person, Malfunction malfunction) {
		if (person == null) {
			throw new IllegalArgumentException("person is null");
		}
		if (malfunction == null) {
			throw new IllegalArgumentException("malfunction is null");
		}

		boolean result = false;
		Unit containerUnit = person.getTopContainerUnit();

		if (containerUnit != null) {
			result = true;
			Inventory inv = containerUnit.getInventory();

			Map<Integer, Integer> repairParts = malfunction.getRepairParts();
			Iterator<Integer> i = repairParts.keySet().iterator();
			while (i.hasNext() && result) {
				Integer part = i.next();
				int number = repairParts.get(part);
				if (inv.getItemResourceNum(part) < number) {
					result = false;
				}
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

		if (containerUnit != null) {
			result = true;
			Inventory inv = containerUnit.getInventory();

			Map<Integer, Integer> repairParts = malfunction.getRepairParts();
			Iterator<Integer> i = repairParts.keySet().iterator();
			while (i.hasNext() && result) {
				Integer part = i.next();
				int number = repairParts.get(part);
				if (inv.getItemResourceNum(part) < number) {
					result = false;
				}
			}
		}

		return result;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
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

		// TODO: double check to see if checking person and robot as follows are valid
		// or not
		if (person != null) {
			// Check if there are no more malfunctions.
			if (!hasMalfunction(person, entity)) {
				endTask();
			}
		} else if (robot != null) {
			// Check if there are no more malfunctions.
			if (!hasMalfunction(robot, entity)) {
				endTask();
			}
		}

		if (isDone()) {
			return time;
		}

		double workTime = 0;

		if (person != null) {
			workTime = time;
		} else if (robot != null) {
			// A robot moves slower than a person and incurs penalty on workTime
			workTime = time / 2;
		}

		// Determine effective work time based on "Mechanic" skill.
		int mechanicSkill = 0;
		if (person != null)
			mechanicSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);
		else if (robot != null)
			mechanicSkill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);

		if (mechanicSkill == 0) {
			workTime /= 2;
		} else if (mechanicSkill > 1) {
			workTime += workTime * (.2D * mechanicSkill);
		}

		// Get a local malfunction.
		Malfunction malfunction = null;
		Iterator<Malfunction> i = entity.getMalfunctionManager().getNormalMalfunctions().iterator();
		while (i.hasNext() && (malfunction == null)) {
			Malfunction tempMalfunction = i.next();

			if (person != null) {
				if (hasRepairPartsForMalfunction(person, tempMalfunction)) {
					malfunction = tempMalfunction;
					setDescription(Msg.getString("Task.description.repairMalfunction.detail", malfunction.getName(),
							entity.getNickName())); // $NON-NLS-1$
				}
			} else if (robot != null) {
				if (hasRepairPartsForMalfunction(robot, tempMalfunction)) {
					malfunction = tempMalfunction;
					setDescription(Msg.getString("Task.description.repairMalfunction.detail", malfunction.getName(),
							entity.getNickName())); // $NON-NLS-1$
				}
			}
		}

		if (person != null) {
			// Add repair parts if necessary.
			if (hasRepairPartsForMalfunction(person, malfunction)) {
				Inventory inv = person.getTopContainerUnit().getInventory();
				Map<Integer, Integer> parts = new HashMap<>(malfunction.getRepairParts());
				Iterator<Integer> j = parts.keySet().iterator();
				while (j.hasNext()) {
					Integer id = j.next();
					int number = parts.get(id);
					inv.retrieveItemResources(id, number);
					malfunction.repairWithParts(id, number, inv);
				}
			}

		} else if (robot != null) {
			// Add repair parts if necessary.
			if (hasRepairPartsForMalfunction(robot, malfunction)) {
				Inventory inv = robot.getTopContainerUnit().getInventory();
				Map<Integer, Integer> parts = new HashMap<>(malfunction.getRepairParts());
				Iterator<Integer> j = parts.keySet().iterator();
				while (j.hasNext()) {
					Integer id = j.next();
					int number = parts.get(id);
					inv.retrieveItemResources(id, number);
					malfunction.repairWithParts(id, number, inv);
				}
			}
		}

		else {
			endTask();
			return time;
		}

		// Add work to malfunction.
		// logger.info(description);
		double workTimeLeft = 0;
		if (person != null) {
			malfunction.addWorkTime(workTime, person.getName());
		} else {
			malfunction.addWorkTime(workTime, robot.getName());
		}

		// Add experience
		addExperience(time);

		// Check if an accident happens during repair.
		checkForAccident(time);

		if (person != null) {
			// Check if there are no more malfunctions.
			if (!hasMalfunction(person, entity)) {
				endTask();
			}
		} else if (robot != null) {
			// Check if there are no more malfunctions.
			if (!hasMalfunction(robot, entity)) {
				endTask();
			}
		}

		return workTimeLeft;
	}

	@Override
	protected void addExperience(double time) {
		// Add experience to "Mechanics" skill
		// (1 base experience point per 20 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		double newPoints = time / 20D;

		if (person != null) {
			int experienceAptitude = person.getNaturalAttributeManager()
					.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
			newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
			newPoints *= getTeachingExperienceModifier();
			person.getMind().getSkillManager().addExperience(SkillType.MECHANICS, newPoints);

		} else if (robot != null) {
			int experienceAptitude = robot.getRoboticAttributeManager()
					.getAttribute(RoboticAttributeType.EXPERIENCE_APTITUDE);
			newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
			newPoints *= getTeachingExperienceModifier();
			robot.getBotMind().getSkillManager().addExperience(SkillType.MECHANICS, newPoints);

		}

	}

	/**
	 * Check for accident with entity during maintenance phase.
	 * 
	 * @param time the amount of time (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .001D;
		int skill = 0;
		if (person != null) {
			// Mechanic skill modification.
			skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);

		} else if (robot != null) {
			// Mechanic skill modification.
			skill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.MECHANICS);

		}

		if (skill <= 3) {
			chance *= (4 - skill);
		} else {
			chance /= (skill - 2);
		}

		// Modify based on the entity's wear condition.
		chance *= entity.getMalfunctionManager().getWearConditionAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			// logger.info(person.getName() + " has accident while " + description);

			if (person != null) {
				logger.info(person.getName() + " ran into an accident while repairing " + entity.getNickName());
				entity.getMalfunctionManager().createASeriesOfMalfunctions("repairing " + entity.getNickName(), person);
			} else if (robot != null) {
				logger.info(robot.getName() + " ran into an accident while repairing " + entity.getNickName());
				entity.getMalfunctionManager().createASeriesOfMalfunctions("repairing " + entity.getNickName(), robot);
			}
		}
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

			if (person != null) {

				if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
					// Walk to malfunctioning building.
					walkToRandomLocInBuilding(building, true);
					isWalk = true;
				}
			}

			else if (robot != null) {
				// Note 1 : robot doesn't need life support
				// Note 2 : robot cannot come thru the airlock yet to the astronomy building
				if (building.getNickName().toLowerCase().contains("astronomy")) {
					if (robot.getSettlement().getBuildingConnectors(building).size() > 0) {
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

			if (person != null) {
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
	public int getEffectiveSkillLevel() {
		SkillManager manager = null;
		if (person != null)
			manager = person.getMind().getSkillManager();
		else if (robot != null)
			manager = robot.getBotMind().getSkillManager();

		return manager.getEffectiveSkillLevel(SkillType.MECHANICS);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.MECHANICS);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();

		entity = null;
	}
}