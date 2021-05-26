/**
 * Mars Simulation Project
 * CookMealMeta.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Chefbot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the CookMeal task.
 */
public class CookMealMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.cookMeal"); //$NON-NLS-1$
    
    public CookMealMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		addFavorite(FavoriteType.COOKING);
		addTrait(TaskTrait.ARTISITC);
		setPreferredJob(JobType.CHEF);
	}

    @Override
    public Task constructInstance(Person person) {
        return new CookMeal(person);
    }

    @Override
    public double getProbability(Person person) {
    	if (person.isOutside())
    		return 0;
    		
        double result = 0D;

        if (person.isInSettlement() && CookMeal.isLocalMealTime(person.getCoordinates(), 20)) {
        	
            // Probability affected by the person's stress and fatigue.
            PhysicalCondition condition = person.getPhysicalCondition();
            double fatigue = condition.getFatigue();
            double stress = condition.getStress();
            double hunger = condition.getHunger();
            
            if (fatigue > 1000 || stress > 50 || hunger > 500)
            	return 0;
            
            // See if there is an available kitchen.
            Building kitchenBuilding = CookMeal.getAvailableKitchen(person);

            if (kitchenBuilding != null) {
                Cooking kitchen = kitchenBuilding.getCooking();

                // Check if enough meals have been cooked at kitchen for this meal time.
                boolean enoughMeals = kitchen.getCookNoMore();

                if (enoughMeals) 
                	return 0;

                // Check if there are enough ingredients to cook a meal.
                // 2015-12-10 Used getNumCookableMeal()
                int numGoodRecipes = kitchen.getNumCookableMeal();

    	        //System.out.println(" # of cookableMeal : " + numGoodRecipes);
                //System.out.println("numGoodRecipes : " + numGoodRecipes);
                if (numGoodRecipes == 0) {
                	// Need to reset numGoodRecipes periodically since it's a cache value
                	// and won't get updated unless a meal is cooked.
                	// Note: it's reset at least once a day at the end of a sol
                	if (RandomUtil.getRandomInt(5) == 0) {
                		// check again to reset the value once in a while
                		numGoodRecipes = kitchen.getMealRecipesWithAvailableIngredients().size();
	        			kitchen.setNumCookableMeal(numGoodRecipes);
                	}
                		//System.out.println("numGoodRecipes : " + numGoodRecipes);
                }

                else {

                    result = 50D;
                    
                	if (CookMeal.isLocalMealTime(person.getCoordinates(), 20)) {
                		result *= 2.5D;
                	}
                	else
                		result *= .25D;   
                	
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, kitchenBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, kitchenBuilding);
                    
                    // Apply the standard Person modifiers
                    result = applyPersonModifier(result, person);
                }
            }
        }

        //System.out.println("cook meal : " + result);
        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new CookMeal(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;


        if (CookMeal.isMealTime(robot, 20)) {

            if (robot.getBotMind().getRobotJob() instanceof Chefbot) {
                // See if there is an available kitchen.
                Building kitchenBuilding = CookMeal.getAvailableKitchen(robot);

                if (kitchenBuilding != null) {

                    Cooking kitchen = kitchenBuilding.getCooking();

                    // Check if enough meals have been cooked at kitchen for this meal time.
                    boolean enoughMeals = kitchen.getCookNoMore();

                    if (enoughMeals) return 0;

                    // Check if there are enough ingredients to cook a meal.
                    // 2015-12-10 Used getNumCookableMeal()
                    int numGoodRecipes = kitchen.getNumCookableMeal();

                    //if (numGoodRecipes < 2) {
                    if (numGoodRecipes == 0) {                    	
                    	// Need to reset numGoodRecipes periodically since it's a cache value
                    	// and won't get updated unless a meal is cooked.
                    	// Note: it's reset at least once a day at the end of a sol
                    	if (RandomUtil.getRandomInt(5) == 0) {
                    		// check again to reset the value once in a while
                    		numGoodRecipes = kitchen.getMealRecipesWithAvailableIngredients().size();
		        			kitchen.setNumCookableMeal(numGoodRecipes);
	                	}
                    }

                    else {

                        result = 300D;
                        // Crowding modifier.
                        result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, kitchenBuilding);
                        // Effort-driven task modifier.
                        result *= robot.getPerformanceRating();
                    }
                }
            }
        }

        //System.out.println("cook meal : " + result);
        return result;
	}
}
