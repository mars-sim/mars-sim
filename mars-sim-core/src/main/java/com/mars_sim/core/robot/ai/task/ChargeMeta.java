/*
 * Mars Simulation Project
 * ChargeMeta.java
 * @date 2022-09-18
 * @author Barry Evans
 */
package com.mars_sim.core.robot.ai.task;

import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.tools.Msg;
import com.mars_sim.tools.util.RandomUtil;

/**
 * Meta task for the Charge task.
 */
public class ChargeMeta extends FactoryMetaTask {

	// Can add back private static SimLogger logger = SimLogger.getLogger(ChargeMeta.class.getName())

    /** Task name */
    private static final String NAME = Msg.getString("Task.description.charge"); //$NON-NLS-1$
		
	private static final double LOW_FACTOR = 5;
	private static final double CHANCE = 0.2;
    
    public ChargeMeta() {
		super(NAME, WorkerType.ROBOT, TaskScope.ANY_HOUR);
	}

	@Override
	public Task constructInstance(Robot robot) {
		return new Charge(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0;

        // No sleeping outside.
        if (robot.isOutside())
            return 0;

        else if (robot.isInVehicle())
        	// Future: will re-enable robot serving in a vehicle
            return result;
        
        // Crowding modifier.
        else if (robot.isInSettlement()) {
     
        	double batteryLevel = robot.getSystemCondition().getBatteryState();
        	
        	// Checks if the battery is low
        	if (robot.getSystemCondition().isLowPower()) {
        		result += (100 - batteryLevel) * LOW_FACTOR;
        	}
        	else if (batteryLevel < 90) {
    			double rand = RandomUtil.getRandomDouble(batteryLevel);
    			if (rand < robot.getSystemCondition().getLowPowerPercent())
    				// At max, ~20% chance it will need to charge 
    				result += (100 - batteryLevel) * CHANCE;
    		}
        	
        	Building currentBldg = robot.getBuildingLocation();
			if (currentBldg == null) {
				return 0;
			}
        	
//			RoboticStation station = currentBldg.getRoboticStation();
//			if (station.getSleepers() >= station.getSlots()) {
//				// This is a good building to sleep and charge up
//				result /= 2;
//				return result;
//			}
        }

        return result;
	}
}