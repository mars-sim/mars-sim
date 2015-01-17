/**
 * Mars Simulation Project
 * PrepareDessertMeta.java
 * @version 3.07 2015-01-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

//import java.util.logging.Logger;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
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
    //private static Logger logger = Logger.getLogger(PrepareDessertMeta.class.getName());
    
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
        
        // TODO: if a person is very hungry, should he come inside and result > 0 ?
        if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
            result = 0D;
        }
        
        if (PrepareDessert.isDessertTime(person)) {

            try {
                // See if there is an available kitchen.
                Building kitchenBuilding = PrepareDessert.getAvailableKitchen(person);
                PreparingDessert kitchen = (PreparingDessert) kitchenBuilding.getFunction(BuildingFunction.PREPARING_DESSERT);
			      	//logger.info("kitchenBuilding.toString() : "+ kitchenBuilding.toString());

                if (kitchenBuilding != null) {
                    
                    String [] dessert = {   "Soymilk",
                            "Sugarcane Juice",
                            "Strawberry",
                            "Granola Bar",
                            "Blueberry Muffin", 
                            "Cranberry Juice"  };
                    
                    // Put together a list of available dessert 
                    for(String n : dessert) {
                        if (kitchen.checkAmountAV(n) > kitchen.getDryMass(n)) {
                        	result += 5D;
                        }
                    }
                    
                    // TODO: if the person likes making desserts
                    // result = result + 200D;
                    
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
            catch (Exception e) {
                //logger.log(Level.INFO,"getProbability() : No room/no kitchen available for cooking meal or outside settlement", e);
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) result *= job.getStartTaskProbabilityModifier(PrepareDessert.class);
        
            //System.out.println(" PrepareDessertMeta : getProbability " + result);
            
        }
        if (result < 0) result = 0;
        return result;
    }
}