/*
 * Mars Simulation Project
 * PrepareDessertMeta.java
 * @date 2021-12-22
 * @author Manny Kung
 */
package com.mars_sim.core.structure.building.function.task;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.cooking.PreparingDessert;
import com.mars_sim.tools.Msg;

/**
 * Meta task for preparing dessert task.
 */
public class PrepareDessertMeta extends FactoryMetaTask {
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.prepareDessertMeta"); //$NON-NLS-1$

    private static final double CAP = 2000D;
    private static final double VALUE = 1;
    private static final double MOD = 0.1D;
    
    public PrepareDessertMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		
		setFavorite(FavoriteType.COOKING);
		setTrait(TaskTrait.ARTISTIC);
		setPreferredJob(JobType.CHEF);
		addPreferredRobot(RobotType.MEDICBOT);
        addPreferredRobot(RobotType.CHEFBOT);
	}

    @Override
    public Task constructInstance(Person person) {
        return new PrepareDessert(person);
    }


    @Override
    public Task constructInstance(Robot robot) {
        return new PrepareDessert(robot);
    }

    /**
     * Assess if a Person can prepare any desserts
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        if (!person.isInSettlement()
            || !person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
            return EMPTY_TASKLIST;
        }
            
        // See if there is an available kitchen.
        Building kitchenBuilding = BuildingManager.getAvailableKitchen(person, FunctionType.PREPARING_DESSERT);
        if (kitchenBuilding == null) {
            return EMPTY_TASKLIST;
        }

        // Calculate the tasks
        PreparingDessert kitchen = kitchenBuilding.getPreparingDessert();
        // Check if there are enough ingredients to prepare a dessert.
        int numGoodRecipes = kitchen.getListDessertsToMake().size();
        // Check if enough desserts have been prepared at kitchen for this meal time.
        boolean enoughMeals = kitchen.getMakeNoMoreDessert();
        if (numGoodRecipes == 0 || enoughMeals) {
            return EMPTY_TASKLIST;
        }
                
        var score = new RatingScore(numGoodRecipes * VALUE);
        score = assessBuildingSuitability(score, kitchenBuilding, person);
        score = assessPersonSuitability(score, person);

        // If it's meal time, decrease probability
        if (CookMeal.isMealTime(person, PrepareDessert.PREP_TIME)) {
            score.addModifier("eatdrink.dessert", MOD);
        }
        return createTaskJobs(score);
    }


	@Override
	public double getProbability(Robot robot) {

       double result = 0D;

       if (CookMeal.isMealTime(robot, PrepareDessert.PREP_TIME)) { 
           // See if there is an available kitchen.
           Building kitchenBuilding =  BuildingManager.getAvailableKitchen(robot, FunctionType.PREPARING_DESSERT);

           if (kitchenBuilding != null) {

               PreparingDessert kitchen = kitchenBuilding.getPreparingDessert();
               // Check if there are enough ingredients to prepare a dessert.
               int numGoodRecipes = kitchen.getListDessertsToMake().size();
               // Check if enough desserts have been prepared at kitchen for this meal time.
               boolean enoughMeals = kitchen.getMakeNoMoreDessert();

               if (numGoodRecipes == 0 || enoughMeals) {
            	   return 0;
               }
               
               result = numGoodRecipes * VALUE;
               // Crowding modifier.
               result *= TaskUtil.getCrowdingProbabilityModifier(robot, kitchenBuilding);
               // Effort-driven task modifier.
               result *= robot.getPerformanceRating();
   
               // If it's meal time, increase probability
               if (CookMeal.isMealTime(robot, 0)) {
            	   result *= MOD;
               }
           }
       }

       if (result > CAP)
       	result = CAP;
       
       return result;
	}
}
