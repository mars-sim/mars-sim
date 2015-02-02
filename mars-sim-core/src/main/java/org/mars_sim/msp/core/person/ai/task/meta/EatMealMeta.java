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
            "Task.description.eatMeal"); //$NON-NLS-1$
    
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
        double hunger = person.getPhysicalCondition().getHunger();
        double energy = person.getPhysicalCondition().getEnergy();
        
        double result = 0D;
        
        if (hunger > 1000 || energy < 2000 )
	        //result =  0.4 * (hunger - 400D);
	        result = 0.007 * (12000 - energy);
        else if (hunger > 800 || energy < 3000 )
        	result = 40D;
        else if (hunger > 600 || energy < 4000 )
        	result = 20D;
        else if (energy < 5000 )
        	result = 10D;
        else 
        	result = 0;
	        
        
        if (result > 0) {
        	
        	// TODO: if a person is in a vehicle
        	
	        // TODO: if a person is very hungry, should he come inside and result > 0 ?
	        if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
	            result = 0D;
	        }
	
	        Building building = EatMeal.getAvailableDiningBuilding(person);
	        if (building != null) {
	        	result += 10D;
	            result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
	            result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
	       
	            Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING);
	            double size = kitchen.getMealRecipesWithAvailableIngredients().size();
	            // More interested in eating if the kitchen has a variety of meals in the menu.
	            result = result + size * 10D;
	        }
	        
	        // Check if there's a cooked meal at a local kitchen.
	        if (EatMeal.getKitchenWithFood(person) != null) {
	            result += 10D;
	        }
	        else {
	            // Check if there is food available to eat.
	            if (!EatMeal.isFoodAvailable(person))
	                result = 0D;
	        }
        }
      //TODO: if the kitchen has the person's favorite meal
        // result += 100D;

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