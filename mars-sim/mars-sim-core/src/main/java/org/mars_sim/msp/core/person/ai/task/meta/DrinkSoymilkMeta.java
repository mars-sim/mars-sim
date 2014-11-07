/**
 * Mars Simulation Project
 * drinkSoymilkMeta.java
 * @version 3.07 2014-11-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.DrinkSoymilk;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;

public class DrinkSoymilkMeta implements MetaTask {
  
	/** default logger. */
	//private static Logger logger = Logger.getLogger(DrinkSoymilkMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.drinkSoymilk"); //$NON-NLS-1$
    
    public DrinkSoymilkMeta() {
       // logger.info("just called DrinkSoymilkMeta's constructor");
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new DrinkSoymilk(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = person.getPhysicalCondition().getHunger() - 250D;
        if (result < 0D) result = 0D;

        if (person.getLocationSituation() == LocationSituation.OUTSIDE) result = 0D;

        Building building = DrinkSoymilk.getAvailableDiningBuilding(person);
        if (building != null) {
            result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, building);
            result *= TaskProbabilityUtil.getRelationshipModifier(person, building);
        }

        // Check if there's a cooked meal at a local kitchen.
        if (DrinkSoymilk.getKitchenWithSoymilk(person) != null) result *= 5D;
        else {
            // Check if there is food available to eat.
            if (!DrinkSoymilk.isSoyAvailable(person)) result = 0D;
        }

        return result;
    }
}