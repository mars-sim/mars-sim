/**
 * Mars Simulation Project
 * CookMeal.java
 * @version 2.84 2008-05-17
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.person.*;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.building.*;
import org.mars_sim.msp.core.structure.building.function.Cooking;

/** 
 * The TendGreenhouse class is a task for cooking meals in a building
 * with the Cooking function.
 * This is an effort driven task.
 */
public class CookMeal extends Task implements Serializable {
    
    	private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.CookMeal";
	
	private static Logger logger = Logger.getLogger(CLASS_NAME);

	// Task phase
	private static final String COOKING = "Cooking";
	
	// Static members
	private static final double STRESS_MODIFIER = -.1D; // The stress modified per millisol.
	
	// Starting mealtimes (millisol) for 0 degrees longitude.
	private static final double BREAKFAST_START = 300D;
	private static final double LUNCH_START = 500D;
	private static final double DINNER_START = 700D;
	
	// Time (millisols) duration of meals.
	private static final double MEALTIME_DURATION = 100D;
	
	// Data members
	private Cooking kitchen; // The kitchen the person is cooking at.

	/**
	 * Constructor
	 * @param person the person performing the task.
	 * @throws Exception if error constructing task.
	 */
	public CookMeal(Person person) throws Exception {
		// Use Task constructor
		super("Cooking", person, true, false, STRESS_MODIFIER, false, 0D);
		
		// Initialize data members
		setDescription("Cooking " + getMealName());
		
		// Get available kitchen if any.
		Building kitchenBuilding = getAvailableKitchen(person);
		if (kitchenBuilding != null) {
			kitchen = (Cooking) kitchenBuilding.getFunction(Cooking.NAME);
			BuildingManager.addPersonToBuilding(person, kitchenBuilding);
		}
		else endTask();
		
		// Add task phase
		addPhase(COOKING);
		setPhase(COOKING);
		
		// String jobName = person.getMind().getJob().getName();
		// logger.info(jobName + " " + person.getName() + " cooking at " + kitchen.getBuilding().getName() + " in " + person.getSettlement());
	}
	
	/** 
	 * Returns the weighted probability that a person might perform this task.
	 * @param person the person to perform the task
	 * @return the weighted probability that a person might perform this task
	 */
	public static double getProbability(Person person) {
		double result = 0D;
        
        if (isMealTime(person)) {
        
			try {
				// See if there is an available kitchen.
				Building kitchenBuilding = getAvailableKitchen(person);
				if (kitchenBuilding != null) {
					result = 200D;
        		
					// Crowding modifier.
					result *= Task.getCrowdingProbabilityModifier(person, kitchenBuilding);
					result *= Task.getRelationshipModifier(person, kitchenBuilding);
					
					// Check if there is enough food available to cook.
					PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
					double foodRequired = config.getFoodConsumptionRate() * (1D / 3D);
					AmountResource food = AmountResource.findAmountResource("food");
					double foodAvailable = person.getSettlement().getInventory().getAmountResourceStored(food);
					if (foodAvailable < foodRequired) result = 0D;
				}
			}
			catch (Exception e) {
			    logger.log(Level.SEVERE,"CookMeal.getProbability()" ,e);
			}
        
			// Effort-driven task modifier.
			result *= person.getPerformanceRating();

			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) result *= job.getStartTaskProbabilityModifier(CookMeal.class);
        }

