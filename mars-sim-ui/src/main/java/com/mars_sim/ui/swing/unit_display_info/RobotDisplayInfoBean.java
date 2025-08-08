/*
 * Mars Simulation Project
 * RobotDisplayInfo.java
 * @date 2025-08-07
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.unit_display_info;

import javax.swing.Icon;

import com.mars_sim.core.Unit;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.sound.SoundConstants;

/**
 * Provides display information about a Robot.
 */
class RobotDisplayInfoBean extends AbstractUnitDisplayInfo {

	// Data members
	private Icon buttonIcon;

	/**
	 * Constructor.
	 */
	RobotDisplayInfoBean() {
		buttonIcon = ImageLoader.getIconByName("robot");
	}

	/**
	 * Gets icon for unit button.
	 * 
	 * @return icon
	 */
	public Icon getButtonIcon(Unit unit) {
		return buttonIcon;
	}

	/**
	 * Gets a sound appropriate for this unit.
	 * 
	 * @param unit the unit to display.
	 * @returns sound filepath for unit or empty string if none.
	 */
	public String getSound(Unit unit) {
		String result = "";

		if (((Robot) unit).isOperable()) 
			result = SoundConstants.SND_ROVER_MOVING;
		else {
			result = SoundConstants.SND_ROVER_MALFUNCTION;

		}
		return result;
	}
}
