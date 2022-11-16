/*
 * Mars Simulation Project
 * ChargeMeta.java
 * @date 2022-09-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.robot.ai.task;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;

/**
 * Meta task for the Charge task.
 */
public class ChargeMeta extends FactoryMetaTask {

	private static SimLogger logger = SimLogger.getLogger(ChargeMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString("Task.description.charge"); //$NON-NLS-1$
		
	private static final double MAX = 1000;
    
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
        	result += 1D;
     
        	double level = robot.getSystemCondition().getBatteryState();
        	
        	// Checks if the battery is low
        	if (robot.getSystemCondition().isLowPower()) {
        		result += (100 - level) * MAX;
        	}
        	else
        		result += (100 - level) * 50;
        	
        	Building currentBldg = robot.getBuildingLocation();
			if (currentBldg == null) {
				logger.warning(robot, "Not in a building");
				return 0;
			}
        	
			RoboticStation station = currentBldg.getRoboticStation();
			if (station.getSleepers() < station.getSlots()) {
				// This is a good building to sleep and charge up
				result *= 10;
				return result;
			}
        	
            Building building = Charge.getAvailableRoboticStationBuilding(robot);
            if (building != null) {
            	// has empty slot
            	result *= 5;
            }
            
            if (result <= 0)
            	logger.info(robot, "level: " + level + "  prob: " + result);
        }

        return result;
	}
}