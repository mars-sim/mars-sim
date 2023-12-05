/*
 * Mars Simulation Project
 * ChargeMeta.java
 * @date 2023-12-04
 * @author Barry Evans
 */
package com.mars_sim.core.robot.ai.task;

import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.SystemCondition;
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
		
    private static final int LEVEL_UPPER_LIMIT = Charge.LEVEL_UPPER_LIMIT;
    
	private static final double LOW_FACTOR = 5;
	private static final double CHANCE = 0.2;
    
	private Building buildingStation;
	
    public ChargeMeta() {
		super(NAME, WorkerType.ROBOT, TaskScope.ANY_HOUR);
	}

	@Override
	public Task constructInstance(Robot robot) {
		return new Charge(robot, buildingStation);
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
        	SystemCondition sc = robot.getSystemCondition();
        	
        	double batteryLevel = sc.getBatteryState();
        	
        	if (batteryLevel >= LEVEL_UPPER_LIMIT) {
        		return 0;
        	}
        	// Checks if the battery is low
        	else if (sc.isLowPower()) {
        		result += (LEVEL_UPPER_LIMIT - batteryLevel) * LOW_FACTOR;
        	}
        	else if (batteryLevel <= sc.getRecommendedThreshold()) {
    			double rand = RandomUtil.getRandomDouble(batteryLevel);
    			if (rand < sc.getLowPowerPercent())
    				// At max, ~20% chance it will need to charge 
    				result += (LEVEL_UPPER_LIMIT - batteryLevel) * CHANCE;
    			else
    				return 0;
    		}
        	else
        		return 0;

        	buildingStation = Charge.findStation(robot);
        	
        	if (buildingStation != null) {
        		return result;
        	}
        }

        return 0;
	}
}