/**
 * Mars Simulation Project
 * TendGreenhouse.java
 * @version 3.07 2014-08-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Crop;
import org.mars_sim.msp.core.structure.building.function.Farming;

/** 
 * The TendGreenhouse class is a task for tending the greenhouse in a settlement.
 * This is an effort driven task.
 */
public class TendGreenhouse
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(TendGreenhouse.class.getName());

	// Task phase
	private static final String TENDING = "Tending";

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;

	// Data members
	/** The greenhouse the person is tending. */
	private Farming greenhouse;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 */
	public TendGreenhouse(Person person) {
		// Use Task constructor
		super("Tending Greenhouse", person, false, false, STRESS_MODIFIER, true, 
				10D + RandomUtil.getRandomDouble(50D));

		// Initialize data members
		if (person.getSettlement() != null) {
			setDescription("Tending Greenhouse at " + person.getSettlement().getName());
		}
		else {
			endTask();
		}

		// Get available greenhouse if any.
		Building farmBuilding = getAvailableGreenhouse(person);
		if (farmBuilding != null) {
			greenhouse = (Farming) farmBuilding.getFunction(BuildingFunction.FARMING);
			
			// Walk to greenhouse.
			walkToActivitySpotInBuilding(farmBuilding);
		}
		else {
			endTask();
		}

		// Initialize phase
		addPhase(TENDING);
		setPhase(TENDING);
	}
	
    @Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.FARMING;
    }

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		}
		else if (TENDING.equals(getPhase())) {
			return tendingPhase(time);
		}
		else {
			return time;
		}
	}

	/**
	 * Performs the tending phase.
	 * @param time the amount of time (millisols) to perform the phase.
	 * @return the amount of time (millisols) left over after performing the phase.
	 */
	private double tendingPhase(double time) {

		// Check if greenhouse has malfunction.
		if (greenhouse.getBuilding().getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}

		// Determine amount of effective work time based on "Botany" skill.
		double workTime = time;
		int greenhouseSkill = getEffectiveSkillLevel();
		if (greenhouseSkill == 0) {
			workTime /= 2;
		}
		else {
			workTime += workTime * (double) greenhouseSkill;
		}

		// Add this work to the greenhouse.
		greenhouse.addWork(workTime);

		// Add experience
		addExperience(time);

		// Check for accident in greenhouse.
		checkForAccident(time);

		return 0D;
	}

	@Override
	protected void addExperience(double time) {
		// Add experience to "Botany" skill
		// (1 base experience point per 100 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		double newPoints = time / 100D;
		int experienceAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
		newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		person.getMind().getSkillManager().addExperience(SkillType.BOTANY, newPoints);
	}

	/**
	 * Check for accident in greenhouse.
	 * @param time the amount of time working (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .001D;

		// Greenhouse farming skill modification.
		int skill = getEffectiveSkillLevel();
		if (skill <= 3) {
			chance *= (4 - skill);
		}
		else {
			chance /= (skill - 2);
		}

		// Modify based on the LUV's wear condition.
		chance *= greenhouse.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			// logger.info(person.getName() + " has accident while tending the greenhouse.");
			greenhouse.getBuilding().getMalfunctionManager().accident();
		}
	}

	/** 
	 * Gets the greenhouse the person is tending.
	 * @return greenhouse
	 */
	public Farming getGreenhouse() {
		return greenhouse;
	}

	/**
	 * Gets an available greenhouse that the person can use.
	 * Returns null if no greenhouse is currently available.
	 * @param person the person
	 * @return available greenhouse
	 */
	public static Building getAvailableGreenhouse(Person person) {

		Building result = null;

		LocationSituation location = person.getLocationSituation();
		if (location == LocationSituation.IN_SETTLEMENT) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> farmBuildings = manager.getBuildings(BuildingFunction.FARMING);
			farmBuildings = BuildingManager.getNonMalfunctioningBuildings(farmBuildings);
			farmBuildings = getFarmsNeedingWork(farmBuildings);
			farmBuildings = BuildingManager.getLeastCrowdedBuildings(farmBuildings);

			if (farmBuildings.size() > 0) {
				Map<Building, Double> farmBuildingProbs = BuildingManager.getBestRelationshipBuildings(
						person, farmBuildings);
				result = RandomUtil.getWeightedRandomObject(farmBuildingProbs);
			}
		}

		return result;
	}

	/**
	 * Gets a list of farm buildings needing work from a list of buildings with the farming function.
	 * @param buildingList list of buildings with the farming function.
	 * @return list of farming buildings needing work.
	 */
	private static List<Building> getFarmsNeedingWork(List<Building> buildingList) {
		List<Building> result = new ArrayList<Building>();

		Iterator<Building> i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Farming farm = (Farming) building.getFunction(BuildingFunction.FARMING);
			if (farm.requiresWork()) {
				result.add(building);
			}
		}

		return result;
	}

	/**
	 * Gets the number of crops that currently need work this Sol.
	 * @param settlement the settlement.
	 * @return number of crops.
	 */
	public static int getCropsNeedingTending(Settlement settlement) {

		int result = 0;

		BuildingManager manager = settlement.getBuildingManager();
		Iterator<Building> i = manager.getBuildings(BuildingFunction.FARMING).iterator();
		while (i.hasNext()) {
			Building building = i.next();
			Farming farm = (Farming) building.getFunction(BuildingFunction.FARMING);
			Iterator<Crop> j = farm.getCrops().iterator();
			while (j.hasNext()) {
				Crop crop = j.next();
				if (crop.requiresWork()) {
					result++;
				}
			}
		}

		return result;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(SkillType.BOTANY);
	}  

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.BOTANY);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();

		greenhouse = null;
	}
}