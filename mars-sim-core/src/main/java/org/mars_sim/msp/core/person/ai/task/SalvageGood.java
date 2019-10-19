/**
 * Mars Simulation Project
 * SalvageGood.java
 * @version 3.1.0 2019-09-20
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

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.manufacture.SalvageProcess;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * A task for salvaging a malfunctionable piece of equipment back down
 * into parts.
 */
public class SalvageGood
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(SalvageGood.class.getName());

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
	 * @param person the person to perform the task
	 */
	public SalvageGood(Person person) {
		super(NAME, person, true, false, STRESS_MODIFIER,
				true, 10D + RandomUtil.getRandomDouble(40D));

		// Get available manufacturing workshop if any.
		Building manufactureBuilding = getAvailableManufacturingBuilding(person);
		if (manufactureBuilding != null) {
			workshop = (Manufacture) manufactureBuilding.getFunction(FunctionType.MANUFACTURE);

			// Walk to manufacturing workshop.
			walkToActivitySpotInBuilding(manufactureBuilding, false);
		}
		else {
			endTask();
		}

		if (workshop != null) {
			// Determine salvage process.
			process = determineSalvageProcess();
			if (process != null) {
				setDescription(process.toString());
			}
			else {
				endTask();
			}
		}

		// Initialize phase
		addPhase(SALVAGE);
		setPhase(SALVAGE);
	}

    @Override
    public FunctionType getLivingFunction() {
        return FunctionType.MANUFACTURE;
    }

	@Override
	protected void addExperience(double time) {
		// Add experience to "Materials Science" skill
		// (1 base experience point per 100 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude"
		// attribute.
		double newPoints = time / 100D;
		int experienceAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		person.getSkillManager().addExperience(SkillType.MATERIALS_SCIENCE, newPoints, time);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.MATERIALS_SCIENCE);
		return results;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
		return manager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
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
	 * Perform the salvaging phase.
	 * @param time the time to perform (millisols)
	 * @return remaining time after performing (millisols)
	 */
	private double salvagePhase(double time) {

		// Check if workshop has malfunction.
		if (workshop.getBuilding().getMalfunctionManager().hasMalfunction()) {
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
			workTime += workTime * (.2D * (double) skill);
		}

		// Apply work time to salvage process.
		double remainingWorkTime = process.getWorkTimeRemaining();
		double providedWorkTime = workTime;
		if (providedWorkTime > remainingWorkTime) {
			providedWorkTime = remainingWorkTime;
		}
		process.addWorkTime(providedWorkTime, skill);
		if (process.getWorkTimeRemaining() <= 0D) {
			workshop.endSalvageProcess(process, false);
			endTask();
		}

		// Add experience
		addExperience(time);

		// Check for accident in workshop.
		checkForAccident(time);

		return 0D;
	}

	/**
	 * Check for accident in manufacturing building.
	 * @param time the amount of time working (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .005D;

		// Materials science skill modification.
		int skill = getEffectiveSkillLevel();
		if (skill <= 3) {
			chance *= (4 - skill);
		}
		else {
			chance /= (skill - 2);
		}

		// Modify based on the workshop building's wear condition.
		chance *= workshop.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {

			if (person != null) {
//				logger.info("[" + person.getLocationTag().getShortLocationName() +  "] " + person.getName() + " has accident while salvaging " +
//					process.getInfo().getItemName() + ".");
				workshop.getBuilding().getMalfunctionManager().createASeriesOfMalfunctions(person);
			}
			else if (robot != null) {
//				logger.info("[" + robot.getLocationTag().getShortLocationName() +  "] " + robot.getName() + " has accident while salvaging " +
//					process.getInfo().getItemName() + ".");
				workshop.getBuilding().getMalfunctionManager().createASeriesOfMalfunctions(robot);
			}
		}
	}

	/**
	 * Gets an available manufacturing building that the person can use.
	 * Returns null if no manufacturing building is currently available.
	 * @param person the person
	 * @return available manufacturing building
	 */
	public static Building getAvailableManufacturingBuilding(Person person) {

		Building result = null;

		SkillManager skillManager = person.getSkillManager();
		int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);

		if (person.getLocationStateType() == LocationStateType.INSIDE_SETTLEMENT) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> manufacturingBuildings = manager.getBuildings(FunctionType.MANUFACTURE);
			manufacturingBuildings = BuildingManager.getNonMalfunctioningBuildings(manufacturingBuildings);
			manufacturingBuildings = getManufacturingBuildingsNeedingSalvageWork(manufacturingBuildings, skill);
			manufacturingBuildings = getBuildingsWithSalvageProcessesRequiringWork(manufacturingBuildings, skill);
			manufacturingBuildings = getHighestManufacturingTechLevelBuildings(manufacturingBuildings);
			manufacturingBuildings = BuildingManager.getLeastCrowdedBuildings(manufacturingBuildings);

			if (manufacturingBuildings.size() > 0) {
				Map<Building, Double> manufacturingBuildingProbs = BuildingManager.getBestRelationshipBuildings(
						person, manufacturingBuildings);
				result = RandomUtil.getWeightedRandomObject(manufacturingBuildingProbs);
			}
		}

		return result;
	}

	/**
	 * Gets a list of manufacturing buildings needing work from a list of buildings
	 * with the manufacture function.
	 * @param buildingList list of buildings with the manufacture function.
	 * @param skill the materials science skill level of the person.
	 * @return list of manufacture buildings needing work.
	 */
	private static List<Building> getManufacturingBuildingsNeedingSalvageWork(List<Building> buildingList,
			int skill) {

		List<Building> result = new ArrayList<Building>();

		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Manufacture manufacturingFunction = (Manufacture) building.getFunction(FunctionType.MANUFACTURE);
			if (manufacturingFunction.requiresSalvagingWork(skill)) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets a subset list of manufacturing buildings with salvage processes requiring work.
	 * @param buildingList the original building list.
	 * @param skill the materials science skill level of the person.
	 * @return subset list of buildings with processes requiring work, or original list if none found.
	 */
	private static List<Building> getBuildingsWithSalvageProcessesRequiringWork(List<Building> buildingList,
			int skill) {

		List<Building> result = new ArrayList<Building>();

		// Add all buildings with processes requiring work.
		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (hasSalvageProcessRequiringWork(building, skill)) {
				result.add(building);
			}
		}

		// If no building with processes requiring work, return original list.
		if (result.size() == 0) {
			result = buildingList;
		}

		return result;
	}

	/**
	 * Checks if manufacturing building has any salvage processes requiring work.
	 * @param manufacturingBuilding the manufacturing building.
	 * @param skill the materials science skill level of the person.
	 * @return true if processes requiring work.
	 */
	public static boolean hasSalvageProcessRequiringWork(Building manufacturingBuilding,
			int skill) {

		boolean result = false;

		Manufacture manufacturingFunction = (Manufacture) manufacturingBuilding.getFunction(FunctionType.MANUFACTURE);
		Iterator<SalvageProcess> i = manufacturingFunction.getSalvageProcesses().iterator();
		while (i.hasNext()) {
			SalvageProcess process = i.next();
			boolean workRequired = (process.getWorkTimeRemaining() > 0D);
			boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
			if (workRequired && skillRequired) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Gets a subset list of manufacturing buildings with the highest tech level from a list of buildings
	 * with the manufacture function.
	 * @param buildingList list of buildings with the manufacture function.
	 * @return subset list of highest tech level buildings.
	 */
	private static List<Building> getHighestManufacturingTechLevelBuildings(
			List<Building> buildingList) {

		List<Building> result = new ArrayList<Building>();

		int highestTechLevel = 0;
		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Manufacture manufacturingFunction = (Manufacture) building.getFunction(FunctionType.MANUFACTURE);
			if (manufacturingFunction.getTechLevel() > highestTechLevel) {
				highestTechLevel = manufacturingFunction.getTechLevel();
			}
		}

		Iterator<Building> j = buildingList.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			Manufacture manufacturingFunction = (Manufacture) building.getFunction(FunctionType.MANUFACTURE);
			if (manufacturingFunction.getTechLevel() == highestTechLevel) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets the highest salvaging process goods value for the person and the
	 * manufacturing building.
	 * @param person the person to perform manufacturing.
	 * @param manufacturingBuilding the manufacturing building.
	 * @return highest process good value.
	 */
	public static double getHighestSalvagingProcessValue(Person person,
			Building manufacturingBuilding) {

		double highestProcessValue = 0D;

		int skillLevel = person.getSkillManager().getEffectiveSkillLevel(
				SkillType.MATERIALS_SCIENCE);

		Manufacture manufacturingFunction = manufacturingBuilding.getManufacture();
		int techLevel = manufacturingFunction.getTechLevel();

		Iterator<SalvageProcessInfo> i = ManufactureUtil.getSalvageProcessesForTechSkillLevel(
				techLevel, skillLevel).iterator();
		while (i.hasNext()) {
			SalvageProcessInfo process = i.next();
			if (ManufactureUtil.canSalvageProcessBeStarted(process, manufacturingFunction) ||
					isSalvageProcessRunning(process, manufacturingFunction)) {
				Settlement settlement = manufacturingBuilding.getSettlement();
				double processValue = ManufactureUtil.getSalvageProcessValue(process, settlement,
						person);
				if (processValue > highestProcessValue) {
					highestProcessValue = processValue;
				}
			}
		}

		return highestProcessValue;
	}

	/**
	 * Checks if a process type is currently running at a manufacturing
	 * building.
	 * @param processInfo the process type.
	 * @param manufactureBuilding the manufacturing building.
	 * @return true if process is running.
	 */
	private static boolean isSalvageProcessRunning(SalvageProcessInfo processInfo,
			Manufacture manufactureBuilding) {
		boolean result = false;

		Iterator<SalvageProcess> i = manufactureBuilding.getSalvageProcesses().iterator();
		while (i.hasNext()) {
			SalvageProcess process = i.next();
			if (process.getInfo().getItemName() == processInfo.getItemName()) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Gets an available running salvage process.
	 * @return process or null if none.
	 */
	private SalvageProcess getRunningSalvageProcess() {
		SalvageProcess result = null;

		int skillLevel = getEffectiveSkillLevel();

		Iterator<SalvageProcess> i = workshop.getSalvageProcesses().iterator();
		while (i.hasNext() && (result == null)) {
			SalvageProcess process = i.next();
			if ((process.getInfo().getSkillLevelRequired() <= skillLevel) &&
					(process.getWorkTimeRemaining() > 0D)) {
				result = process;
			}
		}

		return result;
	}

	/**
	 * Creates a new salvage process if possible.
	 * @return the new salvage process or null if none.
	 */
	private SalvageProcess createNewSalvageProcess() {
		SalvageProcess result = null;

		if (workshop.getTotalProcessNumber() < workshop.getSupportingProcesses()) {

			int skillLevel = getEffectiveSkillLevel();
			int techLevel = workshop.getTechLevel();

			Map<SalvageProcessInfo, Double> processValues = new HashMap<SalvageProcessInfo, Double>();
			Iterator<SalvageProcessInfo> i = ManufactureUtil.getSalvageProcessesForTechSkillLevel(
					techLevel, skillLevel).iterator();
			while (i.hasNext()) {
				SalvageProcessInfo processInfo = i.next();
				if (ManufactureUtil.canSalvageProcessBeStarted(processInfo, workshop)) {
					double processValue = ManufactureUtil.getSalvageProcessValue(processInfo,
							person.getSettlement(), person);
					if (processValue > 0D) {
						processValues.put(processInfo, processValue);
					}
				}
			}

			// Randomly determine process based on value weights.
			SalvageProcessInfo selectedProcess = RandomUtil.getWeightedRandomObject(processValues);

			if (selectedProcess != null) {
				Unit salvagedUnit = ManufactureUtil.findUnitForSalvage(selectedProcess,
						person.getSettlement());
				if (salvagedUnit != null) {
					result = new SalvageProcess(selectedProcess, workshop, salvagedUnit);
					workshop.addSalvageProcess(result);
				}
			}
		}

		return result;
	}

	/**
	 * Determines a salvage process used for the task.
	 * @return salvage process or null if none determined.
	 */
	private SalvageProcess determineSalvageProcess() {
		SalvageProcess process = getRunningSalvageProcess();
		if (process == null) {
			process = createNewSalvageProcess();
		}
		return process;
	}

	@Override
	public void destroy() {
		super.destroy();

		workshop = null;
		process = null;
	}
}