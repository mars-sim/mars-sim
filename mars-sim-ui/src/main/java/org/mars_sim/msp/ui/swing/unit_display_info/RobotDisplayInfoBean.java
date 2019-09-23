/**
 * Mars Simulation Project
 * RobotDisplayInfo.java
 * @version 3.1.0 2017-10-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_display_info;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.sound.SoundConstants;

/**
 * Provides display information about a Robot.
 */
class RobotDisplayInfoBean
implements UnitDisplayInfo {

	// Data members
	private Icon buttonIcon;

	/**
	 * Constructor.
	 */
	RobotDisplayInfoBean() {
		buttonIcon = ImageLoader.getIcon("RobotIcon");
	}

	/**
	 * Checks if unit is to be displayed on the navigator tool map.
	 * @param unit the unit to display
	 * @return true if unit is to be displayed on navigator map.
	 */
	public boolean isMapDisplayed(Unit unit) {
		return false;
	}

	/**
	 * Gets display icon for the surface navigator map.
	 * @param unit the unit to display
	 * @return icon
	 */
	public Icon getSurfMapIcon(Unit unit) {
		return null;
	}

	/**
	 * Gets display icon for topo navigator map.
	 * @param unit the unit to display
	 * @return icon
	 */
	public Icon getTopoMapIcon(Unit unit) {
		return null;
	}

	/**
	 * Checks if the map icon should blink on and off.
	 * @param unit the unit to display
	 * @return true if blink
	 */
	public boolean isMapBlink(Unit unit) {
		return false;
	}

	/**
	 * Gets the label color for surface navigator map.
	 * @return color
	 */
	public Color getSurfMapLabelColor() {
		return null;
	}

	/**
	 * Gets the label color for topo navigator map.
	 * @return color
	 */
	public Color getTopoMapLabelColor() {
		return null;
	}

	/**
	 * Gets the label font for navigator map.
	 * @return font
	 */
	public Font getMapLabelFont() {
		return null;
	}

	/**
	 * Gets the range (km) for clicking on unit on navigator map.
	 * @return clicking range
	 */
	public double getMapClickRange() {
		return 0;
	}

	/**
	 * Checks if the unit is to be displayed on the navigator tool globe.
	 * @param unit the unit to display.
	 * @return true if unit is to be displayed on globe
	 */
	public boolean isGlobeDisplayed(Unit unit) {
		return false;
	}

	/**
	 * Gets display color for surface globe.
	 * @return color
	 */
	public Color getSurfGlobeColor() {
		return null;
	}

	/**
	 * Gets display color for topo globe.
	 * @return color
	 */
	public Color getTopoGlobeColor() {
		return null;
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

	@Override
	public Icon getGeologyMapIcon(Unit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getGeologyMapLabelColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getGeologyGlobeColor() {
		// TODO Auto-generated method stub
		return null;
	}
}