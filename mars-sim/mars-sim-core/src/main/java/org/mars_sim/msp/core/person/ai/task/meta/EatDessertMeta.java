/**
 * Mars Simulation Project
 * EatDessertMeta.java
 * @version 3.07 2014-11-28
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;


import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.task.EatDessert;
import org.mars_sim.msp.core.person.ai.task.EatMeal;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;

public class EatDessertMeta implements MetaTask {
  
	/** default logger. */
	//private static Logger logger = Logger.getLogger(DrinkSoymilkMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.eatDessertMeta"); //$NON-NLS-1$
    
    public EatDessertMeta() {
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new EatDessert(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;
        
        // TODO: if a person is very hungry, should he come inside immediately?
       
	    if (person.getLocationSituation() == LocationSituation.OUTSIDE)     	
	    	result = 0D;

    	// TODO: if a person is in a vehicle
	    
	    else {
        	
	        double hunger = person.getPhysicalCondition().getHunger();
	        double energy = person.getPhysicalCondition().getEnergy();       
	        
	        if (hunger > 1000 || energy < 2000 )
		        //result =  0.4 * (hunger - 400D);
		        result = 0.007 * (12000 - energy);
	        else if (hunger > 800 || energy < 4000 )
	        	result = 40D;
	        else if (hunger > 600 || energy < 5000 )
	        	result = 20D;
	        //else if (hunger > 400 || energy < 7000 )
	        	//result = 5D;        
	        else result = 0;
	        
	        if (result > 0) 
	        	
	        	if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
	        		
	        		// check if desserts are available in the vehicle.
		            if (!EatDessert.isDessertIngredientAvailable(person))
		  	          //TODO: is there a way to switch to EatFoodMeta at this point?
		                result = 0D; 	
	        	}
	        
	        	else if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
		                
	        		Building building = EatDessert.getAvailableDiningBuilding(person);
			        if (building != null) {
			        	
			        	// 2015-02-17 Called setDiningBuilding()
			        	person.setDiningBuilding(building);
			        	
			        	result += 10D;
			        	
			            result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
			            result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
			        
			            PreparingDessert kitchen = EatDessert.getKitchenWithDessert(person);
				        // Check if there's a dessert already made available at a local kitchen. will in terms call kitchen.hasFreshDessert()
				        if (kitchen != null) {			        	
				        	// 2015-02-17 Called setDiningBuilding()
				        	person.setKitchenWithDessert(kitchen);			        	
				            
				        	result += 20D;
				        	
				        	// TODO: check how many desserts available. increase in choice should increase result
				        	
				        }
				        //TODO: check if the kitchen has the person's favorite dessert
				        // result += 100D;
				        
				        else { // there is no fresh dessert available
				            // TODO: do we still need to check if the dessert ingredient are available. ?
				        	// TODO: how to switch to directly preparing the dessert for himself ?
				            //if (!EatDessert.isDessertIngredientAvailable(person)) 
				            	result = 0D;
				        }
			        
			        }
	
		        } // IN_SETTLEMENT
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}