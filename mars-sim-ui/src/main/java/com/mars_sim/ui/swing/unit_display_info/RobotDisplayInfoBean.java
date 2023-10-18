/**
 * Mars Simulation Project
 * RobotDisplayInfo.java
 * @version 3.2.0 2021-06-20
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
	 * @return icon
	 */
	public Icon getButtonIcon(Unit unit) {
		return buttonIcon;
	}

	/**
	 * Gets a sound appropriate for this unit.
	 * @param unit the unit to display.
	 * @returns sound filepath for unit or empty string if none.
	 */
	public String getSound(Unit unit) {
		Robot robot = (Robot) unit;
		String result = "";
		//boolean male = PersonGender.MALE == person.getGender();
		//int randomSoundNum = RandomUtil.getRandomInt(1, 2);
		if (robot.getSystemCondition().isInoperable()) result = SoundConstants.SND_PERSON_DEAD;
		else {
			//if (male) {
				//if (randomSoundNum == 1)
					result = SoundConstants.SND_PERSON_MALE1;
				//else if (randomSoundNum == 2)
					//result = SoundConstants.SND_PERSON_MALE2;
			//}
			//else {
			//	if (randomSoundNum == 1) result = SoundConstants.SND_PERSON_FEMALE1;
			//	else if (randomSoundNum == 2) result = SoundConstants.SND_PERSON_FEMALE2;
			//}
		}
		return result;
	}
}
