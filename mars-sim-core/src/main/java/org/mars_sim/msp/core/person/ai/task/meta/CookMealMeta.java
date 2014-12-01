/**
 * Mars Simulation Project
 * CookMealMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Cooking;

/**
 * Meta task for the CookMeal task.
 */
public class CookMealMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.cookMeal"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(CookMealMeta.class.getName());
    
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
        
        double result = 20D;
 
      	if (CookMeal.isMealTime(person)) {

            try {
                // See if there is an available kitchen.
 			        Building kitchenBuilding = CookMeal.getAvailableKitchen(person);
			        Cooking kitchen = (Cooking) kitchenBuilding.getFunction(BuildingFunction.COOKING);
			      	//logger.info("kitchenBuilding.toString() : "+ kitchenBuilding.toString());

				if (kitchenBuilding != null) {
				 
                    result = 200D;

                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, kitchenBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, kitchenBuilding);

                    
                    // Check if there is enough food available to cook.
                    //PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
                    //double foodRequired = config.getFoodConsumptionRate() * (1D / 3D);
                    //double freshFoodAvailable = kitchen.getTotalFreshFood();
                 	//logger.info(" freshFoodAvailable : " + freshFoodAvailable);
                    //if (freshFoodAvailable < foodRequired) result = 0D;
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
        }

        return result;
    }
}