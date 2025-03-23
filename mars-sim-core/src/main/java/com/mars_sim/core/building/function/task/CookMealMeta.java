/*
 * Mars Simulation Project
 * CookMealMeta.java
 * @date 2022-08-30
 * @author Scott Davis
 */
package com.mars_sim.core.building.function.task;

import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.cooking.Cooking;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.tool.Msg;

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
		addPreferredRobot(RobotType.MEDICBOT);
        addPreferredRobot(RobotType.CHEFBOT);
	}

    @Override
    public Task constructInstance(Person person) {
        return new CookMeal(person);
    }

    /**
     * Assess if a Person could cook a meal. Based on fitness and on the need for more meals
     * @param person
     * @return Potential Task jobs
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {
    	
    	if (person.isOutside()
                || !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)
                || !person.isInSettlement()
                || !CookMeal.isMealTime(person, CookMeal.PREP_TIME)) {
    		return EMPTY_TASKLIST;
        }

        RatingScore score = RatingScore.ZERO_RATING;
    		
        // See if there is an available kitchen.
        Building kitchenBuilding = BuildingManager.getAvailableKitchen(person, FunctionType.COOKING);

        if (kitchenBuilding != null) {
            Cooking kitchen = kitchenBuilding.getCooking();

            // Check if enough meals have been cooked at kitchen for this meal time.
            boolean enoughMeals = kitchen.getCookNoMore();

            if (!enoughMeals && kitchen.canCookMeal()) {

            	score = new RatingScore(100);
            	
        		score.addBase("clealiness", (kitchen.getCleanliness() + 1) * 10);
            
        		double att = person.getNaturalAttributeManager()
        				.getAttribute(NaturalAttributeType.CREATIVITY) / 20.0;
        		
                score.addModifier("attribute", att);
                
                assessBuildingSuitability(score, kitchenBuilding, person);

                // Apply the standard Person modifiers
                assessPersonSuitability(score, person);
            }
        }

        return createTaskJobs(score);
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
                    result *= TaskUtil.getCrowdingProbabilityModifier(robot, kitchenBuilding);
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
