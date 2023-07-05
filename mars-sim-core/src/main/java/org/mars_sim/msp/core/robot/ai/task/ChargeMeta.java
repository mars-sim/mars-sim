/*
 * Mars Simulation Project
 * ChargeMeta.java
 * @date 2022-09-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.robot.ai.task;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;

/**
 * Meta task for the Charge task.
 */
public class ChargeMeta extends FactoryMetaTask {

	// Can add back private static SimLogger logger = SimLogger.getLogger(ChargeMeta.class.getName())

    /** Task name */
    private static final String NAME = Msg.getString("Task.description.charge"); //$NON-NLS-1$
		
	private static final double LOW_FACTOR = 5;
    
    public ChargeMeta() {
		super(NAME, WorkerType.ROBOT, TaskScope.ANY_HOUR);
	}

	@Override
	public Task constructInstance(Robot robot) {
		return new Charge(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 1D;

        // No sleeping outside.
        if (robot.isOutside())
            return 0;

        else if (robot.isInVehicle())
        	// Future: will re-enable robot serving in a vehicle
            return result;
        
        // Crowding modifier.
        else if (robot.isInSettlement()) {
     
        	double level = robot.getSystemCondition().getBatteryState();
        	
        	// Checks if the battery is low
        	if (robot.getSystemCondition().isLowPower()) {
        		result += (100 - level) * LOW_FACTOR;
        	}
        	else
        		result += (100 - level);
        	
        	Building currentBldg = robot.getBuildingLocation();
			if (currentBldg == null) {
				return 0;
			}
        	
			RoboticStation station = currentBldg.getRoboticStation();
			if (station.getSleepers() >= station.getSlots()) {
				// This is a good building to sleep and charge up
				result /= 2;
				return result;
			}
        }

        return result;
	}
}