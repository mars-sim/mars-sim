/*
 * Mars Simulation Project
 * PrepareDessertMeta.java
 * @date 2021-12-22
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.PrepareDessert;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskProbabilityUtil;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;

/**
 * Meta task for preparing dessert task.
 */
public class PrepareDessertMeta extends FactoryMetaTask {
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.prepareDessertMeta"); //$NON-NLS-1$

    private static final double VALUE = 1;
    private static final int MOD = 10;
    private static final int CAP = 2_000;
    
    public PrepareDessertMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		
		setFavorite(FavoriteType.COOKING);
		setTrait(TaskTrait.ARTISTIC);
		setPreferredJob(JobType.CHEF);

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


    @Override
    public double getProbability(Person person) {

        double result = 0D;
        
        if (person.isInSettlement()) {
            // Desserts should be prepared during meal times.
        	
            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;
            
            // See if there is an available kitchen.
            Building kitchenBuilding = BuildingManager.getAvailableKitchen(person, FunctionType.PREPARING_DESSERT);

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
                result *= getBuildingModifier(kitchenBuilding, person);

                // Effort-driven task modifier.
                result *= person.getPerformanceRating();
                
        		// If it's meal time, decrease probability
        		if (CookMeal.isMealTime(person, PrepareDessert.PREP_TIME)) {
        			result /= MOD;
        		}
            }
        }
        
        if (result > CAP)
        	result = CAP;
        
        return result;
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
               result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, kitchenBuilding);
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