		return result;
	}	
	
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (COOKING.equals(getPhase())) return cookingPhase(time);
    	else return time;
    }
	
    /**
     * Performs the cooking phase of the task.
     * @param time the amount of time (millisol) to perform the cooking phase.
     * @return the amount of time (millisol) left after performing the cooking phase.
     * @throws Exception if error performing the cooking phase.
     */
	private double cookingPhase(double time) throws Exception {
		
		// If kitchen has malfunction, end task.
		if (kitchen.getBuilding().getMalfunctionManager().hasMalfunction()) {
			endTask();
			return time;
		}
		
		// If meal time is over, clean up kitchen and end task.
		if (!isMealTime(person)) {
			endTask();
			kitchen.cleanup();
			// logger.info(person.getName() + " finished cooking.");
			return time;
		}
        
		// Determine amount of effective work time based on "Cooking" skill.
		double workTime = time;
		int cookingSkill = getEffectiveSkillLevel();
		if (cookingSkill == 0) workTime /= 2;
		else workTime += workTime * (.2D * (double) cookingSkill);
        
		// Add this work to the kitchen.
		try {
			kitchen.addWork(workTime);
		}
		catch (BuildingException e) {
			// Not enough food.
			endTask();
			return time;
		}
		
		// Add experience
		addExperience(time);
        
		// Check for accident in kitchen.
		checkForAccident(time);
		
		return 0D;
	}
	
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience to "Cooking" skill
		// (1 base experience point per 25 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 25D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
        	NaturalAttributeManager.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(Skill.COOKING, newPoints);
	}
	
	/**
	 * Gets the kitchen the person is cooking in.
	 * @return kitchen
	 */
	public Cooking getKitchen() {
		return kitchen;
	}
	
	/**
	 * Check for accident in kitchen.
	 * @param time the amount of time working (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .001D;

		// Cooking skill modification.
		int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.COOKING);
		if (skill <= 3) chance *= (4 - skill);
		else chance /= (skill - 2);

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			// logger.info(person.getName() + " has accident while cooking.");
			kitchen.getBuilding().getMalfunctionManager().accident();
		}
	}	
	
	/**
	 * Checks if it is currently a meal time at the person's location.
	 * @param person the person to check for.
	 * @return true if meal time
	 */
	private static boolean isMealTime(Person person) {
		boolean result = false;
		
		double timeOfDay = Simulation.instance().getMasterClock().getMarsClock().getMillisol();
		double timeDiff = 1000D * (person.getCoordinates().getTheta() / (2D * Math.PI));
		double modifiedTime = timeOfDay + timeDiff;
		if (modifiedTime >= 1000D) modifiedTime -= 1000D;
        
		if ((modifiedTime >= BREAKFAST_START) && (modifiedTime <= (BREAKFAST_START + MEALTIME_DURATION))) result = true;
		if ((modifiedTime >= LUNCH_START) && (modifiedTime <= (LUNCH_START + MEALTIME_DURATION))) result = true;
		if ((modifiedTime >= DINNER_START) && (modifiedTime <= (DINNER_START + MEALTIME_DURATION))) result = true;		
		
		return result;
	}
	
	/**
	 * Gets the name of the meal the person is cooking based on the time.
	 * @return mean name ("Breakfast", "Lunch" or "Dinner) or empty string if none.
	 */
	private String getMealName() {
		String result = "";
		
		double timeOfDay = Simulation.instance().getMasterClock().getMarsClock().getMillisol();
		double timeDiff = 1000D * (person.getCoordinates().getTheta() / (2D * Math.PI));
		double modifiedTime = timeOfDay + timeDiff;
		if (modifiedTime >= 1000D) modifiedTime -= 1000D;
		
		if ((modifiedTime >= BREAKFAST_START) && (modifiedTime <= (BREAKFAST_START + MEALTIME_DURATION))) result = "Breakfast";
		if ((modifiedTime >= LUNCH_START) && (modifiedTime <= (LUNCH_START + MEALTIME_DURATION))) result = "Lunch";
		if ((modifiedTime >= DINNER_START) && (modifiedTime <= (DINNER_START + MEALTIME_DURATION))) result = "Dinner";
		
		return result;
	}
	
	/**
	 * Gets an available kitchen at the person's settlement.
	 * @param person the person to check for.
	 * @return kitchen or null if none available.
	 */
	private static Building getAvailableKitchen(Person person) throws BuildingException {
		Building result = null;
		
		String location = person.getLocationSituation();
		if (location.equals(Person.INSETTLEMENT)) {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List<Building> kitchenBuildings = manager.getBuildings(Cooking.NAME);
			kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
			kitchenBuildings = getKitchensNeedingCooks(kitchenBuildings);
			kitchenBuildings = BuildingManager.getLeastCrowdedBuildings(kitchenBuildings); 
			kitchenBuildings = BuildingManager.getBestRelationshipBuildings(person, kitchenBuildings);
			
			if (kitchenBuildings.size() > 0) result = (Building) kitchenBuildings.get(0);
		}		
		
		return result;
	}
	
	/**
	 * Gets a list of kitchen buildings that have room for more cooks.
	 * @param kitchenBuildings list of kitchen buildings
	 * @return list of kitchen buildings
	 * @throws BuildingException if error
	 */
	private static List<Building> getKitchensNeedingCooks(List kitchenBuildings) throws BuildingException {
		List<Building> result = new ArrayList<Building>();
		
		if (kitchenBuildings != null) {
			Iterator i = kitchenBuildings.iterator();
			while (i.hasNext()) {
				Building building = (Building) i.next();
				Cooking kitchen = (Cooking) building.getFunction(Cooking.NAME);
				if (kitchen.getNumCooks() < kitchen.getCookCapacity()) result.add(building);
			}
		}
		
		return result;
	}

	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.COOKING);
	}

	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(1);
		results.add(Skill.COOKING);
		return results;
	}
}