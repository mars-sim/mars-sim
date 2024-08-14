/*
 * Mars Simulation Project
 * SavePowerMeta.java
 * @date 2023-12-05
 * @author Manny Kung
 */
package com.mars_sim.core.robot.ai.task;

import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the power saving task.
 */
public class SavePowerMeta extends FactoryMetaTask {

	// Can add back private static SimLogger logger = SimLogger.getLogger(SavePowerMeta.class.getName())

    /** Task name */
    private static final String NAME = Msg.getString("Task.description.savePower"); //$NON-NLS-1$

	private static final int DEFAULT_SCORE = SavePower.DEFAULT_SCORE;

    public SavePowerMeta() {
		super(NAME, WorkerType.ROBOT, TaskScope.ANY_HOUR);
	}

	@Override
	public Task constructInstance(Robot robot) {
		return new SavePower(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        // No power saving outside
        if (robot.isOutside())
            return 0;

    	// Checks if the battery is low
    	if (robot.getSystemCondition().isLowPower()) {
    		return 0;
    	}
  
        return DEFAULT_SCORE;
	}
}