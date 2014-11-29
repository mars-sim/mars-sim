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
        
        double result = person.getPhysicalCondition().getHunger() - 250D;
        if (result < 0D) result = 0D;

        if (person.getLocationSituation() == LocationSituation.OUTSIDE) result = 0D;

        Building building = EatDessert.getAvailableDiningBuilding(person);
        if (building != null) {
            result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
            result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
        }

        // Check if there's a cooked meal at a local kitchen.
        if (EatDessert.getKitchenWithDessert(person) != null) result *= 5D;
        else {
            // Check if there is food available to eat.
            if (!EatDessert.isDessertAvailable(person)) result = 0D;
        }

        return result;
    }
}