/**
 * Mars Simulation Project
 * ReturnLightUtilityVehicleMeta.java
 * @version 3.07 2014-08-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.ReturnLightUtilityVehicle;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;

/**
 * Meta task for the ReturnLightUtilityVehicle task.
 */
public class ReturnLightUtilityVehicleMeta implements MetaTask {

    // TODO: Use enum instead of string for name for internationalization.
    private static final String NAME = "Returning Light Utility Vehicle";
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ReturnLightUtilityVehicle(person);
    }

    @Override
    public double getProbability(Person person) {
        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
            
            if (person.getVehicle() instanceof LightUtilityVehicle) {
                result = 500D;
            }
        }

        return result;
    }
}