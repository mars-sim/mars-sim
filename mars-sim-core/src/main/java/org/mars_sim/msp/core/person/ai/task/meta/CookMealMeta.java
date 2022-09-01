/*
 * Mars Simulation Project
 * CookMealMeta.java
 * @date 2022-08-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

/**
 * Meta task for the CookMeal task.
 */
public class CookMealMeta extends MetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.cookMeal"); //$NON-NLS-1$
    
    public CookMealMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.COOKING);
		setTrait(TaskTrait.ARTISTIC);
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

        if (person.isInSettlement() 
        		&& CookMeal.isMealTime(person, CookMeal.PREP_TIME)) {

            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
            	return 0;
            }
            
            // See if there is an available kitchen.
            Building kitchenBuilding = CookMeal.getAvailableKitchen(person);

            if (kitchenBuilding != null) {
                Cooking kitchen = kitchenBuilding.getCooking();

                // Check if enough meals have been cooked at kitchen for this meal time.
                boolean enoughMeals = kitchen.getCookNoMore();

                if (enoughMeals) 
                	return 0;

                if (kitchen.canCookMeal()) {

                    result = 200;
                	
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, kitchenBuilding);
                    // Effort-driven task modifier.
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, kitchenBuilding);
                    
                    // Apply the standard Person modifiers
                    result = 10 * applyPersonModifier(result, person);
                }
            }
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new CookMeal(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (CookMeal.isMealTime(robot, CookMeal.PREP_TIME)
            && robot.getRobotType() == RobotType.CHEFBOT) {
            // See if there is an available kitchen.
            Building kitchenBuilding = CookMeal.getAvailableKitchen(robot);

            if (kitchenBuilding != null) {

                Cooking kitchen = kitchenBuilding.getCooking();

                // Check if enough meals have been cooked at kitchen for this meal time.
                boolean enoughMeals = kitchen.getCookNoMore();

                if (enoughMeals) 
                	return 0;

                if (kitchen.canCookMeal()) {
                	
                    result = 500D;
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, kitchenBuilding);
                    // Effort-driven task modifier.
                    result *= robot.getPerformanceRating();
                }
            }
        }

        return result;
	}
}
