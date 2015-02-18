/**
 * Mars Simulation Project
 * EatMealMeta.java
 * @version 3.07 2015-01-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.task.EatMeal;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;

public class EatMealMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.eatMealMeta"); //$NON-NLS-1$
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new EatMeal(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;
        
    	
        // TODO: if a person is very hungry, should he come inside immediately?

        if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
            result = 0D;
        }
        
     	// TODO: if a person is in a vehicle
     
        else {
        		        
	        double hunger = person.getPhysicalCondition().getHunger();
	        double energy = person.getPhysicalCondition().getEnergy();
    
	        if (hunger > 1000 || energy < 2000 )
		        //result =  0.4 * (hunger - 400D);
		        result = 0.007 * (12000 - energy);
	        else if (hunger > 800 || energy < 3000 )
	        	result = 40D;
	        else if (hunger > 600 || energy < 4000 )
	        	result = 20D;
	        //else if (energy < 5000 )
	        //	result = 10D;
	        else 
	        	result = 0;
  
	        if (result > 0) {
	        	
		        Building building = EatMeal.getAvailableDiningBuilding(person);
		        if (building != null) {
		        	
		        	// 2015-02-17 Called setDiningBuilding()
		        	person.setDiningBuilding(building);
		        	
		        	result += 10D;
		            result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
		            result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
		       
		            
		            Cooking kitchen = EatMeal.getKitchenWithMeal(person);
		            // Check if there's a cooked meal at a local kitchen. will in terms call kitchen.hasCookedMeal()
			        if (kitchen != null) {
			        	// 2015-02-17 Called setDiningBuilding()
			        	person.setKitchenWithMeal(kitchen);			        	
			            result += 10D;
			                        
			            //Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING);
			            // TODO: how do we make this person go ahead to cook himself a cooked meal, if all the ingredients of a meal is available?
			            //double size = kitchen.getMealRecipesWithAvailableIngredients().size();
			            // should be more interested in eating if the kitchen has a variety of meals in the menu.
			            //result += size * 10D;

			            //TODO: check if the kitchen has the person's favorite meal  result += 100D;
			            
			        }
			        
			        else { // since there is no cooked meal available
			            // check if preserved food is available to eat.
			            if (!EatMeal.isPreservedFoodAvailable(person))
			  	          //TODO: how do we switch to EatDessertMeta at this point ?
			                result = 0D;    
			        }
			        
		        }	  
		        
	        }
	        
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