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
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.job.Chefbot;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PrepareDessert;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Storage;
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
    public Task constructInstance(Robot robot) {
        return new PrepareDessert(robot);
    }
    
    
    @Override
    public double getProbability(Person person) {
             
        double result = 0D;
        
        // TODO: if a person is very hungry, should he come inside and result > 0 ?
        
        if (person.getLocationSituation() != LocationSituation.OUTSIDE) 
        
        	if (PrepareDessert.isDessertTime(person)) {
        		
            try {
                // See if there is an available kitchen.
                Building kitchenBuilding = PrepareDessert.getAvailableKitchen(person);

                if (kitchenBuilding != null) {
                    PreparingDessert kitchen = (PreparingDessert) kitchenBuilding.getFunction(BuildingFunction.PREPARING_DESSERT);
                   
                    int population = person.getSettlement().getCurrentPopulationNum();
                    if (population > 1) {
                    	
                  		String [] availableDesserts = PreparingDessert.getArrayOfDesserts();
            	        boolean isAvailable = false;
            	        for(String n : availableDesserts) {
            	        	double amount = PreparingDessert.getDryMass(n);
            	        	// see if a food resource is available
            	        	isAvailable = Storage.retrieveAnResource(amount, n, kitchen.getInventory(), false);
            	        	if (isAvailable)
            	        		result += 10D;
            	        }
                    	/*
		                   String [] desserts = PreparingDessert.getArrayOfDesserts();
		                   
		                   // See if the desserts are available to be served 
		                   for(String n : desserts) {
	                        if (kitchen.checkAmountAV(n) > kitchen.getDryMass(n)) {
	                        	result += 10D;
	                        }
	                    }
	                    */
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

		
	@Override
	public double getProbability(Robot robot) {
	   double result = 0D;

	   if (robot.getBotMind().getRobotJob() instanceof Chefbot)
		   
		   if (robot.getLocationSituation() != LocationSituation.OUTSIDE)
			   
			   if (PrepareDessert.isDessertTime(robot)) {		   

			       try {
			           // See if there is an available kitchen.
			           Building kitchenBuilding = PrepareDessert.getAvailableKitchen(robot);
			
			           if (kitchenBuilding != null) {
			               PreparingDessert kitchen = (PreparingDessert) kitchenBuilding.getFunction(BuildingFunction.PREPARING_DESSERT);
						      	//logger.info("kitchenBuilding.toString() : "+ kitchenBuilding.toString());
			              
		                    int population = robot.getSettlement().getCurrentPopulationNum();
		                    if (population > 1) {
			               	
				               	if (kitchen.hasFreshDessert() == false)
				               		result += 100D;
				               	
		                 		String [] availableDesserts = PreparingDessert.getArrayOfDesserts();
		            	        boolean isAvailable = false;
		            	        for(String n : availableDesserts) {
		            	        	double amount = PreparingDessert.getDryMass(n);
		            	        	isAvailable = Storage.retrieveAnResource(amount, n, kitchen.getInventory(), false);
		            	        	if (isAvailable)
		            	        		result += 10D;
		            	        }
			                   
			                   // TODO: should we program the robot to avoid crowded places for the benefit of humans? 
			                   // Crowding modifier.
			                   result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(robot, kitchenBuilding);
				                   
			               }
			           }
			       }
			       catch (Exception e) {
			           //logger.log(Level.INFO,"getProbability() : No room/no kitchen available for cooking dessert or outside settlement", e);
			       }
			
			       // Effort-driven task modifier.
			       result *= robot.getPerformanceRating();
		
			   }
			   
			   if (result < 0) result = 0;
	   
		   return result;
	}
}