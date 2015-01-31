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
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;

public class EatDessertMeta implements MetaTask {
  
	/** default logger. */
	//private static Logger logger = Logger.getLogger(DrinkSoymilkMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.eatDessert"); //$NON-NLS-1$
    
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
        double hunger = person.getPhysicalCondition().getHunger();
        double energy = person.getPhysicalCondition().getEnergy();
        
        double result = 0D;
        
        if (hunger > 800 || energy < 2000 )
	        //result =  0.4 * (hunger - 400D);
	        result = 0.01 * (12000 - energy);
        else if (hunger > 600 || energy < 4000 )
        	result = 80D;
        else if (hunger > 400 || energy < 6000 )
        	result = 40D;
        else if (hunger > 300 || energy < 7000 )
        	result = 20D;        
        else result = 0;
        
        if (result > 0) {
	        // TODO: if a person is very hungry, should he come inside and result > 0 ?
	        if (person.getLocationSituation() == LocationSituation.OUTSIDE) result = 0D;
	
	        Building building = EatDessert.getAvailableDiningBuilding(person);
	        if (building != null) {
	        	result += 10D;	        	
	            result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
	            result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
	        }
	
	        // Check if there's a dessert at a local kitchen.
	        if (EatDessert.getKitchenWithDessert(person) != null) 
	        	result += 20D;
	        else {
	            // Check if there is food available to eat.
	            if (!EatDessert.isDessertAvailable(person)) 
	            	result = 0D;
	        }

        }
       //TODO: if the kitchen has the person's favorite dessert
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