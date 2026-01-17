/*
 * Mars Simulation Project
 * RobotDisplayInfo.java
 * @date 2025-08-07
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.unit_display_info;

import com.mars_sim.core.Entity;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.ui.swing.sound.SoundConstants;

/**
 * Provides display information about a Robot.
 */
class RobotDisplayInfoBean extends UnitDisplayInfo {

	/**
	 * Constructor.
	 */
	RobotDisplayInfoBean() {
		super("Robot");
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
			result = SoundConstants.SND_ROVER_MOVING;
		else {
			result = SoundConstants.SND_ROVER_MALFUNCTION;

		}
		return result;
	}
}
