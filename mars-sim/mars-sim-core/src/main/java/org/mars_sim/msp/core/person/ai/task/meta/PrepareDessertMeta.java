/**
 * Mars Simulation Project
 * PrepareDessertMeta.java
 * @version 3.07 2014-11-28
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PrepareDessert;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;

/**
 * Meta task for the PrepareSoymilk task.
 */
//2014-11-28 Changed Class name from MakeSoyMeta to PrepareDessertMeta
public class PrepareDessertMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.prepareDessertMeta"); //$NON-NLS-1$
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(PrepareDessertMeta.class.getName());
    
    public PrepareDessertMeta() {
        //logger.info("just called MakeSoyMeta's constructor");
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new PrepareDessert(person);
    }

    @Override
    public double getProbability(Person person) {
             
        double result = 0D;

        if (PrepareDessert.isDessertTime(person)) {

            try {
                // See if there is an available kitchen.
                Building kitchenBuilding = PrepareDessert.getAvailableKitchen(person);
                PreparingDessert kitchen = (PreparingDessert) kitchenBuilding.getFunction(BuildingFunction.PREPARING_DESSERT);
			      	//logger.info("kitchenBuilding.toString() : "+ kitchenBuilding.toString());

                if (kitchenBuilding != null) {
                    result = 200D;

                    // Crowding modifier.
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, kitchenBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, kitchenBuilding);

                    // Check if there is enough food available to cook.
                    PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
                    double amountRequired = config.getFoodConsumptionRate() * (1D / 3D);
                    // 2014-12-30 Added sugarcaneJuiceAvailable
                    double soymilkAvailable = kitchen.checkAmountAV("Soymilk");
                    double sugarcaneJuiceAvailable = kitchen.checkAmountAV("Sugarcane Juice");
                    if (soymilkAvailable < amountRequired
                    		&& sugarcaneJuiceAvailable < amountRequired) 
                    	result = 0D;
                }
            }
            catch (Exception e) {
                //logger.log(Level.INFO,"getProbability() : No room/no kitchen available for cooking meal or outside settlement", e);
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) result *= job.getStartTaskProbabilityModifier(PrepareDessert.class);
        }

        return result;
    }
}