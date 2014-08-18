/**
 * Mars Simulation Project
 * WalkMeta.java
 * @version 3.07 2014-08-12
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.Walk;

/**
 * Meta task for the Walk task.
 */
public class WalkMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Walking";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new Walk(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        // If person is outside, give high probability to walk to emergency airlock location.
        if (LocationSituation.OUTSIDE == person.getLocationSituation()) {
            result = 1000D;
        }
        else if (LocationSituation.IN_SETTLEMENT == person.getLocationSituation()) {
            // If person is inside a settlement building, may walk to a random location within settlement.
            result = 10D;
        }
        else if (LocationSituation.IN_VEHICLE == person.getLocationSituation()) {
            // If person is inside a rover, may walk to random location within rover.
            result = 10D;
        }
        
        return result;
    }
}