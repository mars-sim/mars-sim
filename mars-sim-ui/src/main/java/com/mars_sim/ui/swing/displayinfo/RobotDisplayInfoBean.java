/*
 * Mars Simulation Project
 * RobotDisplayInfo.java
 * @date 2025-08-07
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.displayinfo;

import com.mars_sim.core.Entity;
import com.mars_sim.core.robot.Robot;

/**
 * Provides display information about a Robot.
 */
class RobotDisplayInfoBean extends EntityDisplayInfo {
	private static final String SND_ROVER_MALFUNCTION = "rover_malfunction.ogg";
	private static final String SND_ROVER_MOVING = "rover_moving.ogg";

	/**
	 * Constructor.
	 */
	RobotDisplayInfoBean() {
		super("robot");
	}

	/**
	 * Gets a sound appropriate for this unit.
	 * 
	 * @param unit the unit to display.
	 * @returns sound filepath for unit or empty string if none.
	 */
	@Override
	public String getSound(Entity unit) {
		String result;

		if (((Robot) unit).isOperable()) 
			result = SND_ROVER_MOVING;
		else {
			result = SND_ROVER_MALFUNCTION;

		}
		return result;
	}
}
