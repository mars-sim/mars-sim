/**
 * Mars Simulation Project
 * TendGreenhouse.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.Fishery;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The TendFishery class is a task for tending the fishery in a
 * settlement. This is an effort driven task.
 */
public class TendFishTank extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TendFishTank.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.tendFishTank"); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase TENDING = new TaskPhase(Msg.getString("Task.phase.tending")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase INSPECTING = new TaskPhase(Msg.getString("Task.phase.inspecting")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase CLEANING = new TaskPhase(Msg.getString("Task.phase.cleaning")); //$NON-NLS-1$
	/** Task phases. */
	private static final TaskPhase CATCHING = new TaskPhase(Msg.getString("Task.phase.catching")); //$NON-NLS-1$	

	// Limit the maximum time spent on a phase
	private static final double MAX_FISHING = 100D;
	private static final double MAX_TEND = 100D;
	
	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -1.1D;

	// Data members
	/** The fish tank the person is tending. */
	private Fishery fishTank;
	private Building building;
	private double fishingTime = 0D;
	private double tendTime = 0D;
	
	/**
	 * Constructor.
	 * 
	 * @param person the person performing the task.
	 */
	public TendFishTank(Person person) {
		// Use Task constructor
		super(NAME, person, false, false, STRESS_MODIFIER, false, 0D);

		if (person.isOutside()) {
			endTask();
			return;
		}

		// Get available greenhouse if any.
		building = getAvailableFishTank(person);
		if (building != null) {
			
			fishTank = (Fishery) building.getFunction(FunctionType.FISHERY);

			// Walk to fish tank.
			walkToTaskSpecificActivitySpotInBuilding(building, false);	

			if (fishTank.getSurplusStock() > 0) {
				// Do fishing
				setPhase(CATCHING);
				addPhase(CATCHING);
			}
			else if (fishTank.getWeedDemand() > 0) {
				setPhase(TENDING);
				addPhase(TENDING);
				addPhase(INSPECTING);
				addPhase(CLEANING);
			}
			else {
				setPhase(INSPECTING);
				addPhase(INSPECTING);
				addPhase(CLEANING);
			}
		}
		else
			endTask();
	}

	/**
	 * Constructor 2.
	 * 
	 * @param robot the robot performing the task.
	 */
	public TendFishTank(Robot robot) {
		// Use Task constructor
		super(NAME, robot, false, false, 0, true, 50D);

		// Initialize data members
		if (robot.isOutside()) {
			endTask();
			return;
		}

		// Get available greenhouse if any.
		building = getAvailableFishTank(person);
		if (building != null) {			
			fishTank = (Fishery) building.getFunction(FunctionType.FISHERY);

			// Walk to fishtank.
			walkToTaskSpecificActivitySpotInBuilding(building, false);
			
			// Initialize phase
			// Robots do not do anything with water
			setPhase(CLEANING);
			addPhase(CLEANING);
			
		} else {
			endTask();
		}
	}

	@Override
	public FunctionType getLivingFunction() {
		return FunctionType.FISHERY;
	}

	@Override
	public FunctionType getRoboticFunction() {
		return FunctionType.FISHERY;
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			return 0;
		} else if (TENDING.equals(getPhase())) {
			return tendingPhase(time);
		} else if (INSPECTING.equals(getPhase())) {
			return inspectingPhase(time);
		} else if (CLEANING.equals(getPhase())) {
			return cleaningPhase(time);
		} else if (CATCHING.equals(getPhase())) {
			return catchingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the tending phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double catchingPhase(double time) {

		double workTime = time;

		if (isDone()) {
			return time;
		}

		// Check if building has malfunction.
		if (building.getMalfunctionManager() != null && building.getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

		double mod = 0;

		if (person != null) {
			mod = 6D;
		}

		else {
			mod = 4D;
		}

		// Determine amount of effective work time based on "Botany" skill
		int skill = getEffectiveSkillLevel();
		if (skill <= 0) {
			mod += RandomUtil.getRandomDouble(.25);
		} else {
			mod += RandomUtil.getRandomDouble(.25) + 1.25 * skill;
		}

		workTime *= mod;

		double remainingTime = fishTank.catchFish(person, workTime);

		// Add experience
		addExperience(time);

		// Check for accident
		checkForAccident(time);

		if ((remainingTime > 0) || (fishTank.getSurplusStock() == 0)) {
			endTask();

			// Scale it back to the. Calculate used time 
			double usedTime = workTime - remainingTime;
			return time - (usedTime / mod);
		}
		else {
			fishingTime += time;
			if (fishingTime > MAX_FISHING) {
				logger.log(building, person, Level.INFO, 0, "Giving up on fishing", null);
				endTask();
			}
		}
		
		return 0;
	}
	
	
	/**
	 * Performs the tending phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double tendingPhase(double time) {

		double workTime = time;

		if (isDone()) {
			return time;
		}

		// Check if building has malfunction.
		if (building.getMalfunctionManager() != null && building.getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

		double mod = 1;

		// Determine amount of effective work time based on "Botany" skill
		int skill = getEffectiveSkillLevel();
		if (skill > 0) {
			mod += RandomUtil.getRandomDouble(.25) + 1.25 * skill;
		}

		workTime *= mod;

		double remainingTime = fishTank.tendWeeds(workTime);

		// Add experience
		addExperience(time);

		// Check for accident
		checkForAccident(time);

		if (remainingTime > 0) {
			setPhase(INSPECTING);

			// Scale it back to the. Calculate used time 
			double usedTime = workTime - remainingTime;
			return time - (usedTime / mod);
		}
		else if (tendTime > MAX_TEND) {
			logger.log(building, person, Level.INFO, 0, "Giving up on tending", null);
			endTask();
		}
		tendTime += time;
		
		return 0;
	}
	
	/**
	 * Performs the inspecting phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double inspectingPhase(double time) {

		List<String> uninspected = fishTank.getUninspected();
		int size = uninspected.size();

		if (size > 0) {
			int rand = RandomUtil.getRandomInt(size - 1);

			String goal = uninspected.get(rand);

			fishTank.markInspected(goal);

			setDescription(Msg.getString("Task.description.tendGreenhouse.inspect", goal));
		}
		setPhase(CLEANING);
		return 0;
	}

	/**
	 * Performs the cleaning phase.
	 * 
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double cleaningPhase(double time) {

		List<String> uncleaned = fishTank.getUncleaned();
		int size = uncleaned.size();

		if (size > 0) {
			int rand = RandomUtil.getRandomInt(size - 1);

			String goal = uncleaned.get(rand);

			fishTank.markCleaned(goal);

			setDescription(Msg.getString("Task.description.tendGreenhouse.clean", goal));
		}

		endTask();
		return 0;
	}


	@Override
	protected void addExperience(double time) {
		// Add experience to "Botany" skill
		// (1 base experience point per 100 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		double newPoints = time / 100D;
		int experienceAptitude = 0;
		if (person != null)
			experienceAptitude = person.getNaturalAttributeManager()
					.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		else if (robot != null)
			experienceAptitude = robot.getRoboticAttributeManager()
					.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);

		newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		if (person != null)
			person.getSkillManager().addExperience(SkillType.BIOLOGY, newPoints, time);
		else if (robot != null)
			robot.getSkillManager().addExperience(SkillType.BIOLOGY, newPoints, time);

	}

	/**
	 * Check for accident in greenhouse.
	 * 
	 * @param time the amount of time working (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .005D;

		// Greenhouse farming skill modification.
		int skill = getEffectiveSkillLevel();
		if (skill <= 3) {
			chance *= (4 - skill);
		} else {
			chance /= (skill - 2);
		}

		// Modify based on the wear condition.
		chance *= building.getMalfunctionManager().getWearConditionAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			if (person != null) {
				building.getMalfunctionManager().createASeriesOfMalfunctions(person);
			} else if (robot != null) {
				building.getMalfunctionManager().createASeriesOfMalfunctions(robot);
			}
		}
	}

	/**
	 * Gets an available building with fis tanks that the person can use. Returns null if no
	 * greenhouse is currently available.
	 * 
	 * @param person the person
	 * @return available fish tanks
	 */
	public static Building getAvailableFishTank(Unit unit) {
		Building result = null;
		Person person = null;
		Robot robot = null;
		BuildingManager buildingManager;

		if (unit instanceof Person) {
			person = (Person) unit;
			if (person.isInSettlement()) {
				buildingManager = person.getSettlement().getBuildingManager();
				List<Building> buildings = buildingManager.getBuildings(FunctionType.FISHERY);

				if (!buildings.isEmpty()) {
					Map<Building, Double> buildingProb = BuildingManager
							.getBestRelationshipBuildings(person, buildings);
					result = RandomUtil.getWeightedRandomObject(buildingProb);
				}
			}
		}

		else if (unit instanceof Robot) {
			robot = (Robot) unit;
			if (robot.isInSettlement()) {
				buildingManager = robot.getSettlement().getBuildingManager();
				List<Building> buildings = buildingManager.getBuildings(FunctionType.FISHERY);

				// Choose the building the robot is at.
				if (buildings.contains(robot.getBuildingLocation())) {
					return robot.getBuildingLocation();
				}

				if (!buildings.isEmpty()) {
					result = buildings.get(RandomUtil.getRandomInt(0, buildings.size() - 1));
				}
			}
		}
		return result;
	}

	@Override
	public int getEffectiveSkillLevel() {	
		if (person != null)
			return person.getEffectiveSkillLevel(SkillType.BIOLOGY);
		else if (robot != null)
			return robot.getEffectiveSkillLevel(SkillType.BIOLOGY);
		return 0;
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<>(1);
		results.add(SkillType.BIOLOGY);
		return results;
	}
}