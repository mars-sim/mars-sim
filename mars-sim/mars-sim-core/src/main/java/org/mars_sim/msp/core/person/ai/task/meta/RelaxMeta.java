/**
 * Mars Simulation Project
 * RelaxMeta.java
 * @version 3.07 2014-08-04
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Relax;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * Meta task for the Relax task.
 */
public class RelaxMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Relaxing";
    
    /** default logger. */
    private static Logger logger = Logger.getLogger(RelaxMeta.class.getName());
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new Relax(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 10D;

        // Stress modifier
        result += person.getPhysicalCondition().getStress();

        // Crowding modifier
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            try {
                Building recBuilding = Relax.getAvailableRecreationBuilding(person);
                if (recBuilding != null) {
                    result *= TaskProbabilityUtil.getCrowdingProbabilityModifier(person, recBuilding);
                    result *= TaskProbabilityUtil.getRelationshipModifier(person, recBuilding);
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }

        return result;
    }
}
