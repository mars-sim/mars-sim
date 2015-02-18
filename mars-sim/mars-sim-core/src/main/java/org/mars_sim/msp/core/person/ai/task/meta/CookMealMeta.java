/**
 * Mars Simulation Project
 * CookMealMeta.java
 * @version 3.07 2015-01-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.RobotJob;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

/**
 * Meta task for the CookMeal task.
 */
public class CookMealMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.cookMeal"); //$NON-NLS-1$
    
    /** default logger. */
    //private static Logger logger = Logger.getLogger(CookMealMeta.class.getName());
    
    //private RobotJob robotJob ;
    
    public CookMealMeta () {

    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new CookMeal(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;
  
      	if (CookMeal.isMealTime(person)) {

            try {
                // See if there is an available kitchen.
                Building kitchenBuilding = CookMeal.getAvailableKitchen(person);
               
				if (kitchenBuilding != null) {
					
	                Cooking kitchen = (Cooking) kitchenBuilding.getFunction(BuildingFunction.COOKING);
	                
                    int population = person.getSettlement().getCurrentPopulationNum();
                    if (population < 2)
                    	result = 0;
                    
                    else {
                    	
	                	if (kitchen.hasCookedMeal() == false)
	                		result += 100D;
	                    
	                	// TODO: cache the meal ingredients so that it doesn't have to do it again in CookMeal.java and Cooking.java
	                    //double size = kitchen.getMealRecipesWithAvailableIngredients().size();
	                    int size = kitchen.getHotMealCacheSize();

	                    // if more meals (thus more ingredients) are available at kitchen.
	                    // to Chef's delight, he/she is more motivated to cook 
	                    result += size * 50D;
	              	  
	  
	                    // TODO: if the person likes cooking 
	                    // result = result + 200D;
	                    //if (size == 0) result = 0D;
	                   
	                    // TODO: the cook should check if he himself or someone else is hungry, 
	                    // he's more eager to cook except when he's tired
	                    double hunger = person.getPhysicalCondition().getHunger();
	                    if ((hunger > 300D) && (result > 0D)) {
	                        result += (hunger - 300D);
	                    }
	                    double fatigue = person.getPhysicalCondition().getFatigue();
	                    if ((fatigue > 700D) && (result > 0D)) {
	                        result -= .4D * (fatigue - 700D);
	                    }
	                    
	                    if (result < 0D) {
	                        result = 0D;
	                    }
	                    
	                    // Crowding modifier.
	                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, kitchenBuilding);
	                    result *= TaskProbabilityUtil.getRelationshipModifier(person, kitchenBuilding);
	                    
                    }
                }
            }
            catch (Exception e) {
                //logger.log(Level.INFO,"getProbability() : No room/no kitchen available for cooking meal or outside settlement" ,e);
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) result *= job.getStartTaskProbabilityModifier(CookMeal.class);
        

            //System.out.println(" cookMealMeta : getProbability " + result);
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
        
        if (robot.getLocationSituation() != LocationSituation.OUTSIDE) 
            //result = 0D;
	 
	        if (CookMeal.isMealTime(robot)) {
	        	// Job modifier.      
		        result = robot.getBotMind().getRobotJob().getStartTaskProbabilityModifier(CookMeal.class);
		        
		        //System.out.println(robot.getName() + " : CookMeal  : " + result);
		            
		        if (result > 0 ) {// if task penalty is not zero
		     
		      		result += 100D;
		      		
		            try {
		                // See if there is an available kitchen.
		                Building kitchenBuilding = CookMeal.getAvailableKitchen(robot);
		               
						if (kitchenBuilding != null) {
							
			                Cooking kitchen = (Cooking) kitchenBuilding.getFunction(BuildingFunction.COOKING);
			                
		                    int population = robot.getSettlement().getCurrentPopulationNum();
		                    if (population < 2)
		                    	result = 0;
		                    
		                    else {                    	
			                    
			                	if (kitchen.hasCookedMeal() == false)
			                		result += 100D;
			
			                    //double size = kitchen.getMealRecipesWithAvailableIngredients().size();
			                    int size = kitchen.getHotMealCacheSize();
			                    result += size * 50D;
			                    
			                    if (result < 0D)  result = 0D;
			                    
			                    // Crowding modifier.
			                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, kitchenBuilding);
		                    }
		                }
		            }
		            catch (Exception e) {
		                //logger.log(Level.INFO,"getProbability() : No room/no kitchen available for cooking meal or outside settlement" ,e);
		            }	
		            // Effort-driven task modifier.
		            result *= robot.getPerformanceRating();	
		        }
	        
	        }
        
        return result;
	}
}