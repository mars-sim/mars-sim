/*
 * Mars Simulation Project
 * CookMealMeta.java
 * @date 2022-08-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskProbabilityUtil;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

/**
 * Meta task for the CookMeal task.
 */
public class CookMealMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.cookMeal"); //$NON-NLS-1$
    
	private static final int CAP = 6_000;
	
    public CookMealMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.COOKING);
		setTrait(TaskTrait.ARTISTIC);
		setPreferredJob(JobType.CHEF);

        addPreferredRobot(RobotType.CHEFBOT);
	}

    @Override
    public Task constructInstance(Person person) {
        return new CookMeal(person);
    }

    @Override
    public double getProbability(Person person) {
    	
    	if (person.isOutside())
    		return 0;
    		
        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
        	return 0;
        
        double result = 0D;

        if (person.isInSettlement() 
        	&& CookMeal.isMealTime(person, CookMeal.PREP_TIME)) {

            // See if there is an available kitchen.
            Building kitchenBuilding = BuildingManager.getAvailableKitchen(person, FunctionType.COOKING);

            if (kitchenBuilding != null) {
                Cooking kitchen = kitchenBuilding.getCooking();

                // Check if enough meals have been cooked at kitchen for this meal time.
                boolean enoughMeals = kitchen.getCookNoMore();

                if (enoughMeals) 
                	return 0;

                if (kitchen.canCookMeal()) {

                    result = 200;
                    result *= getBuildingModifier(kitchenBuilding, person);

                    // Apply the standard Person modifiers
                    result *= getPersonModifier(person);
                }
            }
        }

        if (result > CAP)
        	result = CAP;
        
        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new CookMeal(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (CookMeal.isMealTime(robot, CookMeal.PREP_TIME)) {
            // See if there is an available kitchen.
            Building kitchenBuilding = BuildingManager.getAvailableKitchen(robot, FunctionType.COOKING);

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
        
        if (result > CAP)
        	result = CAP;

        return result;
	}
}
