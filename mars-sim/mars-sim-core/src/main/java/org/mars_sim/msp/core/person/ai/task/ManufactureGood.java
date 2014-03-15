/**
 * Mars Simulation Project
 * ManufactureGood.java
 * @version 3.06 2014-02-25
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Manufacture;

/**
 * A task for working on a manufacturing process.
 */
public class ManufactureGood
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ManufactureGood.class.getName());

	// TODO Task phase should be an enum.
	private static final String MANUFACTURE = "Manufacture";

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .1D;

	// Data members
	/** The manufacturing workshop the person is using. */
	private Manufacture workshop;

	/**
	 * Constructor.
	 * @param person the person to perform the task
	 */
	public ManufactureGood(Person person) {
		super("Manufacturing", person, true, false, STRESS_MODIFIER, true, 
				10D + RandomUtil.getRandomDouble(50D));

		// Initialize data members
		if (person.getSettlement() != null) {
			setDescription("Manufacturing at " + person.getSettlement().getName());
		}
		else {
			endTask();
		}

		// Get available manufacturing workshop if any.
		Building manufactureBuilding = getAvailableManufacturingBuilding(person);
		if (manufactureBuilding != null) {
			workshop = (Manufacture) manufactureBuilding.getFunction(BuildingFunction.MANUFACTURE);

			// Walk to manufacturing building.
			walkToManufacturingBuilding(manufactureBuilding);
		} 
		else {
			endTask();
		}

		// Initialize phase
		addPhase(MANUFACTURE);
		setPhase(MANUFACTURE);
	}

	/**
	 * Returns the weighted probability that a person might perform this task.
	 * @param person the person to perform the task
	 * @return the weighted probability that a person might perform this task
	 */
	public static double getProbability(Person person) {
		double result = 0D;

		// Cancel any manufacturing processes that's beyond the skill of any people 
		// associated with the settlement.
		cancelDifficultManufacturingProcesses(person);

		if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

			// See if there is an available manufacturing building.
			Building manufacturingBuilding = getAvailableManufacturingBuilding(person);
			if (manufacturingBuilding != null) {
				result = 1D;

				// Crowding modifier.
				result *= Task.getCrowdingProbabilityModifier(person, manufacturingBuilding);
				result *= Task.getRelationshipModifier(person, manufacturingBuilding);

				// Manufacturing good value modifier.
				result *= getHighestManufacturingProcessValue(person, manufacturingBuilding);

				if (result > 100D) {
					result = 100D;
				}

				// If manufacturing building has process requiring work, add
				// modifier.
				SkillManager skillManager = person.getMind().getSkillManager();
				int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
				if (hasProcessRequiringWork(manufacturingBuilding, skill)) {
					result += 10D;
				}

				// If settlement has manufacturing override, no new
				// manufacturing processes can be created.
				else if (person.getSettlement().getManufactureOverride()) {
					result = 0;
				}
			}
		}

		// Effort-driven task modifier.
		result *= person.getPerformanceRating();

		// Job modifier.
		Job job = person.getMind().getJob();
		if (job != null) {
			result *= job.getStartTaskProbabilityModifier(ManufactureGood.class);
		}

		return result;
	}

	/**
	 * Walk to manufacturing building.
	 * @param manufactureBuilding the manufacturing building.
	 */
	private void walkToManufacturingBuilding(Building manufactureBuilding) {

		// Determine location within manufacturing building.
		// TODO: Use action point rather than random internal location.
		Point2D.Double buildingLoc = LocalAreaUtil.getRandomInteriorLocation(manufactureBuilding);
		Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
				buildingLoc.getY(), manufactureBuilding);

		if (Walk.canWalkAllSteps(person, settlementLoc.getX(), settlementLoc.getY(), 
				manufactureBuilding)) {

			// Add subtask for walking to manufacture building.
			addSubTask(new Walk(person, settlementLoc.getX(), settlementLoc.getY(), 
					manufactureBuilding));
		}
		else {
			logger.fine(person.getName() + " unable to walk to manufacture building " + 
					manufactureBuilding.getName());
			endTask();
		}
	}

	/** 
	 * Cancel any manufacturing processes that's beyond the skill of any people 
	 * associated with the settlement.
	 * @param person the person
	 */
	private static void cancelDifficultManufacturingProcesses(Person person) {

		Settlement settlement = person.getSettlement();
		if (settlement != null) {
			int highestSkillLevel = 0;
			Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
			while (i.hasNext()) {
				Person tempPerson = i.next();
				SkillManager skillManager = tempPerson.getMind().getSkillManager();
				int skill = skillManager.getSkillLevel(SkillType.MATERIALS_SCIENCE);
				if (skill > highestSkillLevel) {
					highestSkillLevel = skill;
				}
			}

			BuildingManager manager = person.getSettlement().getBuildingManager();
			Iterator<Building> j = manager.getBuildings(BuildingFunction.MANUFACTURE).iterator();
			while (j.hasNext()) {
				Building building = (Building) j.next();
				Manufacture manufacturingFunction = (Manufacture) building.getFunction(BuildingFunction.MANUFACTURE);
				List<ManufactureProcess> processes = new ArrayList<ManufactureProcess>(
						manufacturingFunction.getProcesses());
				Iterator<ManufactureProcess> k = processes.iterator();
				while (k.hasNext()) {
					ManufactureProcess process = k.next();
					int processSkillLevel = process.getInfo().getSkillLevelRequired();
					if (processSkillLevel > highestSkillLevel) {
						// Cancel manufacturing process.
						manufacturingFunction.endManufacturingProcess(process, true);
					}
				}
			}
		}
	}

	/**
	 * Gets an available manufacturing building that the person can use. Returns
	 * null if no manufacturing building is currently available.
	 * @param person the person
	 * @return available manufacturing building
	 */
	private static Building getAvailableManufacturingBuilding(Person person) {

		Building result = null;

		SkillManager skillManager = person.getMind().getSkillManager();
		int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);

		if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> manufacturingBuildings = manager.getBuildings(BuildingFunction.MANUFACTURE);
			manufacturingBuildings = BuildingManager.getNonMalfunctioningBuildings(manufacturingBuildings);
			manufacturingBuildings = getManufacturingBuildingsNeedingWork(manufacturingBuildings, skill);
			manufacturingBuildings = getBuildingsWithProcessesRequiringWork(manufacturingBuildings, skill);
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
	 * Gets a list of manufacturing buildings needing work from a list of
	 * buildings with the manufacture function.
	 * @param buildingList list of buildings with the manufacture function.
	 * @param skill the materials science skill level of the person.
	 * @return list of manufacture buildings needing work.
	 */
	private static List<Building> getManufacturingBuildingsNeedingWork(
			List<Building> buildingList, int skill) {

		List<Building> result = new ArrayList<Building>();

		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Manufacture manufacturingFunction = (Manufacture) building.getFunction(BuildingFunction.MANUFACTURE);
			if (manufacturingFunction.requiresManufacturingWork(skill)) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets a subset list of manufacturing buildings with processes requiring
	 * work.
	 * @param buildingList the original building list.
	 * @param skill the materials science skill level of the person.
	 * @return subset list of buildings with processes requiring work, or
	 *         original list if none found.
	 */
	private static List<Building> getBuildingsWithProcessesRequiringWork(
			List<Building> buildingList, int skill) {

		List<Building> result = new ArrayList<Building>();

		// Add all buildings with processes requiring work.
		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			if (hasProcessRequiringWork(building, skill)) {
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
	 * Checks if manufacturing building has any processes requiring work.
	 * @param manufacturingBuilding the manufacturing building.
	 * @param skill the materials science skill level of the person.
	 * @return true if processes requiring work.
	 */
	private static boolean hasProcessRequiringWork(Building manufacturingBuilding, int skill) {

		boolean result = false;

		Manufacture manufacturingFunction = (Manufacture) manufacturingBuilding.getFunction(BuildingFunction.MANUFACTURE);
		Iterator<ManufactureProcess> i = manufacturingFunction.getProcesses().iterator();
		while (i.hasNext()) {
			ManufactureProcess process = i.next();
			boolean workRequired = (process.getWorkTimeRemaining() > 0D);
			boolean skillRequired = (process.getInfo().getSkillLevelRequired() <= skill);
			if (workRequired && skillRequired) result = true;
		}

		return result;
	}

	/**
	 * Gets a subset list of manufacturing buildings with the highest tech level
	 * from a list of buildings with the manufacture function.
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
			Manufacture manufacturingFunction = (Manufacture) building.getFunction(BuildingFunction.MANUFACTURE);
			if (manufacturingFunction.getTechLevel() > highestTechLevel) {
				highestTechLevel = manufacturingFunction.getTechLevel();
			}
		}

		Iterator<Building> j = buildingList.iterator();
		while (j.hasNext()) {
			Building building = j.next();
			Manufacture manufacturingFunction = (Manufacture) building.getFunction(BuildingFunction.MANUFACTURE);
			if (manufacturingFunction.getTechLevel() == highestTechLevel) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets the highest manufacturing process goods value for the person and the
	 * manufacturing building.
	 * @param person the person to perform manufacturing.
	 * @param manufacturingBuilding the manufacturing building.
	 * @return highest process good value.
	 */
	private static double getHighestManufacturingProcessValue(Person person, 
			Building manufacturingBuilding) {

		double highestProcessValue = 0D;

		int skillLevel = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);

		Manufacture manufacturingFunction = (Manufacture) manufacturingBuilding.getFunction(BuildingFunction.MANUFACTURE);
		int techLevel = manufacturingFunction.getTechLevel();

		Iterator<ManufactureProcessInfo> i = ManufactureUtil.getManufactureProcessesForTechSkillLevel(
				techLevel, skillLevel).iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			if (ManufactureUtil.canProcessBeStarted(process, manufacturingFunction) || 
					isProcessRunning(process, manufacturingFunction)) {
				Settlement settlement = manufacturingBuilding.getBuildingManager().getSettlement();
				double processValue = ManufactureUtil.getManufactureProcessValue(process, settlement);
				if (processValue > highestProcessValue) {
					highestProcessValue = processValue;
				}
			}
		}

		return highestProcessValue;
	}

	@Override
	protected void addExperience(double time) {
		// Add experience to "Materials Science" skill
		// (1 base experience point per 100 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude"
		// attribute.
		double newPoints = time / 100D;
		int experienceAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
		newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		person.getMind().getSkillManager().addExperience(SkillType.MATERIALS_SCIENCE, newPoints);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.MATERIALS_SCIENCE);
		return results;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		}
		else if (MANUFACTURE.equals(getPhase())) {
			return manufacturePhase(time);
		}
		else {
			return time;
		}
	}

	/**
	 * Perform the manufacturing phase.
	 * @param time the time to perform (millisols)
	 * @return remaining time after performing (millisols)
	 */
	private double manufacturePhase(double time) {

		// Check if workshop has malfunction.
		if (workshop.getBuilding().getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

		// Determine amount of effective work time based on "Materials Science"
		// skill.
		double workTime = time;
		int skill = getEffectiveSkillLevel();
		if (skill == 0) {
			workTime /= 2;
		}
		else {
			workTime += workTime * (.2D * (double) skill);
		}

		// Apply work time to manufacturing processes.
		while ((workTime > 0D) && !isDone()) {
			ManufactureProcess process = getRunningManufactureProcess();
			if (process != null) {
				double remainingWorkTime = process.getWorkTimeRemaining();
				double providedWorkTime = workTime;
				if (providedWorkTime > remainingWorkTime) {
					providedWorkTime = remainingWorkTime;
				}
				process.addWorkTime(providedWorkTime);
				workTime -= providedWorkTime;

				if ((process.getWorkTimeRemaining() <= 0D) && 
						(process.getProcessTimeRemaining() <= 0D)) {
					workshop.endManufacturingProcess(process, false);
				}
			} else {
				if (!person.getSettlement().getManufactureOverride()) {
					process = createNewManufactureProcess();
				}
				if (process == null) {
					endTask();
				}
			}
		}

		// Add experience
		addExperience(time);

		// Check for accident in workshop.
		checkForAccident(time);

		return 0D;
	}

	/**
	 * Gets an available running manufacturing process.
	 * @return process or null if none.
	 */
	private ManufactureProcess getRunningManufactureProcess() {
		ManufactureProcess result = null;

		int skillLevel = getEffectiveSkillLevel();

		Iterator<ManufactureProcess> i = workshop.getProcesses().iterator();
		while (i.hasNext() && (result == null)) {
			ManufactureProcess process = i.next();
			if ((process.getInfo().getSkillLevelRequired() <= skillLevel) && 
					(process.getWorkTimeRemaining() > 0D)) {
				result = process;
			}
		}

		return result;
	}

	/**
	 * Checks if a process type is currently running at a manufacturing
	 * building.
	 * @param processInfo the process type.
	 * @param manufactureBuilding the manufacturing building.
	 * @return true if process is running.
	 */
	private static boolean isProcessRunning(ManufactureProcessInfo processInfo, 
			Manufacture manufactureBuilding) {
		boolean result = false;

		Iterator<ManufactureProcess> i = manufactureBuilding.getProcesses().iterator();
		while (i.hasNext()) {
			ManufactureProcess process = i.next();
			if (process.getInfo().getName() == processInfo.getName()) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * Creates a new manufacturing process if possible.
	 * @return the new manufacturing process or null if none.
	 */
	private ManufactureProcess createNewManufactureProcess() {
		ManufactureProcess result = null;

		if (workshop.getTotalProcessNumber() < workshop.getConcurrentProcesses()) {

			int skillLevel = getEffectiveSkillLevel();
			int techLevel = workshop.getTechLevel();

			// Determine all manufacturing processes that are possible and profitable.
			Map<ManufactureProcessInfo, Double> processProbMap = new HashMap<ManufactureProcessInfo, Double>();
			Iterator<ManufactureProcessInfo> i = ManufactureUtil.getManufactureProcessesForTechSkillLevel(
					techLevel, skillLevel).iterator();
			while (i.hasNext()) {
				ManufactureProcessInfo processInfo = i.next();
				if (ManufactureUtil.canProcessBeStarted(processInfo, workshop)) {
					double processValue = ManufactureUtil.getManufactureProcessValue(processInfo, 
							person.getSettlement());
					if (processValue > 0D) {
						processProbMap.put(processInfo, processValue);
					}
				}
			}

			// Randomly choose among possible manufacturing processes based on their relative profitability. 
			ManufactureProcessInfo chosenProcess = null;
			if (!processProbMap.isEmpty()) {
				chosenProcess = RandomUtil.getWeightedRandomObject(processProbMap);
			}

			// Create chosen manufacturing process.
			if (chosenProcess != null) {
				result = new ManufactureProcess(chosenProcess, workshop);
				workshop.addProcess(result);
			}
		}

		return result;
	}

	/**
	 * Check for accident in manufacturing building.
	 * @param time the amount of time working (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .001D;

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
			logger.info(person.getName() + " has accident while manufacturing.");
			workshop.getBuilding().getMalfunctionManager().accident();
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		workshop = null;
	}
}