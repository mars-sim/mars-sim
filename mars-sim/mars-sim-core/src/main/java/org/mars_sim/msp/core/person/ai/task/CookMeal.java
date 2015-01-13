/**
 * Mars Simulation Project
 * CookMeal.java
 * @version 3.07 2015-01-06
 * @author Scott Davis
 * 
 * 2014-10-15 mkung: check if there are any fresh food, if not, endTask()  
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

/** 
 * The CookMeal class is a task for cooking meals in a building
 * with the Cooking function.
 * This is an effort driven task.
 */
public class CookMeal
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(CookMeal.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.cookMeal"); //$NON-NLS-1$
	
    /** Task phases. */
    private static final TaskPhase COOKING = new TaskPhase(Msg.getString(
            "Task.phase.cooking")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.1D;

	// Starting meal times (millisol) for 0 degrees longitude.
	// 2014-12-03 Added MIDNIGHT_SHIFT_MEAL_START
	private static final double BREAKFAST_START = 250D; // at 6am
	private static final double LUNCH_START = 500D; // at 12 am
	private static final double DINNER_START = 750D; // at 6 pm
	private static final double MIDNIGHT_SHIFT_MEAL_START = 005D; // avoid conflict with TabPanelCooking when at 0D all yesterday's cookedMeals are removed

	// Time (millisols) duration of meals.
	private static final double MEALTIME_DURATION = 100D; // 250 milliSol = 6 hours

	// Data members
	/** The kitchen the person is cooking at. */
	private Cooking kitchen;
	private int counter;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 * @throws Exception if error constructing task.
	 */
	public CookMeal(Person person) {
        // Use Task constructor
        super(NAME, person, true, false, STRESS_MODIFIER, false, 0D);

        //logger.info("just called CookMeal's constructor");
        
        // Initialize data members
        setDescription(Msg.getString("Task.description.cookMeal.detail", 
                getMealName())); //$NON-NLS-1$
        
        // Get available kitchen if any.
        Building kitchenBuilding = getAvailableKitchen(person);

	    if (kitchenBuilding != null) {
	    	kitchen = (Cooking) kitchenBuilding.getFunction(BuildingFunction.COOKING);
	        // Walk to kitchen building.
	    	walkToActivitySpotInBuilding(kitchenBuilding, false);	
		   
		    double size = kitchen.getMealRecipesWithAvailableIngredients().size();	       
	        if (size == 0) {
	        	counter++;
	        	if (counter < 2)
	        		logger.severe("Warning: cannot cook meals in " 
	            		+ kitchenBuilding.getBuildingManager().getSettlement().getName() 
	            		+ " because none of the ingredients of a meal are available ");
	            
	            endTask();
	            kitchen.cleanup();
	        
		    } else {
		    	
		    	counter = 0;
				// 2015-01-06
				kitchen.setChef(person.getName());
				
		    	// Add task phase
			    addPhase(COOKING);
				setPhase(COOKING);	
				  
				String jobName = person.getMind().getJob().getName(person.getGender());
				logger.finest(jobName + " " + person.getName() + " cooking at " + kitchen.getBuilding().getNickName() + 
				    	                " in " + person.getSettlement());      

		    }
	    }
	    else endTask();
	    
    }
    
    @Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.COOKING;
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     */
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("The Cooking task phase is null");
        }
        else if (COOKING.equals(getPhase())) {
            return cookingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the cooking phase of the task.
     * @param time the amount of time (millisol) to perform the cooking phase.
     * @return the amount of time (millisol) left after performing the cooking phase.
     */
    private double cookingPhase(double time) {
    	//System.out.println("CookMeal.java entering cookingPhase() ");
    	
        // If kitchen has malfunction, end task.
        if (kitchen.getBuilding().getMalfunctionManager().hasMalfunction()) {
            endTask();
            return time;
        }

        // If meal time is over, clean up kitchen and end task.
        if (!isMealTime(person)) {
        	endTask();
            kitchen.cleanup();
            //logger.info(person.getName() + " just finished cooking.");
            return time;
        }
        
        // 2015-01-04a Added getCookNoMore() condition 
        if (kitchen.getCookNoMore()) {
        	//System.out.println("CookMeal.java cookingPhase() : cookNoMore = true. calling endTask() ");
        	endTask();
        	kitchen.cleanup();
        	return time;
        }
        
	        // Determine amount of effective work time based on "Cooking" skill.
        double workTime = time;
	    int cookingSkill = getEffectiveSkillLevel();
	    if (cookingSkill == 0) workTime /= 2;
	    	else workTime += workTime * (.2D * (double) cookingSkill);
	
	    // Add this work to the kitchen.
	    kitchen.addWork(workTime);
	
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
                NaturalAttribute.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(SkillType.COOKING, newPoints);
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
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        // Modify based on the kitchen building's wear condition.
        chance *= kitchen.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

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
    public static boolean isMealTime(Person person) {
        boolean result = false;

        double timeOfDay = Simulation.instance().getMasterClock().getMarsClock().getMillisol();
        double timeDiff = 1000D * (person.getCoordinates().getTheta() / (2D * Math.PI));
        double modifiedTime = timeOfDay + timeDiff;
        if (modifiedTime >= 1000D) {
            modifiedTime -= 1000D;
        }

        if ((modifiedTime >= BREAKFAST_START) && (modifiedTime <= (BREAKFAST_START + MEALTIME_DURATION))) {
            result = true;
        }
        if ((modifiedTime >= LUNCH_START) && (modifiedTime <= (LUNCH_START + MEALTIME_DURATION))) {
            result = true;
        }
        if ((modifiedTime >= DINNER_START) && (modifiedTime <= (DINNER_START + MEALTIME_DURATION))) {
            result = true;		
        }
    	// 2014-12-03 Added MIDNIGHT_SHIFT_MEAL_START
        if ((modifiedTime >= MIDNIGHT_SHIFT_MEAL_START) && (modifiedTime <= (MIDNIGHT_SHIFT_MEAL_START + MEALTIME_DURATION))) {
            result = true;		
        }
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
        if (modifiedTime >= 1000D) {
            modifiedTime -= 1000D;
        }

        if ((modifiedTime >= BREAKFAST_START) && (modifiedTime <= (BREAKFAST_START + MEALTIME_DURATION))) {
            result = "Breakfast";
        }
        if ((modifiedTime >= LUNCH_START) && (modifiedTime <= (LUNCH_START + MEALTIME_DURATION))) {
            result = "Lunch";
        }
        if ((modifiedTime >= DINNER_START) && (modifiedTime <= (DINNER_START + MEALTIME_DURATION))) {
            result = "Dinner";
        }
    	// 2014-12-03 Added MIDNIGHT_SHIFT_MEAL_START
        if ((modifiedTime >= MIDNIGHT_SHIFT_MEAL_START) && (modifiedTime <= (MIDNIGHT_SHIFT_MEAL_START + MEALTIME_DURATION))) {
            result = "Midnight Meal";
        }

        return result;
    }

    /**
     * Gets an available kitchen at the person's settlement.
     * @param person the person to check for.
     * @return kitchen or null if none available.
     */
    public static Building getAvailableKitchen(Person person) {
        Building result = null;

        LocationSituation location = person.getLocationSituation();
        if (location == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = person.getSettlement().getBuildingManager();
            List<Building> kitchenBuildings = manager.getBuildings(BuildingFunction.COOKING);
            kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
            kitchenBuildings = getKitchensNeedingCooks(kitchenBuildings);
            kitchenBuildings = BuildingManager.getLeastCrowdedBuildings(kitchenBuildings); 

            if (kitchenBuildings.size() > 0) {
                Map<Building, Double> kitchenBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                        person, kitchenBuildings);
                result = RandomUtil.getWeightedRandomObject(kitchenBuildingProbs);
            }
        }		

        return result;
    }

    /**
     * Gets a list of kitchen buildings that have room for more cooks.
     * @param kitchenBuildings list of kitchen buildings
     * @return list of kitchen buildings
     * @throws BuildingException if error
     */
    private static List<Building> getKitchensNeedingCooks(List<Building> kitchenBuildings) {
        List<Building> result = new ArrayList<Building>();

        if (kitchenBuildings != null) {
            Iterator<Building> i = kitchenBuildings.iterator();
            while (i.hasNext()) {
                Building building = i.next();
                Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING);
                if (kitchen.getNumCooks() < kitchen.getCookCapacity()) result.add(building);
            }
        }

        return result;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
        return manager.getEffectiveSkillLevel(SkillType.COOKING);
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(1);
        results.add(SkillType.COOKING);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        kitchen = null;
    }
}