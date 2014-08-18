/**
 * Mars Simulation Project
 * RelaxMeta.java
 * @version 3.07 2014-08-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.Yoga;

/**
 * Meta task for the Yoga task.
 */
public class YogaMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Doing Yoga";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new Yoga(person);
    }

    @Override
    public double getProbability(Person person) {
        
        double result = 0D;

        // Stress modifier
        result += person.getPhysicalCondition().getStress() / 2D;
        
        // No yoga outside.
        if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
            result = 0D;
        }

        return result;
    }
}