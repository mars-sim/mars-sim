/**
 * Mars Simulation Project
 * CookMeal.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.tool.RandomUtil;


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

    private static String sourceName = logger.getName();
    
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
	private static final double MEALTIME_DURATION = 75D; // 250 milliSol = 6 hours
	
	private static final String NO_INGREDIENT = " cannot cook any meals. None of the ingredients are available.";

	// Data members
	//private int counter;
	//private int solElapsedCache;

	/** Log cache array for storing previous log statements */
	//private static String[] logCache = new String[]{"", "", "", ""};

	/** The kitchen the person is cooking at. */
	private Cooking kitchen;
	private Building kitchenBuilding;


	/**
	 * Constructor.
	 * @param person the person performing the task.
	 * @throws Exception if error constructing task.
	 */
	public CookMeal(Person person) {
        // Use Task constructor
        super(NAME, person, true, false, STRESS_MODIFIER, false, 0D);

        sourceName = sourceName.substring(sourceName.lastIndexOf(".") + 1, sourceName.length());
        
        // Initialize data members
        setDescription(Msg.getString("Task.description.cookMeal.detail",
                getTypeOfMeal())); //$NON-NLS-1$

        // Get an available kitchen.
        kitchenBuilding = getAvailableKitchen(person);

	    if (kitchenBuilding != null) {
	    	kitchen = kitchenBuilding.getCooking();

	        // Walk to kitchen building.
	    	walkToActivitySpotInBuilding(kitchenBuilding, false);

		    //int size = kitchen.getMealRecipesWithAvailableIngredients().size();
	    	// 2015-12-10 Used getNumCookableMeal()
		    int size = kitchen.getNumCookableMeal();

	        //System.out.println(" # of cookableMeal : " + size);

        	// Need to reset numGoodRecipes periodically since it's a cache value
        	// and won't get updated unless a meal is cooked.
        	// Note: it's reset at least once a day at the end of a sol
		    if (size == 0) {
	        	if (RandomUtil.getRandomInt(5) == 0) {
	        		// check again to reset the value once in a while
	        		size = kitchen.getMealRecipesWithAvailableIngredients().size();
	        		kitchen.setNumCookableMeal(size);
	        	}
		    }

	        if (size == 0) {
/*
	        	counter++;

	        	// display the msg when no ingredients are detected at first and after n warnings
	        	if (counter % 30 == 0 && counter < 150) {
	        		logger.severe("Warning: cannot cook meals in "
	            		+ person.getSettlement().getName()
	            		+ " because none of the ingredients of a meal are available ");
	        	}
*/

				StringBuilder log = new StringBuilder();
				
				log.append("[" + person.getSettlement().getName() + "] ").append(person).append(NO_INGREDIENT);
				
				LogConsolidated.log(logger, Level.WARNING, 5000, sourceName, log.toString(), null);
				


/*
	            // 2015-01-15 Added solElapsed
	            MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();
	            int solElapsed = marsClock.getSolElapsedFromStart();
	            if (solElapsed != solElapsedCache) {
	            	counter = 0;
	            	logCache[2] = "";
	            	solElapsedCache = solElapsed;
	            }
*/
	            endTask();
		    }
	        else {

		    	//counter = 0;

		    	// Add task phase
			    addPhase(COOKING);
				setPhase(COOKING);

				//String jobName = person.getMind().getJob().getName(person.getGender());

				//String newLog = jobName + " " + person.getName() + " made some meals at " + kitchen.getBuilding().getNickName() +
				//    	                " in " + person.getSettlement();

				//LogConsolidated.log(logger, Level.INFO, 5000, sourceName, newLog, null);

		    }
	    }
	    else {
	        endTask();
	    }
    }

	public CookMeal(Robot robot) {
        // Use Task constructor
        super(NAME, robot, true, false, STRESS_MODIFIER, false, 0D);

        //logger.info("just called CookMeal's constructor");

        // Initialize data members
        setDescription(Msg.getString("Task.description.cookMeal.detail",
                getTypeOfMeal())); //$NON-NLS-1$

        // Get available kitchen if any.
        kitchenBuilding = getAvailableKitchen(robot);

	    if (kitchenBuilding != null) {
	    	kitchen = kitchenBuilding.getCooking();

	        // Walk to kitchen building.
	    	walkToActivitySpotInBuilding(kitchenBuilding, false);

		    //int size = kitchen.getMealRecipesWithAvailableIngredients().size();
	    	// 2015-12-10 Used getNumCookableMeal()
		    int numGoodRecipes = kitchen.getNumCookableMeal();

        	// Need to reset numGoodRecipes periodically since it's a cache value
        	// and won't get updated unless a meal is cooked.
        	// Note: it's reset at least once a day at the end of a sol
		    if (numGoodRecipes < 2) {
	        	if (RandomUtil.getRandomInt(5) == 0) {
	        		// check again to reset the value once in a while
	        		numGoodRecipes = kitchen.getMealRecipesWithAvailableIngredients().size();
	        		kitchen.setNumCookableMeal(numGoodRecipes);
	        	}
		    }

	    	if (numGoodRecipes == 0) {
/*
	    		counter++;
	        	if (counter % 30 == 0 && counter < 150)
	        		logger.severe("Warning: cannot cook meals in "
	            		+ robot.getSettlement().getName()
	            		+ " because none of the ingredients of any meals are available ");
*/

				StringBuilder log = new StringBuilder();
				
				log.append("[" + robot.getSettlement().getName() + "] ").append(robot).append(NO_INGREDIENT);

				LogConsolidated.log(logger, Level.WARNING, 5000, logger.getName(), log.toString(), null);

	            endTask();

		    }
	        else {

		    	//counter = 0;

		    	// Add task phase
			    addPhase(COOKING);
				setPhase(COOKING);

				//String jobName = RobotJob.getName(robot.getRobotType());

				//String newLog = jobName + " " + robot.getName() + " made meal at " + kitchen.getBuilding().getNickName() +
    	        //        " in " + robot.getSettlement();

				//LogConsolidated.log(logger, Level.INFO, 5000, sourceName, newLog, null);


		    }
	    }
	    else endTask();

    }

    @Override
    protected FunctionType getLivingFunction() {
        return FunctionType.COOKING;
    }

    @Override
    protected FunctionType getRoboticFunction() {
        return FunctionType.COOKING;
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
            //System.out.println(person + " ending cooking due to malfunction.");
            return time;
        }

	    String nameOfMeal = null;
        double workTime = time;

		if (person != null) {
	        // If meal time is over, end task.
	        if (!isMealTime(person.getCoordinates())) {
	            logger.finest(person + " ending cooking due to meal time over.");
	        	endTask();
	            return time;
	        }

	        // If enough meals have been cooked for this meal, end task.
	        if (kitchen.getCookNoMore()) {
	            logger.finest(person + " ending cooking due cook no more.");
	            endTask();
	            return time;
	        }

		    // Add this work to the kitchen.
	        nameOfMeal = kitchen.addWork(workTime, person);
		}
		else if (robot != null) {
	        // If meal time is over, end task.
	        if (!isMealTime(robot)) {
	            logger.finest(robot + " ending cooking due to meal time over.");
	        	endTask();
	            return time;
	        }

	        // If enough meals have been cooked for this meal, end task.
            if (kitchen.getCookNoMore()) {
                logger.finest(robot + " ending cooking due cook no more.");
                endTask();
                return time;
            }

		     // A robot moves slower than a person and incurs penalty on workTime
	        workTime = time/3;
    	    // Add this work to the kitchen.
            nameOfMeal = kitchen.addWork(workTime, robot);
		}


        if (nameOfMeal != null) {

        	setDescription(Msg.getString("Task.description.cookMeal.detail.finish",
        		nameOfMeal)); //$NON-NLS-1$

    		// Determine amount of effective work time based on "Cooking" skill.
    	    int cookingSkill = getEffectiveSkillLevel();
    	    if (cookingSkill == 0) {
    	        workTime /= 2;
    	    }
    	    else {
    	        workTime += workTime * (.2D * (double) cookingSkill);
    	    }

    	    // Add experience
    	    addExperience(time);

    	    // Check for accident in kitchen.
    	    checkForAccident(time);

        }

        //else
        //    setDescription(Msg.getString("Task.description.cookMeal.detail",
        //            getTypeOfMeal())); //$NON-NLS-1$



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
        int experienceAptitude = 0;

		if (person != null) {
			experienceAptitude = person.getNaturalAttributeManager().getAttribute(
	                NaturalAttributeType.EXPERIENCE_APTITUDE);
		}
		else if (robot != null) {
			experienceAptitude = robot.getRoboticAttributeManager().getAttribute(
                RoboticAttributeType.EXPERIENCE_APTITUDE);
		}

        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();

		if (person != null) {
			person.getMind().getSkillManager().addExperience(SkillType.COOKING, newPoints);
		}
		else if (robot != null) {
			robot.getBotMind().getSkillManager().addExperience(SkillType.COOKING, newPoints);
		}
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

        double chance = .005D;
        int skill = 0;

		if (person != null)
	        // Cooking skill modification.
	        skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
		else if (robot != null)
	        skill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);

        if (skill <= 3) {
            chance *= (4 - skill);
        }
        else {
            chance /= (skill - 2);
        }

        // Modify based on the kitchen building's wear condition.
        chance *= kitchen.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

        if (RandomUtil.lessThanRandPercent(chance * time)) {

			if (person != null) {
//	            logger.info("[" + person.getSettlement() +  "] " + person.getName() + " has an accident while cooking.");
	            kitchen.getBuilding().getMalfunctionManager().createASeriesOfMalfunctions(person);
			}
			else if (robot != null) {
//				logger.info("[" + robot.getSettlement() +  "] " + robot.getName() + " has an accident while cooking.");
	            kitchen.getBuilding().getMalfunctionManager().createASeriesOfMalfunctions(robot);
			}


        }
    }

    /**
     * Checks if it is currently a meal time at the location.
     * @param location the coordinate location to check for.
     * @return true if meal time
     */
    public static boolean isMealTime(Coordinates location) {
        double timeDiff = 1000D * (location.getTheta() / (2D * Math.PI));
	    return mealTime(timeDiff);
    }

    public static boolean isMealTime(Robot robot) {
        //double timeDiff = 1000D * (robot.getCoordinates().getTheta() / (2D * Math.PI));
		//return mealTime(timeDiff);
		return isMealTime(robot.getCoordinates());
    }

    public static boolean mealTime(double timeDiff) {

        boolean result = false;
        double timeOfDay = Simulation.instance().getMasterClock().getMarsClock().getMillisol();
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
    private String getTypeOfMeal() {
        String result = "";
        double timeDiff = 0;

		if (person != null)
	        timeDiff = 1000D * (person.getCoordinates().getTheta() / (2D * Math.PI));
		else if (robot != null)
			timeDiff = 1000D * (robot.getCoordinates().getTheta() / (2D * Math.PI));

        double timeOfDay = Simulation.instance().getMasterClock().getMarsClock().getMillisol();

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
     * Gets an available kitchen building at the person's settlement.
     * @param person the person to check for.
     * @return kitchen building or null if none available.
     */
    public static Building getAvailableKitchen(Person person) {
        Building result = null;

        LocationSituation location = person.getLocationSituation();
        if (location == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = person.getSettlement().getBuildingManager();
            List<Building> kitchenBuildings = manager.getBuildings(FunctionType.COOKING);
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

    public static Building getAvailableKitchen(Robot robot) {
        Building result = null;

        LocationSituation location = robot.getLocationSituation();
        if (location == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = robot.getSettlement().getBuildingManager();
            List<Building> kitchenBuildings = manager.getBuildings(FunctionType.COOKING);
            kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
            kitchenBuildings = getKitchensNeedingCooks(kitchenBuildings);
            kitchenBuildings = BuildingManager.getLeastCrowded4BotBuildings(kitchenBuildings);

            if (kitchenBuildings.size() > 0) {
                //Map<Building, Double> kitchenBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                       // robot, kitchenBuildings);
                //result = RandomUtil.getWeightedRandomObject(kitchenBuildingProbs);
              	int selected = RandomUtil.getRandomInt(kitchenBuildings.size()-1);
            	result = kitchenBuildings.get(selected);
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
                Cooking kitchen = (Cooking) building.getFunction(FunctionType.COOKING);
                if (kitchen.getNumCooks() < kitchen.getCookCapacity()) {
                    result.add(building);
                }
            }
        }

        return result;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = null;
		if (person != null) {
			manager = person.getMind().getSkillManager();
		}
		else if (robot != null) {
			manager = robot.getBotMind().getSkillManager();
		}

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