/**
 * Mars Simulation Project
 * ReturnLightUtilityVehicleMeta.java
 * @version 3.07 2014-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Robot;
import org.mars_sim.msp.core.person.ai.task.ReturnLightUtilityVehicle;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;

/**
 * Meta task for the ReturnLightUtilityVehicle task.
 */
public class ReturnLightUtilityVehicleMeta implements MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.returnLightUtilityVehicle"); //$NON-NLS-1$
    
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

	@Override
	public Task constructInstance(Robot robot) {
		return new ReturnLightUtilityVehicle(robot);
	}

	@Override
	public double getProbability(Robot robot) {
        double result = 0D;

        if (robot.getLocationSituation() == LocationSituation.IN_VEHICLE) {
            
            if (robot.getVehicle() instanceof LightUtilityVehicle) {
                result = 500D;
            }
        }

        return result;
    }
}