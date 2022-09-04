/*
 * Mars Simulation Project
 * PrepareDessertMeta.java
 * @date 2021-12-22
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.PrepareDessert;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;

/**
 * Meta task for preparing dessert task.
 */
public class PrepareDessertMeta extends MetaTask {
	
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.prepareDessertMeta"); //$NON-NLS-1$

    private static final int PREP_TIME = 10;
    private static final double VALUE = 10;
    private static final int MOD = 5;
    private static final double CAP = 3_000D;
    
    public PrepareDessertMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		
		setFavorite(FavoriteType.COOKING);
		setTrait(TaskTrait.ARTISTIC);
		setPreferredJob(JobType.CHEF);
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
        
        if (person.isInSettlement() 
        		&& CookMeal.isMealTime(person, PrepareDessert.PREP_TIME)) {
            // Desserts should be prepared during meal times.
        	
            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;
            
            // See if there is an available kitchen.
            Building kitchenBuilding = PrepareDessert.getAvailableKitchen(person);

            if (kitchenBuilding != null) {

                PreparingDessert kitchen = kitchenBuilding.getPreparingDessert();
                // Check if there are enough ingredients to prepare a dessert.
                int numGoodRecipes = kitchen.getListDessertsToMake().size();
                // Check if enough desserts have been prepared at kitchen for this meal time.
                boolean enoughMeals = kitchen.getMakeNoMoreDessert();

                if ((numGoodRecipes > 0) && !enoughMeals) {

                    result = numGoodRecipes * VALUE;
                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, kitchenBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, kitchenBuilding);

                    result = applyPersonModifier(result, person);
                }
                
        		// If it's meal time, increase probability
        		if (CookMeal.isMealTime(person, PREP_TIME)) {
        			result *= MOD;
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

       if (CookMeal.isMealTime(robot, PrepareDessert.PREP_TIME) 
    		   && robot.getRobotType() == RobotType.CHEFBOT) {
           // See if there is an available kitchen.
           Building kitchenBuilding = PrepareDessert.getAvailableKitchen(robot);

           if (kitchenBuilding != null) {

               PreparingDessert kitchen = kitchenBuilding.getPreparingDessert();
               // Check if there are enough ingredients to prepare a dessert.
               int numGoodRecipes = kitchen.getListDessertsToMake().size();
               // Check if enough desserts have been prepared at kitchen for this meal time.
               boolean enoughMeals = kitchen.getMakeNoMoreDessert();

               if ((numGoodRecipes > 0) && !enoughMeals) {

            	   result = numGoodRecipes * VALUE;
                   // Crowding modifier.
                   result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, kitchenBuilding);
                   // Effort-driven task modifier.
                   result *= robot.getPerformanceRating();
               }
               
               // If it's meal time, increase probability
               if (CookMeal.isMealTime(robot, PREP_TIME)) {
            	   result *= MOD;
               }
           }
       }

       if (result > CAP)
       	result = CAP;
       
       return result;
	}
}
